package de.soho5k.utils

import de.soho5k.game.GameManager
import net.axay.kspigot.runnables.task
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import util.ItemBuilder

val queueItem: ItemStack = ItemBuilder(Material.HEART_OF_THE_SEA).name("&6Queue").build()

val kbFfaStick: ItemStack = ItemBuilder(Material.STICK).enchant(Enchantment.KNOCKBACK, 2).build()
val spleefShovel: ItemStack = ItemBuilder(Material.GOLDEN_SHOVEL).setUnbreakable(true).build()
val snowFightShovel: ItemStack =
    ItemBuilder(Material.WOODEN_SHOVEL).setUnbreakable(true).enchant(Enchantment.DIG_SPEED, 1).build()
val oitcBow: ItemStack = ItemBuilder(Material.BOW).setUnbreakable(true).build()
val oitcSword: ItemStack = ItemBuilder(Material.WOODEN_SWORD).setUnbreakable(true).build()
val butcherSword: ItemStack = ItemBuilder(Material.NETHERITE_SWORD).setUnbreakable(true).build()
val molecraftPick: ItemStack = ItemBuilder(Material.IRON_PICKAXE).setUnbreakable(true).build()
val molecraftShovel: ItemStack =
    ItemBuilder(Material.IRON_SHOVEL).enchant(Enchantment.DIG_SPEED, 5).setUnbreakable(true).build()
val kothStick: ItemStack = ItemBuilder(Material.STICK).enchant(Enchantment.KNOCKBACK, 4).build()

val hpHead = ItemBuilder(Material.TNT).name("&cHot Potato").setUnbreakable(true).build()
val hpChest = ItemBuilder(Material.LEATHER_CHESTPLATE).armorColor(Color.RED).setUnbreakable(true).build()
val hpLegs = ItemBuilder(Material.LEATHER_LEGGINGS).armorColor(Color.RED).setUnbreakable(true).build()
val hpBoots = ItemBuilder(Material.LEATHER_BOOTS).armorColor(Color.RED).setUnbreakable(true).build()
val trident = ItemBuilder(Material.TRIDENT).enchant(Enchantment.RIPTIDE, 1).setUnbreakable(true).build()


fun Player.clearInv() {
    inventory.clear()
    inventory.setArmorContents(null)
}

fun Player.lobbyEquip() {
    inventory.clear()
    task(delay = 2L, howOften = 1L) {
        if (!GameManager.gameStarted) {
            inventory.setItem(8, getPatchBook(locale))
        }
    }
}

fun Player.knockbackFFAEquip() {
    inventory.clear()
    inventory.heldItemSlot = 0
    inventory.setItemInMainHand(kbFfaStick)
}

fun Player.spleefEquip() {
    inventory.clear()
    inventory.heldItemSlot = 0
    inventory.setItemInMainHand(spleefShovel)
}

fun Player.oitcEquip() {
    inventory.clear()
    inventory.setItem(0, oitcSword)
    inventory.setItem(1, oitcBow)
    inventory.setItem(2, ItemStack(Material.ARROW, 1))
}

fun Player.moleCraftEquip() {
    inventory.clear()
    inventory.setItem(0, molecraftPick)
    inventory.setItem(1, molecraftShovel)
}

fun Player.hpEquip() {
    inventory.helmet = hpHead
    inventory.chestplate = hpChest
    inventory.leggings = hpLegs
    inventory.boots = hpBoots
}

fun Player.kothEquip() {
    inventory.clear()
    inventory.setItem(0, kothStick)
}

fun Player.riptideEquip() {
    inventory.clear()
    inventory.setItem(0, trident)
}

fun Player.snowFightEquip() {
    inventory.clear()
    inventory.setItem(0, snowFightShovel)
}

