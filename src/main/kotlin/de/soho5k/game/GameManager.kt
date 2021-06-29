package de.soho5k.game

import de.dytanic.cloudnet.ext.bridge.bukkit.BukkitCloudNetHelper
import de.hglabor.velocity.queue.constants.QChannels
import de.hglabor.velocity.queue.jedis.JedisManager
import de.soho5k.commands.Games
import de.soho5k.game.events.LobbyEvents
import de.soho5k.game.games.*
import de.soho5k.knockout.Knockout
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.KnockoutPlayer
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.clearInv
import de.soho5k.utils.extensions.*
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.extensions.server
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.pluginmessages.PluginMessageConnect
import net.axay.kspigot.runnables.firstSync
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.thenSync
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object GameManager {

    /**
     * List of all Minigames that CAN be drawn
     */
    var gamePool: ArrayList<GameSchematic> = arrayListOf(
        AnvilRain(),
//        Firefly(),
        HotPotato(),
//        KnockbackFFA(),
        KOTH(),
        Minefield(),
        OreHunt(),
        OITC(),
        RainbowRun(),
//        Redlights(),
//        Riptide(),
//        Spleef(),
        SurvivalGames(),
        TNTRun(),
//        Snowfight()
    )

    /**
     * List of selected Minigames that HAVE been drawn for this session
     */
    val activeGames: ArrayList<GameSchematic> = arrayListOf()

    /**
     * currently active Game
     */
    lateinit var activeGame: GameSchematic

    /**
     * true if Players are currently in the Lobby (false if they are in a Minigame)
     */
    var inLobby = true

    /**
     * true if LobbyPhase has ended (enough players were there to start the game)
     */
    var gameStarted = false

    /**
     * true if all games have been played OR only 1 Player was left
     */
    var ended = false

    /**
     *  Time Players have to wait in Lobby (will ich noch abhängig von Spielerzahl machen)
     */
    var lobbyTime: Int = ConfigManager.lobbytime

    /**
     *  The max value of games
     */
    var maxGames = if (gamePool.size < 10) gamePool.size else 10

    /**
     *  Icon-List for Settings-GUI
     */
    val gameIcons: ArrayList<Games> = arrayListOf()

    /**
     * Instance of all Events (for easier un-/registration)
     */
    val events = LobbyEvents()

    val activeGameInit: Boolean
        get() = this::activeGame.isInitialized

    fun enable() {
        println(">> Starting GameManager")
        Bukkit.getPluginManager().registerEvents(events, Knockout.INSTANCE)
        registerIcons()
        tick()
    }

    /**
     * Tick the Game
     */
    private fun tick() {
        task(period = 20) {
            if ((PlayerManager.alivePlayers + PlayerManager.spectatingPlayer == 1 || onlinePlayers.size == 1) && gameStarted) {
                wrapUpGame()
                it.cancel()
            }
            if (PlayerManager.alivePlayers + PlayerManager.queuedPlayers >= ConfigManager.minPlayers && !gameStarted) {
                if (lobbyTime == 0) {
                    gameStarted = true
                    inLobby = false
                    for (i in 0 until maxGames) {
                        if (gamePool.isNotEmpty()) {
                            val game = gamePool.random()
                            activeGames.add(game)
                            gamePool.remove(game)
                        } else {
                            break
                        }
                    }
                    activeGame = drawNewGame()
                    BukkitCloudNetHelper.changeToIngame()
                    activeGame.start()
                } else {
                    if (lobbyTime.shouldBroadcast(10)) {
                        for (i in PlayerManager.players.values) {
                            i.player?.sendLocalizedMessage(
                                "MESSAGES.game.starting",
                                replacements = arrayOf(lobbyTime.toTime())
                            )
                            i.player?.ping()
                        }
                    }
                    PlayerManager.refreshAllBoards()
                    lobbyTime--
                    if (lobbyTime == 9) {
                        onlinePlayers.forEach { p ->
                            p.clearInv()
                        }
                    }
                    if (lobbyTime == 8) {
                        if (Knockout.useQueue) JedisManager.publish(
                            QChannels.QUEUE_MOVE.get(),
                            Knockout.jedis_serverName
                        )
                    }

                    if (lobbyTime == 2) {
                        onlinePlayers.forEach { p -> p.mode = PlayerMode.PLAY }
                    }
                }
            } else {
                lobbyTime = ConfigManager.lobbytime
                PlayerManager.refreshAllBoards()
            }
        }
    }


    /**
     * Draw a new game from the existing Game-Pool
     */
    fun drawNewGame(): GameSchematic {
        if (activeGames.isEmpty()) {
            //END THE GAME BECAUSE NO GAMES ARE LEFT AND NO PLAYER WON AT THIS POINT
            throw Exception("There is no game left to be drawn but no player won the game until now.")
        }
        val game = activeGames.random()
        activeGames.remove(game)
        return game
    }

    /**
     * Register the Icon for the Games
     */
    private fun registerIcons() {
        gameIcons.add(
            Games(
                "Anvilrain",
                "anvilrain",
                ItemStack(Material.ANVIL),
                arrayListOf("", "&7Players need to survive", "&7anvils falling down from", "&7the sky."),
                "Medium"
            )
        )
//        gameIcons.add(Games("Firefly", "firefly", itemStack(Material.ORANGE_WOOL) {}))
//        gameIcons.add(Games("Knockback FFA","kbffa",itemStack(Material.STICK) { addUnsafeEnchantment(Enchantment.KNOCKBACK, 1) }))
        gameIcons.add(
            Games(
                "OITC",
                "oitc",
                ItemStack(Material.ARROW),
                arrayListOf(
                    "",
                    "&7Players need to kill others",
                    "&7with their bow.",
                    "&7Killing a player rewards",
                    "&7you with an arrow."
                ),
                "Medium"
            )
        )
        gameIcons.add(
            Games(
                "Rainbowrun",
                "rainbowrun",
                itemStack(Material.PINK_STAINED_GLASS) {},
                arrayListOf("", "&7Players need to complete ", "&7a Jump and Run"),
                "Short"
            )
        )
        gameIcons.add(
            Games(
                "Survival Games",
                "sg",
                itemStack(Material.CHEST) {},
                arrayListOf("", "&7Last Man Standing wins"),
                "Long"
            )
        )
        gameIcons.add(
            Games(
                "TNTRun",
                "tntrun",
                itemStack(Material.TNT) {},
                arrayListOf("", "&7Run while the floor", "&7Starts to disappear"),
                "Depends on Player Count (Long)"
            )
        )
        gameIcons.add(
            Games(
                "King of the Hill", "koth", itemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {},
                arrayListOf("", "&7Earn points by standing", "&7on the Gold-Blocks"), "Short"
            )
        )
        gameIcons.add(
            Games(
                "Minefield",
                "minefield",
                itemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {},
                arrayListOf("", "&7Get to the End but", "&7avoid the iron", "&7pressure plate"),
                "Short"
            )
        )
        gameIcons.add(
            Games(
                "Redlights",
                "redlights",
                itemStack(Material.RED_WOOL) {},
                arrayListOf("", "&7Get to the end but", "&7don't walk on red"),
                "Short"
            )
        )
        gameIcons.add(
            Games(
                "Ore Hunt",
                "orehunt",
                itemStack(Material.DIRT) {},
                arrayListOf("", "&7Mine down in the dirt", "&7and mine ores for points"),
                "Short"
            )
        )
        gameIcons.add(Games("Hot Potato", "hotpotato", itemStack(Material.POTATO) {}))
//        gameIcons.add(Games("Spleef", "spleef", itemStack(Material.GOLDEN_SHOVEL) {}))
//        gameIcons.add(Games("Riptide", "riptide", itemStack(Material.TRIDENT) {}))
//        gameIcons.add(Games("Snowfight", "snowfight", itemStack(Material.SNOWBALL) {}))

        gameIcons.sortBy { it.name }

        gameIcons.forEach {
            val meta = it.icon.itemMeta
            meta?.setDisplayName("${ChatColor.GOLD}${it.name}")
            meta?.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
            it.icon.itemMeta = meta
        }

    }

    /**
     * Enable the game with given id
     * @param id ID of the Game you want to enable
     * @return If the Addition was successful or not
     */
    fun enableGame(id: String): Boolean {
        if (!isGameEnabled(id)) {
            when (id) {
                "anvilrain" -> gamePool.add(AnvilRain())
                "firefly" -> gamePool.add(Firefly())
                "hotpotato" -> gamePool.add(HotPotato())
                "kbffa" -> gamePool.add(KnockbackFFA())
                "koth" -> gamePool.add(KOTH())
                "minefield" -> gamePool.add(Minefield())
                "orehunt" -> gamePool.add(OreHunt())
                "oitc" -> gamePool.add(OITC())
                "rainbowrun" -> gamePool.add(RainbowRun())
                "redlights" -> gamePool.add(Redlights())
                "riptide" -> gamePool.add(Riptide())
                "snowfight" -> gamePool.add(Snowfight())
                "spleef" -> gamePool.add(Spleef())
                "sg" -> gamePool.add(SurvivalGames())
                "tntrun" -> gamePool.add(TNTRun())
                else -> return false
            }
            return true
        }
        return false
    }

    /**
     * Disable the Game with given id
     * @param id ID of the Game
     * @return If the Deletion was successful or not
     */
    fun disableGame(id: String): Boolean {
        return isGameEnabled(id).also {
            if (it) gamePool.remove(gamePool.find { g -> g.id == id })
        }
    }

    /**
     * Check if a game is enabled/not enabled
     * @param id the ID you want to check
     * @return  **true** if enabled / **false** if disabled
     */
    fun isGameEnabled(id: String): Boolean {
        return gamePool.find { it.id == id } != null
    }

    /**
     * End the Game, Count Points and print them.
     */
    fun wrapUpGame() {
        if (ended) return
        ended = true
        PlayerManager.teleportToLobby()

        if (activeGame.isRunning) activeGame.stop()


        task(delay = 60L) {
            val list = PlayerManager.players.values.sortedBy { it.points }.reversed()

            val text =
                arrayListOf("[rgb:255,77,77] 1. &7${list.first().name} &8- &7${list.first().points} P.".tc())

            if (list.second() != null) {
                if (list.second() is KnockoutPlayer) {
                    text.add("[rgb:255,111,66] 2. &7${(list.second() as KnockoutPlayer).name} &8- &7${(list.second() as KnockoutPlayer).points} P.".tc())
                }
            }

            if (list.third() != null) {
                if (list.third() is KnockoutPlayer) {
                    text.add("[rgb:255,152,70] 3. &7${(list.third() as KnockoutPlayer).name} &8- &7${(list.third() as KnockoutPlayer).points} P.".tc())
                }
            }

            for (i in Bukkit.getOnlinePlayers()) {
                i.sendLocalizedMessage("MESSAGES.gameover_1", false, ChatColor.RED)
                i.sendMessage("")
                text.forEach { i.sendMessage(it) }
                i.sendMessage("")
                i.sendLocalizedMessage(
                    "MESSAGES.gameover_2",
                    false,
                    ChatColor.RED,
                    replacements = arrayOf("${Knockout.restartTime}")
                )
                i.refreshBoard()
            }

            firstSync {
                Knockout.multiverse.deleteWorld("sg")
            }.thenSync {
                Knockout.multiverse.cloneWorld("sg_default", "sg", null)
            }.execute()

            task(delay = (Knockout.restartTime * 20).toLong()) {
                onlinePlayers.forEach { PluginMessageConnect(Knockout.jedis_lobbyServerName).sendWithPlayer(it) }
                server.spigot().restart()
            }
        }
    }
}