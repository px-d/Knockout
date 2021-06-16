package de.soho5k.knockout.queue

import com.google.gson.Gson
import de.hglabor.velocity.queue.constants.QChannels
import de.hglabor.velocity.queue.jedis.JedisManager
import de.hglabor.velocity.queue.pojo.QGameInfo
import de.hglabor.velocity.queue.pojo.QPlayerInfo
import de.soho5k.game.GameManager
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.task
import redis.clients.jedis.JedisPubSub
import java.util.*

object GameInfoProvider {

    fun enable(serverName: String) {
        println("Enabled GameInfoProvider.. Starting Task...")
        task(period = 20) {
            val phase = if (GameManager.gameStarted) "Playing: ${GameManager.activeGame.name}" else "Lobby"
            val time = if (GameManager.gameStarted) "Running" else "${GameManager.lobbyTime}"

            val gameInfo = QGameInfo(
                serverName,
                phase,
                ConfigManager.minPlayers,
                ConfigManager.maxPlayers,
                PlayerManager.alivePlayers + PlayerManager.queuedPlayers,
                time,
                true
            )

            JedisManager.publish(QChannels.QUEUE_INFO.get(), Gson().toJson(gameInfo, QGameInfo::class.java))
        }
    }
}

class QueueChannel(private val serverName: String) : JedisPubSub() {

    override fun onMessage(channel: String, message: String) {
        val inLobby = GameManager.gameStarted

        if (channel == QChannels.QUEUE_JOIN.get()) {
            val qPlayerInfo = Gson().fromJson(message, QPlayerInfo::class.java)
            if (qPlayerInfo.serverName == serverName && inLobby) {
                val playerInfo = Gson().fromJson(message, QPlayerInfo::class.java)
                PlayerManager.registerQueuePlayer(UUID.fromString(playerInfo.uuid))
            }
        }
        if (channel == QChannels.QUEUE_LEAVE.get()) {
            if (onlinePlayers.any { it.uniqueId.toString().equals(message, true) }) {
                PlayerManager.players[UUID.fromString(message)]?.mode = PlayerMode.PLAY
            } else {
                PlayerManager.unregisterQueuePlayer(UUID.fromString(message))
            }
        }
    }

}