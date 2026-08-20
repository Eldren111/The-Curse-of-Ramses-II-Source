package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Consumer;

public class ModMobEffects {
    private static final ResourceLocation DROUGHT_ATTACK_MODIFIER = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "drought_attack");
    private static final ResourceLocation DROUGHT_ARMOR_MODIFIER = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "drought_armor");
    private static final String DROUGHT_IMMUNITY_UNTIL = "RamessesDroughtImmunityUntil";
    private static final ResourceLocation HIT_SLOW_ATTACK_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "hit_slow_attack_speed");

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, RamessesCurseMod.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> INVERSION =
            MOB_EFFECTS.register("inversion", InversionMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> DROUGHT =
            MOB_EFFECTS.register("drought", DroughtMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> HIT_SLOW =
            MOB_EFFECTS.register("hit_slow", HitSlowMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FLIGHT_FALL =
            MOB_EFFECTS.register("flight_fall", FlightFallMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> PITCHFIRE_LAVA_WALK =
            MOB_EFFECTS.register("pitchfire_lava_walk", PitchfireLavaWalkMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> BLOOD_FORFEIT =
            MOB_EFFECTS.register("blood_forfeit", BloodForfeitMobEffect::new);

    public static void tickDrought(Player player) {
        if (!ModDimensions.DUAT_DESERT.equals(player.level().dimension())) {
            player.removeEffect(DROUGHT);
            clearDroughtModifiers(player);
            return;
        }

        long gameTime = player.level().getGameTime();
        long immunityUntil = player.getPersistentData().getLong(DROUGHT_IMMUNITY_UNTIL);
        if (immunityUntil > gameTime) {
            player.removeEffect(DROUGHT);
            clearDroughtModifiers(player);
            return;
        }
        if (immunityUntil > 0L) {
            player.getPersistentData().remove(DROUGHT_IMMUNITY_UNTIL);
        }

        player.addEffect(new MobEffectInstance(DROUGHT, 40, 0, false, false, true));
        applyDroughtModifier(player, Attributes.ATTACK_DAMAGE, DROUGHT_ATTACK_MODIFIER, -3.0D, AttributeModifier.Operation.ADD_VALUE);
        applyDroughtModifier(player, Attributes.ARMOR, DROUGHT_ARMOR_MODIFIER, -3.0D, AttributeModifier.Operation.ADD_VALUE);
    }

    public static void grantDroughtImmunity(Player player, int durationTicks) {
        player.getPersistentData().putLong(
                DROUGHT_IMMUNITY_UNTIL,
                player.level().getGameTime() + Math.max(0, durationTicks));
        player.removeEffect(DROUGHT);
        clearDroughtModifiers(player);
    }

    private static void applyDroughtModifier(Player player, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(id);
        if (existing != null) {
            if (Math.abs(existing.amount() - amount) < 1.0E-6D && existing.operation() == operation) {
                return;
            }
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    private static void removeDroughtModifier(Player player, Holder<Attribute> attribute, ResourceLocation id) {
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
        private static final ResourceLocation INVERSION_ICON = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/mob_effect/inversion.png");
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
        private static final ResourceLocation DROUGHT_ICON = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/mob_effect/drought.png");
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
        private static final ResourceLocation HIT_SLOW_ICON = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/mob_effect/hit_slow.png");
        private static final int ICON_SIZE = 18;

        public HitSlowMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x65708A);
            addAttributeModifier(
                    Attributes.ATTACK_SPEED,
                    HIT_SLOW_ATTACK_SPEED_MODIFIER,
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
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
        private static final ResourceLocation FLIGHT_FALL_ICON = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/mob_effect/flight_fall.png");
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
        private static final ResourceLocation BLOOD_FORFEIT_ICON = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/mob_effect/blood_forfeit.png");
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

