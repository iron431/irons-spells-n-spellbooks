### Additions
- Added New School, Eldritch
  - Spells
    - Added Planar Sight Spell
    - Added Sonic Boom Spell
    - Added Telekinesis Spell
    - Reenabled Abyssal Shroud Spell
    - Reworked Sculk Tentacles Spell (was Void Tentacles)
  - Items
    - Added Eldritch Manuscript Item
    - Added Ancient Knowledge Fragment Item
  - Research System
    - Eldritch Spells are unlearned by default. (Cannot be crafted nor cast)
    - Added Research Menu, accessed by Eldritch Manuscripts
- Misc
  - Added Divine Smite Spell
    - This spell is always uninterruptible
  - Added additional Wandering Trader trades
    - Sell ink to Wandering Trader
    - Buy Curios from Wandering Trader
    - Buy scrolls from Wandering Trader
    - Buy mystery bags of scrolls from Wandering Trader
    - Buy Ancient Knowledge Fragments from Wandering Trader
  - Added a Patchouli page for Schools
### Changes
- Spell Balance
  - Ice Block Spell now moves faster and initiates its fall sooner
  - Counterspell Default cooldown reduced from 15s -> 10s (existing worlds need config reset)
  - Improved Lightningbolt Spell's hit detection/placement
  - Increased Lightning Lance Spell's Damage
  - Decreased Lightning Lance Spell's Default Rarity from Rare -> Uncommon (existing worlds need config reset)
  - Magma Bomb impact damage no longer scales with spell level
  - Magma Bomb aoe damage slightly increased
  - Magma Bomb radius is reduced, and now scales with spell power
### Fixes
-

### API
- Spells now have methods for crafting criteria, craftability, lootability, and interruptibility
- Consolidated mana and cooldown checks into a CastResult class
- Added canBeCastBy (returns CastResult) method to AbstractSpell