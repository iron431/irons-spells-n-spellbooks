### Additions
- Added Fire Artifact: the Infernal Sorcerer's Chestplate
  - Has normal Artifact attributes
  - Has passive ability: Immolation
- Added the Twilight Gale, a crystalline spear enthralled with lightning magic
  - Imbued with Volt Strike 5
  - Can be thrown with enhanced trident-like mechanics
    - Item is not consumed to throw, but goes on 10s cooldown
    - Picking up thrown item immediately resets cooldown
    - Throw damage scales with attack damage and enchantments
    - Can be enchanted as a Sword
    - Can be enchanted with Loyalty
    - Can be enchanted with Channeling, which empowers all throws with lightning, making thrown damage scale off of Lightning Spell Power
- Added Config for disabling Ice Spider Patrols

### Changes
- Adjusted new School Armor Smithing Recipes to avoid recipe viewer conflict
  - Recipes now require Arcane Essence as an additional material, because the missing slot bugged out non-JEI recipe viewers
  - Added a crafting recipe variant to make the change more intuitive (although Smithing Table is still required to transfer enchantments/imbues/others)
  - May be revisited in the future
- Rebalanced Ice Spider Patrol spawnrate
  - Delay range increased from 4-5 minutes to 8-11 minutes
  - Player count less significantly reduces delay
  - Spawn chance increased from 25% to 50%, reducing inconsistency in spawnrates
  - Added config for disabling partrols
  - Fixed events not firing, preventing certain compatibilities between mods
- Improved Spell Wheel text display
  - Now displays cooldown, if the cooldown is greater than zero
  - Mana cost line is no longer displayed if the mana cost is zero
  - Fixed visual bug with line separator
- Pocket Dimension now prevents all types of teleportation
- Overhauled Ice Spike visuals

### Fixes
- Fixed Ice Spider Patrol not firing associated spawning events, breaking compatibility with some mods
- Fixed spectators being able to be hit by raycast spells
- Fixed casting Pocket Dimension while riding a mob desyncing the player
- Fixed Chain Lightning entities persisting longer than they should
- Fixed Ice Tomb collision preventing Ice Blocks from hitting/proccing them
- Fire Fields more strictly respects friendlyfire
- Snowball more strictly respects friendlyfire
- Fixed Pocket Dimension blocks from being destroyed by the Wither
- Fixed Ice Spikes from clipping through the ground
- Fixed Ice Tomb collision from blocking certain raycasts
- Fixed Cursed Armor Stands from not have persistence in the Citadel structure
- Fixed incompatibility with some shaders playing mob animations during different render passes

### API
- Removed deprecated `SyncedSpellData` effect flags system (replaced by api-friendly `ISyncedMobEffect`)
  - Removed SyncedSpellData  static fields `ANGEL_WINGS`, `EVASION`, `HEARTSTOP`, `ABYSSAL_SHROUD`, `ASCENSION`, `TRUE_INVIS`, `CHARGED`, and `PLANAR_SIGHT`
  - Removed `SyncedSpellData#hasEffect`, `SyncedSpellData#hasLocalEffect`, `SyncedSpellData#addEffect`, `SyncedSpellData#addLocalEffect`, `SyncedSpellData#removeEffect`, `SyncedSpellData#removeLocalEffect`, `SyncedSpellData#addEffects`, `SyncedSpellData#removeEffects`
  - Constructors for `EnergySwirlLayer`s no longer take a long flag, instead a predicate or `Holder<MobEffect>`
