/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package net.trueog.oldenchantog;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @authors NotAlexNoyle, Maxlego08 Website: https://true-og.net/
 *
 *          The plugin allows to have the enchantments of the 1.7.10 in the
 *          recent versions of minecraft.
 */
public class OldEnchantOG extends JavaPlugin implements Listener {

    private static final int ENCHANTING_LAPIS_SLOT = 1;
    private static final int AUTO_LAPIS_AMOUNT = 64;

    @Override
    public void onEnable() {

        Bukkit.getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event) {

        fillUpEnchantingTable(event.getInventory());

    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {

        Inventory inventory = event.getInventory();
        if (!inventory.getType().equals(InventoryType.ENCHANTING)) {

            return;

        }

        if (event.getRawSlot() == ENCHANTING_LAPIS_SLOT) {

            event.setCancelled(true);

        } else if (event.getClick() == ClickType.DOUBLE_CLICK && isLapis(event.getCursor())) {

            event.setCancelled(true);

        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && isLapis(event.getCurrentItem())) {

            event.setCancelled(true);

        }

        refillEnchantingTableNextTick(inventory);

    }

    @EventHandler
    public void inventoryDrag(InventoryDragEvent event) {

        Inventory inventory = event.getInventory();
        if (!inventory.getType().equals(InventoryType.ENCHANTING)) {

            return;

        }

        if (event.getRawSlots().contains(ENCHANTING_LAPIS_SLOT) || isLapis(event.getOldCursor())) {

            event.setCancelled(true);

        }

        refillEnchantingTableNextTick(inventory);

    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {

        Inventory inventory = event.getInventory();
        if (inventory.getType().equals(InventoryType.ENCHANTING)) {

            ((EnchantingInventory) inventory).setSecondary(null);

        }

    }

    @EventHandler
    public void enchantItem(EnchantItemEvent event) {

        int newLevel = event.getEnchanter().getLevel() - event.getExpLevelCost() + (event.whichButton() + 1);
        newLevel = newLevel < 1 ? 1 : newLevel;

        event.getEnchanter().setLevel(newLevel);
        event.setExpLevelCost(1);
        ((EnchantingInventory) event.getInventory()).setSecondary(getLapis());

    }

    private void fillUpEnchantingTable(Inventory inventory) {

        if (inventory == null || inventory.getType() != InventoryType.ENCHANTING) {

            return;

        }

        EnchantingInventory enchantingInventory = (EnchantingInventory) inventory;
        if (!isLapis(enchantingInventory.getSecondary())
                || enchantingInventory.getSecondary().getAmount() != AUTO_LAPIS_AMOUNT)
        {

            enchantingInventory.setSecondary(getLapis());

        }

    }

    private void refillEnchantingTableNextTick(Inventory inventory) {

        Bukkit.getScheduler().runTask(this, () -> fillUpEnchantingTable(inventory));

    }

    private ItemStack getLapis() {

        return new ItemStack(Material.LAPIS_LAZULI, AUTO_LAPIS_AMOUNT);

    }

    private boolean isLapis(ItemStack item) {

        return item != null && item.getType() == Material.LAPIS_LAZULI;

    }

    /*
     * MIT License
     * 
     * Copyright (c) 2024 BySwiizen
     * 
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     * 
     * The above copyright notice and this permission notice shall be included in
     * all copies or substantial portions of the Software.
     * 
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     * SOFTWARE.
     */
    // Prevent "too expensive" message in anvils. From BySwiizen/TooExpensiveFix.
    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {

        event.getInventory().setMaximumRepairCost(21862);
        if (event.getInventory().getItem(2) != null) {

            if (event.getInventory().getItem(0).getDisplayName() != event.getInventory().getItem(2).getDisplayName()
                    && event.getInventory().getItem(1) == null)
            {

                event.getInventory().setMaximumRepairCost(1);

            }

        }

    }

}
