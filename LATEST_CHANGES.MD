### Additions
- Added Shadow Slash spell
- Added Throw spell
- Added Portuguese-Brazil localization, thanks to PrincessStellar

### Changes
- Blaze Rods are no longer lootable in focus loot tables

### Fixes
- Fixed Raise Hell spell potential ground clipping issues near the edge of blocks 
- Fixed selected spell display in Spellbook tooltip not being initialized by default 
- Fixed API break with `Utils#shouldHealEntity`
- Fixed True Invisibility behaving inconsistently in multiplayer
- Fixed Left-Handed skin options not being reflected in spell animations 

### API
- Added `lootable_focus` item tag, which is now used in the `magic_items/all_focuses` loot table instead of hardcoded loot drops