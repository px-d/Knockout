package de.soho5k.game.events

import de.soho5k.game.GameManager
import de.soho5k.knockout.Knockout
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.QUOTATION
import de.soho5k.utils.extensions.*
import events.GameEndEvent
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

object GeneralEvents : Listener {

    fun enable() {
        listen<PlayerLoginEvent> {
            if (GameManager.gameStarted) {
                it.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Game is currently running.")
            }
        }

        listen<PlayerJoinEvent> {
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
        }
        listen<PlayerQuitEvent> {
            it.quitMessage = ""
            PlayerManager.unregisterPlayer(it.player)
        }
        listen<GameEndEvent> {
            PlayerManager.refreshAllBoards()
            if (GameManager.activeGames.size > 0) {
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
                if (it.player.mode == PlayerMode.SPECTATE) {
                    p.sendMessage("&c${it.player.name} &8(&7${it.player.points}&8) $QUOTATION &7${it.message.sc()}".tc())
                }
                if (it.player.mode == PlayerMode.PLAY) {
                    p.sendMessage("&a${it.player.name} &8(&7${it.player.points}&8) $QUOTATION &7${it.message.sc()}".tc())
                }
            }
        }
    }
}