/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package net.trueog.oldenchantog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

// Re-implement 1.17.10 enchanting algoritm with modern enchantments.
// Vibe-coded by Claude Opus 4.6.
@SuppressWarnings("deprecation")
public final class OldEnchantAlgorithm {

    private OldEnchantAlgorithm() {

    }

    private static final int MAX_ENCHANT_COST = 30;
    private static final int MAX_BOOKSHELVES = 15;

    private static final class EnchantData {

        final Enchantment enchantment;
        final int weight;
        final int maxLevel;
        final IntUnaryOperator minPower;
        final IntUnaryOperator maxPower;

        EnchantData(Enchantment enchantment, int weight, int maxLevel, IntUnaryOperator minPower,
                IntUnaryOperator maxPower)
        {

            this.enchantment = enchantment;
            this.weight = weight;
            this.maxLevel = maxLevel;
            this.minPower = minPower;
            this.maxPower = maxPower;

        }

    }

    private static final class EnchantCandidate {

        final Enchantment enchantment;
        final int level;
        final int weight;

        EnchantCandidate(Enchantment enchantment, int level, int weight) {

            this.enchantment = enchantment;
            this.level = level;
            this.weight = weight;

        }

    }

    // Enchantments obtainable from a standard enchanting table, excluding those
    // irrelevant to 1.8 PvP (e.g. Sweeping Edge, which requires 1.9+ sweep
    // attacks).
    // Weight, maxLevel, minPower, and maxPower match vanilla values.
    // Newer enchantments (trident, crossbow) use values consistent with the
    // original system's design patterns.
    private static final List<EnchantData> ENCHANT_DATA = List.of(
            // Armor - Protection
            new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 10, 4, l -> 1 + (l - 1) * 11, l -> 12 + (l - 1) * 11),
            new EnchantData(Enchantment.PROTECTION_FIRE, 5, 4, l -> 10 + (l - 1) * 8, l -> 18 + (l - 1) * 8),
            new EnchantData(Enchantment.PROTECTION_FALL, 5, 4, l -> 5 + (l - 1) * 6, l -> 11 + (l - 1) * 6),
            new EnchantData(Enchantment.PROTECTION_EXPLOSIONS, 2, 4, l -> 5 + (l - 1) * 8, l -> 13 + (l - 1) * 8),
            new EnchantData(Enchantment.PROTECTION_PROJECTILE, 5, 4, l -> 3 + (l - 1) * 6, l -> 9 + (l - 1) * 6),
            // Armor - Other
            new EnchantData(Enchantment.THORNS, 1, 3, l -> 10 + (l - 1) * 20, l -> 60 + (l - 1) * 20),
            new EnchantData(Enchantment.OXYGEN, 2, 3, l -> 10 + (l - 1) * 10, l -> 40 + (l - 1) * 10),
            new EnchantData(Enchantment.WATER_WORKER, 2, 1, l -> 1, l -> 41),
            new EnchantData(Enchantment.DEPTH_STRIDER, 2, 3, l -> l * 10, l -> l * 10 + 15),
            // Weapon - Damage
            new EnchantData(Enchantment.DAMAGE_ALL, 10, 5, l -> 1 + (l - 1) * 11, l -> 21 + (l - 1) * 11),
            new EnchantData(Enchantment.DAMAGE_UNDEAD, 5, 5, l -> 5 + (l - 1) * 8, l -> 25 + (l - 1) * 8),
            new EnchantData(Enchantment.DAMAGE_ARTHROPODS, 5, 5, l -> 5 + (l - 1) * 8, l -> 25 + (l - 1) * 8),
            // Weapon - Utility
            new EnchantData(Enchantment.KNOCKBACK, 5, 2, l -> 5 + (l - 1) * 20, l -> 55 + (l - 1) * 20),
            new EnchantData(Enchantment.FIRE_ASPECT, 2, 2, l -> 10 + (l - 1) * 20, l -> 60 + (l - 1) * 20),
            new EnchantData(Enchantment.LOOT_BONUS_MOBS, 2, 3, l -> 15 + (l - 1) * 9, l -> 65 + (l - 1) * 9),
            // Tool
            new EnchantData(Enchantment.DIG_SPEED, 10, 5, l -> 1 + (l - 1) * 10, l -> 51 + (l - 1) * 10),
            new EnchantData(Enchantment.SILK_TOUCH, 1, 1, l -> 15, l -> 65),
            new EnchantData(Enchantment.DURABILITY, 5, 3, l -> 5 + (l - 1) * 8, l -> 55 + (l - 1) * 8),
            new EnchantData(Enchantment.LOOT_BONUS_BLOCKS, 2, 3, l -> 15 + (l - 1) * 9, l -> 65 + (l - 1) * 9),
            // Bow
            new EnchantData(Enchantment.ARROW_DAMAGE, 10, 5, l -> 1 + (l - 1) * 10, l -> 16 + (l - 1) * 10),
            new EnchantData(Enchantment.ARROW_KNOCKBACK, 2, 2, l -> 12 + (l - 1) * 20, l -> 37 + (l - 1) * 20),
            new EnchantData(Enchantment.ARROW_FIRE, 2, 1, l -> 20, l -> 50),
            new EnchantData(Enchantment.ARROW_INFINITE, 1, 1, l -> 20, l -> 50),
            // Fishing Rod
            new EnchantData(Enchantment.LUCK, 2, 3, l -> 15 + (l - 1) * 9, l -> 65 + (l - 1) * 9),
            new EnchantData(Enchantment.LURE, 2, 3, l -> 15 + (l - 1) * 9, l -> 65 + (l - 1) * 9),
            // Trident
            new EnchantData(Enchantment.LOYALTY, 5, 3, l -> 5 + (l - 1) * 7, l -> 50),
            new EnchantData(Enchantment.IMPALING, 2, 5, l -> 1 + (l - 1) * 8, l -> 21 + (l - 1) * 8),
            new EnchantData(Enchantment.RIPTIDE, 2, 3, l -> 10 + (l - 1) * 7, l -> 25 + (l - 1) * 7),
            new EnchantData(Enchantment.CHANNELING, 1, 1, l -> 25, l -> 50),
            // Crossbow
            new EnchantData(Enchantment.MULTISHOT, 2, 1, l -> 20, l -> 50),
            new EnchantData(Enchantment.QUICK_CHARGE, 5, 3, l -> 12 + (l - 1) * 20, l -> 50),
            new EnchantData(Enchantment.PIERCING, 10, 4, l -> 1 + (l - 1) * 10, l -> 50));

    // Returns the enchantability value for a given material.
    static int getEnchantability(Material material) {

        final String name = material.name();

        if (name.startsWith("LEATHER_")) {

            return 15;

        }

        if (name.startsWith("CHAINMAIL_")) {

            return 12;

        }

        if ("TURTLE_HELMET".equals(name)) {

            return 9;

        }

        final boolean isArmor = name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS")
                || name.contains("BOOTS");

        if (name.startsWith("IRON_")) {

            return isArmor ? 9 : 14;

        }

        if (name.startsWith("GOLDEN_")) {

            return isArmor ? 25 : 22;

        }

        if (name.startsWith("DIAMOND_")) {

            return 10;

        }

        if (name.startsWith("NETHERITE_")) {

            return 15;

        }

        if (name.startsWith("WOODEN_")) {

            return 15;

        }

        if (name.startsWith("STONE_")) {

            return 5;

        }

        // Book, Bow, Crossbow, Fishing Rod, Trident, and fallback.
        return 1;

    }

    // Calculates three enchantment slot costs using the 1.7.10 formula. Costs scale
    // with bookshelf count (0-15) and range from 1 to 30.
    static int[] calculateCosts(int bookshelves) {

        final Random random = ThreadLocalRandom.current();
        final int b = Math.min(bookshelves, MAX_BOOKSHELVES);
        final int base = random.nextInt(8) + 1 + (b >> 1) + random.nextInt(b + 1);

        final int[] costs = new int[3];
        costs[0] = Math.max(base / 3, 1);
        costs[1] = (base * 2) / 3 + 1;
        costs[2] = Math.max(base, b * 2);

        for (int i = 0; i < 3; i++) {

            costs[i] = Math.min(costs[i], MAX_ENCHANT_COST);

        }

        return costs;

    }

    // Selects enchantments using the 1.7.10 algorithm for a given cost and item.
    static Map<Enchantment, Integer> selectEnchantments(int cost, ItemStack item) {

        if (cost <= 0 || item == null) {

            return Collections.emptyMap();

        }

        final Random random = ThreadLocalRandom.current();
        final int enchantability = getEnchantability(item.getType());
        final boolean isBook = item.getType() == Material.BOOK;

        // Calculate modified enchantment level.
        int modifiedLevel = cost + 1 + random.nextInt((enchantability >> 2) + 1)
                + random.nextInt((enchantability >> 2) + 1);

        // Apply random variation of +/- 15%.
        final float variation = (random.nextFloat() + random.nextFloat() - 1.0f) * 0.15f;
        modifiedLevel = Math.round(modifiedLevel * (1.0f + variation));
        modifiedLevel = Math.max(modifiedLevel, 1);

        // Build candidate list from all applicable enchantments.
        final List<EnchantCandidate> candidates = buildCandidates(modifiedLevel, item, isBook);
        if (candidates.isEmpty()) {

            return Collections.emptyMap();

        }

        // Pick the first enchantment by weighted random.
        final Map<Enchantment, Integer> result = new LinkedHashMap<>();
        EnchantCandidate picked = weightedRandom(candidates, random);
        result.put(picked.enchantment, picked.level);
        candidates.remove(picked);

        // Attempt to add more enchantments with decreasing probability.
        modifiedLevel /= 2;
        while (!candidates.isEmpty() && random.nextInt(50) <= modifiedLevel) {

            removeConflicting(candidates, result.keySet());
            if (candidates.isEmpty()) {

                break;

            }

            picked = weightedRandom(candidates, random);
            result.put(picked.enchantment, picked.level);
            candidates.remove(picked);

            modifiedLevel /= 2;

        }

        return result;

    }

    private static List<EnchantCandidate> buildCandidates(int modifiedLevel, ItemStack item, boolean isBook) {

        final List<EnchantCandidate> candidates = new ArrayList<>();

        for (EnchantData data : ENCHANT_DATA) {

            // Books accept all non-treasure enchantments in enchanting tables.
            if (!isBook && !data.enchantment.canEnchantItem(item)) {

                continue;

            }

            // Find the highest enchantment level whose power range contains the
            // modified level.
            for (int lvl = data.maxLevel; lvl >= 1; lvl--) {

                final int min = data.minPower.applyAsInt(lvl);
                final int max = data.maxPower.applyAsInt(lvl);
                if (modifiedLevel >= min && modifiedLevel <= max) {

                    candidates.add(new EnchantCandidate(data.enchantment, lvl, data.weight));
                    break;

                }

            }

        }

        return candidates;

    }

    private static void removeConflicting(List<EnchantCandidate> candidates, Set<Enchantment> existing) {

        candidates.removeIf(c -> {

            for (Enchantment e : existing) {

                if (c.enchantment.equals(e) || c.enchantment.conflictsWith(e) || e.conflictsWith(c.enchantment)) {

                    return true;

                }

            }

            return false;

        });

    }

    private static EnchantCandidate weightedRandom(List<EnchantCandidate> candidates, Random random) {

        int totalWeight = 0;
        for (EnchantCandidate c : candidates) {

            totalWeight += c.weight;

        }

        int roll = random.nextInt(totalWeight);
        for (EnchantCandidate c : candidates) {

            roll -= c.weight;
            if (roll < 0) {

                return c;

            }

        }

        // Fallback (should not be reached).
        return candidates.get(candidates.size() - 1);

    }

}
