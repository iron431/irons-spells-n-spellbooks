### Changes
- Tyros now uses cast components instead of attribute modifier hack to quickly channel his midair fireballs

### API
- Added `createLevelRenderable` to AbstractSkill
  - Now auto-populates a level renderable if set on `onClientCastStart`
- Added `levelRenderableRotatesWithCast` to AbstractSkill
- [Breaking] `createClientTicker` now takes a CastContext
- [Breaking] Removed `SpellSummonEvent` and `SetSummonOwnerEvent`. Replaced by the `SummonEvent` suite (`api.events.summon`):
  - `SummonEvent.Pre` (cancellable, entity substitutable) and `SummonEvent.Post`, posted by the new `SummonManager#summon(CastContext, ...)` for every cast-produced summon (Summon Polar Bear now fires them too)
  - `SummonEvent.OwnerSet`, posted by `SummonManager#setOwner`
  - `SummonEvent.Unsummon` with a `Reason`, posted by the new `SummonManager#unsummon` (expiry, dismissal, owner dimension change, anti-magic)
  - Owners are exposed as `CasterRef`; `getOwnerEntity()` covers the entity case
- Cast-produced summons are now attached to the cast context (`ATTACHED_ENTITIES`)
- [Breaking] Removed `MagicSwordItem`; use `ExtendedSwordItem.imbued(properties, skills...)`
- [Breaking] Removed `ImbuableChestplateArmorItem`; use `ExtendedArmorItem.imbuable(properties)` at registration
- [Breaking] Removed `AbstractSpellCastingMob#attemptInitiateCastSpell`; use `SkillcastingUtils.attemptInitiateMobCast`
- [Breaking] `CastSource.type` now identifies what kind of source cast the skill; for equipment gathered by skillcasting it is the registry id of the data component holding the container (e.g. `irons_spellbooks:imbued_container`). `SpellCastSources.SPELLBOOK/SCROLL/IMBUE` are those ids.
- [Breaking] Removed `CastSource.of(EquipmentSlot)` and `CastSource.of(String)`; use `CastSource.of(type, slot)`
- [Breaking] `GatherSkillSelectionEvent#addSource(container, EquipmentSlot, priority)` is now `addSource(container, DataComponentType origin, EquipmentSlot, priority)`
- Datapack `power_multiplier` and `mana_multiplier` and the `swordsConsumeMana` server config are applied again
- Added `SpellcastingComponentTypes.DIG_DEPTH`
- Restored 1.21 values for Blaze Storm, Ice Block, Fireball, Magma Bomb, Throw, Starfall, Magic Arrow, Summon Horse, Sculk Tentacles, Arrow Volley, Lightning Bolt, Poison Arrow, Spectral Hammer, Telekinesis; fixed Shadow Slash hitbox and Gust strength scaling; Earthquake, Root, Stomp, Thunderstorm apply school and config power again
