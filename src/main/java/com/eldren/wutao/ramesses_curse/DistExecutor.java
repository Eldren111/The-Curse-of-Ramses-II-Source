package com.eldren.wutao.ramesses_curse;

import java.util.function.Supplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class DistExecutor {
    private DistExecutor() {
    }

    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> work) {
        if (FMLEnvironment.dist == dist) {
            work.get().run();
        }
    }
}
