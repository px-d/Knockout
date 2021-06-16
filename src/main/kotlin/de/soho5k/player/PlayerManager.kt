package de.soho5k.player

import board.FastBoard
import de.soho5k.game.GameManager
import de.soho5k.knockout.Knockout
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.knockout.managers.RankingManager
import de.soho5k.utils.Localization.getLocalized
import de.soho5k.utils.extensions.*
import de.soho5k.utils.lobbyEquip
import de.soho5k.utils.queueItem
import net.axay.kspigot.extensions.bukkit.feed
import net.axay.kspigot.extensions.bukkit.heal
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import java.util.*

object PlayerManager {

    val players = hashMapOf<UUID, KnockoutPlayer>()

    val totalPlayers: Int
        get() = players.size

    //How many Players are still Playing
    val alivePlayers: Int
        get() = players.values.filter { it.mode == PlayerMode.PLAY }.size

    //How many Players died (in Games like Spleef) and are now Spectating
    val spectatingPlayer: Int
        get() = players.values.filter { it.mode == PlayerMode.SPECTATE }.size

    //How many Players left the Game
    val leftPlayers: Int
        get() = players.values.filter { it.mode == PlayerMode.LEFT }.size

    val staffOnline: Int
        get() = players.values.filter { it.mode == PlayerMode.STAFF }.size

    val queuedPlayers: Int
        get() = players.values.filter { it.mode == PlayerMode.QUEUE }.size


    fun enable() {
        println(">> Starting PlayerManager")
    }

    fun refreshAllBoards() {
        for (i in Bukkit.getOnlinePlayers()) {
            i.refreshBoard()
        }
    }

    fun teleportToLobby() {
        RankingManager.updateRankings()
        for (i in Bukkit.getOnlinePlayers()) {
            i.feed()
            i.isCollidable = true
            i.teleport(ConfigManager.lobbyLoc)
            i.teleportSound()
            i.lobbyEquip()
            i.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = 4.0
            if (i.mode != PlayerMode.STAFF) {
                removeFromSpectator(i)
            }
        }
        Knockout.team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
        Knockout.team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS)
        Knockout.team.setCanSeeFriendlyInvisibles(false)
        updateTab()
    }

    fun registerPlayer(player: Player) {
        if (!players.containsKey(player.uniqueId)) {
            players[player.uniqueId] = KnockoutPlayer(player.uniqueId, player.name)
            player.mode = PlayerMode.PLAY
        }
        if (!GameManager.gameStarted) checkTime()
        player.board = FastBoard(player)
        player.updateBoardTitle("&b&l${player.getLocalized("SCOREBOARD.lobby.title")}")

        task(delay = 2L, howOften = 1L) {
            player.teleport(ConfigManager.lobbyLoc)
            updateTab()
            RankingManager.updateRankings()
        }
        player.lobbyEquip()
        player.flySpeed = .1F
        player.walkSpeed = .2F
        player.heal()
        player.feed()
        refreshAllBoards()
        Knockout.team.addEntry(player.name)
        for (i in player.activePotionEffects) {
            player.removePotionEffect(i.type)
        }
        if (GameManager.inLobby && !GameManager.gameStarted && GameManager.lobbyTime > 10 && Knockout.useQueue) {
            player.inventory.setItem(4, queueItem)
        }
    }

    fun unregisterPlayer(player: Player) {
        RankingManager.updateRankings()
        if (player.mode == PlayerMode.QUEUE) {
            Knockout.team.removeEntry(player.name)
            player.deleteBoard()
            refreshAllBoards()
        } else {
            if (GameManager.gameStarted) {
                players[player.uniqueId]?.mode = PlayerMode.LEFT
            } else {
                players.remove(player.uniqueId)
            }
        }
    }

    fun registerQueuePlayer(uuid: UUID) {
        players[uuid] = KnockoutPlayer(uuid, "")
        players[uuid]?.mode = PlayerMode.QUEUE
    }

    fun unregisterQueuePlayer(uuid: UUID) {
        players.remove(uuid)
    }


    fun setToSpectator(player: Player, mode: PlayerMode = PlayerMode.SPECTATE) {
        player.walkSpeed = 0.2f
        player.mode = mode
        player.inventory.clear()
        player.allowFlight = true
        player.isCollidable = false
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false))
        for (i in onlinePlayers) {
            if (i.mode == PlayerMode.PLAY) {
                i.hidePlayer(Knockout.INSTANCE, player)
            } else {
                i.showPlayer(Knockout.INSTANCE, player)
            }
        }
        updateTab()
    }

    private fun removeFromSpectator(player: Player) {
        player.mode = PlayerMode.PLAY
        player.inventory.clear()
        player.allowFlight = false
        player.isCollidable = true
        player.removeAllPotionEffects()
        for (i in Bukkit.getOnlinePlayers()) i.showPlayer(Knockout.INSTANCE, player)
    }

    fun updateTab() {
        for (i in players.values) {
            if (i.mode == PlayerMode.SPECTATE || i.mode == PlayerMode.LEFT) {
                i.player?.setPlayerListName(" &b${i.points} &7${i.player?.name}".tc())
            } else {
                i.player?.setPlayerListName(" &b${i.points} &r${i.player?.name}".tc())
            }
        }
    }

    fun checkForAlive() {
        if (alivePlayers <= 1 && !GameManager.inLobby) {
            GameManager.activeGame.stop()
        }
    }

    private fun checkTime() {
        val amount = onlinePlayers.filter { it.mode == PlayerMode.PLAY }.size
        val minPlayers = ConfigManager.minPlayers
        when (amount) {
            in 0 until minPlayers -> {
                GameManager.lobbyTime = ConfigManager.lobbytime
            }
            in minPlayers..minPlayers + 5 -> {
                if (GameManager.lobbyTime > 120) {
                    GameManager.lobbyTime = 120
                    onlinePlayers.forEach {
                        it.ping()
                        it.sendLocalizedMessage(
                            "MESSAGE.time_shortened",
                            replacements = arrayOf(120.toTime())
                        )
                    }
                }
            }
            in minPlayers + 6..minPlayers + 16 -> {
                if (GameManager.lobbyTime > 90) {
                    GameManager.lobbyTime = 90
                    onlinePlayers.forEach {
                        it.ping()
                        it.sendLocalizedMessage(
                            "MESSAGE.time_shortened",
                            replacements = arrayOf(90.toTime())
                        )
                    }
                }
            }
            in minPlayers + 17..minPlayers + 27 -> {
                if (GameManager.lobbyTime > 60) {
                    GameManager.lobbyTime = 60
                    onlinePlayers.forEach {
                        it.ping()
                        it.sendLocalizedMessage(
                            "MESSAGE.time_shortened",
                            replacements = arrayOf(60.toTime())
                        )
                    }
                }
            }
            else -> {
                if (GameManager.lobbyTime > 30) {
                    GameManager.lobbyTime = 30
                    onlinePlayers.forEach {
                        it.ping()
                        it.sendLocalizedMessage(
                            "MESSAGE.time_shortened",
                            replacements = arrayOf(30.toTime())
                        )
                    }
                }
            }
        }
    }
}