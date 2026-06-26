package io.redspace.skillcasting.irons_spellbooks.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class IceBlockSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public IceBlockSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 14;
        this.spellPowerPerLevel = 2;
        this.castTime = 30;
        this.baseManaCost = 40;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public Optional<PlayableSound> getCastChannelSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.ICE_BLOCK_CAST).toOpt();
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 48f).intValue(), 0.35f, false);
        return true;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 spawn = null;
        LivingEntity target = null;
        int spawnheight = 4;
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData != null) {
            target = targetData.getFirstLivingEntityTarget((ServerLevel) level);
            if (target != null) {
                spawn = target.position();
                spawnheight += (int) (target.getBbHeight() * 0.5f);
            }
        }
        if (spawn == null) {
            HitResult raycast = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 48f))
                    .bbInflation(0.25f)
                    .checkForBlocks(true)
                    .build();
            if (raycast.getType() == HitResult.Type.ENTITY) {
                spawn = ((EntityHitResult) raycast).getEntity().position();
                if (((EntityHitResult) raycast).getEntity() instanceof LivingEntity livingEntity)
                    target = livingEntity;
            } else {
                spawn = raycast.getLocation().subtract(castContext.direction());
            }
        }

        IceBlockProjectile iceBlock = new IceBlockProjectile(level, castContext.asEntityCaster(), target);
        iceBlock.moveTo(raiseWithCollision(spawn, spawnheight, level));
        if (!level.noBlockCollision(iceBlock, iceBlock.getBoundingBox())) {
            iceBlock.noPhysics = true;
        }
        iceBlock.setAirTime(target == null ? 25 : 35);
        iceBlock.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        level.addFreshEntity(iceBlock);
    }

    private Vec3 raiseWithCollision(Vec3 start, int blocks, Level level) {
        for (int i = 0; i < blocks; i++) {
            Vec3 raised = start.add(0, 1, 0);
            if (level.getBlockState(BlockPos.containing(raised)).isAir())
                start = raised;
            else
                break;
        }
        return start;
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setFreezeTicks(100).setIFrames(0);
    }
}
