### Additions
- Added Eldritch Spell Power and Resistance attributes (not utilized in game yet)

### Changes
- Increased the caps on most attributes. Mana regen can now be 0, but not negative

### Fixes
- Fixed Casting Mob model overrides messing with the Dead King's new melee animation
- Fixed client data persisting after logout (such as the spell bar)
- Fixed left-clicking while the spell wheel is open to grab the screen without closing the menu
- Fixed Quick Casts only being able to cast from player's spell book instead of all their spells

### API
- Moved the SpellSelectionManager into the API package (may be a breaking change for mods relying on more than the API)
- Added SpellSelectionManager.SpellSelectionEvent for modders to add custom spell sources to the player
- Split the SpellCastEvent into SpellPreCastEvent and SpellOnCastEvent
