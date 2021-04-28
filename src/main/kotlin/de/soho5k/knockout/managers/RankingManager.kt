package de.soho5k.knockout.managers

import de.soho5k.knockout.Knockout
import de.soho5k.utils.extensions.getOrSet
import de.soho5k.utils.extensions.points
import de.soho5k.utils.soholo.Holo
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.Bukkit
import org.bukkit.Location

object RankingManager {

    lateinit var location: Location
    lateinit var holo: Holo

    fun enable() {
        location =
            Knockout.INSTANCE.config.getOrSet(
                "locations.hologram",
                Location(Bukkit.getWorld("world"), 0.5, 51.0, -3.5)
            ) as Location

        holo = Knockout.holoManager.createHolo(
            location,
            false,
            false,
            true,
            "&b&lKnockout Ranking:",
            " ",
            "&cNaN",
            "&cNaN",
            "&cNaN",
            "&cNaN",
            "&cNaM",
            "&cNaN",
            "&cNaN",
            "&cNaN",
            "&cNaN",
            "&cNaN",
            "&cNaN"
        )

        updateRankings()
    }

    fun updateRankings() {
        val players = onlinePlayers.sortedByDescending { it.points }
        val topTen = players.subList(0, if (players.size > 10) 10 else players.size)

        for (i in topTen) {
            holo.setLine(topTen.indexOf(i) + 2, "&e${topTen.indexOf(i) + 1}. &6${i.name} &7- &e${i.points}")
        }
    }

}