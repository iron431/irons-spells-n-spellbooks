### Additions
- Added Shockwave Spell
- Added Boss Music to the Dead King boss fight, courtesy of Caner Crebes
- Added Hogskin -> Leather recipe
- Added blastwave particle to Starfall Comets

### Changes
- Dying entities can no longer be targeted by spells
- Reworked Spell Griefing Mechanics
  - Instead of using mobGriefing gamerule, there is now a spellGriefing server config
  - The default is off (no spell griefing)
- Reworked Fireball Spell
  - Now has 5 levels (requires config reset)
  - Minimum rarity is rare (requires config reset)
  - Explosion radius is larger at lower levels
  - Damage is lower at higher levels
  - Reworked explosion visuals
  - Now requires spellGriefing to be enabled to break and light blocks
- Adjusted various ring textures
- Reworked Catacombs Throne Room
- Reworked Dead King melee animations
- Dead King Balance Changes
  - Decreased projectile resistance by 25%
  - Increased based health by 25%
  - Now immune to lava
  - Now immune to fall damage
- Reworked all Casting Mobs' look control for better vertical spell casting

### Fixes
- Fixed Spell Bar configured Y offset applying to X and Y
- Fixed Dead King's legs improperly animating during phase transition animation
- Fixed Zap Particle rendering at many rotations
- Fixed Shriving Stones being able to remove the spells of unique items (Hither-Thither wand)
- Fixed client crash on the inscription table caused by JEI resetting the spellbook slot
- Fixed Arrow Volley entity not having an owner
- Fixed Evoker Fort cages having a gap, which pillagers could shoot and kill captive villagers through
- Fixed Apotheosis gem values

### API
- Added interface for easier use of ExtendedArmorMaterials
