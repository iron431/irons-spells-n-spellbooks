### Additions
- 

### Changes
- Spell Config Overhaul
  - Spell configs are now json-based
  - File structure is `config/irons_spellbooks_spell_config/modid/spellid.json`
  - Serverconfig directory (`world/serverconfig/`) takes precedence over instance config
  - Example config file shows all configurable metrics, and their value types

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