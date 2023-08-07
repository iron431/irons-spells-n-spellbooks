package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.player.ClientInputEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundCastErrorMessage {
    public enum ErrorType {
        COOLDOWN,
        MANA
    }

    private final ErrorType errorType;
    private final String spellId;

    public ClientboundCastErrorMessage(ErrorType errorType, AbstractSpell spell) {
        this.spellId = spell.getSpellId();
        this.errorType = errorType;
    }

    public ClientboundCastErrorMessage(FriendlyByteBuf buf) {
        errorType = buf.readEnum(ErrorType.class);
        spellId = buf.readUtf();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(errorType);
        buf.writeUtf(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var spell = SpellRegistry.getSpell(spellId);
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
