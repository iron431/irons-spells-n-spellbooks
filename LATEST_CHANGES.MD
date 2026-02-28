### Additions
- Added Dead King scaling configs
- Added French localization support, thanks to acorsicanfrog

### Changes
- Reworked Dead King player scaling to match Tyros
- Improved Magma Bomb fire field placement
- Improved wizard support goal consistency
- Improve player animation adjustments
- Tweaked Chained Book texture
- Update Polish localization, thanks to TopTophat

### Fixes
- Fixed Cryomancer still being able to be hostile instead of neutral
- Fixed potential CME due to mulithreaded particle rendering
- Fixed Wandering Trader random curio cost
- Fixed AOE entities being able to move
- Fixed generic curio slot applying attributes and spell slots incorrectly
- Fixed Raise Dead spawn placement
- Fixed Guiding Bolt affecting mob's own projectiles

### API
- Abstracted animation helper methods into `AnimationHelper`
  - Deprecated `ClientSpellCastHelper#animatePlayerStart`, use `AnimationHelper#animatePlayerStart` instead
- Added `SwirlingParticle`
- Reworked input keybinds to use tick-base codepath instead of native event subscription
  - Courtesy of EchoEllet