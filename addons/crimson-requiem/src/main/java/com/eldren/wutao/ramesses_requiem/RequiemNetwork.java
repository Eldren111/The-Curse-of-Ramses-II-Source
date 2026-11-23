package com.eldren.wutao.ramesses_requiem;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class RequiemNetwork {
    private static final String VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RequiemAddon.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);
    private RequiemNetwork() {}
    public static void register() {
        CHANNEL.registerMessage(1, com.eldren.wutao.ramesses_requiem.boss.WusheScenePacket.class,
                com.eldren.wutao.ramesses_requiem.boss.WusheScenePacket::encode,
                com.eldren.wutao.ramesses_requiem.boss.WusheScenePacket::decode,
                com.eldren.wutao.ramesses_requiem.boss.WusheScenePacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(0, CrimsonRequiemAttackPacket.class, CrimsonRequiemAttackPacket::encode,
                CrimsonRequiemAttackPacket::decode, CrimsonRequiemAttackPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
