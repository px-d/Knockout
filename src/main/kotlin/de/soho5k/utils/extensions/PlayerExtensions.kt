package de.soho5k.utils.extensions

import board.FastBoard
import de.soho5k.game.GameManager
import de.soho5k.game.games.AnvilRain
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.Localization.ll
import net.axay.kspigot.extensions.bukkit.feed
import net.axay.kspigot.extensions.bukkit.heal
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
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
    if (GameManager.inLobby) {
        if (!GameManager.gameStarted) {
            updateBoard(
                ll("SCOREBOARD.lobby.text")
                    .toScoreboard(
                        PlayerManager.totalPlayers,
                        PlayerManager.alivePlayers,
                        GameManager.lobbytime,
                        ConfigManager.maxPlayers,
                        ConfigManager.minPlayers,
                        PlayerManager.queuedPlayers
                    )
            )
        } else if (GameManager.ended) {
            updateBoardTitle(ll("SCOREBOARD.lobby.ended.title"))
            updateBoard(ll("SCOREBOARD.lobby.ended.text").toScoreboard(points = PlayerManager.players.values.find { it.player.uniqueId == uniqueId }?.points))
        } else {
            updateBoardTitle(ll("SCOREBOARD.lobby.title"))
            updateBoard(ll("SCOREBOARD.lobby.waiting.text").toScoreboard(points = PlayerManager.players.values.find { it.player.uniqueId == uniqueId }?.points))
        }
    } else {
        val id = GameManager.activeGame.id
        updateBoardTitle(ll("SCOREBOARD.title.$id").tc())
        updateBoard(
            ll("SCOREBOARD.text.$id").toScoreboard(
                PlayerManager.totalPlayers,
                PlayerManager.alivePlayers,
                GameManager.activeGame.time,
                ConfigManager.maxPlayers,
                ConfigManager.minPlayers,
                PlayerManager.players.values.find { it.player.uniqueId == uniqueId }?.points,
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

fun Player.msg(
    key: String,
    a: String? = null,
    b: String? = null,
    c: String? = null,
    d: String? = null,
    e: String? = null,
    f: String? = null,
    g: String? = null,
) {
    var text = ll(key)
    text = text.replace("{prefix}", ll("PREFIX"))
    if (a != null) text = text.replace("{0}", a)
    if (b != null) text = text.replace("{1}", b)
    if (c != null) text = text.replace("{2}", c)
    if (d != null) text = text.replace("{3}", d)
    if (e != null) text = text.replace("{4}", e)
    if (f != null) text = text.replace("{5}", f)
    if (g != null) text = text.replace("{6}", g)
    for (i in text.split("{n}")) {
        sendMessage(i.tc())
    }
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
            msg("MESSAGES.coins.gained", "${abs(amount)}", "$points")
        } else {
            msg("MESSAGES.coins.lost", "${abs(amount)}", "$points")
        }
    }
}

fun Player.ping() {
    playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
}

fun Player.teleportSound() {
    playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
}

fun Player.killSound() {
    playSound(location, Sound.BLOCK_BELL_USE, .5f, .1f)
}

fun Player.suicideSound() {
    playSound(location, Sound.ENTITY_IRON_GOLEM_DAMAGE, 1f, 1f)
}

fun Player.fuseSound() {
    playSound(location, Sound.ENTITY_TNT_PRIMED, 0.5f, 1f)
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
        for (x in i.ll("MESSAGES.intro.$id").split("{n}")) {
            i.sendMessage(x.tc())
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

fun messageAll(key: String, key2: String, value: String) {
    for (i in Bukkit.getOnlinePlayers()) {
        i.msg(key, i.ll(key2), value)
    }
}

fun messageAll(
    key: String,
    a: String? = null,
    b: String? = null,
    c: String? = null,
    d: String? = null,
    e: String? = null,
    f: String? = null,
) {
    for (i in Bukkit.getOnlinePlayers()) {
        i.msg(key, a, b, c, d, e, f)
    }
}

fun titleToAll(title: String, subtitle: String = "", fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 10) {
    for (i in Bukkit.getOnlinePlayers()) {
        i.title(title, subtitle, fadeIn, stay, fadeOut)
    }
}

//TODO("var Player.team: Team")
