package com.example.testmod.setup;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.network.*;
import com.example.testmod.gui.network.PacketInscribeSpell;
import com.example.testmod.gui.network.PacketRemoveSpell;
import com.example.testmod.spells.network.PacketAddMotionToClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Messages {

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(TestMod.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

//        net.messageBuilder(PacketCastSpell.class, id(), NetworkDirection.PLAY_TO_CLIENT)
//                .decoder(PacketCastSpell::new)
//                .encoder(PacketCastSpell::toBytes)
//                .consumer(PacketCastSpell::handle)
//                .add();

        net.messageBuilder(PacketCastingState.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketCastingState::new)
                .encoder(PacketCastingState::toBytes)
                .consumer(PacketCastingState::handle)
                .add();

        net.messageBuilder(PacketAddMotionToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketAddMotionToClient::new)
                .encoder(PacketAddMotionToClient::toBytes)
                .consumer(PacketAddMotionToClient::handle)
                .add();

        net.messageBuilder(PacketCancelCast.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketCancelCast::new)
                .encoder(PacketCancelCast::toBytes)
                .consumer(PacketCancelCast::handle)
                .add();

        net.messageBuilder(PacketSyncManaToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncManaToClient::new)
                .encoder(PacketSyncManaToClient::toBytes)
                .consumer(PacketSyncManaToClient::handle)
                .add();

        net.messageBuilder(PacketInscribeSpell.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketInscribeSpell::new)
                .encoder(PacketInscribeSpell::toBytes)
                .consumer(PacketInscribeSpell::handle)
                .add();

//        net.messageBuilder(PacketGenerateScroll.class, id(), NetworkDirection.PLAY_TO_SERVER)
//                .decoder(PacketGenerateScroll::new)
//                .encoder(PacketGenerateScroll::toBytes)
//                .consumer(PacketGenerateScroll::handle)
//                .add();

        net.messageBuilder(PacketRemoveSpell.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketRemoveSpell::new)
                .encoder(PacketRemoveSpell::toBytes)
                .consumer(PacketRemoveSpell::handle)
                .add();

        net.messageBuilder(PacketSyncCooldownToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncCooldownToClient::new)
                .encoder(PacketSyncCooldownToClient::toBytes)
                .consumer(PacketSyncCooldownToClient::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

}