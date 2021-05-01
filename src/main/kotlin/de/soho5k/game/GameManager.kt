package de.soho5k.game

import de.hglabor.velocity.queue.constants.QChannels
import de.hglabor.velocity.queue.jedis.JedisManager
import de.soho5k.commands.Games
import de.soho5k.game.events.LobbyEvents
import de.soho5k.game.games.*
import de.soho5k.knockout.Knockout
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.KnockoutPlayer
import de.soho5k.player.PlayerManager
import de.soho5k.utils.extensions.*
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.runnables.firstSync
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.thenSync
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

object GameManager {

    //List of all Minigames
    var gamePool: ArrayList<GameSchematic> = arrayListOf(
//        AnvilRain(),
//        Firefly(),
//        HotPotato(),
//        KnockbackFFA(),
//        KOTH(),
//        Minefield(),
//        MoleCraft(),
//        OITC(),
//        RainbowRun(),
//        Riptide(),
//        Spleef(),
//        SurvivalGames(),
//        TNTRun(),
//        Snowfight()
    )

    /**
     * List of selected Minigames for this Game
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
    var lobbytime = ConfigManager.lobbytime

    /**
     *  The max value of games
     */
    var maxGames = if (gamePool.size < 10) gamePool.size else 10

    /**
     *  Iconlist for settingsgui
     */

    val gameIcons: ArrayList<Games> = arrayListOf()

    /**
     * Instance of all Events (for easier un-/registration)
     */
    val events = LobbyEvents()

    fun enable() {
        println(">> Starting GameManager")
        Bukkit.getPluginManager().registerEvents(events, Knockout.INSTANCE)
        registerIcons()
        tick()
        println(ConfigManager.knockoutFont)
    }

    /**
     * Tick the Game
     */
    private fun tick() {
        task(period = 20) {
            if (PlayerManager.alivePlayers + PlayerManager.spectatingPlayer == 1 && gameStarted) {
                wrapUpGame()
                it.cancel()
            }
            if (PlayerManager.alivePlayers >= ConfigManager.minPlayers && !gameStarted) {
                if (lobbytime == 0) {
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
                    activeGame.start()
                } else {
                    if (lobbytime.shouldBroadcast(10)) {
                        for (i in PlayerManager.players.values) {
                            i.player?.sendLocalizedMessage(
                                "MESSAGES.game.starting",
                                replacements = arrayOf(lobbytime.toTime())
                            )
                            i.player?.ping()
                        }
                    }
                    PlayerManager.refreshAllBoards()
                    lobbytime--

                    if (lobbytime <= 5 && Knockout.useQueue) {
                        JedisManager.publish(QChannels.QUEUE_MOVE.get(), Knockout.serverName)
                    }
                }
            } else {
                lobbytime = ConfigManager.lobbytime
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

    fun registerIcons() {

        gameIcons.add(Games("Anvilrain", "anvilrain", itemStack(Material.ANVIL) {}))
        gameIcons.add(Games("Firefly", "firefly", itemStack(Material.ORANGE_WOOL) {}))
        gameIcons.add(
            Games(
                "Knockback FFA",
                "kbffa",
                itemStack(Material.STICK) { addUnsafeEnchantment(Enchantment.KNOCKBACK, 1) })
        )
        gameIcons.add(Games("OITC", "oitc", itemStack(Material.WOODEN_SWORD) {}))
        gameIcons.add(Games("Rainbowrun", "rainbowrun", itemStack(Material.PINK_STAINED_GLASS) {}))
        gameIcons.add(Games("Survivalgames", "sg", itemStack(Material.CHEST) {}))
        gameIcons.add(Games("Spleef", "spleef", itemStack(Material.GOLDEN_SHOVEL) {}))
        gameIcons.add(Games("TNTRun", "tntrun", itemStack(Material.TNT) {}))
        gameIcons.add(Games("King of the Hill", "koth", itemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {}))
        gameIcons.add(Games("Minefield", "minefield", itemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {}))
        gameIcons.add(Games("Riptide", "riptide", itemStack(Material.TRIDENT) {}))
        gameIcons.add(Games("Snowfight", "snowfight", itemStack(Material.SNOWBALL) {}))


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
                "anvilrain" -> {
                    gamePool.add(AnvilRain())
                }
                "firefly" -> {
                    gamePool.add(Firefly())
                }
                "kbffa" -> {
                    gamePool.add(KnockbackFFA())
                }
                "oitc" -> {
                    gamePool.add(OITC())
                }
                "rainbowrun" -> {
                    gamePool.add(RainbowRun())
                }
                "sg" -> {
                    gamePool.add(SurvivalGames())
                }
                "spleef" -> {
                    gamePool.add(Spleef())
                }
                "tntrun" -> {
                    gamePool.add(TNTRun())
                }
                "koth" -> {
                    gamePool.add(KOTH())
                }
                "minefield" -> {
                    gamePool.add(Minefield())
                }
                "riptide" -> {
                    gamePool.add(Riptide())
                }
                "snowfight" -> {
                    gamePool.add(Snowfight())
                }
                else -> {
                    println("No game found named: $id")
                    return false
                }
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
        if (isGameEnabled(id)) {
            gamePool.remove(gamePool.find { it.id == id })
            return true
        }
        return false
    }

    /**
     * Check if a game is enabled/not enabled
     * @param id the ID you want to check
     * @return  true if enabled
     *          false if disabled
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

        task(delay = 60L) {
            val list = PlayerManager.players.values.sortedBy { it.points }.reversed()

            val text =
                arrayListOf("[rgb:255,77,77] 1. &7${list.first().player?.name} &8- &7${list.first().points} P.".tc())

            if (list.second() != null) {
                if (list.second() is KnockoutPlayer) {
                    text.add("[rgb:255,111,66] 2. &7${(list.second() as KnockoutPlayer).player?.name} &8- &7${(list.second() as KnockoutPlayer).points} P.".tc())
                }
            }

            if (list.third() != null) {
                if (list.third() is KnockoutPlayer) {
                    text.add("[rgb:255,152,70] 3. &7${(list.third() as KnockoutPlayer).player?.name} &8- &7${(list.third() as KnockoutPlayer).points} P.".tc())
                }
            }

            for (i in Bukkit.getOnlinePlayers()) {
                i.sendLocalizedMessage("MESSAGES.gameover_1", false, ChatColor.RED)
                i.sendMessage("")
                text.forEach { i.sendMessage(it) }
                i.sendMessage("")
                i.sendLocalizedMessage("MESSAGES.gameover_2", false, ChatColor.RED, replacements = arrayOf("60"))
                i.refreshBoard()
            }

            firstSync {
                Knockout.multiverse.deleteWorld("sg")
            }.thenSync {
                Knockout.multiverse.cloneWorld("sg_default", "sg", null)
            }.execute()

            task(delay = 60 * 20) {
                //restart server
            }
        }
    }
}