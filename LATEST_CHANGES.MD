### Additions
- 

### Changes
- 

### Fixes
- Fixed Alchemist Cauldron holding on to level state for dispenser interactions
- Fixed Shield not blocking line of sight. Shields, and other solid entities such as boats and shulkers, now block splash and area damage behind them
- Fixed bosses and spellcasting mobs losing their custom spell power, spell resistance and cast speed, such as the Dead King's and Tyros' bonus spell power
- Fixed summons treating every other summon or tamed animal as an ally when their owner is on a scoreboard team
- Fixed the Amulet of Teleportation granting Evasion endlessly when an Evasion dodge itself teleports
- Fixed an empty or malformed spell config file aborting the whole spell config load. A spell whose default school does not exist now falls back to Evocation and logs an error
- Fixed spell school changes from spell configs (`/reload`, datapacks) not reaching the Scroll Forge, JEI and school-filtered loot
- Fixed Counterspell and unsummoning not removing the Dead King's Undead Rift
- Fixed Shadow Slash knocking enemies back far too hard
- Fixed Ascension missing targets around its lightning strike when cast in the air
- Fixed players who come into range of a mob mid-cast not seeing the cast until the next one
- Fixed armor losing all its upgrades, or failing to load, when one of its upgrade orb types no longer exists. Unknown upgrades are now dropped with a warning
- Fixed a rare race that could wipe the upgrades of old-format upgraded armor while loading
- Fixed spell containers with out-of-range slot data crashing instead of loading
- Fixed Touch Dig and Spectral Hammer cast by mobs skipping block protection checks
- Fixed Recall never working for mobs with a home position
- Fixed the recast overlay drawing partly or fully off screen with any anchor other than Top Center
- Fixed Necromancers rarely spawning from trial spawners

### API
- 
