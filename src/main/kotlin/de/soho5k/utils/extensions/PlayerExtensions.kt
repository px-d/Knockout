package de.soho5k.utils.extensions

import board.FastBoard
import de.soho5k.game.GameManager
import de.soho5k.game.games.AnvilRain
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.Localization.getLocalized
import de.soho5k.utils.PREFIX
import de.soho5k.utils.QUOTATION
import net.axay.kspigot.extensions.bukkit.feed
import net.axay.kspigot.extensions.bukkit.heal
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.runnables.task
import org.bukkit.*
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

val boards = hashMapOf<UUID, FastBoard>()

fun Player.removeAllPotionEffects() {
    for (i in activePotionEffects) {
        removePotionEffect(i.type)
    }
}

var Player.board: FastBoard
    get() {
        return if (boards.containsKey(uniqueId)) {
            boards[uniqueId]!!
        } else {
            createBoard(this)
        }
    }
    set(value) {
        boards[uniqueId] = value
    }

fun createBoard(player: Player): FastBoard {
    val board = FastBoard(player)
    boards[player.uniqueId] = board
    return board
}

fun Player.updateBoard(lines: List<String>) {
    boards[uniqueId]?.lines = lines
}

fun Player.refreshBoard() {
    if (!boards.containsKey(uniqueId)) return
    if (GameManager.inLobby) {
        if (!GameManager.gameStarted) {
            updateBoard(
                getLocalized("SCOREBOARD.lobby.text")
                    .toScoreboard(
                        PlayerManager.totalPlayers,
                        PlayerManager.alivePlayers,
                        GameManager.lobbytime,
                        ConfigManager.maxPlayers,
                        ConfigManager.minPlayers,
                        null,
                        null,
                        PlayerManager.queuedPlayers
                    )
            )
        } else if (GameManager.ended) {
            updateBoardTitle(getLocalized("SCOREBOARD.lobby.ended.title"))
            updateBoard(getLocalized("SCOREBOARD.lobby.ended.text").toScoreboard(points = PlayerManager.players.values.find { it.player?.uniqueId == uniqueId }?.points))
        } else {
            updateBoardTitle(getLocalized("SCOREBOARD.lobby.title"))
            updateBoard(getLocalized("SCOREBOARD.lobby.waiting.text").toScoreboard(points = PlayerManager.players.values.find { it.player?.uniqueId == uniqueId }?.points))
        }
    } else {
        val id = GameManager.activeGame.id
        updateBoardTitle("&b&l${getLocalized("SCOREBOARD.title.$id")}".tc())
        updateBoard(
            getLocalized("SCOREBOARD.text.$id").toScoreboard(
                PlayerManager.totalPlayers,
                PlayerManager.alivePlayers,
                GameManager.activeGame.time,
                ConfigManager.maxPlayers,
                ConfigManager.minPlayers,
                PlayerManager.players.values.find { it.player?.uniqueId == uniqueId }?.points,
                if (GameManager.activeGame is AnvilRain) (GameManager.activeGame as AnvilRain).rate else 0
            )
        )
    }
}

fun Player.updateBoardTitle(title: String) {
    boards[uniqueId]?.updateTitle(title.tc())
}

fun Player.deleteBoard() {
    boards.remove(uniqueId)?.delete()
}

fun Player.sendLocalizedMessage(
    key: String,
    prefix: Boolean = true,
    mainColor: ChatColor = ChatColor.GRAY,
    accentColor: ChatColor = ChatColor.YELLOW,
    vararg replacements: String
) {
    val localized = getLocalized(key)
    val inserts = arrayListOf<String>()
    replacements.forEach { inserts.add("$accentColor$it$mainColor") }
    val pref = if (prefix) "$PREFIX $QUOTATION " else ""
    sendMessage("$pref$mainColor${localized.format(*inserts.toArray())}")
}

var Player.mode: PlayerMode
    get() {
        return PlayerManager.players[uniqueId]?.mode ?: throw Exception("Error retreiving Payer Mode")
    }
    set(value) {
        PlayerManager.players[uniqueId]?.mode = value
    }

var Player.points: Int
    get() {
        return PlayerManager.players[uniqueId]?.points ?: 0
    }
    set(value) {
        PlayerManager.players[uniqueId]?.points = value
        PlayerManager.updateTablists()
    }

fun Player.addPoints(amount: Int, silent: Boolean = false) {
    points += amount
    if (points < 0) points = 0
    if (!silent) {
        if (amount > 0) {
            sendLocalizedMessage("MESSAGES.coins.gained", replacements = arrayOf("${abs(amount)}", "$points"))
        } else {
            sendLocalizedMessage("MESSAGES.coins.lost", replacements = arrayOf("${abs(amount)}", "$points"))
        }
    }
}

fun Player.ping(pitch: Float = 1f) {
    playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, pitch)
}

fun Player.teleportSound(pitch: Float = 1f) {
    playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, pitch)
}

fun Player.killSound(pitch: Float = 1f) {
    playSound(location, Sound.BLOCK_BELL_USE, .5f, pitch)
}

fun Player.suicideSound(pitch: Float = 1f) {
    playSound(location, Sound.ENTITY_IRON_GOLEM_DAMAGE, 1f, pitch)
}

fun Player.fuseSound(pitch: Float = 1f) {
    playSound(location, Sound.ENTITY_TNT_PRIMED, 0.5f, pitch)
}

fun Player.xpSound(pitch: Float = 1f) {
    playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, pitch)
}


fun Player.timeBombSound() {
    playSound(location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, .6f)
    task(delay = 1, howOften = 1) {
        playSound(location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, .8f)
    }
    task(delay = 2, howOften = 1) {
        playSound(location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f)
    }
}

fun Player.amountOf(mat: Material): Int {
    var a = 0
    for (i in inventory.contents) {
        if (i != null) {
            if (i.type == mat) {
                a += i.amount
            }
        }
    }

    return a
}


fun broadcastIntroToAll(id: String) {
    for (i in Bukkit.getOnlinePlayers()) {
        i.sendMessage("&7===========================================".tc())
        i.sendMessage("&7${i.getLocalized("TEXT.welcome_to")}: &b&l${i.getLocalized("SCOREBOARD.title.$id")}".tc())
        i.sendMessage("&9${i.getLocalized("TEXT.how_does_it_work")}:".tc())
        i.sendMessage("")
        for (x in i.getLocalized("MESSAGES.intro.$id").split("{n}")) {
            i.sendMessage("&7$x".tc())
        }
        i.sendMessage("&7===========================================".tc())
    }
}

fun teleportAllToGame(loc: Location) {
    for (i in Bukkit.getOnlinePlayers()) {
        i.teleport(loc)
        i.heal()
        i.feed()
        if (i.mode == PlayerMode.STAFF) {
            i.isFlying = true
            i.allowFlight = true
        }
    }
}