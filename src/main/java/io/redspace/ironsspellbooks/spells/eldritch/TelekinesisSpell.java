package io.redspace.ironsspellbooks.spells.eldritch;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class TelekinesisSpell extends AbstractEldritchSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "telekinesis");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(300)
            .build();

    public TelekinesisSpell() {
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 4;
        this.castTime = 100;
        this.baseManaCost = 50;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
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
        return Optional.of(SoundRegistry.TELEKINESIS_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.TELEKINESIS_LOOP.get());
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, getRange(spellLevel, entity), .15f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof CastTargetingData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            if (targetEntity != null) {
                if ((targetEntity.isRemoved() || targetEntity.isDeadOrDying()) && entity instanceof ServerPlayer serverPlayer) {
                    Utils.serverSideCancelCast(serverPlayer);
                    return;
                }
                targetEntity.resetFallDistance();
                targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.ANTIGRAVITY.get(), 10, 0));
                float resistance = Mth.clamp(1 - (float) targetEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), .2f, 1f);
                Vec3 force = (entity.getForward().normalize().scale(targetEntity.distanceTo(entity)).add(entity.position()).subtract(targetEntity.position())).scale(.15f * resistance);
                Vec3 travel = new Vec3(targetEntity.getX() - targetEntity.xOld, targetEntity.getY() - targetEntity.yOld, targetEntity.getZ() - targetEntity.zOld);
                int airborne = (int) (travel.x * travel.x + travel.z * travel.z) / 2;
                targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.AIRBORNE.get(), 30, airborne));
                targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(force));
                targetEntity.hurtMarked = true;
            }
        }

        super.onCast(world, spellLevel, entity, playerMagicData);
    }

    @Override
    public Vector3f getTargetingColor() {
        //color similar to the spell icon
        return new Vector3f(1f, .24f, .95f);
    }

    private int getRange(int spellLevel, LivingEntity caster) {
        return 8 + (spellLevel - 1) * 4;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
