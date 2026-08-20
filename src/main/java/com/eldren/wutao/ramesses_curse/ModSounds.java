package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, RamessesCurseMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HYMN_OF_THE_TWO_LANDS =
            SOUND_EVENTS.register("music_disc.hymn_of_the_two_lands",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "music_disc.hymn_of_the_two_lands")));

    private ModSounds() {
    }
}

