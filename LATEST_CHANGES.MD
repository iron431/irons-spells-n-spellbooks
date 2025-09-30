### Additions
- 

### Changes
- Neutral Wizard anger/aggro is now tracked per-player
- Adjust Pyromancer trade balance
- Adjust Pyromancer tower structure
  - Now uses Wisewood bookshelves, and new decoration blocks
- Potion Fluid tooltips now include duration, if applicable

### Fixes
- Fixed Twilight Gale missing better combat support
- Summon Ender Chest now consumes scrolls properly
- Fixed armor cape physics when multiple entities are preset
- Frost Field more strictly respects friendlyfire
- Fixed merchant wizard's not preventing interactions when hostile on the client
- Fixed merchant wizards not properly looking at trading player

### API
- Added `IMerchantWizard#stopTrading` helper
- Added `FocusOnTradingPlayerGoal`
- Added `NeutralWizard#increaseAngerLevel(Player, int, boolean)`
- Deprecated `NeutralWizard#inreaseAngerLevel(int, boolean)`
  - Use player-sensitive version instead
- Notes: the fixes for merchant wizards listed above need to be implemented on a per-entity basis. [See commit details here](https://github.com/iron431/irons-spells-n-spellbooks/commit/c8349f9b6afc6ef25e3a8302efd182b93ebda8a4#diff-e2c10ac92eb7764ce0290bb54a244538cf6cbf247f8309ab2d0d97a621657b5d)
