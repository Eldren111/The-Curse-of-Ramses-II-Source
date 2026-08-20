package com.eldren.wutao.ramesses_curse;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class NetworkEvent {
    private NetworkEvent() {
    }

    public static final class Context {
        private final IPayloadContext delegate;

        Context(IPayloadContext delegate) {
            this.delegate = delegate;
        }

        public void enqueueWork(Runnable work) {
            delegate.enqueueWork(work);
        }

        public ServerPlayer getSender() {
            return delegate.player() instanceof ServerPlayer player ? player : null;
        }

        public void setPacketHandled(boolean handled) {
            // Payload handlers are considered handled when this callback returns.
        }
    }
}
