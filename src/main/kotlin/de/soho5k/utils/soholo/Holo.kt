package de.soho5k.utils.soholo

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType

class Holo(private val location: Location, text: List<String>, private val small: Boolean, val id: String) {

    val armorstands = arrayListOf<ArmorStand>()
    private var originLoc = location.clone()

    init {
        for (i in text) {
            addLine(i)
        }
    }

    /**
     * Add a Line to the existing Hologram
     * @param text The line you want to add
     */
    private fun addLine(text: String) {
        val loc = if (armorstands.size > 0) {
            armorstands.last().location.add(0.0, -0.25, 0.0)
        } else {
            location
        }

        val armorStand = loc.world!!.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.isSmall = small
        armorStand.isVisible = false
        armorStand.isInvulnerable = true
        armorStand.setGravity(false)
        armorStand.customName = ChatColor.translateAlternateColorCodes('&', text)
        armorStand.isCustomNameVisible = text.replace(" ", "").isNotEmpty()
        armorStand.isCollidable = false
        armorstands.add(armorStand)
    }

    /**
     * Change the Origin of the Hologram
     * @param location The New Origin
     */
    fun setOrigin(location: Location) {
        this.originLoc = location
        var tempLoc = originLoc
        for (i in armorstands) {
            i.teleport(tempLoc)
            tempLoc = tempLoc.add(0.0, -0.25, 0.0)
        }
    }

    fun setLine(index: Int, text: String) {
        if(index>armorstands.size) {
            println("You want to set a Line that doesn't exist.")
            return
        }
        armorstands[index].customName = ChatColor.translateAlternateColorCodes('&', text)
    }

    fun deleteAll() {
        for (i in armorstands) {
            i.remove()
        }
    }
}