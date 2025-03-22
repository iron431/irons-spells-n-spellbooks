### Additions
-

### Changes
- Rebalance Arrow Volley Spell
  - Arrows no longer proc iframes
  - Arrow damage no longer scales from level, only spell power
- Update Chinese and Japanese translations 
- Casting Implements are no longer Multihand Items by default
  - Using a casting implement is intended to be a way of commiting to a mage build, but being able to utilize casting implements in the offhand gave all the benefits while mitigating most cost

### Fixes
- Fixed Wisewood Bookshelves not being able to power enchanting tables
- Fixed missing Better Combat weapon tags
- Fixed non-nullsafe jei code due to upgrade orb changes
- Fixed Spell Filters having an invalid yet nonempty return if all spells are disabled/disabled looting
- Fixed XP Mana bar no longer hiding the XP bar while active
- Fixed Betrayer Signet Ring multiplying damage instead of multiplying bonus damage
- Fixed Stomp and Arrow Volley direction resetting

### API
- Added optional "force" field to Spell Filters. If true, it forces spells to be lootable (useful for eldritch loot tables)