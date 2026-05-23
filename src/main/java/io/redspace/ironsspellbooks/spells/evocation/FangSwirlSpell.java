package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.FangSwirlEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class FangSwirlSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "fang_swirl");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getSwirlRadius(spellLevel, caster), 2))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(28)
            .setAllowCrafting(false)
            .build();

    public FangSwirlSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 55;
    }

    @Override
    public boolean allowLooting() {
        return false;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_ATTACK);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, (int) getRange(spellLevel, entity), 0.35f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 dest = null;
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            LivingEntity target = castTargetingData.getTarget((ServerLevel) level);
            if (target != null) {
                dest = target.position();
            }
        }
        if (dest == null) {
            HitResult raycast = RaycastBuilder.begin(level, entity)
                    .range(getRange(spellLevel, entity))
                    .checkForBlocks(true)
                    .bbInflation(0.35f)
                    .build();
            if (raycast.getType() == HitResult.Type.ENTITY) {
                dest = ((EntityHitResult) raycast).getEntity().position();
            } else {
                dest = raycast.getLocation().subtract(entity.getForward().normalize());
            }
        }
        dest = Utils.moveToRelativeGroundLevel(level, dest, 6);
        Vec3 start = entity.position();
        float horizontalDist = (float) dest.subtract(start).horizontalDistance();
        int delay = Math.max(8, Math.min(40, Mth.ceil(horizontalDist * 1.5f))) * 2/ 3;

        FangSwirlEntity swirl = new FangSwirlEntity(EntityRegistry.FANG_SWIRL.get(), level);
        swirl.moveTo(dest.x, dest.y, dest.z);
        swirl.setStartPos(start);
        swirl.setDelay(delay);
        swirl.setRadius(getSwirlRadius(spellLevel, entity));
        swirl.setDuration(getSwirlDurationTicks(spellLevel, entity));
        swirl.setOwner(entity);
        swirl.setDamage(getDamage(spellLevel, entity));
        level.addFreshEntity(swirl);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getRange(int spellLevel, LivingEntity caster) {
        return 32;
    }

    private float getSwirlRadius(int spellLevel, LivingEntity caster) {
        return 4.5f + 0.5f * spellLevel * getEntityPowerMultiplier(caster);
    }

    private int getSwirlDurationTicks(int spellLevel, LivingEntity caster) {
        return 8 * 20;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * .75f;
    }
}
