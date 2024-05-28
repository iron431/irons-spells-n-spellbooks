### Additions
- Added config for disabling Wandering Trader magic item trades (default is enabled)

### Changes
- While using the Cast command, if the executing entity could not support long casts, they now skip the cast time in order to successfully cast the spell
- Adjusted Cone of Cold particles

### Fixes
- Fixed Netherite Battlemage armor not being fire resistant
- Fixed Black Hole Spell's order in creative menu/JEI
- Fixed Spellcasting mobs casting Fang Ward while out of range
- Fixed disabling all spells to cause merchant trying to sell scrolls to crash the client
- Fixed Ice Block Projectile serialization
- Fixed Scroll exploit

### API
- Improved functionality of SpellSelectionManager.SpellSelectionEvent. Still a work in progress
- Removed Deprecated Methods/Events
  - SpellCastEvent (Previously Replaced by SpellPreCastEvent and SpellOnCastEvent)
  - SpellSelectionManager#initOther
  - AbstractSpell#getLevel(int, LivingEntity) (Previously replaced by getLevelFor(int, LivingEntity))
  - AbstractSpell#getMana(int, LivingEntity) (Previously replace by getMana(int))
  - AbstractSpell#onCast(Level, int, LivingEntity, MagicData) (Previously replace by onCast(Level, int, LivingEntity, CastSource, MagicData))
  - Utils#doMeleeAttack (Previously replaced by doMeleeAttack(Mob, Entity, DamageSource))
