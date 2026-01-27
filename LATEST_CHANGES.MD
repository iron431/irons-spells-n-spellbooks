### Additions
- Added support for organizational subdirectories in spell config folders/datapacks
  - The spell id is still determined by the root directory and file name, but intermediary directories are now allowed for better organization
  - i.e. `/modid/my_folder/spell_name.json` now maps to `modid:spell_name`
- Added `/ironsSpellbooks` subcommand: `/ironsSpellbooks config`
  - Contains helpers for new datadriven configs
  - `regenerate_example`: Regenerates `example.txt` file in instance config folder, showing all configurable parameters and their default values.
  - `generate_file <spell_id> <full | skeleton> [override]`: Generates a json file for the given spell in the instance config folder
    - Full: fills out all config parameters with the spell's default values
    - Skeleton: generates empty json file
    - Override: whether to abort or override if the file already exists
  - `list`: Lists all configurable spell parameters in chat

### Changes
- Reworked Surface and Cave portion of Ice Spider Den
  - Replaced Surface ruin with more defined bell tower, and now includes randomly generated side room interiors
  - Replaced Ice Den with more visually appealing den structure
  - Improved cavern decorations to be much more varied and organic
  - No changes to the dungeon

### Fixes
- Fixed Boots of Speed model pivot
- Fixed dyable items not having cauldron interactions for clearing dye color
- Significantly optimized spells/effects using falling block visuals

### API
- Added `FallingBlockParticleOption`, replacing the usage of `VisualFallingBlockEntity`'s in code
  - Now used by helpers such as `Utils#createTremorBlock`
  - `VisualFallingBlockEntity` marked as deprecated and should be removed from any usage
- In order to maintain better compatibility with 1.20.1, new standalone sub-items have been added for some items which used to be component driven:
  - Added `irons_spellbooks:furled_map_citadel` item
  - Added `irons_spellbooks:furled_map_ice_spider_den` item
  - Added `irons_spellbooks:wizard_hat` item
- Added new item tags for crafting school armor: `#irons_spellbooks:wizard_base_boots`, `#irons_spellbooks:wizard_base_leggings`, `#irons_spellbooks:wizard_base_chestplate`, and `#irons_spellbooks:wizard_base_helmet`
  - By default, contains the respective base wizard armor set item
  - Is now used in crafting upgraded school/netherite armor sets
