package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.thunderwave.ThunderwaveProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ThunderwaveSpell extends AbstractSpell {
    private static final float DEGREES_PER_PROJECTILE = 20f;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "thunderwave");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", getProjectileCount(spellLevel))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(12)
            .build();

    public ThunderwaveSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 35;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SHOCKWAVE_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int count = getProjectileCount(spellLevel);
        double center = (count - 1) / 2.0;
        Vec3 direction = new Vec3(0, 0, 1).yRot(-entity.getYRot() * Mth.DEG_TO_RAD);

        Vec3 origin = entity.getEyePosition();
        float damage = getDamage(spellLevel, entity);

        for (int i = 0; i < count; i++) {
            float yawOffset = (float) ((i - center) * DEGREES_PER_PROJECTILE * Mth.DEG_TO_RAD);
            Vec3 forward = direction.yRot(yawOffset);
            ThunderwaveProjectile projectile = new ThunderwaveProjectile(level, entity);
            projectile.setPos(Utils.moveToRelativeGroundLevel(level, origin.add(forward.scale(2)), 4));
            projectile.shoot(forward);
            projectile.setDamage(damage);
            level.addFreshEntity(projectile);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static int getProjectileCount(int spellLevel) {
        return (spellLevel - 1) * 2 + 1;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.4f;
    }
}
