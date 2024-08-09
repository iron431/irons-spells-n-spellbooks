### Additions
- Added crouch display to Alchemist Cauldron contents
- Added specific Stronghold/End City loot tables for magic items
- Added Loot to Trial Chambers
  - Pots
    - Chance to drop Arcane Essence, or rarely drop a Blank Rune or Scroll 
  - Chests
    - Arcane Essence, Cloth, Scrolls, Blank Runes, Curios, Ink, and Elixirs
  - Spawners
    - Mana Potions
  - Ominous Spawners
    - High Quality Mana Potions, and Elixirs
  - Vaults
    - Scrolls, Ink, Curios, Arcane Essence, and Blank Runes
  - Ominous Vaults
    - Magic Equipment, High Tier Resources, Empty Upgrade Orbs, High Quality Scrolls/Ink, and Arcane Essence
- Added Mithril
  - Mithril Ingot
  - Mithril Scrap
  - Raw Mithril
  - Mithril Ore
  - Deepslate Mithril Ore
- Added Weapon Parts, crafted from Mithril and used for crafting magic weapons
- Added Spellbreaker, an imbued sword
- Added Amethyst Rapier, an imbued sword
- Added Autoloader Crossbow, a unique Trial Chambers reward
- Added Ring of Expulsion, a unique Trial Chambers reward
- Added Ring of Visibility, a unique Trial Chambers reward

### Changes
- Affinity Data can now specify a bonus greater than 1
- Arcane Anvil now returns Upgrade Orbs when using a Shriving Stone
- Reworked Alchemist Cauldron
  - Water Level and Liquid Contents are no longer separate values. This should make interactions much more intuitive
- Loot-Only curios can now be recycled
  - Can be Smelted into Mithril Scrap, and crafted from Mithril Scrap and a designated item
- Affinity Rings can now be attuned to specific spells by combining a ring with a scroll of any level in the Arcane Anvil
- Pyromancer Chestplate now has a cape addition
- Casting Mobs now display capes from armor with capes

### Fixes
- Fixed Firely Jar not having a loot table
- Fixed True Invisibility not affecting the aggro of mobs that use Brain for targeting
- Fixed Inscription Table ghost block/dupe when exploded
- Fixed Abyssal Shroud desintation logic
- Fixed Inscription Table bug/exploit relating to death

### API
- Removed irsonspellbooks from java group id
  - io.redspace.ironsspellbooks.irons_spellbooks => io.redspace.irons_spellbooks
- Removed Deprecated Content
  - CastType#holdToCast
  - AbstractSpell#needsLearning (replaced by AbstractSpell#requiresLearning)
  - AbstractEldritchSpell class
- Added Events
  - AlchemistCauldronBuildInteractionsEvent

### 1.21 Snapshot Changes
(Snapshot 3 -> 4)
- port builtin resource packs
- port enchantments affecting melee spells. this still does not show up in tooltips due to minecraft bug
- port burning dash visual effects for casting mobs
- port loot modifiers
- port arcane debris requiring netherite tools
- fixed arcane anvil recipe
- fixed inscription table block drop when exploded
- fixed summoned vex follow goal
- fixed various heartstop issues
- fixed various true invisibility issues
- fixed apothecarist throwing only water potions
- fixed apothecarist being able to damage himself with his own harming potions
- fixed magic effects not clearing their abilities on player death
- adjusted alchemist cauldron crouch display
- alchemist cauldron no longer requires at least 1 level of fluid before being able to cook mobs for blood vials

