package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.player.ClientInputEvents;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundCastErrorMessage {
    public enum ErrorType {
        COOLDOWN,
        MANA
    }

    private final ErrorType errorType;
    private final int spellType;

    public ClientboundCastErrorMessage(ErrorType errorType, SpellType spellType) {
        this.spellType = spellType.getValue();
        this.errorType = errorType;
    }

    public ClientboundCastErrorMessage(FriendlyByteBuf buf) {
        errorType = buf.readEnum(ErrorType.class);
        spellType = buf.readInt();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(errorType);
        buf.writeInt(spellType);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            SpellType spell = SpellType.getTypeFromValue(spellType);
            if (errorType == ErrorType.COOLDOWN) {
                //ignore cooldown message if we are simply holding right click.
                if (ClientInputEvents.hasReleasedSinceCasting)
                    Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("ui.irons_spellbooks.cast_error_cooldown", spell.getDisplayName()).withStyle(ChatFormatting.RED), false);
            } else {
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("ui.irons_spellbooks.cast_error_mana", spell.getDisplayName()).withStyle(ChatFormatting.RED), false);
            }
        });

        return true;
    }
}
