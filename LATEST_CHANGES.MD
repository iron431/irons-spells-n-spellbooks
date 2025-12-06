### Additions
- 

### Changes

- Datadriven Spell Config Overhaul
    - Spell configs are no longer in Server Configs
    - Global Spell Configs are now accessible in custom directory in instance config
      - Config File structure is `config/irons_spellbooks_spell_config/<modid>/<spellid>.json`
    - Per-World Spell Configs are now accessible through datapacks
      - Datapack File structure is `data/<modid>/irons_spellbooks_spell_config/<spellid>.json`
      - Datapack takes precedence over global config. Global config is entirely ignored if a configuration datapack is present for a
        world.
    - Example config file shows all configurable metrics, and their value types
    - Added command to automatically convert existing config/serverconfig
        - `/ironsSpellbooks convert_legacy_config`
        - Automatically targets latest config backup (bak) file

### Fixes
- 

### API

- All old config infrastructure marked as deprecated
  - `@AutoSpellConfig`
  - `ServerConfigs#getSpellConfig(AbstractSpell)`
  - `ServerConfigs#getSpellConfigs()`
- Added `SpellConfigManager`, which holds all config data now. Important Methods:
    - `SpellConfigManager#getInstance()`
    - `SpellConfigManager#getSpellConfigValue(AbstractSpell, SpellConfigParameter<T>)`
    - `SpellConfigManager#getDefaultSpellConfigValue(AbstractSpell, SpellConfigParameter<T>)`
- Added `SpellConfigParameter<T>`, singleton keying objects which hold the information for identifying and
  reading/writing config values
- Added `IronConfigParameters`, a class holding all default `SpellConfigParameter<T>`s
- Added event `ModifyDefaultConfigValuesEvent`, which allows modders to edit default config values without the need of mixins
    - `ModifyDefaultConfigValuesEvent#getSpell()`
    - `ModifyDefaultConfigValuesEvent#setDefaultValue(SpellConfigParameter<T>, T)`
- Added event `RegisterConfigParametersEvent`, which allows modders to add custom entries to spell config files
    - `RegisterConfigParametersEvent#register(SpellConfigParameter<?>)`