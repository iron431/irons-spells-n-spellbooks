package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundLearnSpell {
    private final byte hand;
    private final String spell;

    public ServerboundLearnSpell(byte interactionHand, String spell) {
        this.hand = interactionHand;
        this.spell = spell;
    }

    public ServerboundLearnSpell(FriendlyByteBuf buf) {
        hand = buf.readByte();
        spell = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(hand);
        buf.writeUtf(spell);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            ItemStack itemStack = serverPlayer.getItemInHand(hand > 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            AbstractSpell spell = SpellRegistry.getSpell(this.spell);
            if (spell != SpellRegistry.none() && itemStack.is(ItemRegistry.ELDRITCH_PAGE.get()) && itemStack.getCount() > 0) {
                MagicData.getPlayerMagicData(serverPlayer).getSyncedData().learnSpell(spell);
                if (!serverPlayer.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
        });
        return true;
    }
}
