### Additions
- 

### Changes
- Backported Spell Balance Changes
  - Guiding Bolt
    - Guiding Bolt Projectile Speed increased by 30%
    - Guided Effect duration increased from 15s -> 25s
    - Guided Projectiles home from farther, and more quickly
    - These changes should make hitting the Guiding Bolt debuff more rewarding and worthwhile
  - Fortify
    - Decreased cast range from 16 -> 8 blocks
  - Sunbeam
    - Increased base damage from 10 -> 12
    - Increased damage per level from 1 -> 1.5
    - Decreased windup time from 20 -> 15 ticks
    - These changes should make Sunbeam more viable compared to other pillar aoe spells
  - Heal (Requires Config Reset)
    - Base Healing reduced from 6 -> 5 HP
    - Max Level reduced from 10 -> 8
    - Min Rarity increased from common -> uncommon
    - Cooldown increased from 25s -> 30s
    - Mana Cost per level increased from 10 -> 15
    - While Heal was intended to be a jack-of-all-trades, Heal always outperformed and overshadowed other healing spells in their own niche. These changes should make relying on just Heal without specing holy power less universally viable
  - Blessing of Life
    - Base healing increased from 4 -> 6 HP
  - Healing Circle
    - Increased radius from 4 -> 5 blocks
  - Divine Smite
    - Improved damage hitbox placement
    - Increased damage hitbox radius
    - Increased base damage from 5 -> 8
  - Flaming Strike
    - Improved damage hitbox placement and hit registration
    - Decreased damage per level from 3 -> 2
    - Redid Spell Icon
  - Acid Spit:
    - Remove spell power scaling of rend percent
    - Increased base rend percent (5%->20%)
    - Increased base effect duration (15s->20s)
  - Heat Surge:
    - Removed spell power scaling of rend percent
    - Decreased default rend percent (20% -> 15%)
  - Blight:
    - Removed spell power scaling from effect percents
  - Teleport, Blood Step, Frost Step:
    - Teleport range now scales with a softcap
- Backported Nerfed Necromancer’s spell power
### Fixes
- Backported Fixes
  - Fixed Flaming Strike Spell being able to damage items
  - Fixed Scorch Spell entity recast positioning
  - Fixed some spells not using config power multiplier
  - Fixed Affinity Rings being able to roll disabled spells 
  - Fixed Chain Lightning entity using a dangerous comparator
  - Fixed Priest dangerous level lookup during spawn
  - Fixed casting mobs dangerous server operation during load from nbt

### API
- 
