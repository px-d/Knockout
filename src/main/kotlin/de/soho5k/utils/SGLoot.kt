package de.soho5k.utils

import de.soho5k.knockout.Knockout
import de.soho5k.utils.extensions.tc
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import util.ItemBuilder
import util.RandomCollection
import kotlin.random.Random

//TODO("High Tier Loot for Barrels ans stuff")
object SGLoot {

    private val lowtierloot = RandomCollection<ItemStack>()
    private val snorlax: ItemStack =
        ItemBuilder(Material.GOLDEN_SWORD).name("&fRelaxo-Slayer").enchant(Enchantment.FIRE_ASPECT, 1).build()

    val fns: ItemStack = ItemBuilder(Material.FLINT_AND_STEEL).damage(3).build()

    init {
        lowtierloot.add(150.0, item(Material.IRON_INGOT))
        lowtierloot.add(100.0, item(Material.IRON_INGOT, 2))
        lowtierloot.add(80.0, item(Material.DIAMOND))
        lowtierloot.add(400.0, item(Material.LAPIS_LAZULI))
        lowtierloot.add(250.0, item(Material.LAPIS_LAZULI, 2))
        lowtierloot.add(400.0, item(Material.EXPERIENCE_BOTTLE))
        lowtierloot.add(250.0, item(Material.EXPERIENCE_BOTTLE, 2))
        lowtierloot.add(50.0, snorlax)
        lowtierloot.add(500.0, item(Material.APPLE))
        lowtierloot.add(450.0, item(Material.APPLE, 2))
        lowtierloot.add(100.0, item(Material.MUSHROOM_STEW))
        lowtierloot.add(400.0, item(Material.COOKED_PORKCHOP))
        lowtierloot.add(100.0, item(Material.GOLDEN_APPLE))
        lowtierloot.add(650.0, item(Material.BREAD))
        lowtierloot.add(500.0, item(Material.BREAD, 2))
        lowtierloot.add(300.0, item(Material.BREAD, 3))
        lowtierloot.add(350.0, item(Material.CAKE))
        lowtierloot.add(200.0, makePotion(PotionEffectType.POISON, 10, throwable = true))
        lowtierloot.add(200.0, makePotion(PotionEffectType.SPEED, 20, throwable = true))
        lowtierloot.add(100.0, makeEBook(Enchantment.DAMAGE_ALL, 1))
        lowtierloot.add(50.0, makeEBook(Enchantment.DAMAGE_ALL, 2))
        lowtierloot.add(100.0, makeEBook(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        lowtierloot.add(50.0, makeEBook(Enchantment.PROTECTION_ENVIRONMENTAL, 2))
        lowtierloot.add(500.0, item(Material.FISHING_ROD))
        lowtierloot.add(400.0, item(Material.BOW))
        lowtierloot.add(100.0, item(Material.CROSSBOW))
        lowtierloot.add(600.0, item(Material.WOODEN_SWORD))
        lowtierloot.add(500.0, item(Material.STONE_SWORD))
        lowtierloot.add(200.0, item(Material.IRON_SWORD))
        lowtierloot.add(300.0, item(Material.WOODEN_AXE))
        lowtierloot.add(200.0, item(Material.STONE_AXE))
        lowtierloot.add(500.0, item(Material.SHIELD))
        lowtierloot.add(300.0, fns)
        lowtierloot.add(600.0, item(Material.COMPASS, 1, "&fTracker"))
        lowtierloot.add(200.0, item(Material.TNT))
        lowtierloot.add(250.0, item(Material.CHAINMAIL_HELMET))
        lowtierloot.add(100.0, item(Material.CHAINMAIL_CHESTPLATE))
        lowtierloot.add(100.0, item(Material.CHAINMAIL_LEGGINGS))
        lowtierloot.add(250.0, item(Material.CHAINMAIL_BOOTS))
        lowtierloot.add(400.0, item(Material.GOLDEN_HELMET))
        lowtierloot.add(300.0, item(Material.GOLDEN_CHESTPLATE))
        lowtierloot.add(300.0, item(Material.GOLDEN_LEGGINGS))
        lowtierloot.add(400.0, item(Material.GOLDEN_BOOTS))
        lowtierloot.add(100.0, item(Material.IRON_HELMET))
        lowtierloot.add(50.0, item(Material.IRON_CHESTPLATE))
        lowtierloot.add(50.0, item(Material.IRON_LEGGINGS))
        lowtierloot.add(100.0, item(Material.IRON_BOOTS))
        lowtierloot.add(200.0, item(Material.SPECTRAL_ARROW, 2))
        lowtierloot.add(150.0, item(Material.SPECTRAL_ARROW, 3))
        lowtierloot.add(450.0, item(Material.ARROW, 2))
        lowtierloot.add(400.0, item(Material.ARROW, 4))
        lowtierloot.add(350.0, item(Material.ARROW, 6))
        lowtierloot.add(150.0, item(Material.COBWEB))
        lowtierloot.add(200.0, item(Material.STICK))
        lowtierloot.add(100.0, item(Material.STICK, 2))
        lowtierloot.add(500.0, item(Material.LEATHER_HELMET))
        lowtierloot.add(400.0, item(Material.LEATHER_CHESTPLATE))
        lowtierloot.add(400.0, item(Material.LEATHER_LEGGINGS))
        lowtierloot.add(500.0, item(Material.LEATHER_BOOTS))
        lowtierloot.add(100.0, item(Material.LAVA_BUCKET))
        lowtierloot.add(200.0, item(Material.WATER_BUCKET))
    }

    fun setLoot(block: Block) {
        if (!block.hasMetadata("opened")) {
            val chest = block.state as Chest

            val amount = Random.nextInt(4) + 1
            chest.inventory.clear()
            for (i in 0..amount) {
                chest.inventory.setItem(Random.nextInt(27), lowtierloot.next())
            }
            block.setMetadata("opened", FixedMetadataValue(Knockout.INSTANCE,true))
        }
    }

    private fun item(mat: Material, amount: Int = 1, name: String? = null): ItemStack {
        return if (name == null) ItemStack(mat, amount) else ItemBuilder(mat).amount(amount).name(name.tc()).build()
    }

    private fun makeEBook(enchantment: Enchantment, amplifier: Int): ItemStack {
        val book = ItemStack(Material.ENCHANTED_BOOK)
        val meta = book.itemMeta as EnchantmentStorageMeta
        meta.addStoredEnchant(enchantment, amplifier, true)
        book.itemMeta = meta
        return book
    }

    private fun makePotion(
        type: PotionEffectType,
        duration: Int,
        amplifier: Int = 0,
        throwable: Boolean = false
    ): ItemStack {
        val potion = if (throwable) ItemStack(Material.SPLASH_POTION) else ItemStack(Material.POTION)
        val pmeta = potion.itemMeta as PotionMeta
        pmeta.addCustomEffect(PotionEffect(type, duration * 20, amplifier, false, false, false), true)
        when (type) {
            PotionEffectType.POISON -> {
                pmeta.color = org.bukkit.Color.GREEN
                pmeta.setDisplayName("[rgb:0,150,0]Poison".tc())
            }
            PotionEffectType.HEAL -> {
                pmeta.color = org.bukkit.Color.RED
                pmeta.setDisplayName("[rgb:150,50,50]Heal".tc())
            }
            PotionEffectType.SPEED -> {
                pmeta.color = org.bukkit.Color.AQUA
                pmeta.setDisplayName("[rgb:50,150,150]Speed".tc())
            }
        }
        potion.itemMeta = pmeta
        return potion
    }

}