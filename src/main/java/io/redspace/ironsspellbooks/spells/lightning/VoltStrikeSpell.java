package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.ImpulseCastData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;


@AutoSpellConfig
public class VoltStrikeSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "volt_strike");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getDamage(spellLevel, caster)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public VoltStrikeSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
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
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.hasImpulse = true;
        float multiplier = (15 + getSpellPower(spellLevel, entity)) / 20f;

        Vec3 forward = entity.getLookAngle();

        //Create Dashing Movement Impulse
        var upwardness = forward.dot(new Vec3(0, 1, 0));
        var remap = 1 - (Math.max(0, upwardness) * 0.6f);
        var impulse = forward.scale(3 * multiplier).multiply(1, remap, 1);
        //Start Spin Attack
        if (entity.onGround()) {
            entity.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerPositionPacket(0.0, 1.1999999F, 0.0, 0, 0, RelativeMovement.ALL, serverPlayer.getId()));
            }
            impulse.add(0, 0.5, 0);
        }else{
            impulse.add(0, 0.25, 0);
        }
        entity.setDeltaMovement(new Vec3(
                Mth.lerp(.75f, entity.getDeltaMovement().x, impulse.x),
                Mth.lerp(.75f, entity.getDeltaMovement().y, impulse.y),
                Mth.lerp(.75f, entity.getDeltaMovement().z, impulse.z)
        ));
        entity.hurtMarked = true;

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.VOLT_STRIKE, 10, getDamage(spellLevel, entity), false, false, false));
        entity.invulnerableTime = 20;
        playerMagicData.getSyncedData().setSpinAttackType(SpinAttackType.LIGHTNING);
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getDamage(int spellLevel, LivingEntity caster) {
        return (int) (5 + getSpellPower(spellLevel, caster));
    }
}
