package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.client.SkillcastingDebugOverlay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record DebugHudTogglePacket() implements CustomPacketPayload {

    public static final Type<DebugHudTogglePacket> TYPE = new Type<>(Skillcasting.id("debug_hud_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DebugHudTogglePacket> STREAM_CODEC =
            StreamCodec.unit(new DebugHudTogglePacket());

    public static void handle(DebugHudTogglePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            SkillcastingDebugOverlay.toggleVisible();
            if (context.player() != null) {
                boolean visible = SkillcastingDebugOverlay.isVisible();
                context.player().displayClientMessage(
                        Component.literal("Skillcasting debug HUD: " + (visible ? "on" : "off")),
                        true);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
