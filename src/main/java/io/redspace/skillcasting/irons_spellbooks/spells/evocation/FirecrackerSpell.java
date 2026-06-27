package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FirecrackerSpell extends AbstractSpellSkill {

    private static final int[] DYE_COLORS = {
            11546150,
            6192150,
            3949738,
            8991416,
            1481884,
            15961002,
            8439583,
            16701501,
            3847130,
            13061821,
            16351261,
            16383998
    };

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(1.5)
            .build();

    public FirecrackerSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
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
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float spellPower = getSpellPower(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, spellPower);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, (float) (15 + (int) (spellPower * 2)));
        castContext.set(SkillcastingComponentTypes.RANDOM_SEED, castContext.level().random.nextInt(Integer.MAX_VALUE));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 direction = castContext.direction().normalize();
        Vec3 spawn = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 15f))
                .checkForBlocks(true)
                .build()
                .getLocation()
                .subtract(direction.scale(.25f));

        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        Entity owner = castContext.asEntityCaster();
        var damageSource = getDamageSource(level, null, owner);

        dealFirecrackerDamage(level, spawn, damage, damageSource);
    }

    @Override
    public void onClientCastComplete(CastContext castContext, CastEndReason castEndReason) {
        super.onClientCastComplete(castContext, castEndReason);
        Vec3 direction = castContext.direction();
        Vec3 spawn = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 15f))
                .checkForBlocks(true)
                .build()
                .getLocation()
                .subtract(direction.scale(.25f));
        RandomSource random = RandomSource.create(castContext.getOrDefault(SkillcastingComponentTypes.RANDOM_SEED, castContext.level().random.nextInt(Integer.MAX_VALUE)));
        Fireworks fireworks = randomFireworkRocket(random);
        castContext.level().createFireworks(spawn.x, spawn.y, spawn.z, direction.x, direction.y, direction.z, fireworks.explosions());
    }

    private static void dealFirecrackerDamage(Level level, Vec3 hitPos, float damage, SpellSkillDamageSource damageSource) {
        double explosionRadius = 2;
        AABB area = new AABB(
                hitPos.subtract(explosionRadius, explosionRadius, explosionRadius),
                hitPos.add(explosionRadius, explosionRadius, explosionRadius));
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living.isAlive() && living.isPickable() && Utils.hasLineOfSight(level, hitPos, living.getBoundingBox().getCenter(), true)) {
                DamageSources.applyDamage(living, damage, damageSource);
            }
        }
    }

    private static Fireworks randomFireworkRocket(RandomSource randomSource) {
        byte type = (byte) (randomSource.nextInt(3) * 2);
        if (randomSource.nextFloat() < .08f) {
            type = 3;
        }
        FireworkExplosion.Shape shape = FireworkExplosion.Shape.values()[type];
        return new Fireworks(-1, List.of(new FireworkExplosion(
                shape,
                new IntImmutableList(randomColors(randomSource)),
                new IntImmutableList(new int[0]),
                randomSource.nextInt(3) == 0,
                randomSource.nextInt(3) == 0)));
    }

    private static int[] randomColors(RandomSource randomSource) {
        int[] colors = new int[3];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = DYE_COLORS[randomSource.nextInt(DYE_COLORS.length)];
        }
        return colors;
    }
}
