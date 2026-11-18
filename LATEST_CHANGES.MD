### Additions
- Added Alchemist Cauldron modded pipe support
- Added Alchemist Cauldron thrown-item pickup
  - Items thrown in to cauldron that are acceptable ingredients will be slotted into the next free space
  - When full or when trying to throw an invalid ingredient, the item will bounce off

### Changes
- Amulet of Teleportation no longer grants Evasion from an Evasion teleport
- Improved Ice Spider Dens anti-aquifer detection

### Fixes
- Fixed Alchemist Cauldron holding on to level state for dispenser interactions
- Fixed Shield not always blocking line of sight
- Fixed spellcasting attributes on mobs being overridden by default values
- Fixed summons ally check being too permissive due to typo
- Fixed an unhandled spell config file error aborting the whole spell config load.
- Fixed spell configs changes not always being re-cached into gameplay systems
- Fixed Counterspell and unsummoning not removing the Dead King's Undead Rift
- Fixed Shadow Slash knocking enemies back far too hard
- Fixed Ascension damage hitbox location
- Fixed unhandled upgrade data serialization
- Fixed Touch Dig and Spectral Hammer cast by mobs skipping block protection checks
- Fixed Recall never working for mobs with a home position
- Fixed the recast overlay drawing partly or fully off screen with any anchor other than Top Center
- Fixed echoing arrow from not checking whether it has a target
- Fixed a crash saving chunks that hold portal frames during world generation
- Fixed Counterspell putting active summon spells on cooldown
- Fixed Black Hole fluid ticking performance
- Fixed the Inscription Table screen using the block name for its title
- Fixed the Cryomancer Tower pedestal holding an empty potion
- Fixed Raise Dead summons not counting as undead
- Fixed Necromancers rarely spawning from trial spawners
- Fixed Teleport sinking into farmland with crops

### API
- 
