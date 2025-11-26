### Additions
- Added Shadow Slash spell
- Added Throw spell
- Added Portuguese-Brazil localization, thanks to PrincessStellar

### Changes
- Duplicate spell options (primarily from imbued items) are now merged into one spell slot on the spell wheel, taking the highest level.
- Blaze Rods are no longer lootable in focus loot tables
- Reduced quality of loot in `generic_magic_treasure` loot table (commonly found in secret locations)
  - It is important for this type of loot to be rewarding, but too often it served as an early game skip, especially when veteran players know all the secret locations
  - Arcane Essence drop count reduced [5,16]->[3,13]
  - Spell level quality reduced [20%,75%]->[0%,60%]
  - Focus rolls reduced [1,3]->[1,2], and quantity decreased [1,4]->[1,2]
  - Ink roll quality reduced, increase roll count [1,3]->[1,4]
  - Curio drop chance reduced 30%->20%
  - Material reward rolls reduced 8->6
    - Diamond weight reduced 3->1
    - Raw Gold replaced with Copper Ingots
  - Equipment Drops nerfed
    - Iron equipment is now twice as likely to roll as Diamond
    - Imbued Spell level quality reduced [20%,75%]->[0%,50%]
    - Enchanted level reduced [10,39]->[1,15]
    - Can now spawn damaged (With [30%,100%] durability)

### Fixes
- Fixed Raise Hell spell potential ground clipping issues near the edge of blocks 
- Fixed selected spell display in Spellbook tooltip not being initialized by default 
- Fixed API break with `Utils#shouldHealEntity`
- Fixed True Invisibility behaving inconsistently in multiplayer
- Fixed Left-Handed skin options not being reflected in spell animations 
- Fixed the World Border from interfering with Pocket Dimension dimension

### API
- Added `lootable_focus` item tag, which is now used in the `magic_items/all_focuses` loot table instead of hardcoded loot drops