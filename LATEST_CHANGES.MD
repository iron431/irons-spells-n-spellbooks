### Additions
- Added Dead King respawning
  - Added Dead King Phylactery and Dead King Phylactery Shards
  - Added Dead King Soul, an entity that flies back to the Dead King's spawn location after his defeat
    - Phylactery can be consumed to respawn the Dead King repeatedly
    - Soul can be Counterspelled to be destroyed permanently
  - Shards are dropped from Catacombs Trial Spawners
  - Phylactery is crafted from 8 shards, and a Soul Lantern
- Added Trial Vaults to Catacombs: Bone Vaults
  - Added Bone Key: A per-player Dead King drop that unlocks Bone Vaults
- Added Ominous Dead King Bossfight
  - Dead King now spawns in with Ominous Buffs when summoned by a survival player with Bad Omen or Trial Omen
    - +20% Melee Damage and Spell Power
    - +10% Move and Fly speed
    - +50% Summon Damage
    - +30 Armor
    - 1000 Total HP
  - Uses Ominous Abilities
    - Applies Sacrificial Mark to summons, Sacrificing them on death
    - Summons Undead Rifts, portals which spew undead minions onto the battlefield
    - Uses Fang Swirl spell
  - Drops additional loot in Ominous mode
    - Wicked Bone Ring (50%)
      - Grants +1 Spell Projectile Ricocheting
    - 1-2 Blank Runes
    - Twice as much Arcane Essence
    - Chance to drop new boss-exclusive spell: Fang Swirl
    - Tincture of Forgetfulness, per player
  - Updated Catacombs Throne Room
    - Expanded Throne Room
    - Replaced Railing
    - Replaced Reward Sarcophagus with two Bone Vaults
  - Improved Dead King flying move controller
  - Improved Dead King flying animation
  - Added unique texture variants for Ominous mode
- Added custom Trial Vault variants for boss rewards: Bone Vault (Catacombs), and Cinderous Vault (Citadel)
- Added Sacrificial Mark mobeffect
  - Affected Summons auto-sacrifice on death
  - Indicated by dripping blood particles
- Added Fang Swirl spell
- Added `reduceTrialOmenParticles` client config for fading the opacity of nearby Trial Omen particles. Defaulted to enabled
- Added `tyrosOminousEnabled` server config, which enables unfinished Ominous Bossfight for Tyros. Default is disabled. 
- Added Tincture of Forgetfulness
  - Can be used on Vaults to allow a player to reuse the vault
- Added Tinted Bubble Particle for the Alchemist Cauldron
- Added tooltip to Scrolls showing their held spell's parent Addon, if any

### Changes
- Mobs casting Ray of Siphoning have client-server synced aim, which slowly tracks the player
- Improved Per-Player Boss loot handling
  - Bosses now always drop at least 1 set of per-player loot, even if "0" survival player summoned them
  - Per-Player boss loot is now tracked more closely to individual players, and is dropped near each player instead of all piled on the normal Boss Loot
- Dead King and Tyros can no longer enter minecarts
- Updated Twilight Gale texture (adjusted spearhead)
- Improved Zap Particle visuals and continuity

### Fixes
- Fixed Dead King Corpse ambient music playing in the wrong sound channel
- Fixed Brazier blocks not contributing to mob pathfinding malus
- Fixed container blocks (Scroll Forge, Pedestal) not implementing `Clearable`
- Fixed animation bug where fast player rotations could cause unwrapped interpolation between body and head rotations
- Fixed Amulet of Teleportation not working as intended in non-necklace curio slots
- Fixed Tyros Statue block drops
- Fixed serverbound packet with too much authority

### API
- Added `cant_ricochet` entity tag, which prevents tagged spell projectiles from using `AbstractMagicProjectile`'s ricochet mechanics
