### Additions
- Added Wizard Armorset, made from Arcane Cloth
  - Now acts as a crafting precursor to all previous armors made from Arcane Cloth
  - Helmet has a Hood and Hat variant (can be used interchangeably in crafting recipes)
  - Has the same base stats as late game armor, but lacks any specializations
  - Dyeable
- Added Dying to Netherite Battlemage Armorset
- Added Greater Conjurer's Talisman necklace
- Added configurable glow settings to summoned entities
    - By default, owned summons now glow a soft green
- Added advancements for recently added spellbooks and staves (Frostbranded Book, Vampiric Spellbook, Pyrium Staff)

### Changes
- Reworked late game armor recipes
  - School armor is now crafted in the smithing table, using Wizard armor and a School rune
  - Netherite Battelmage armor is now crafted in the smithing table, using Wizard armor, a Netherite Ingot, and Netherite Smithing Upgrade Template
- Overhauled summon spells and mechanics
    - Summon spells are now recastable, where recasting unsummons them. This also limits active summon count
    - Summoned mobs log-in and log-out with their summoner
    - Removed summon timer mobeffects
    - Summons cannot leave the summoner's dimension, and are unsummoned if the summoner changes dimension
    - Rebalanced all summon spells:
        - Raise Dead base summon count buffed 1 -> 3
        - Summon Vex base summon count buffed 1 -> 3
        - Summoned Polar Bear health now scales with spell power
        - Summoned Polar Bear now has passive regen
        - Summon Horse attribute scaling completely overhauled
        - No changes to Summoned Swords
- Buffed Conjurer's Talisman summon damage (10% -> 15%)
- Replaced Scroll and Arcane Cloth textures, thanks to CrispyTwig
- Melee damage spells now reflect their bonus damage in the tooltip again

### Fixes
- Fixed a variety of bugs surrounding summon state tracking and desync
- Fixed nonmagic summon damage being able to damage owner
- Fixed summons not correctly updating lastHurtBy tracking, affecting when mobs fight back against summons
- Fixed recast bar not correctly adapting to new custom boss bars
- Fixed Summoned Horse safe fall distance not scaling with its jump strength
- Fixed Summoned Polar Bear tooltip not showing damage scaling with spell power
- Fixed players with high enough scale attribute from immediately teleporting out of Pocket Dimension by clipping into Pocket Dimension Portal Frame 
- Implemented a hacky fix for geckolib's glowing mob issues (geomobs are now no longer allowed to glow)
- Fixed Stomp falling blocks visual offset

### API
- Added `DyeableArmorRenderer`. Automatically adjusts color of bones that start with the prefix "dye" to the item's dyecolor
- Added `SummonManager`
    - Added `SummonManager#getOwner`
    - Added `SummonManager#setOwner`
    - Added `SummonManager#getSummons`
    - Added `SummonManager#initSummon`
    - Added `SummonManager#setDuration`
    - Added `SummonManager#removeSummon`
    - Added `SummonManager#recastFinishedHelper`
    - Added `SummonManager#stopTrackingExpiration`
    - Handles all summon state tracking and serialization. Porting guide available on iron.wiki
- Added `AbstractSpell#getLockedMessage` for custom unlearned error messages in Scroll Forge (courtesy of FireOfPower)

