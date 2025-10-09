### Additions
- Added The Chronicle
  - A craftable book commemorating the names of all past and present Patrons
  - Can be read in game
  - Can be placed on a lectern

### Changes
- Reworked Flaming Barrage Spell:
  - Recasts no longer select targets
  - Fireballs no longer home on preset entities
  - Recast to fire individual fireballs
  - The fireballs now home towards your cursor, or the entity targeted by your cursor
- Small Fireballs now emit particles and sounds on impact
- Small Fireballs now emit trail particles after 3 ticks of being alive

### Fixes
- Fixed Ice Tomb not being able to be crit
- Fixed Counterspell hit registration breaking in a previous update
- Fixed Breath attack spells having inconsistent base damage
- Fixed Electrocute rendering
- Fixed Zap particle rendering in certain circumstances when the particle atlas wasn't active

### API
- Removed all special features out of AbstractMagicProjectile child classes, and integrated them into AbstractMagicProjectile for generic use by all children. 
  - These features currently include: Piercing, Ricocheting, Entity homing, and Cursor homing
  - The goal is that all magic projectiles can be modified or utilize these special behaviors. The system is still WIP, so they are all considered "unstable" for addon use.
  - (API Unstable) Added `AbstractMagicProjectile#setPierceLevel`
  - (API Unstable) Added `AbstractMagicProjectile#getPierceLevel`
  - (API Unstable) Added `AbstractMagicProjectile#getHomingTarget`
  - (API Unstable) Added `AbstractMagicProjectile#setHomingTarget`
  - (API Unstable) Added `AbstractMagicProjectile#isCursorHoming`
  - (API Unstable) Added `AbstractMagicProjectile#setCursorHoming`
  - (API Unstable) Added `AbstractMagicProjectile#canRicochet`
  - (API Unstable) Added `AbstractMagicProjectile#setCanRicochet`
  - (API Unstable) Added `AbstractMagicProjectile#handleCursorHoming`
  - (API Unstable) Added `AbstractMagicProjectile#handleEntityHoming`
  - (API Unstable) Added `AbstractMagicProjectile#rotateWithMotion`
  - (API Unstable) Added `AbstractMagicProjectile#discardHelper`
