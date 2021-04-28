package de.soho5k.player

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class KnockoutPlayer(
    val uuid: UUID,
    var survivedRounds: Int = 0,
    var points: Int = 0,
    var mode: PlayerMode = PlayerMode.PLAY
) {

    val player: Player
        get() = Bukkit.getPlayer(uuid) ?: throw Exception ("Player Not Online")

}