### Additions
- Added Ice Tomb spell
  - Cast to entomb yourself in a protective layer of ice, slowing healing while within, and completely absorbing one hit or counterspell before breaking
- Added Snowball spell
  - Cast to throw a large snowball, freezing creatures on impact, and leaving a frosted field which continuously builds up freeze on creatures within
- Added Frostbite spell
  - Cast to imbue yourself with a frostbiting magic, causing fully-frozen creatures you kill to turn into Ice Shadows
- Added Ice Spider entity
  - Has 3 melee attacks, a grappling pounce attack, and a defensive leap
    - Can cast Snowball or Ice Spikes after leaping away
    - Grappled enemies are entombed in ice. Breaking the tomb transfers double damage to the entity inside
    - Grapple attack can be blocked, disabling the shield
  - Has accurate moving hitboxes
  - Can crouch and climb through obstacles
  - Can drop an Icy Fang upon death, a useful crafting ingredient
- Added Ice Spider Egg block
  - When found in the world, it has a protective layer of ice around it
  - Breaking the ice allows it to be harvested, and will summon nearby Ice Spider to protect it
  - Requires silk touch to harvest after ice shell has been destroyed
- Added Ice Spider den structure
  - Approach an unassuming derelict structure in icy biomes
  - Descend into an infested dungeon, a ruinous landscape repurposed as an Ice Spider's den, full of Ice Spider Eggs
- Added Icy Fang item, a drop from Ice Spiders
- Added Ice Venom Vial item, a crafting ingredient refined from Icy Fangs
- Added the Boreal Blade, a magic ice greatsword crafted from Mithril and Ice Venom
- Added the Frostbranded Book, a top-their Ice spellbook
- Added craftable Furled Map variant, leading to the Infested Ruins
- Added lens flare to Magic Missile rendering
- Added Spell Wheel config option to make it ignore Gui Scale (always fit screen). Not enabled by default.

### Changes
- Frozen Humanoid mobs (Ice Shadows) can now mimick entities
- Rebalanced/Fixed Echoing Strikes spell power scaling
- Nerf Summoned Swords spell
    - All entity's base damage reduced by 2
    - Sword and Rapier base health reduced by 5
- Long Casts are no longer interrupted by Heartstop Damage, Fall Damage, or Drowning Damage

### Fixes
- Fixed craftable Furled Map item translations
- Fixed hoppers being able to insert stacks of items into Alchemist Cauldron input slots
- Fixed Evasion being able to dodge Heartstop damage
- Fixed Dead King's second phase transition not cancelling his current action 
- Significantly optimize shield-particle collision handling; add client config to completely disable it

### API
- Added structure-based aquifer management, to help encase and protect underground structures from aquifer generation
  - Experimental, maybe unstable, and scarcely accessible through `AquiferHelper#registerTrackedStructure`
- Fixed `IndividualTerrainStructurePoolElement` beardifier config from generating jigsaw junction beardification (causing terrain "residue" at surface level around jigsaw x-z coordinates)
