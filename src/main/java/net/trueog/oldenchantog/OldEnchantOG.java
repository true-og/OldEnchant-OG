/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package net.trueog.oldenchantog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
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
    private static final int ENCHANTING_ITEM_SLOT = 0;
    private static final int AUTO_LAPIS_AMOUNT = 64;

    // Per-player storage for 1.7.10-style enchantment offers.
    private final Map<UUID, int[]> playerCosts = new HashMap<>();
    private final Map<UUID, List<Map<Enchantment, Integer>>> playerEnchantments = new HashMap<>();
    private final Map<UUID, Location> playerEnchantingLocations = new HashMap<>();

    @Override
    public void onEnable() {

        Bukkit.getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event) {

        final Inventory inventory = event.getInventory();

        fillUpEnchantingTable(inventory);

    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {

        final Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.ENCHANTING) {

            return;

        }

        refreshEnchantingOffersIfNeeded(event);

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

        final Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.ENCHANTING) {

            return;

        }

        refreshEnchantingOffersIfNeeded(event);

        if (event.getRawSlots().contains(ENCHANTING_LAPIS_SLOT) || isLapis(event.getOldCursor())) {

            event.setCancelled(true);

        }

        refillEnchantingTableNextTick(inventory);

    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {

        final Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.ENCHANTING) {

            ((EnchantingInventory) inventory).setSecondary(null);

            final UUID playerId = event.getPlayer().getUniqueId();
            playerCosts.remove(playerId);
            playerEnchantments.remove(playerId);
            playerEnchantingLocations.remove(playerId);

        }

    }

    @EventHandler
    public void prepareEnchant(PrepareItemEnchantEvent event) {

        final UUID playerId = event.getEnchanter().getUniqueId();
        final ItemStack item = event.getItem();
        final int bookshelves = event.getEnchantmentBonus();

        // Track the enchanting table block location for container reopening.
        playerEnchantingLocations.put(playerId, event.getEnchantBlock().getLocation());

        // Calculate 1.7.10 costs based on bookshelf count.
        final int[] costs = OldEnchantAlgorithm.calculateCosts(bookshelves);
        playerCosts.put(playerId, costs);

        // Generate enchantments for each of the 3 slots.
        final List<Map<Enchantment, Integer>> slotEnchantments = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {

            slotEnchantments.add(OldEnchantAlgorithm.selectEnchantments(costs[i], item));

        }

        playerEnchantments.put(playerId, slotEnchantments);

        // Update the enchantment offers displayed to the player.
        final EnchantmentOffer[] offers = event.getOffers();
        for (int i = 0; i < 3; i++) {

            final Map<Enchantment, Integer> enchants = slotEnchantments.get(i);
            if (enchants.isEmpty()) {

                offers[i] = null;
                continue;

            }

            // Use the first enchantment as the clue shown in the UI.
            final Map.Entry<Enchantment, Integer> clue = enchants.entrySet().iterator().next();

            if (offers[i] != null) {

                offers[i].setEnchantment(clue.getKey());
                offers[i].setEnchantmentLevel(clue.getValue());
                offers[i].setCost(costs[i]);

            } else {

                offers[i] = new EnchantmentOffer(clue.getKey(), clue.getValue(), costs[i]);

            }

        }

    }

    @EventHandler
    public void enchantItem(EnchantItemEvent event) {

        final UUID playerId = event.getEnchanter().getUniqueId();
        final int slot = event.whichButton();

        // Apply 1.7.10 XP level cost (player pays the full displayed cost).
        final int[] costs = playerCosts.get(playerId);
        if (costs != null && slot < costs.length) {

            event.setExpLevelCost(costs[slot]);

        }

        // Apply the pre-generated 1.7.10 enchantments.
        final List<Map<Enchantment, Integer>> slotEnchantments = playerEnchantments.get(playerId);
        if (slotEnchantments != null && slot < slotEnchantments.size()) {

            final Map<Enchantment, Integer> enchants = slotEnchantments.get(slot);
            if (!enchants.isEmpty()) {

                event.getEnchantsToAdd().clear();
                event.getEnchantsToAdd().putAll(enchants);

            }

        }

        ((EnchantingInventory) event.getInventory()).setSecondary(getLapis());

    }

    private void fillUpEnchantingTable(Inventory inventory) {

        if (inventory == null || inventory.getType() != InventoryType.ENCHANTING) {

            return;

        }

        final EnchantingInventory enchantingInventory = (EnchantingInventory) inventory;
        if (!isLapis(enchantingInventory.getSecondary())
                || enchantingInventory.getSecondary().getAmount() != AUTO_LAPIS_AMOUNT)
        {

            enchantingInventory.setSecondary(getLapis());

        }

    }

    private void refillEnchantingTableNextTick(Inventory inventory) {

        Bukkit.getScheduler().runTask(this, () -> fillUpEnchantingTable(inventory));

    }

    private void refreshEnchantingOffersIfNeeded(InventoryClickEvent event) {

        if (event.getRawSlot() == ENCHANTING_ITEM_SLOT) {

            refreshEnchantingOffersNextTick(event.getWhoClicked());

            return;

        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && isEnchantable(event.getCurrentItem())) {

            refreshEnchantingOffersNextTick(event.getWhoClicked());

        }

    }

    private void refreshEnchantingOffersIfNeeded(InventoryDragEvent event) {

        if (event.getRawSlots().contains(ENCHANTING_ITEM_SLOT)) {

            refreshEnchantingOffersNextTick(event.getWhoClicked());

        }

    }

    private void refreshEnchantingOffersNextTick(HumanEntity humanEntity) {

        Bukkit.getScheduler().runTask(this, () -> {

            final Inventory inventory = humanEntity.getOpenInventory().getTopInventory();
            if (inventory.getType() != InventoryType.ENCHANTING) {

                return;

            }

            final EnchantingInventory enchantingInventory = (EnchantingInventory) inventory;
            final ItemStack item = enchantingInventory.getItem(ENCHANTING_ITEM_SLOT);
            if (item == null || item.getType() == Material.AIR) {

                return;

            }

            final UUID playerId = humanEntity.getUniqueId();
            final Location tableLocation = playerEnchantingLocations.get(playerId);
            if (tableLocation == null) {

                return;

            }

            // Save the item, then clear both slots before closing to prevent
            // the server from returning them to the player's inventory.
            final ItemStack savedItem = item.clone();
            enchantingInventory.setItem(ENCHANTING_ITEM_SLOT, null);
            enchantingInventory.setSecondary(null);

            // Set a new enchantment seed. The new EnchantmentMenu created by
            // openEnchanting reads this seed into its DataSlot, so the client
            // will render fresh galactic text.
            humanEntity.setEnchantmentSeed(ThreadLocalRandom.current().nextInt());

            // Reopen the enchanting table (internally closes the old container).
            humanEntity.openEnchanting(tableLocation, true);

            // Restore lapis and the item in the new container.
            // setItem triggers PrepareItemEnchantEvent which regenerates our
            // custom 1.7.10 offers.
            final Inventory newInventory = humanEntity.getOpenInventory().getTopInventory();
            if (newInventory.getType() == InventoryType.ENCHANTING) {

                fillUpEnchantingTable(newInventory);
                ((EnchantingInventory) newInventory).setItem(ENCHANTING_ITEM_SLOT, savedItem);

            } else {

                // Reopen failed; give the item back.
                humanEntity.getInventory().addItem(savedItem);

            }

        });

    }

    private ItemStack getLapis() {

        return new ItemStack(Material.LAPIS_LAZULI, AUTO_LAPIS_AMOUNT);

    }

    private boolean isLapis(ItemStack item) {

        return item != null && item.getType() == Material.LAPIS_LAZULI;

    }

    private boolean isEnchantable(ItemStack item) {

        if (item == null || item.getType() == Material.AIR) {

            return false;

        }

        for (org.bukkit.enchantments.Enchantment enchantment : org.bukkit.enchantments.Enchantment.values()) {

            if (enchantment.canEnchantItem(item)) {

                return true;

            }

        }

        return false;

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

        final boolean condition = event.getInventory().getItem(2) != null
                && !event.getInventory().getItem(0).getDisplayName()
                        .equals(event.getInventory().getItem(2).getDisplayName())
                && event.getInventory().getItem(1) == null;
        if (condition) {

            event.getInventory().setMaximumRepairCost(1);

        }

    }

}
