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
- Fixed Cyromancer armor using Pyromancer attributes
- Fixed Tyros melee scale modifiers not applying correctly
- Fixed Ray of Siphoning and Eldritch Blast from not being able to register hits against multipart entity hitboxes
- Fixed summons potentially copying an invalid target from summoner, if summoned by a mob

### API
- Improved API compatibility with TO Tweaks
- Improved API compatibility with Geomancy
- Improved API compatibility with Cataclysm: Spellbooks

changes since: patreon 7

Known Api Breaks:
- Summons
  - OwnerGetter removed (Can basically be recompiled away)
  - getOwner LivingEntity -> Entity
- Upgrade Orbs
  - todo
