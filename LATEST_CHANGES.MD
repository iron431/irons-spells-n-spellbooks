### Additions
- Overhauled Alchemist Cauldron
  - Now is a fully-fledged fluid tank, with modded piping compatibility
    - Reworked JEI, crouch display, and according features to use fluids and milibuckets
  - All Recipes are now Datadriven, with dynamic potion handling
  - Added "Empty", "Fill", and "Brew" Recipe Types
  - Added Timeless Slurry texture
  - Added Crying Obsidian recipe
  - Added Bloody Vellum item and recipe
- Added Fluids
  - Blood
  - Timeless Slurry
  - Potion
    - Uses `minecraft:potion_contents` and `irons_spellbooks:potion_bottle_type` components to hold potion information
  - All Inks
  - All Elixirs
- Added Dye support to Portal Frame portals, courtesy of Electroely
  - Portal Frames can no longer be right-clicked to teleport
  - Portal Frames can now be right-clicked with a dye in hand to dye the portal color (at both ends)
  - Added Portal Frame config to only allow owner to dye the portal (default: enabled)
  - Added Portal Frame config to only allow owner to break the portal frame block (default: disabled)
- Added Bloody Vellum item (crafting ingredient)
- Added Cursed Doll spellbook item
  - Grants Blood Spell Power, Spell Resistance, and Affinities to Acupuncture and Blood Needles
  - Patreon request of Apshock

### Changes
- Rebalance Frost Step
  - Ice Shadow now taunts nearby enemies, taking their aggro
  - Ice Shadow now takes 5 seconds to explode without taking damage
  - Stats now scale along reasonable numbers
- Rework Starfall Targeting
  - Comets are now biased towards landing on top of entities in the zone
  - Zone is no longer outlined
  - Improve overall comet trajectory in relation to the zone
- Adjust Ice Spell Icons
- Improve Ice Spike target detection
- Rework Ender Particle
  - Now uses spell sprite instead of dust sprite
  - Is now used more liberally across ender spells
- Necronomicon now gives +2 levels to Raise Dead instead of +1

### Fixes
- Fixed Priest House structures not utilizing mossify structure processors
- Fixed Summoned Swords not being able to be counterspelled

### API
- Alchemist Cauldron
  - `AlchemistCauldronBuildInteractionsEvent` is now no longer fired, and does nothing (deprecated, pending removal)
  - `AlchemistCauldronRecipeRegistry` is now no longer used, and adding recipes to it does nothing (deprecated, pending removal)
  - `CauldronPlatformHelper`, `AlchemistCauldronRecipe`, and `AlchemistCauldronInteraction` are now no-op classes (deprecated, pending removal)
  - Added `irons_spellbooks:alchemist_cauldron_brew` Recipe Type
    - Takes a base fluidstack `base_fluid`, an item ingredient `input`, and produces a list of fluidstacks `results`, and optional byproduct itemstack `byproduct`
    - Can be datagenned
  - Added `irons_spellbooks:alchemist_cauldron_fill` Recipe Type (fills the cauldron with a fluid from a given item, such as an ink bottle)
    - Takes an input ingredient `input`, a resulting fluidstack to fill the cauldron `fluid`, an itemstack to return `result`, an optional sound event `sound` (defaults to water bottle empty sound), and an optional boolean `mustFitAll` (default: true) of whether the entirety of the fluid must fit in the cauldron in order to perform the action
    - Can be datagenned
  - Added `irons_spellbooks:alchemist_cauldron_empty` Recipe Type (empties the cauldron via an item, such as an empty bucket)
    - Takes an input ingredient `input`, a required fluidstack `fluid`, resulting itemstack `result`, and optional sound event `sound` (defaults to water bottle fill sound)
    - Can be datagenned
- Added `handleCustomLecternPosing` to `ILecternPlaceable`, allowing custom posestack manipulation
- Affinity Data Rework
  - Affinity data now supports multiple spells per item.
  - Old component format (`id` and `bonus` field) is temporarily supported (deprecated, pending removal)
  - New component format is a map-like entry `bounses`, with key-value pairs of spellId to bonus level, ie `bonuses:{"irons_spellbooks:fireball":1,"irons_spellbooks:magic_missile":2}` 