package net.trueog.oldenchantog;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @authors NotAlexNoyle, Maxlego08
 * Website: https://true-og.net/
 *
 * The plugin allows to have the enchantments of the 1.7.10 in the recent versions of minecraft.
 */
public class OldEnchantOG extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {

		Bukkit.getPluginManager().registerEvents(this, this);

	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {

		Inventory inventory = event.getInventory();
		if (inventory.getType().equals(InventoryType.ENCHANTING) && event.getRawSlot() == 1) {

			event.setCancelled(true);

		}

	}

	@EventHandler
	public void inventoryClose(InventoryCloseEvent event) {

		Inventory inventory = event.getInventory();
		if (inventory.getType().equals(InventoryType.ENCHANTING)) {

			inventory.setItem(1, null);

		}

	}

	@EventHandler
	public void enchantItem(EnchantItemEvent event) {

		int newLevel = event.getEnchanter().getLevel() - event.getExpLevelCost() + (event.whichButton() + 1);
		newLevel = newLevel < 1 ? 1 : newLevel;

		event.getEnchanter().setLevel(newLevel);
		event.setExpLevelCost(1);

	}

	/*MIT License

	Copyright (c) 2024 BySwiizen

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE. */
	// Prevent "too expensive" message in anvils. From BySwiizen/TooExpensiveFix.
	@EventHandler
	public void onAnvil(PrepareAnvilEvent event) {

		event.getInventory().setMaximumRepairCost(21862);
		if (event.getInventory().getItem(2) != null) {

			if (event.getInventory().getItem(0).getDisplayName() != event.getInventory().getItem(2).getDisplayName() && event.getInventory().getItem(1) == null) {

				event.getInventory().setMaximumRepairCost(1);

			}

		}

	}

}