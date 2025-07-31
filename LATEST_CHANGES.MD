### Additions
- Added Volt Strike spell

### Changes
- Only Eldritch Spells now appear in the Eldritch Learning Screen, instead of all learnable spells
- Adjust Black Hole visuals and sounds
- Rework Soft Cap formula
  - Affects all reductive attributes (Cooldown Reduction, Spell Resistance, Cast Time Reduction, etc)
  - Softcap begins sooner and approaches maximum slower 
- Updated Korean Localizations, thanks to moralless0591
- Tweak Wizard Armor icons
- Spell Balance
  - Rework Chilled debuff
    - No longer doubles incoming freeze time
    - If affected creatures become fully frozen, the effect is now consumed to encase them in an evil Ice Tomb
      - (Evil Ice Tombs double incoming damage that breaks them to creature inside)
    - Snowball's explosion now afflicted Chilled, not freeze
    - Ice Tombs no longer afflict Chilled
    - Frostwave still afflicts Chilled
  - Rebalance Ball Lightning
    - Damage range reduced from [6,15] to [5,9.5]
    - Lifetime increased from 4s to 5s
  - Buff Ice Shadows
    - Now create a small explosion around them for half their shatter damage
  - Buff Lob Creeper Spell
    - Increase explosion radius 3.5 → 5 blocks
    - Increase base projectile velocity
  - Buff Chain Creeper Spell
    - Increase explosion radius 3.5 → 5 blocks
    - Chained explosions now count down from the previous chain count, instead of only spawning 3 more bombs
  - Nerf Heartstop
    - Reduce Max Level 10 → 5
    - Reduce Initial Duration 15s → 10s
    - Increase Min Rarity Common → Rare
    - Increase Base Mana Cost 50 → 100
  - Nerf Acid Spit
    - Reduce Max Level 10 → 8
    - Increase Base Mana Cost 30 → 40
    - Increase Mana Cost Per Level 3 → 10
  - Nerf Heat Surge
    - Reduce Max Level 8 → 6
    - Increase Mana Cost Per Level 8 → 10
  - Nerf Fortify
    - Increase Base Cooldown 35s → 60s
    - Increase Base Mana Cost 40 → 80
    - Increase Mana Cost Per Level 5 → 10
    - Increase Cast Time 2s → 3s
  - Nerf Recall
    - Explicitly no longer able to be cast from combat (similar to Pocket Dimension)
    - Increase Cast Time 4s → 5s
    - No longer affected by Cast Time Reduction
  - Nerf Teleport
    - Increase Mana Cost Per Level 2 → 5
  - Nerf Bloodstep
    - Increase Cooldown 5s → 12s
  - Nerf Frost Step 
    - Increase Cooldown 10s → 12s
  - Nerf Earthquake
    - Slowness effect is now capped at amplifier 3
  - Nerf Sculk Tentacles
    - Damage no longer scales with level
    - Reduce tentacle damage hitbox size
  - Nerf Blight
    - Increase Base Cooldown 35s → 90s
    - Increase Base Mana Cost 10 → 60
    - Increase Mana Cost Per Level 5 → 20
  - Nerf Angel Wings
    - Reduced Base Duration 30s → 10s
    - Reduced Duration per Level 30s → 10s
    - Increased Base Mana Cost 60 → 80
    - Increased Min Rarity Epic → Legendary
    - Now acts more like a short-term glider with low uptime, unless the caster actively specs into cooldown and holy power
  


### Fixes
- Fixed centered Mana Bar overlay not respecting hotbar height 
- Fixed Sacrifice explosions being able to damage items
- Fixed Creeper Bomb explosions being able to damage items

### API
- Added `/ironsSpellbooks upgrade` command
- Added `/ironsSpellbooks it` command
- Added `/it` command while in development environment
- Added `ISyncedMobEffect`, an interface that makes the API sync affected `MobEffects` to all clients
  - Allows client-based state tracking native to effect lookups 
  - Replaces `SyncedSpellData` synced effect flags. Currently only used by Volt Strike and Abyssal Shroud

