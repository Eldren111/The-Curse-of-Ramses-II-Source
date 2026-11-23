package com.eldren.explorerscompassdurability;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DurabilityConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue MAX_DURABILITY;
    private static final ForgeConfigSpec.IntValue DAMAGE_PER_USE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        MAX_DURABILITY = builder.comment("罗盘最大耐久。默认 240；必须大于 0。")
                .defineInRange("maxDurability", 240, 1, Integer.MAX_VALUE);
        DAMAGE_PER_USE = builder.comment("每次发起搜索消耗的耐久（包括搜索下一个）。默认 1；0 表示不消耗。",
                        "仅打开界面、取消搜索、冷却中被拒绝的搜索不会消耗。")
                .defineInRange("damagePerUse", 1, 0, Integer.MAX_VALUE);
        SPEC = builder.build();
    }

    private DurabilityConfig() {}

    public static int maxDurability() {
        return SPEC.isLoaded() ? MAX_DURABILITY.get() : 240;
    }

    public static int damagePerUse() {
        return SPEC.isLoaded() ? DAMAGE_PER_USE.get() : 1;
    }
}
