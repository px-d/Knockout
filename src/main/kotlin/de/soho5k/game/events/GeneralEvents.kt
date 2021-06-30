package de.soho5k.game.events

import de.soho5k.events.GameEndEvent
import de.soho5k.game.GameManager
import de.soho5k.knockout.Knockout
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.QUOTATION
import de.soho5k.utils.extensions.*
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerListPingEvent

object GeneralEvents : Listener {

    fun enable() {
        listen<ServerListPingEvent> {
            if (GameManager.inLobby && !GameManager.gameStarted) {
                if (PlayerManager.alivePlayers + PlayerManager.queuedPlayers >= ConfigManager.minPlayers) {
                    it.motd = "&7Warmup: &c${GameManager.lobbyTime.toTime()}s".tc()
                } else {
                    it.motd = "&cWaiting...".tc()
                }
            } else {
                if (GameManager.ended) {
                    it.motd = "&cGame Over!".tc()
                } else {
                    it.motd = "&7Current Game: &b${GameManager.activeGame?.name}".tc()
                }
            }
        }

        listen<PlayerLoginEvent> {
            if (GameManager.gameStarted) {
                if ((it.player.hasPermission("knockout.rejoin") && PlayerManager.players.containsKey(it.player.uniqueId)) || it.player.isOp) {
                    if (!PlayerManager.players.containsKey(it.player.uniqueId)) PlayerManager.registerPlayer(it.player)
                    task(delay = 5L, howOften = 1L) { _ ->
                        GameManager.activeGame.onRejoin(it.player)
                    }
                } else {
                    it.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Game is currently running.")
                }
            }
            PlayerManager.refreshAllBoards()
        }

        listen<PlayerJoinEvent> {
            PlayerManager.updateTab()
            it.joinMessage = ""

            PlayerManager.registerPlayer(it.player)

            for (i in Bukkit.getOnlinePlayers()) {
                for (h in Bukkit.getOnlinePlayers()) {
                    if (i.mode == PlayerMode.STAFF) {
                        h.hidePlayer(Knockout.INSTANCE, i)
                    }
                }
            }
            it.player.exp = 0F
            it.player.level = 0

            it.player.sendLocalizedMessage(
                "MESSAGES.content_creator_intro",
                false,
                ChatColor.RED,
                ChatColor.YELLOW,
                "/game",
                "/start"
            )
        }
        listen<PlayerQuitEvent> {
            it.quitMessage = ""
            if (GameManager.gameStarted) {
                if (!it.player.hasPermission("knockout.rejoin")) {
                    PlayerManager.unregisterPlayer(it.player)
                } else {
                    PlayerManager.players[it.player.uniqueId]?.mode = PlayerMode.LEFT
                }
            } else {
                PlayerManager.unregisterPlayer(it.player)
            }

        }
        listen<GameEndEvent> {
            PlayerManager.refreshAllBoards()
            if (GameManager.activeGames.isNotEmpty()) {
                onlinePlayers.forEach {
                    it.sendLocalizedMessage(
                        "MESSAGES.game.next",
                        replacements = arrayOf(GameManager.activeGame.name, "10")
                    )
                }
                GameManager.activeGame = GameManager.drawNewGame()
                task(delay = 10 * 20, howOften = 1L) {
                    GameManager.activeGame.start()
                }
            } else {
                GameManager.wrapUpGame()
            }
        }

        listen<AsyncPlayerChatEvent> {
            it.isCancelled = true
            onlinePlayers.forEach { p ->
                if (it.player.isSpectating) {
                    p.sendMessage("&c${it.player.name} &8(&7${it.player.points}&8) $QUOTATION &7${it.message.sc()}".tc())
                }
                if (it.player.mode == PlayerMode.PLAY) {
                    p.sendMessage("&a${it.player.name} &8(&7${it.player.points}&8) $QUOTATION &7${it.message.sc()}".tc())
                }
            }
        }
    }
}