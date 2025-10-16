### Additions
- Added Create compat for fluid filters, allowing you to use bottled item to filter fluids (Ink Bottles, Blood Vials, etc)
- Added camera shake command (/ironsSpellbooks camera_shake <pos> <radius> <ticks>)

### Changes
- Removed cooldown from Infernal Sorcerer Chestplate passive ability
- Passive Ability tooltips now use a purple color scheme instead of a green-yellow color scheme
- Cursor homing will no longer allow interference of friendly targets

### Fixes
- Fixed log complaining about Patchouli recipe if Patchouli is not installed
- Fixed Ice Tomb being subject to friendlyfire
- Fixed CME with camera shake manager
- Fixed camera shakes ignoring dimension
- Fixed other visual issues with camera shaking

### API
- Creating a camera shake data now requires a level to be passed in 
  - Deprecated `CameraShakeData(int, Vec3, float)`, which now only adds the camera shake instance to the overworld
  - Added `CameraShakeData(Level, int, Vec3, float)`, which adds the camera shake to the given level instead of all dimensions
