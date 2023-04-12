package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class IceBlockSpell extends AbstractSpell {
    public IceBlockSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(caster), 1)));
    }

    public IceBlockSpell(int level) {
        super(SpellType.ICE_BLOCK_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
        this.castTime = 30;
        this.baseManaCost = 40;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ICE_BLOCK_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        HitResult raycast = Utils.raycastForEntity(level, entity, 40, true);
        Vec3 spawn;
        LivingEntity target = null;
        if (raycast.getType() == HitResult.Type.ENTITY) {
            spawn = ((EntityHitResult) raycast).getEntity().getEyePosition();
            if (((EntityHitResult) raycast).getEntity() instanceof LivingEntity livingEntity)
                target = livingEntity;
        } else {
            spawn = raycast.getLocation().subtract(entity.getForward());
            spawn = Utils.raycastForBlock(level, spawn.add(0, 2, 0), spawn.subtract(0, 2, 0), ClipContext.Fluid.NONE).getLocation().add(0, 1, 0);
        }
        IceBlockProjectile iceBlock = new IceBlockProjectile(level, entity, target);
        iceBlock.moveTo(spawn.add(0, 4, 0));
        iceBlock.setAirTime(target == null ? 20 : 50);
        iceBlock.setDamage(getDamage(entity));
        level.addFreshEntity(iceBlock);
        super.onCast(level, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return this.getSpellPower(entity);
    }
}
