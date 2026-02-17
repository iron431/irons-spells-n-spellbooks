### Additions
- 

### Changes
- 

### Fixes
- Fixed Cryomancer still being able to be hostile instead of neutral
- Fixed potential CME due to mulithreaded particle rendering
- Fixed Wandering Trader random curio cost

### API
- Abstracted animation helper methods into `AnimationHelper`
  - Deprecated `ClientSpellCastHelper#animatePlayerStart`, use `AnimationHelper#animatePlayerStart` instead