package com.example.testmod.setup;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.mana.network.PacketCastSpell;
import com.example.testmod.capabilities.mana.network.PacketSyncManaToClient;
import com.example.testmod.capabilities.scroll.network.PacketUseScroll;
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

        net.messageBuilder(PacketCastSpell.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketCastSpell::new)
                .encoder(PacketCastSpell::toBytes)
                .consumer(PacketCastSpell::handle)
                .add();

        net.messageBuilder(PacketUseScroll.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketUseScroll::new)
                .encoder(PacketUseScroll::toBytes)
                .consumer(PacketUseScroll::handle)
                .add();

        net.messageBuilder(PacketSyncManaToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncManaToClient::new)
                .encoder(PacketSyncManaToClient::toBytes)
                .consumer(PacketSyncManaToClient::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

}