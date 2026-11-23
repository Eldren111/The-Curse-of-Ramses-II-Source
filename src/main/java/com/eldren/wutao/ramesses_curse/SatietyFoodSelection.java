package com.eldren.wutao.ramesses_curse;

import java.util.Set;

/** Item IDs define a food type; stack size and NBT variants never add another type. */
final class SatietyFoodSelection {
    static final int REQUIRED = 3;

    private SatietyFoodSelection() {}

    static boolean record(Set<String> eaten, String food) {
        return food != null && !food.isBlank() && eaten.size() < REQUIRED && eaten.add(food);
    }

    static boolean complete(Set<String> eaten) {
        return eaten.size() >= REQUIRED;
    }
}
