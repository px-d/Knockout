package de.soho5k.utils

import de.soho5k.player.PlayerMode
import de.soho5k.utils.extensions.addPoints
import de.soho5k.utils.extensions.mode
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Class to easily add points for players based on different
 */
class PointDistributor {

    /**
     * List of all Players who are taking part in the ranking
     */
    private val players = arrayListOf<Player>()

    /**
     * Add a player to be ranked
     * @param player The Player you want to add
     * @param killer The Killer who might also receive points
     * @param points The amount of points the Player might receive
     */
    fun addPlayer(player: Player, killer: Player? = null, points: Int? = null) {
        if (!players.contains(player) && (player.mode == PlayerMode.PLAY || player.mode == PlayerMode.SPECTATE)) {
            players.add(player)
        }
        if (killer != null && points != null) {
            killer.addPoints(points)
        }
    }

    /**
     * Adds all remaining Players so no one is missing
     */
    private fun addRemainingPlayers() {
        for (i in onlinePlayers) {
            if (!players.contains(i) && (i.mode == PlayerMode.PLAY || i.mode == PlayerMode.SPECTATE)) {
                players.add(i)
            }
        }
    }

    /**
     * Distribute Rewards
     * @param mode How you want to distribute Points
     *      FIRST = First to Last -> Points List must not be null (Points get Distributed after Points List)
     *      PER_POSITION = First in list gets 1 Point, Second 2, Third 3 and so on
     */
    fun giveRewards(mode: RewardMode = RewardMode.FIRST_TO_LAST, points: List<Int> = listOf(5, 4, 3, 2, 1)) {
        when (mode) {
            RewardMode.FIRST_TO_LAST -> {
                addRemainingPlayers()
                val list = players.reversed()
                for (i in list.indices) {
                    if ((points.size - 1) >= i) {
                        list[i].addPoints(points[i])
                    }
                }
            }
            RewardMode.LAST_TO_FIRST ->{
                addRemainingPlayers()
                val list = players.reversed()
                for(i in list)
                    i.addPoints(list.indexOf(i))
            }
            RewardMode.PER_POSITION -> {
                addRemainingPlayers()
                for (i in players) {
                    i.addPoints(players.indexOf(i) + 1)
                }
            }
            RewardMode.POSITION_THEN_X -> {
                val remaining = onlinePlayers.filter { it.mode == PlayerMode.PLAY && !players.contains(it) }
                    .sortedBy { it.location.x }.reversed()
                remaining.forEach { players.add(it) }
                for (i in players.reversed()) {
                    i.addPoints(players.reversed().indexOf(i) + 1)
                }
            }
            RewardMode.POSITION_THEN_Y -> {
                val remaining = onlinePlayers.filter { it.mode == PlayerMode.PLAY && !players.contains(it) }
                    .sortedBy { it.location.y }.reversed()
                remaining.forEach { players.add(it) }
                for (i in players.reversed()) {
                    i.addPoints(players.reversed().indexOf(i) + 1)
                }
            }
            RewardMode.POSITION_THEN_Z -> {
                val remaining = onlinePlayers.filter { it.mode == PlayerMode.PLAY && !players.contains(it) }
                    .sortedBy { it.location.z }.reversed()
                remaining.forEach { players.add(it) }
                for (i in players.reversed()) {
                    i.addPoints(players.reversed().indexOf(i) + 1)
                }
            }
        }
    }
}

enum class RewardMode {
    FIRST_TO_LAST, LAST_TO_FIRST, PER_POSITION, POSITION_THEN_X, POSITION_THEN_Y, POSITION_THEN_Z
}