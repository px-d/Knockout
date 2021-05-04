package de.soho5k.utils.extensions

import de.soho5k.utils.PREFIX
import org.bukkit.ChatColor
import java.awt.Color

fun String.toScoreboard(
    totalPlayers: Int? = null,
    alivePlayers: Int? = null,
    time: Int? = null,
    max_players: Int? = null,
    min_players: Int? = null,
    points: Int? = null,
    anvilRate: Int? = null,
    queuePlayers: Int? = null
): List<String> {
    var text = this
    if (totalPlayers != null) text = text.replace("{total_players}", "$totalPlayers")
    if (alivePlayers != null) text = text.replace("{alive_players}", "&a$alivePlayers&r".tc())
    if (time != null) text = text.replace("{time}", "&a${time.toTime()}&r".tc())
    if (max_players != null) text = text.replace("{max_players}", "$max_players")
    if (min_players != null) text = text.replace("{min_players}", "$min_players")
    if (points != null) text = text.replace("{points}", "&a$points&r".tc())
    if (anvilRate != null) text = text.replace("{anvilrate}", "$anvilRate")
    if (queuePlayers != null) text = text.replace("{queue_players}", "$queuePlayers")
    text = text.replace("{prefix}", PREFIX)
    return text.split("{n}")
}

fun String.tc(): String {
    val regex = "\\[rgb:[0-9]+,[0-9]+,[0-9]+]".toRegex()
    var text = this
    for (i in regex.findAll(this)) {
        val components = i.value.replace("[rgb:", "").replace("]", "").split(",")
        val color = net.md_5.bungee.api.ChatColor.of(
            Color(
                components[0].toInt(),
                components[1].toInt(),
                components[2].toInt()
            )
        )
        text = text.replace(i.value, "$color")
    }
    return ChatColor.translateAlternateColorCodes('&', text)
}

fun String.sc(): String {
    return "${ChatColor.stripColor(this)}"
}

fun String.toRgb(): Color {
    val hex = this.replace("#", "")
    return Color(
        Integer.valueOf(hex.substring(0, 2), 16),
        Integer.valueOf(hex.substring(2, 4), 16),
        Integer.valueOf(hex.substring(4, 6), 16)
    )
}
