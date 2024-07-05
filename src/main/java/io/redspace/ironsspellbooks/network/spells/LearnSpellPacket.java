package io.redspace.ironsspellbooks.network.ported.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class LearnSpellPacket implements CustomPacketPayload {
    private final byte hand;
    private final String spell;

    public static final CustomPacketPayload.Type<LearnSpellPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "learn_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LearnSpellPacket> STREAM_CODEC = CustomPacketPayload.codec(LearnSpellPacket::write, LearnSpellPacket::new);

    public LearnSpellPacket(InteractionHand interactionHand, String spell) {
        this.hand = handToByte(interactionHand);
        this.spell = spell;
    }

    public LearnSpellPacket(FriendlyByteBuf buf) {
        hand = buf.readByte();
        spell = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(hand);
        buf.writeUtf(spell);
    }

    public static void handle(LearnSpellPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack itemStack = serverPlayer.getItemInHand(byteToHand(packet.hand));
                AbstractSpell spell = SpellRegistry.getSpell(packet.spell);
                var data = MagicData.getPlayerMagicData(serverPlayer).getSyncedData();
                if (spell != SpellRegistry.none() && !data.isSpellLearned(spell) && itemStack.is(ItemRegistry.ELDRITCH_PAGE.get()) && itemStack.getCount() > 0) {
                    data.learnSpell(spell);
                    if (!serverPlayer.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                }
            }
        });
    }

    public static byte handToByte(InteractionHand hand) {
        return (byte) (hand == InteractionHand.MAIN_HAND ? 1 : 0);
    }

    public static InteractionHand byteToHand(byte b) {
        return b > 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
