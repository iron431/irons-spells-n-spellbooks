### Additions
- Added Magic Spawner block and Magic Vault block (1:1 ports of Trial Spawners and Trial Vaults)

### Changes
- Backported 3.14.6
  - Added Create compat for fluid filters, allowing you to use bottled item to filter fluids (Ink Bottles, Blood Vials, etc)
  - Added camera shake command (/ironsSpellbooks camera_shake <pos> <radius> <ticks>)
  - Removed cooldown from Infernal Sorcerer Chestplate passive ability
  - Passive Ability tooltips now use a purple color scheme instead of a green-yellow color scheme
  - Cursor homing will no longer allow interference of friendly targets
  - Fixed log complaining about Patchouli recipe if Patchouli is not installed
  - Fixed Ice Tomb being subject to friendlyfire
  - Fixed CME with camera shake manager
  - Fixed camera shakes ignoring dimension
  - Fixed other visual issues with camera shaking

### Fixes
- Fixed Angel Wings spell
- Fixed Smithing Table not respecting imbued containers (Hellrazor now comes with Raise Hell again)
- Fixed stonecutter recipes missing
- Fixed client desync of Upgrade Orb Types causing crash connecting to dedicated server
- Fixed going through the end portal potentially deleting persistent spell data (such as learned spells)
- Fixed missing spawners and vaults in Catacombs, Citadel, and Ice Spider Den
- Fixed missing minimum forge version

### API
- 

changes since: patreon 5