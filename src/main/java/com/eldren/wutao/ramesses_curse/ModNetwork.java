package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RamessesCurseMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;
    private static boolean registered = false;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.registerMessage(
                packetId++,
                GuiltSyncPacket.class,
                GuiltSyncPacket::encode,
                GuiltSyncPacket::decode,
                GuiltSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartAwakeningPacket.class,
                HeartAwakeningPacket::encode,
                HeartAwakeningPacket::decode,
                HeartAwakeningPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartWardTriggeredPacket.class,
                HeartWardTriggeredPacket::encode,
                HeartWardTriggeredPacket::decode,
                HeartWardTriggeredPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartWardActivatePacket.class,
                HeartWardActivatePacket::encode,
                HeartWardActivatePacket::decode,
                HeartWardActivatePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartStateSyncPacket.class,
                HeartStateSyncPacket::encode,
                HeartStateSyncPacket::decode,
                HeartStateSyncPacket::handle);
    }
}
