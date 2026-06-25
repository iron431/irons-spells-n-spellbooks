### Additions
- Added Gravity Fissure Spell
- Added a description tooltip to the Dead King Phylactery
- Added VFX to various spells
  - Added damage vfx to Ender Chains
  - Added vfx and sound effects to impacts for Magic Missile
  - Added damage vfx to Oakskin
  - Added cast vfx to Heartstop, Invisibility, Angel Wings, Thunderstorm, and Frostbite

### Changes
- Updated Magic Arrow, Arcane Shackle, and Portal spell icons
- Spell Balance
  - Root Entity now has health, and can be damaged and killed
  - Oakskin Rebalance
    - Movement speed penalty increased from -20% to -25%
    - Mana Cost increased from 15~50 to 25~95
    - Base Damage Reduction is now fixed at 25%, and scales with spell power when cast from a spell
    - Oakskin Elixir duration increased from 45s to 60s
    - Greater Oakskin Elixir duration increased from 90s to 120s
  - Charge Nerf
    - Movement Speed per level decreased from 20% to 5%
    - Attack Damage per level decreased from 10% to 5%
    - 5% Spell Power per level has been replaced with 5% Lightning Spell Power per level
    - Duration no longer scales with level
  - Echoing Strikes Rework
    - Now grants a number of echoing hits, instead of infinite echoing hits for a duration (3 to 7)
    - Now applied at a fixed 2-minute duration
    - Melee hits echo with an area-based sword attack
    - Ranged hits echo with a piercing arrow
    - Base echo damage is now fixed at 100%, and scales with spell power
  - Nerfed Teleports
    - Teleport range decreased from 10~50 to 8~16
    - Teleport cooldown doubled from 3s to 6s
    - Frost Step range rebalanced from 10.5 to 8~11.5
    - Blood Step range decreased from 12~28 to 8~12
  - Rebalanced Haste
    - Haste amount is now fixed at 20%, and scales with spell power
    - Haste is now single target
    - Hastened no longer grants additional Mana Regeneration
  - Rebalanced Slow (to be in line with Haste)
    - Slow amount is now fixed at 20%, and scales with spell power
    - Slow is now single target
    - Slow no longer applies reduced Mana Regeneration
  - Evasion
    - Fixed Evasion teleport solver
    - Now biases teleport direction towards backwards
    - Range should no longer balloon to large quantities
- Staff Balance
  - Decreased Artificer's Cane Cooldown Reduction and Cast Time Reduction from 15% to 10%
  - Decreased Rod o' Lightning's Cooldown Reduction from 15% to 10%
  - All staffs have been brought in line with the magnitude of "extra" attributes that they give
    - School staffs give 15% School Power, 5% Spell Power, and one extra attribute
    - Generic staffs now give 10% Spell Power, and 1-2 extra attributes depending on the tier

### Fixes
- Fixed Shadow Slash from being able to trigger a parry from grounded arrows
- Fixed the extra particles no longer showing around the shockwave produced by Heat Surge and Frostwave

### API
- 
