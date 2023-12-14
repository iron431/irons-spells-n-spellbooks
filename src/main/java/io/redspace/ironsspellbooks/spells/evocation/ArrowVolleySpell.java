package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.entity.spells.ArrowVolleyEntity;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ArrowVolleySpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "arrow_volley");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(spellLevel, caster), 1)),
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
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 0;
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
        return Optional.empty();
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
            HitResult raycast = Utils.raycastForEntity(level, entity, 32, true);
            if (raycast.getType() == HitResult.Type.ENTITY) {
                targetLocation = ((EntityHitResult) raycast).getEntity().position();
            } else {
                targetLocation = Utils.moveToRelativeGroundLevel(level, raycast.getLocation().subtract(entity.getForward().normalize()).add(0, 2, 0), 0);
            }
        }
        Vec3 spawnLocation = targetLocation.subtract(entity.getForward().normalize().scale(2)).add(0, 5, 0);
        ArrowVolleyEntity arrowVolleyEntity = new ArrowVolleyEntity(EntityRegistry.ARROW_VOLLEY_ENTITY.get(), level);
        arrowVolleyEntity.moveTo(spawnLocation);
        arrowVolleyEntity.setYRot(Utils.getAngle(entity.getX(), entity.getZ(), spawnLocation.x, spawnLocation.z) * Mth.RAD_TO_DEG + 90);
        level.addFreshEntity(arrowVolleyEntity);

        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    private int getCount(int spellLevel, LivingEntity entity) {
        return 3 + getLevel(spellLevel, entity) - 1;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity);
    }
}
