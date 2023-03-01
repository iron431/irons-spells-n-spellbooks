package com.example.testmod.network;

import com.example.testmod.player.ClientInputEvents;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//This packet is from QOL of spamming instant cast spells
public class ClientboundCastError {
    private final int messageId;
    private final int spellId;

    public ClientboundCastError(int messageId, int spellId) {

        this.messageId = messageId;
        this.spellId = spellId;
    }

    public ClientboundCastError(FriendlyByteBuf buf) {
        messageId = buf.readInt();
        spellId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(messageId);
        buf.writeInt(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            boolean wouldSpamInstantCaster = SpellType.getTypeFromValue(spellId).getCastType() == CastType.INSTANT && !ClientInputEvents.hasReleasedSinceCasting;
            if (!wouldSpamInstantCaster) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable(CastErrorMessages.getKey(messageId), SpellType.getTypeFromValue(spellId).getDisplayName()).withStyle(ChatFormatting.RED));
            }
        });
        return true;
    }

    public enum CastErrorMessages {
        MANA(0, "ui.testmod.cast_error_mana"),
        COOLDOWN(1, "ui.testmod.cast_error_cooldown");

        public final int id;
        public final String translationKey;

        CastErrorMessages(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        static String getKey(int id) {
            if (id < CastErrorMessages.values().length)
                return CastErrorMessages.values()[id].translationKey;
            else
                return "none";
        }
    }
}

