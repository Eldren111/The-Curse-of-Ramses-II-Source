package com.eldren.explorerscompassdurability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WearTest {
    @Test void defaultCompassLasts240Charges() {
        int damage = 0;
        for (int use = 1; use <= 240; use++) {
            damage = Wear.afterUse(damage, 1, 240);
            assertEquals(use, damage);
            assertEquals(use == 240, damage >= 240);
        }
    }

    @Test void customCostClampsAtBreakThreshold() {
        assertEquals(7, Wear.afterUse(0, 7, 100));
        assertEquals(100, Wear.afterUse(98, 7, 100));
        assertEquals(1, Wear.afterUse(0, 1, 1));
    }

    @Test void hugeConfigCannotOverflowAndRestoreDurability() {
        assertEquals(Integer.MAX_VALUE, Wear.afterUse(100, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(240, Wear.afterUse(1, Integer.MAX_VALUE, 240));
    }

    @Test void lowerMaximumAndDisabledCost() {
        assertEquals(100, Wear.afterUse(200, 1, 100));
        assertEquals(17, Wear.afterUse(17, 0, 240));
    }
}
