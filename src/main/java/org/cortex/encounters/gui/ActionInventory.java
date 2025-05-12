package org.cortex.encounters.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.cortex.core.util.ItemBuilder;
import org.cortex.rpchat.item.CustomItem;

public class ActionInventory implements InventoryHolder {

    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, 9, "Choose your defensive action");

        inventory.setItem(0, new ItemBuilder(new ItemStack(Material.FEATHER)).withName(ChatColor.GRAY + "Dodge")
                .withLore("Rolls the flexibility specialty skill to determine success",
                        "Rolling more than the attacker will result in a successfully dodge and you will take 0 damage")
                .getItemStack());
        inventory.setItem(1, new ItemBuilder(new ItemStack(Material.SHIELD)).withName(ChatColor.GRAY + "Shield-up")
                .withLore("Requires a shield in your off-hand!", "Raises your shield to block the attack", "Roll is based on its action difficulty")
                .hideAttributes()
                .getItemStack());
        inventory.setItem(2, new ItemBuilder(new ItemStack(Material.IRON_SWORD)).withName(ChatColor.GRAY + "Brace")
                .withLore("Do nothing and take the impact", "Benefits from base defense points and defensive stance")
                .hideAttributes()
                .getItemStack());

        return inventory;
    }
}
