### Additions
- 

### Changes
- Reworked Surface and Cave portion of Ice Spider Den
  - Replaced Surface ruin with more defined bell tower, and now includes randomly generated side room interiors
  - Replaced Ice Den, improve cavern decorations

### Fixes
- Fixed Boots of Speed model pivot
- Significantly optimized spells/effects using falling block entities

### API
- Added `FallingBlockParticleOption`, replacing the usage of `VisualFallingBlockEntity`'s in code
  - Now used by helpers such as `Utils#createTremorBlock`
  - `VisualFallingBlockEntity` marked as deprecated and should be removed from any usage