package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CurseSevenHintPacket {

    private final String text;
    private final String translatedText;
    private final String style;
    private final int durationTicks;
    private final boolean closeScreen;

    public CurseSevenHintPacket(String text, String style, int durationTicks) {
        this(text, "", style, durationTicks, false);
    }

    public CurseSevenHintPacket(String text, String style, int durationTicks, boolean closeScreen) {
        this(text, "", style, durationTicks, closeScreen);
    }

    public CurseSevenHintPacket(String text, String translatedText, String style, int durationTicks) {
        this(text, translatedText, style, durationTicks, false);
    }

    public CurseSevenHintPacket(String text, String translatedText, String style, int durationTicks, boolean closeScreen) {
        this.text = text;
        this.translatedText = translatedText;
        this.style = style;
        this.durationTicks = durationTicks;
        this.closeScreen = closeScreen;
    }

    public static void encode(CurseSevenHintPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.text);
        buffer.writeUtf(packet.translatedText);
        buffer.writeUtf(packet.style);
        buffer.writeVarInt(packet.durationTicks);
        buffer.writeBoolean(packet.closeScreen);
    }

    public static CurseSevenHintPacket decode(FriendlyByteBuf buffer) {
        return new CurseSevenHintPacket(buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(CurseSevenHintPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModEvents.ClientOverlayEvents.showCurseSevenHint(packet.text, packet.translatedText, packet.style, packet.durationTicks);
            if (packet.closeScreen) {
                ClientPacketHandlers.closeEmberRiddleScreen();
            }
        }));
        context.setPacketHandled(true);
    }
}
