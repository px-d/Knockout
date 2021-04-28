package de.soho5k.game

import de.hglabor.velocity.queue.constants.QChannels
import de.hglabor.velocity.queue.jedis.JedisManager
import de.soho5k.game.events.LobbyEvents
import de.soho5k.game.games.*
import de.soho5k.knockout.ConfigManager
import de.soho5k.knockout.Knockout
import de.soho5k.player.KnockoutPlayer
import de.soho5k.player.PlayerManager
import de.soho5k.utils.extensions.*
import net.axay.kspigot.runnables.firstSync
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.thenSync
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object GameManager {

    //List of all Minigames
    var gamePool: ArrayList<GameSchematic> = arrayListOf(
//        AnvilRain(),
//        Firefly(),
//        HotPotato(),
//        KnockbackFFA(),
        KOTH(),
        Minefield(),
//        MoleCraft(),
//        OITC(),
//        RainbowRun(),
//        Riptide(),
//        Spleef(),
//        SurvivalGames(),
//        TNTRun()
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
     * Instance of all Events (for easier un-/registration)
     */
    val events = LobbyEvents()

    fun enable() {
        println(">> Starting GameManager")
        Bukkit.getPluginManager().registerEvents(events, Knockout.INSTANCE)
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
                    for (i in 0..10) {
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
                            i.player.msg("MESSAGES.game.starting", lobbytime.toTime())
                            i.player.ping()
                        }
                    }
                    PlayerManager.refreshAllBoards()
                    lobbytime--

                    if (lobbytime <= 5) {
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

    fun disableGame(source: CommandSender? = null, id: String) {
        val game = gamePool.find { it.id == id }
        if (game != null) {
            gamePool.remove(game)

            source?.sendMessage("&e$id ${net.md_5.bungee.api.ChatColor.of("7BFF42".toRgb())}wurde entfernt.".tc())
        } else {
            source?.sendMessage("&e$id ${net.md_5.bungee.api.ChatColor.of("FF9600".toRgb())}gibt es nicht.".tc())
        }
    }

    fun enableGame(source: CommandSender? = null, id: String) {
        if (gamePool.find { it.id == id } == null) {
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
            }
            source?.sendMessage("&e$id ${net.md_5.bungee.api.ChatColor.of("7BFF42".toRgb())}wurde hinzugefügt.".tc())
        } else {
            source?.sendMessage("&e$id ${net.md_5.bungee.api.ChatColor.of("FF9600".toRgb())}ist schon aktiv.".tc())
        }

    }

    /**
     * End the Game, Count Points and print them.
     * TODO("Differentiate between 'Single Player Performance' and 'Team Performance'")
     */
    fun wrapUpGame() {
        if (ended) return
        ended = true
        PlayerManager.teleportToLobby()

        task(delay = 60L) {
            val list = PlayerManager.players.values.sortedBy { it.points }.reversed()
            val second = list.second() as KnockoutPlayer?
            val third = list.third() as KnockoutPlayer?
            var text = "[rgb:255,77,77] 1. &7${list.first().player.name} &8- &7${list.first().points} P."

            if (second != null) {
                text = "$text{n}[rgb:255,175,77] 2. &7${second.player.name} &8- &7${second.points} P."
            }

            if (third != null) {
                text = "$text{n}[rgb:255,237,77] 3. &7${third.player.name} &8- &7${third.points} P."
            }

            for (i in Bukkit.getOnlinePlayers()) {
                i.msg("MESSAGES.gameover", text)
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