### Additions
- Added crouch display to Alchemist Cauldron contents

### Changes
- Affinity Data can now specify a bonus greater than 1
- Arcane Anvil now returns Upgrade Orbs when using a Shriving Stone
- Reworked Alchemist Cauldron

### Fixes
- 
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

