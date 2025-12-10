### Additions
- 

### Changes
- 

### Fixes
- Fixed potential "Invalid Player Data" error on login
- Fixed API mismatch for rendering some mobs, such as T.O Tweaks Fading Mage

### API
- 

changes since: patreon rc 2

Known Api Breaks:
- Summons
  - OwnerGetter removed (Can basically be recompiled away)
  - getOwner LivingEntity -> Entity
- Upgrade Orbs
  - Orbs without datadriven support (all addon orbs, currently) do not work and default to being a mana upgrade so as to not crash the game. Addon authors must eventually support the new datadriven format
