### Additions
- Added Iron's Patreon Lib integration
- Added Tyros Statue, a new decoration multiblock
- Added boss configs for the Dead King (mirroring Tyro's configs)
  - Added `additionalHealth` config
  - Added `additionalAttackDamage` config
  - Added `additionalSpellPower` config
- Added Per-Player loot to the Dead King
  - Drops new Bone Key item for each player
- Added Soul Burn debuff
  - Decreases incoming healing by 5% per level
- Added Ominous Boss Fight to Tyros
  - Has higher base stats
  - Starts in Soul Mode
  - Inflicts stacks of Soul Burn on melee hits
  - Unlocks new melee attack combo
  - Unlocks new dagger throw ability
  - Unlocks higher max Knight summon count
  - Will throw Fire Orbs into the arena, which must be destroyed before fully charging. Fully charged orbs cause a devastating explosion
- Added Ominous Boss Fight to the Dead King
  - Killing summons has a chance to trigger a Sacrifice explosion
  - Now creates Undead Rifts intermittently, spewing a large amount of summons onto the battlefield

### Changes
- Improve Dead King movement controllers when flying, walking, and attacking
- Reworked Dead King's player scaling to mirror Tyro's
- Telekinesis has unique animation
- Reworked Dead King Loot
  - todo
- Changed Catacombs
  - todo
  - (also there's new pieces)

### Fixes
- Fixed Continuous Cast interval shortness
- 1.20.1: Fixed an issue where armorstands in spawn chunks wearing upgraded armor would lookup unloaded upgrade types
- Fixed entity summon tracking on world relog
- Fixed ranged spell attacks without a projectile from being marked as direct damage
- Fixed Raise Dead spell from not checking collision when creating entities
- Tyros and Keepers will not be able to aggro the Cursed Armor Stands

### API
- Created `RaycastBuilder`, a helper to more easily configure raycasting and raycast settings, opposed to all the helpers in `Utils` (which are now deprecated, and simply return a `RaycastBuilder` result)
- Now depends on Iron's Patreon Lib
- Added `IOminousEntity`
  - Entities implement this interface automatically turn ominous (`onOminousTrigger()`) when first joining a level near a player with Bad Omen/Trial Omen
- Added `SummonManager#saveSummonerData` helper for serializing any entity's summon data to world storage
- `SummonManager#handlePlayerDisconnect` is now deprecated
  - Use aforementioned `SummonManager#saveSummonerData` instead
- Added `SpellDamageSource#indirect()`, which marks a spell's damage as indirect, whether there is a projectile or not
- Added `irons_spellbooks:clear_vault_data` Structure Processor to help ensure vaults used in structures are of a clean slate
