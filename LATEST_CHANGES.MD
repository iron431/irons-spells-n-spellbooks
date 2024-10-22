### Additions
- Added Item Component for Casting Implement Functionality
  - irons_spellbooks:casting_implement
- Added Item Component for Multihand Weapon Functionality
  - irons_spellbooks:multihand_weapon
- Added Config to disable Scroll Upgrading, courtesy of Cephelo
- Added Mana Regeneration Multiplier Config
- Added Casting Movement Speed Attribute
  - Affects the user's movement speed while casting
  - Currently Unused
- Added Apothic Attributes Formatting Compatibility
- Expanded Lectern Functionality
  - Spellbooks can now be placed on lecterns
  - "Lore Items" can now be placed on lecterns
- Added Archevoker Logbook item
  - Has translated and untranslated variants
  - Spawns in Evoker Fort tower lectern
  - Replaces Written Book in Villager Bible Questline
### Changes
- Updated Languages
  - Russian, thanks to Tefny and Quark
  - Added Vietnamese support, thanks to Le P. Thanh Sang
- Root no longer affects any boss (instead of just Ender Dragon)
- Black hole is now less effective against bosses, as well as creatures with high knockback resistance

### Fixes
- Void Tentacles are no longer affected by Black Hole
- Fixed Pedestal being unable to sync empty item stacks
- Fixed Staffs always showing imbued item's cooldown reduction
- Fixed Energy Swirl effect incorrectly calculating model transforms
- Fixed Aoe Entities being affected by explosion knockback
- Fixed Inscription Table Exploit

### API
- Added Events, courtesy of clement
  - CounterSpellEvent
  - SpellSummonEvent
- Multihand and Casting Implement functionality is now component based. Nothing needs to be done if you were not manually implementing these

