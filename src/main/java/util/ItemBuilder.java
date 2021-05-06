package util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Simple {@link ItemStack} builder
 *
 * @author MrMicky
 */
public class ItemBuilder {

    private final ItemMeta meta;
    private ItemStack item;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this.item = Objects.requireNonNull(item, "item");
        this.meta = item.getItemMeta();

        if (meta == null) {
            throw new IllegalArgumentException("The type " + item.getType() + " don't support item meta");
        }
    }

    public ItemBuilder type(Material material) {
        item.setType(material);
        return this;
    }

    public ItemBuilder data(int data) {
        return durability((short) data);
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder durability(short durability) {
        item.setDurability(durability);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        meta.removeEnchant(enchantment);
        return this;
    }

    public ItemBuilder damage(int amount) {
        if (item instanceof Damageable) {
            Damageable dmg = (Damageable) item;
            dmg.setDamage(amount);
            item = (ItemStack) dmg;
        }
        return this;
    }

    public ItemBuilder removeEnchants() {
        meta.getEnchants().keySet().forEach(meta::removeEnchant);
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        metaConsumer.accept(meta);
        return this;
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        if (metaClass.isInstance(meta)) {
            metaConsumer.accept(metaClass.cast(meta));
        }
        return this;
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemBuilder lore(String lore) {
        return lore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', lore)));
    }

    public ItemBuilder clearLore() {
        meta.setLore(new ArrayList<String>());
        return this;
    }

    public ItemBuilder removeLastLore() {
        List<String> m = meta.getLore();
        assert m != null;
        m.remove(m.size() - 1);
        meta.setLore(m);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : lore) {
            list.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return lore(list);
    }

    public ItemBuilder lore(List<String> lore) {
        ArrayList<String> lines = new ArrayList<>();
        for (String s : lore) {
            lines.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        meta.setLore(lines);
        return this;
    }

    public ItemBuilder addLore(String line) {
        line = ChatColor.translateAlternateColorCodes('&', line);
        List<String> lore = meta.getLore();

        if (lore == null) {
            return lore(line);
        }

        lore.add(line);
        return lore(lore);
    }

    public ItemBuilder addLore(String... lines) {
        ArrayList<String> line = new ArrayList<>();
        for (String s : lines) {
            line.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return addLore(line);
    }

    public ItemBuilder addLore(List<String> lines) {
        List<String> lore = meta.getLore();
        if (lore == null) {
            return lore(lines);
        }
        ArrayList<String> line = new ArrayList<>();
        for (String s : lines) {
            line.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        lore.addAll(line);
        return lore(lore);
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder flags() {
        return flags(ItemFlag.values());
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        meta.removeItemFlags(flags);
        return this;
    }

    public ItemBuilder removeFlags() {
        return removeFlags(ItemFlag.values());
    }

    public ItemBuilder armorColor(Color color) {
        return meta(LeatherArmorMeta.class, armorMeta -> armorMeta.setColor(color));
    }

    public ItemBuilder setUnbreakable(Boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
