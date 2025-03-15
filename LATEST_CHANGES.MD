### Additions
- Return of the Citadel:
  - Added the Citadel structure
  - Added Fire Boss
    - Spellcasting Enemy
    - 5 Melee Combos
    - 4 Unique Boss abilities
    - Summons Citadel Keepers
    - Music by Caner Crebes
  - Added Cursed Armorstands
- Spells:
  - Added Fire Arrow Spell
  - Added Raise Hell Spell
  - Added Summon Swords Spell (Patreon request of Hazen)
  - Added Touch Dig Spell
- Items:
  - Added Hellrazor
  - Added Decrepit Scythe
  - Added Legionnaire Flameberge
  - Added Pyrium Staff
  - Added Signet of the Betrayer
  - Added Cinderous Soulcaller
  - Added Ancient Furled Map
  - Added Decrepit Key
  - Added Chained Book
  - Added Divine Soulshard
  - Added Mithril Weave (crafting ingredient)
  - Added Pyrium Ingot (crafting ingredient)
  - Added Timeless Slurry (crafting ingredient)
  - Added Holy Artifact: Lightbringer's Chestplate
  - Added Evocation Artifact: Boots of Speed
  - Added Music Disc: Flame Felled
  - Added Flame Felled Music Disc Shards
- Blocks:
  - Added Cinderous Keystone block
  - Added Brazier and Soul Brazier blocks
  - Added Nether Brick Pillar block
  - Added Book Stack decoration block
  - Added Wisewood Bookshelf and Chiseled Bookshelf blocks
- Added Pedestal block Recipe
- Added Creative Inventory Tab for Iron's Spellbooks Blocks 

### Changes
- Flaming Strike Visual Entity replaced with particle
- Replaced soul campfires with soul braziers in the Catacombs throne room 
- Changed Ancient Knight visuals
  - Armor is no longer made out of netherite, but a decrepit material
  - Reworked Decrepit Flamberge texture
- Armor Piles
  - Reworked Visuals 
  - No longer requires diamond tools
  - Nerfed Drops
- Rebalance ink drops across all loot
  - Lower qualities of ink are more plentiful, while higher qualities are more rare
  - Regular casting mobs (Pyromancer, Apothecarist, etc) now cannot drop legendary ink
- Summons now preemptively scan for and attack creatures that target their owner
- Eldritch Blast no longer procs Iframes

### Fixes
- Melee mobs can no longer hit through blocks

### API
- Removed `TransformStack#pushRotationWithBase`
- Added `guiding_bolt_immune` tag for projectiles