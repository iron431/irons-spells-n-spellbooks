### Additions
- 

### Changes
- Updated Fire Resistance of items
  - All Mithril and their derivative items (upgrade orbs, weapons, artifacts)
  - Spellbooks
  - Staffs
  - Fire Ale
- Updated all Ender Spell Icons
- Updated to Gems 'n Jewelry 2.0 data format
- Tweaked Ricochet Target selection to be more consistent with large hitboxes
- Tweaked Cinderous Vault textures
- Tyros now gains 60% damage reduction during Stance Break animations instead of 40%
- Tyros now removes nearby fiery daggers on death
- Fiery Smoke particles fade towards transparent if they are too close to the player's camera

### Fixes
- Fixed a targeting goal potentially having a null target after starting
- Fixed Tyros summoning Ancient Knights in the walls of the arena
- Fixed Tyros's half health attack not cancelling if killed during it

### API
- Removed or Deprecated all Item Constructors which did not take Item.Properties argument
  - This is mostly internal, but some constructors were left deprecated instead of removed:
    - SpellBook
    - UniqueSpellBook
    - SpellSlotUpgradeItem
    - FurledMapItem
  - This is to update code to be more similar to 26.1.2 conventions
- Deprecated `ItemPropertiesHelper` class
  - This was a version abstraction for 1.19.2, and should no longer be used (especially with upcoming 26.1.2 changes)
