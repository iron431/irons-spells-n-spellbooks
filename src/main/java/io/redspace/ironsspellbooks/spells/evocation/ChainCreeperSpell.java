package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.network.spell.ClientboundSyncTargetingData;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ChainCreeperSpell extends AbstractSpell {
    public ChainCreeperSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", getCount()));
    }

    public ChainCreeperSpell(int level) {
        super(SpellType.CHAIN_CREEPER_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 0;
        this.castTime = 30;
        this.baseManaCost = 40;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.CREEPER_PRIMED);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public boolean checkPreCastConditions(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        HitResult raycast = Utils.raycastForEntity(level, entity, 40, true);

        if (raycast.getType() == HitResult.Type.ENTITY && ((EntityHitResult) raycast).getEntity() instanceof LivingEntity target) {
            playerMagicData.setAdditionalCastData(new CastTargetingData(target));
            if (entity instanceof ServerPlayer serverPlayer)
                Messages.sendToPlayer(new ClientboundSyncTargetingData(target, getSpellType()), serverPlayer);
        }
        return true;

    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        HitResult raycast = Utils.raycastForEntity(level, entity, 40, true);
        Vec3 spawn;
        if (playerMagicData.getAdditionalCastData() instanceof CastTargetingData castTargetingData) {
            spawn = castTargetingData.getTarget((ServerLevel) level).getEyePosition();
        } else if (raycast.getType() == HitResult.Type.ENTITY) {
            spawn = ((EntityHitResult) raycast).getEntity().getEyePosition();
        } else {
            spawn = raycast.getLocation().subtract(entity.getForward());
            spawn = Utils.raycastForBlock(level, spawn.add(0, 2, 0), spawn.subtract(0, 2, 0), ClipContext.Fluid.NONE).getLocation().add(0, 1, 0);
        }
        summonCreeperRing(level, entity, spawn, getDamage(entity), getCount());

        super.onCast(level, entity, playerMagicData);
    }

    public static void summonCreeperRing(Level level, LivingEntity owner, Vec3 origin, float damage, int count) {
        int degreesPerCreeper = 360 / count;
        for (int i = 0; i < count; i++) {

            Vec3 motion = new Vec3(0, 0, .3 + count * .01f);
            motion = motion.xRot(75 * Mth.DEG_TO_RAD);
            motion = motion.yRot(degreesPerCreeper * i * Mth.DEG_TO_RAD);


            CreeperHeadProjectile head = new CreeperHeadProjectile(owner, level, motion, damage);
            head.setChainOnKill(true);

            Vec3 spawn = origin.add(motion.multiply(1, 0, 1).normalize().scale(.3f));
            var angle = Utils.rotationFromDirection(motion);

            head.moveTo(spawn.x, spawn.y - head.getBoundingBox().getYsize() / 2, spawn.z, angle.y, angle.x);
            level.addFreshEntity(head);
        }
    }

    private int getCount() {
        return 3 + level - 1;
    }

    private float getDamage(LivingEntity entity) {
        return this.getSpellPower(entity);
    }
}
