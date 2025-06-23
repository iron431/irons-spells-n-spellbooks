### Additions
- Added Ice Spider entity
  - Has 3 melee attacks, a grappling pounce attack, and a defensive leap
    - Can cast Snowball or Ice Spikes after leaping away
    - Grapples and entombs pounced enemies. Breaking the tomb causes double damage to afflict the entombed
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
- Spells
  - Ice Tomb spell
    - Cast to entomb yourself in a protective layer of ice, slowing healing while within, and completely absorbing one hit or counterspell before breaking
  - Snowball spell
    - Cast to throw a large snowball, freezing creatures on impact, and leaving a frosted field which continuously builds up freeze on creatures within 
  - Frostbite spell
    - Cast to imbue yourself with a frostbiting magic, causing fully-frozen creatures you kill to turn into Ice Shadows

### Changes
- Frozen Humanoid mobs (Ice Shadows) can now mimick entities

### Fixes
- Fixed craftable Furled Map item translations
- Fixed `IndividualTerrainStructurePoolElement` beardifier config from generating jigsaw junction beardification (causing terrain "residue" at surface level around jigsaw x-z coordinates)

### API
- Added structure-based aquifer management, to help encase and protect underground structures from aquifer generation
  - Experimental, maybe unstable, and scarcely accessible through `AquiferHelper#registerTrackedStructure`