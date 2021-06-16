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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import util.ItemBuilder
import util.RandomCollection
import kotlin.random.Random

//TODO("High Tier Loot for Barrels ans stuff")
object SGLoot {

    private val low_tier = RandomCollection<ItemStack>()
    private val special_sword: ItemStack =
        ItemBuilder(Material.GOLDEN_SWORD).name("&fRelaxo-Slayer").enchant(Enchantment.FIRE_ASPECT, 1).build()

    init {
        low_tier.add(150.0, item(Material.IRON_INGOT))
        low_tier.add(100.0, item(Material.IRON_INGOT, 2))
        low_tier.add(80.0, item(Material.DIAMOND))
        low_tier.add(400.0, item(Material.LAPIS_LAZULI))
        low_tier.add(250.0, item(Material.LAPIS_LAZULI, 2))
        low_tier.add(400.0, item(Material.EXPERIENCE_BOTTLE))
        low_tier.add(250.0, item(Material.EXPERIENCE_BOTTLE, 2))
        low_tier.add(50.0, special_sword)
        low_tier.add(500.0, item(Material.APPLE))
        low_tier.add(450.0, item(Material.APPLE, 2))
        low_tier.add(100.0, item(Material.MUSHROOM_STEW))
        low_tier.add(400.0, item(Material.COOKED_PORKCHOP))
        low_tier.add(100.0, item(Material.GOLDEN_APPLE))
        low_tier.add(650.0, item(Material.BREAD))
        low_tier.add(500.0, item(Material.BREAD, 2))
        low_tier.add(300.0, item(Material.BREAD, 3))
        low_tier.add(350.0, item(Material.CAKE))
        low_tier.add(200.0, makePotion(PotionEffectType.POISON, 10, throwable = true))
        low_tier.add(200.0, makePotion(PotionEffectType.SPEED, 20, throwable = true))
        low_tier.add(100.0, makeEBook(Enchantment.DAMAGE_ALL, 1))
        low_tier.add(50.0, makeEBook(Enchantment.DAMAGE_ALL, 2))
        low_tier.add(100.0, makeEBook(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        low_tier.add(50.0, makeEBook(Enchantment.PROTECTION_ENVIRONMENTAL, 2))
        low_tier.add(500.0, item(Material.FISHING_ROD))
        low_tier.add(400.0, item(Material.BOW))
        low_tier.add(100.0, item(Material.CROSSBOW))
        low_tier.add(600.0, item(Material.WOODEN_SWORD))
        low_tier.add(500.0, item(Material.STONE_SWORD))
        low_tier.add(200.0, item(Material.IRON_SWORD))
        low_tier.add(300.0, item(Material.WOODEN_AXE))
        low_tier.add(200.0, item(Material.STONE_AXE))
        low_tier.add(500.0, item(Material.SHIELD))
        low_tier.add(200.0, item(Material.FIRE_CHARGE,1))
        low_tier.add(250.0, item(Material.FIRE_CHARGE,2))
        low_tier.add(300.0, item(Material.FIRE_CHARGE,3))
        low_tier.add(600.0, item(Material.COMPASS, 1, "&fTracker"))
        low_tier.add(200.0, item(Material.TNT))
        low_tier.add(250.0, item(Material.CHAINMAIL_HELMET))
        low_tier.add(100.0, item(Material.CHAINMAIL_CHESTPLATE))
        low_tier.add(100.0, item(Material.CHAINMAIL_LEGGINGS))
        low_tier.add(250.0, item(Material.CHAINMAIL_BOOTS))
        low_tier.add(400.0, item(Material.GOLDEN_HELMET))
        low_tier.add(300.0, item(Material.GOLDEN_CHESTPLATE))
        low_tier.add(300.0, item(Material.GOLDEN_LEGGINGS))
        low_tier.add(400.0, item(Material.GOLDEN_BOOTS))
        low_tier.add(100.0, item(Material.IRON_HELMET))
        low_tier.add(50.0, item(Material.IRON_CHESTPLATE))
        low_tier.add(50.0, item(Material.IRON_LEGGINGS))
        low_tier.add(100.0, item(Material.IRON_BOOTS))
        low_tier.add(200.0, item(Material.SPECTRAL_ARROW, 2))
        low_tier.add(150.0, item(Material.SPECTRAL_ARROW, 3))
        low_tier.add(450.0, item(Material.ARROW, 2))
        low_tier.add(400.0, item(Material.ARROW, 4))
        low_tier.add(350.0, item(Material.ARROW, 6))
        low_tier.add(150.0, item(Material.COBWEB))
        low_tier.add(220.0, item(Material.STICK))
        low_tier.add(120.0, item(Material.STICK, 2))
        low_tier.add(500.0, item(Material.LEATHER_HELMET))
        low_tier.add(400.0, item(Material.LEATHER_CHESTPLATE))
        low_tier.add(400.0, item(Material.LEATHER_LEGGINGS))
        low_tier.add(500.0, item(Material.LEATHER_BOOTS))
        low_tier.add(100.0, item(Material.LAVA_BUCKET))
        low_tier.add(200.0, item(Material.WATER_BUCKET))
    }

    fun setLoot(block: Block) {
        if (!block.hasMetadata("opened")) {
            val chest = block.state as Chest

            val amount = Random.nextInt(4) + 1
            chest.inventory.clear()
            for (i in 0..amount) {
                chest.inventory.setItem(Random.nextInt(27), low_tier.next())
            }
            block.setMetadata("opened", FixedMetadataValue(Knockout.INSTANCE, true))
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
        val potionMeta = potion.itemMeta as PotionMeta
        potionMeta.addCustomEffect(PotionEffect(type, duration * 20, amplifier, false, false, false), true)
        when (type) {
            PotionEffectType.POISON -> {
                potionMeta.color = org.bukkit.Color.GREEN
                potionMeta.setDisplayName("[rgb:0,150,0]Poison".tc())
            }
            PotionEffectType.HEAL -> {
                potionMeta.color = org.bukkit.Color.RED
                potionMeta.setDisplayName("[rgb:150,50,50]Heal".tc())
            }
            PotionEffectType.SPEED -> {
                potionMeta.color = org.bukkit.Color.AQUA
                potionMeta.setDisplayName("[rgb:50,150,150]Speed".tc())
            }
        }
        potion.itemMeta = potionMeta
        return potion
    }

}