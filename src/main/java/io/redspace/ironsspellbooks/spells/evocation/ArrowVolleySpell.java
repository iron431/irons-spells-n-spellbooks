package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.entity.spells.ArrowVolleyEntity;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ArrowVolleySpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "arrow_volley");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", getCount(spellLevel, caster)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public ArrowVolleySpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 40;
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
        return Optional.of(SoundRegistry.ARROW_VOLLEY_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, 48, .25f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Vec3 targetLocation = null;
        if (playerMagicData.getAdditionalCastData() instanceof CastTargetingData castTargetingData) {
            targetLocation = castTargetingData.getTargetPosition((ServerLevel) level);
        }
        if (targetLocation == null) {
            targetLocation = Utils.raycastForEntity(level, entity, 100, true).getLocation();
        }
        Vec3 backward = new Vec3(targetLocation.x - entity.getX(), 0, targetLocation.z - entity.getZ()).normalize().scale(-4);
        Vec3 spawnLocation = Utils.moveToRelativeGroundLevel(level, Utils.moveToRelativeGroundLevel(level, targetLocation, 6).add(backward), 1, 2);
        spawnLocation = Utils.raycastForBlock(level, spawnLocation.add(0, 0.25, 0), spawnLocation.add(0, 6, 0), ClipContext.Fluid.NONE).getLocation().add(0, -1, 0);

        float dx = Mth.sqrt((float) ((spawnLocation.x - targetLocation.x) * (spawnLocation.x - targetLocation.x) + (spawnLocation.z - targetLocation.z) * (spawnLocation.z - targetLocation.z)));
        float arrowAngleX = dx == 0 ? 70 : (float) (Mth.atan2(dx, (spawnLocation.y - targetLocation.y)) * Mth.RAD_TO_DEG);
        float arrowAngleY = entity.getX() == targetLocation.x && entity.getZ() == targetLocation.z ? (entity.getYRot() - 90) * Mth.DEG_TO_RAD : Utils.getAngle(entity.getX(), entity.getZ(), targetLocation.x, targetLocation.z);

        ArrowVolleyEntity arrowVolleyEntity = new ArrowVolleyEntity(EntityRegistry.ARROW_VOLLEY_ENTITY.get(), level);
        arrowVolleyEntity.moveTo(spawnLocation);
        arrowVolleyEntity.setYRot(arrowAngleY * Mth.RAD_TO_DEG + 90);
        arrowVolleyEntity.setXRot(arrowAngleX + 15);
        arrowVolleyEntity.setDamage(getDamage(spellLevel, entity));
        arrowVolleyEntity.setArrowsPerRow(getArrowsPerRow(spellLevel, entity));
        arrowVolleyEntity.setRows(getRows(spellLevel, entity));
        level.addFreshEntity(arrowVolleyEntity);

        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    private int getCount(int spellLevel, LivingEntity entity) {
        return getRows(spellLevel, entity) * getArrowsPerRow(spellLevel, entity);
    }

    private int getRows(int spellLevel, LivingEntity entity) {
        return 4 + spellLevel;
    }

    private int getArrowsPerRow(int spellLevel, LivingEntity entity) {
        return 5 + spellLevel / 2;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity) * .25f;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }
}
