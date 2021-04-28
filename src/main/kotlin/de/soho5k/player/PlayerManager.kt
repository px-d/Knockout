package de.soho5k.player

import board.FastBoard
import de.soho5k.game.GameManager
import de.soho5k.knockout.ConfigManager
import de.soho5k.knockout.Knockout
import de.soho5k.knockout.RankingManager
import de.soho5k.utils.Localization.ll
import de.soho5k.utils.extensions.*
import de.soho5k.utils.lobbyEquip
import net.axay.kspigot.extensions.bukkit.feed
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
    }

    fun registerPlayer(player: Player) {
        players[player.uniqueId] = KnockoutPlayer(player.uniqueId)
        player.board = FastBoard(player)
        player.updateBoardTitle(player.ll("SCOREBOARD.lobby.title"))

        task(delay = 5L, howOften = 1L) {
            player.teleport(ConfigManager.lobbyLoc)
        }
        player.lobbyEquip()
        player.flySpeed = .1F
        player.walkSpeed = .2F
        refreshAllBoards()
        Knockout.team.addEntry(player.name)
        for (i in player.activePotionEffects) {
            player.removePotionEffect(i.type)
        }
        updateTablists()
        RankingManager.updateRankings()
    }

    fun registerQueuePlayer(uuid: UUID){
        players[uuid] = KnockoutPlayer(uuid)
        players[uuid]?.mode = PlayerMode.QUEUE
    }

    fun unregisterQueuePlayer(uuid: UUID){
        players.remove(uuid)
    }

    fun unregisterPlayer(player: Player) {
        if (player.mode == PlayerMode.QUEUE) {
            player.deleteBoard()
            refreshAllBoards()
            Knockout.team.removeEntry(player.name)
        } else {
            if (!GameManager.gameStarted) {
                players.remove(player.uniqueId)
            } else {
                players[player.uniqueId]?.mode = PlayerMode.LEFT
            }
        }
    }

    fun setToSpectator(player: Player) {
        player.walkSpeed = 0.2f
        player.mode = PlayerMode.SPECTATE
        player.inventory.clear()
        player.allowFlight = true
        player.isCollidable = false
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false))
        for (i in Bukkit.getOnlinePlayers()) i.hidePlayer(Knockout.INSTANCE, player)
    }

    fun removeFromSpectator(player: Player) {
        player.mode = PlayerMode.PLAY
        player.inventory.clear()
        player.allowFlight = false
        player.isCollidable = true
        for (i in player.activePotionEffects) player.removePotionEffect(i.type)
        for (i in Bukkit.getOnlinePlayers()) i.showPlayer(Knockout.INSTANCE, player)
    }

    fun updateTablists() {
        for (i in players.values) {
            i.player.setPlayerListName(" &b${i.points} &r${i.player.name}".tc())
        }
    }

    fun checkForAlive() {
        if (alivePlayers <= 1 && !GameManager.inLobby) {
            GameManager.activeGame.stop()
        }
    }
}