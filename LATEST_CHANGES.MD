### Additions
- Added Ice themed music disc: Whispers of Ice by Caner Crebes
  - Obtained by trading
- Added Polish localization, thanks to TopTophat

### Changes
- Rune balance rework
  - Cryomancer is now Neutral, and will trade with the player
  - Merchant Wizards now trade for a rune of their respective school
    - Added Fire Rune trade to Pyromancer (4 Chained Books, 1 per restock)
    - Added Ice Rune trade to Cryomancer (2 Icy Fangs, 1 per restock)
    - Added Nature Rune trade to Apothecarist (4 Greater Elixirs of a random type, 1 per restock)
    - A Priest trade will be added later
  - Neutral Wizards no longer drop runestones of their school
    - The value of the rune drop incentivized killing the neutral wizards too strongly. Now, earning a rune from neutral wizards is more involved
  - Hostile Wizards rune drops rate reduced to a 50% chance, and now explicitly requires player kill credit
- Tweaked Flaming Strike animation
- Tweaked Cooldown and Silver ring textures
- Buffed Ring of Expulsion: Cooldown reduced from 10s -> 2s

### Fixes
- Fixed Merchant Wizard restocking
- Fixed spell bar displaying spell book slots with a purple border
- Fixed potential client-server desync of spell config if the player's client never loaded configs
- Fixed blaze and ghast fireballs being rotated incorrectly
- Fixed stretched textures on fireball model
- 1.20.1: Fixed client-only helper being used for lectern placeable books
- 1.20.1: Fixed Horse jump meter rendering over XP mana bar
- 1.20.1: Fixed Firefly Jar recipe using invalid tag for glass

### API
- `SpellConfigParameter#defaultValue()` is now typed as a `Supplier<T>`, instead of a raw `T` value. 
- Added mob effect tag `irons_spellbooks:affected_by_spider_aspect` (courtesy of VeroxUniverse)
- Magic damage types are now tagged as `neoforge:is_magic`