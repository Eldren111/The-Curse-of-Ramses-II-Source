package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.UUID;

public class ModMobEffects {
    private static final UUID DROUGHT_ATTACK_MODIFIER = UUID.fromString("f5f43cf5-4e02-4c9c-8a2d-8f8b0d31e101");
    private static final UUID DROUGHT_ARMOR_MODIFIER = UUID.fromString("ccaf8268-ae3b-44ca-9c14-2c58a8a2f102");
    private static final String DROUGHT_IMMUNITY_UNTIL = "RamessesDroughtImmunityUntil";
    private static final UUID HIT_SLOW_ATTACK_SPEED_MODIFIER = UUID.fromString("ae23b569-8d4b-49f0-b35e-599bc09b9006");

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RamessesCurseMod.MODID);


    public static final RegistryObject<MobEffect> INVERSION =
            MOB_EFFECTS.register("inversion", InversionMobEffect::new);

    public static final RegistryObject<MobEffect> DROUGHT =
            MOB_EFFECTS.register("drought", DroughtMobEffect::new);
    public static final RegistryObject<MobEffect> HIT_SLOW =
            MOB_EFFECTS.register("hit_slow", HitSlowMobEffect::new);
    public static final RegistryObject<MobEffect> FLIGHT_FALL =
            MOB_EFFECTS.register("flight_fall", FlightFallMobEffect::new);
    public static final RegistryObject<MobEffect> PITCHFIRE_LAVA_WALK =
            MOB_EFFECTS.register("pitchfire_lava_walk", PitchfireLavaWalkMobEffect::new);
    public static final RegistryObject<MobEffect> BLOOD_FORFEIT =
            MOB_EFFECTS.register("blood_forfeit", BloodForfeitMobEffect::new);

    public static void tickDrought(Player player) {
        if (!ModDimensions.DUAT_DESERT.equals(player.level().dimension())) {
            player.removeEffect(DROUGHT.get());
            clearDroughtModifiers(player);
            return;
        }

        long gameTime = player.level().getGameTime();
        long immunityUntil = player.getPersistentData().getLong(DROUGHT_IMMUNITY_UNTIL);
        if (immunityUntil > gameTime) {
            player.removeEffect(DROUGHT.get());
            clearDroughtModifiers(player);
            return;
        }
        if (immunityUntil > 0L) {
            player.getPersistentData().remove(DROUGHT_IMMUNITY_UNTIL);
        }

        player.addEffect(new MobEffectInstance(DROUGHT.get(), 40, 0, false, false, true));
        applyDroughtModifier(player, Attributes.ATTACK_DAMAGE, DROUGHT_ATTACK_MODIFIER, -3.0D, AttributeModifier.Operation.ADDITION);
        applyDroughtModifier(player, Attributes.ARMOR, DROUGHT_ARMOR_MODIFIER, -3.0D, AttributeModifier.Operation.ADDITION);
    }

    public static void grantDroughtImmunity(Player player, int durationTicks) {
        player.getPersistentData().putLong(
                DROUGHT_IMMUNITY_UNTIL,
                player.level().getGameTime() + Math.max(0, durationTicks));
        player.removeEffect(DROUGHT.get());
        clearDroughtModifiers(player);
    }

    private static void applyDroughtModifier(Player player, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(id);
        if (existing != null) {
            if (Math.abs(existing.getAmount() - amount) < 1.0E-6D && existing.getOperation() == operation) {
                return;
            }
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, "ramesses_curse_drought", amount, operation));
    }

    private static void removeDroughtModifier(Player player, Attribute attribute, UUID id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static void clearDroughtModifiers(Player player) {
        removeDroughtModifier(player, Attributes.ATTACK_DAMAGE, DROUGHT_ATTACK_MODIFIER);
        removeDroughtModifier(player, Attributes.ARMOR, DROUGHT_ARMOR_MODIFIER);
    }

    public static class InversionMobEffect extends MobEffect {
        private static final ResourceLocation INVERSION_ICON = new ResourceLocation(RamessesCurseMod.MODID, "textures/mob_effect/inversion.png");
        private static final int ICON_SIZE = 16;
        private static final int ICON_OFFSET_X = 1;
        private static final int ICON_OFFSET_Y = 1;

        public InversionMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x6A2CA0);
        }

        @Override
        public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
            consumer.accept(new IClientMobEffectExtensions() {
                @Override
                public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
                    guiGraphics.blit(INVERSION_ICON, x + ICON_OFFSET_X, y + ICON_OFFSET_Y, blitOffset, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    return true;
                }

                @Override
                public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                    guiGraphics.blit(INVERSION_ICON, x + ICON_OFFSET_X, y + ICON_OFFSET_Y, (int) z, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    return true;
                }
            });
        }
    }

    public static class DroughtMobEffect extends MobEffect {
        private static final ResourceLocation DROUGHT_ICON = new ResourceLocation(
                RamessesCurseMod.MODID,
                "textures/mob_effect/drought.png");
        private static final int ICON_SIZE = 18;

        public DroughtMobEffect() {
            super(MobEffectCategory.HARMFUL, 0xC58A3A);
        }

        @Override
        public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
            consumer.accept(new IClientMobEffectExtensions() {
                @Override
                public boolean renderInventoryIcon(
                        MobEffectInstance instance,
                        EffectRenderingInventoryScreen<?> screen,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        int blitOffset) {
                    guiGraphics.blit(
                            DROUGHT_ICON,
                            x,
                            y,
                            blitOffset,
                            0.0F,
                            0.0F,
                            ICON_SIZE,
                            ICON_SIZE,
                            ICON_SIZE,
                            ICON_SIZE);
                    return true;
                }

                @Override
                public boolean renderGuiIcon(
                        MobEffectInstance instance,
                        Gui gui,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        float z,
                        float alpha) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                    guiGraphics.blit(
                            DROUGHT_ICON,
                            x,
                            y,
                            (int) z,
                            0.0F,
                            0.0F,
                            ICON_SIZE,
                            ICON_SIZE,
                            ICON_SIZE,
                            ICON_SIZE);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    return true;
                }
            });
        }
    }

    public static class HitSlowMobEffect extends MobEffect {
        private static final ResourceLocation HIT_SLOW_ICON = new ResourceLocation(
                RamessesCurseMod.MODID,
                "textures/mob_effect/hit_slow.png");
        private static final int ICON_SIZE = 18;

        public HitSlowMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x65708A);
            addAttributeModifier(
                    Attributes.ATTACK_SPEED,
                    HIT_SLOW_ATTACK_SPEED_MODIFIER.toString(),
                    -0.50D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        @Override
        public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
            return -Math.min(1.0D, Math.max(0.0D,
                    DynastyAffixData.number("hit_slow", 1, "attack_speed_reduction")));
        }

        @Override
        public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
            consumer.accept(new IClientMobEffectExtensions() {
                @Override
                public boolean renderInventoryIcon(
                        MobEffectInstance instance,
                        EffectRenderingInventoryScreen<?> screen,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        int blitOffset) {
                    guiGraphics.blit(HIT_SLOW_ICON, x, y, blitOffset, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    return true;
                }

                @Override
                public boolean renderGuiIcon(
                        MobEffectInstance instance,
                        Gui gui,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        float z,
                        float alpha) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                    guiGraphics.blit(HIT_SLOW_ICON, x, y, (int) z, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    return true;
                }
            });
        }
    }

    public static class FlightFallMobEffect extends MobEffect {
        private static final ResourceLocation FLIGHT_FALL_ICON = new ResourceLocation(
                RamessesCurseMod.MODID,
                "textures/mob_effect/flight_fall.png");
        private static final int ICON_SIZE = 18;

        public FlightFallMobEffect() {
            super(MobEffectCategory.HARMFUL, 0xC9952E);
        }

        @Override
        public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
            consumer.accept(new IClientMobEffectExtensions() {
                @Override
                public boolean renderInventoryIcon(
                        MobEffectInstance instance,
                        EffectRenderingInventoryScreen<?> screen,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        int blitOffset) {
                    guiGraphics.blit(FLIGHT_FALL_ICON, x, y, blitOffset, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    return true;
                }

                @Override
                public boolean renderGuiIcon(
                        MobEffectInstance instance,
                        Gui gui,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        float z,
                        float alpha) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                    guiGraphics.blit(FLIGHT_FALL_ICON, x, y, (int) z, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    return true;
                }
            });
        }
    }

    public static class PitchfireLavaWalkMobEffect extends MobEffect {
        public PitchfireLavaWalkMobEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xD65A18);
        }
    }

    public static class BloodForfeitMobEffect extends MobEffect {
        private static final ResourceLocation BLOOD_FORFEIT_ICON = new ResourceLocation(
                RamessesCurseMod.MODID,
                "textures/mob_effect/blood_forfeit.png");
        private static final int ICON_SIZE = 18;

        public BloodForfeitMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x7A1018);
        }

        @Override
        public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
            consumer.accept(new IClientMobEffectExtensions() {
                @Override
                public boolean renderInventoryIcon(
                        MobEffectInstance instance,
                        EffectRenderingInventoryScreen<?> screen,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        int blitOffset) {
                    guiGraphics.blit(BLOOD_FORFEIT_ICON, x, y, blitOffset, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    return true;
                }

                @Override
                public boolean renderGuiIcon(
                        MobEffectInstance instance,
                        Gui gui,
                        GuiGraphics guiGraphics,
                        int x,
                        int y,
                        float z,
                        float alpha) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                    guiGraphics.blit(BLOOD_FORFEIT_ICON, x, y, (int) z, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    return true;
                }
            });
        }
    }
}
