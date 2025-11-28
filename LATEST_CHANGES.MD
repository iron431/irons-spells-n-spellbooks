### Additions
- Added upcoming 3.15 Content
  - Shadow Slash Spell
  - Throw Spell
  - Blaze Rods are no longer lootable in focus loot tables
  - Fixed Raise Hell spell potential ground clipping issues near the edge of blocks
  - Fixed selected spell display in Spellbook tooltip not being initialized by default
  - Fixed True Invisibility behaving inconsistently in multiplayer
  - Fixed Left-Handed skin options not being reflected in spell animations

### Changes
- Backported all JEI Alchemist Cauldron interfaces
- Backported Ice Spider patrol Spawner
- Backported Music Manager
- Reimplemented Apotheosis Compat

### Fixes
- Fixed Brazier lighting and extinguishing interactions
- Fixed Necromancers not spawning naturally

### API
- 

changes since: patreon 6

Known Api Breaks:
- Summons
  - OwnerGetter removed (Can basically be recompiled away)
  - getOwner LivingEntity -> Entity
