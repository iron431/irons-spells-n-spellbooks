package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammer;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SpectralHammerSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "spectral_hammer");

    private static final int distance = 16;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.dimensions", 1 + getRadius(spellLevel, caster) * 2, 1 + getRadius(spellLevel, caster) * 2, getDepth(spellLevel, caster) + 1),
                Component.translatable("ui.irons_spellbooks.distance", distance)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(2)
            .build();

    public SpectralHammerSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 15;
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
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if(entity instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE){
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_adventure").withStyle(ChatFormatting.RED)));
            return false;
        }
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, distance);
        boolean success = blockHitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(blockHitResult.getBlockPos()).is(ModTags.SPECTRAL_HAMMER_MINEABLE);
        if (!success && entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_spectral_hammer").withStyle(ChatFormatting.RED)));
        }
        return success;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var blockPosition = Utils.getTargetBlock(world, entity, ClipContext.Fluid.NONE, distance);
        var face = blockPosition.getDirection();

        int radius = getRadius(spellLevel, entity);
        int depth = getDepth(spellLevel, entity);

        var spectralHammer = new SpectralHammer(world, entity, blockPosition, depth, radius);
        Vec3 position = Vec3.atCenterOf(blockPosition.getBlockPos());

        if (!face.getAxis().isVertical()) {
            position = position.subtract(0, 2, 0).subtract(entity.getForward().normalize().scale(1.5));
        } else if (face == Direction.DOWN) {
            position = position.subtract(0, 3, 0);
        }

        spectralHammer.setPos(position.x, position.y, position.z);
        world.addFreshEntity(spectralHammer);
        //IronsSpellbooks.LOGGER.debug("SpectralHammerSpell.onCast pos:{}", position);
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getDepth(int spellLevel, LivingEntity caster) {
        return (int) getSpellPower(spellLevel, caster);
    }

    private int getRadius(int spellLevel, LivingEntity caster) {
        return (int) Math.max(getSpellPower(spellLevel, caster) * .5f, 1);
    }
}
