package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RamessesCurseMod.MODID);

    public static final RegistryObject<SoundEvent> MUSIC_DISC_HYMN_OF_THE_TWO_LANDS =
            SOUND_EVENTS.register("music_disc.hymn_of_the_two_lands",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RamessesCurseMod.MODID, "music_disc.hymn_of_the_two_lands")));

    public static final RegistryObject<SoundEvent> APEP_BATTLE =
            SOUND_EVENTS.register("boss.apep_battle",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RamessesCurseMod.MODID, "boss.apep_battle")));

    private ModSounds() {
    }
}
