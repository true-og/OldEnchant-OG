# OldEnchant-OG

A fork of [zOldEnchant](https://github.com/Maxlego08/zOldEnchant). Makes the enchanting table in 1.19.4 work like it did in 1.7.10, with modern enchantments.

![Gif of enchantment table](https://img.groupez.dev/zoldenchant/enchant.gif)

## What it does

- Enchantment costs use the 1.7.10 formula (1-30 levels, based on bookshelves).
- Removing and re-placing an item rerolls the offers (costs, enchantments, and lore text all change).
- Enchantments are selected using the 1.7.10 weighted random algorithm, with a chance of getting multiple enchantments per item.
- All standard 1.19.4 table enchantments are available, including ones added after 1.7.10 (Sweeping Edge, trident, crossbow, etc.).
- Lapis is auto-filled. Players don't need to supply it.
- Removes the "Too Expensive!" cap on anvils. Renaming costs 1 level.

## Compatibility

- Server: Purpur/Paper 1.19.4
- Clients: 1.8.0 through 1.21.1+ via [ViaSuite](https://github.com/ViaVersion)

## Changes over zOldEnchant

- Full 1.7.10 enchantment algorithm (bookshelf-based costs, weighted random selection, multi-enchant).
- Updated for 1.19.4.
- Switched from Maven to Gradle.
- Automatic lapis module adapted from old OldCombatMechanics versions (MPLv2).
- Anvil fix from [BySwiizen/TooExpensiveFix](https://github.com/BySwiizen/TooExpensiveFix) (MIT).

## License

Automatic lapis module: [MPLv2](https://mozilla.org/MPL/2.0/). Everything else: MIT.
