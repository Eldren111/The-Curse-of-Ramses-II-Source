package com.eldren.wutao.ramesses_curse;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientAffixKnowledge {
    private static long learnedMask;

    private ClientAffixKnowledge() {
    }

    public static void update(long mask) {
        learnedMask = mask;
    }

    public static boolean hasLearned(String affixId) {
        int index = DynastyAffixData.KNOWN_AFFIXES.indexOf(affixId);
        return index >= 0 && (learnedMask & (1L << index)) != 0L;
    }
}

