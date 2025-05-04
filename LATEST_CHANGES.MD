### Additions
- 

### Changes
- Updated Tyros Attack Abilities
  - Reworked triple-slash animation
  - Added new melee move, utilizing a fiery dagger
  - Added ability to parry
- Adjust Dead King Balance
  - Drops half as much Arcane Essence
  - Named drops now have a 50% chance of dropping
  - Rune loot drops separated from ink loot drops and merged into scroll loot drops
  - Improved pathfinding
  - Now has damage resistance to projectiles
  - Health increased 400 -> 500hp
- Remodeled Shadow-Walker armorset
- Nerfed Ancient Knight netherite droprate (25% -> 12.5% chance)
- Nerfed Lightbringer Chestplate (removed spell resistance)
- Reworked Cursed Doll Spellbook visuals
  - No longer a doll, but a dedicated spellbook - the Vampiric Spellbook
- Buffed Villager Bible spell book attributes to be in line with Cursed Doll (Vampiric) Spellbook
- Updated Iron's Jewelry Compat
  - Cooldown runes now give twice as much reduction
  - Retextured Rune-Inscribed Ring and Rune Palettes
- Tweaked Spellbreaker texture
- Tweaked Hellrazor texture
- Adjust Fire Spell Icons
  - Retexture Firebolt spell icon
  - Adjust Wall of Fire, Scorch, Flaming Barrage, Fire Breath, and Burning Dash to use the same fire color palette

### Fixes
- Fixed Summon Damage attribute only affecting spell damages sources
- Prevent Merchant wizard trades from being read client-side
- Fixed Pyrium Ingot not being fire resistant
- Removed Active Spell Overlay
- Fixed armor rendering improperly with latest Geckolib
- Fixed casting mobs not rendering Ray of Siphoning
- Fixed Dead King not looking at his targets
- Fixed Wither Skull and Creeper Head projectile rotation rendering
- Fixed Spell Selection Manager not defaulting to first spell without manual selection
- Fixed Ice Spikes generating in the air (now need a block to support them)
- Fixed Ice Spikes targeting potentially stopping short

### API
- 