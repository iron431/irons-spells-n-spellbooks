### Additions
- 

### Changes
- Datadriven Spell Config Overhaul
  - Spell configs are now json-based, using a hybrid of instance config as well as datapacks for maximum accessibility
  - Config File structure is `config/irons_spellbooks_spell_config/<modid>/<spellid>.json`
  - Datapack File structure is `data/<modid>/irons_spellbooks_spell_config/<spellid>.json`
  - Datapack takes precedence over global config. Global config is entirely ignored if datapack is present for a world.
  - Example config file shows all configurable metrics, and their value types
  - Added command to automatically convert existing config/serverconfig
    - `/ironsSpellbooks convert_legacy_config`
    - Automatically targets latest config bak file

### Fixes
- 

### API
- All old config infrastructure marked as deprecated.
- Added `SpellConfigManager`, which holds all config data now
  - Instance stored at `IronsSpellbooks.CONFIG_MANAGER`
  - Static Helper `SpellConfigManager#getSpellConfigValue(AbstractSpell, SpellConfigParameter<T>)`
  - Static Helper `SpellConfigManager#getDefaultSpellConfigValue(AbstractSpell, SpellConfigParameter<T>)`
- Added `SpellConfigParameter<T>`, singleton keying objects which hold the information for identifying and reading/writing config values
- Added `IronConfigParameters`, class holding all default `SpellConfigParameter<T>`s
- Added `ModifyDefaultConfigValuesEvent`, which allows modders to edit default config values without the need of mixins
  - `ModifyDefaultConfigValuesEvent#getSpell()`
  - `ModifyDefaultConfigValuesEvent#setDefaultValue(SpellConfigParameter<T>, T)`
- Added `RegisterConfigParametersEvent`, which allows modders to add custom entries to spell config files
  - `RegisterConfigParametersEvent#register(SpellConfigParameter<?>)`