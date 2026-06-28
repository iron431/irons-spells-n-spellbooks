package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record SpinAttackType(ResourceLocation textureId, boolean fullbright) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SpinAttackType> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SpinAttackType::textureId,
            ByteBufCodecs.BOOL, SpinAttackType::fullbright,
            SpinAttackType::new
    );
    public static final SpinAttackType FIRE = new SpinAttackType(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/fire_riptide.png"), true);
    public static final SpinAttackType LIGHTNING = new SpinAttackType(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/lightning_riptide.png"), true);
    public static final SpinAttackType RIPTIDE = new SpinAttackType(ResourceLocation.withDefaultNamespace("textures/entity/trident_riptide.png"), false);
}
