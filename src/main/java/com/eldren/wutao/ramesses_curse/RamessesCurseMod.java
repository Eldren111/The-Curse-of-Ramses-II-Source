package com.eldren.wutao.ramesses_curse;

import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;
import top.theillusivec4.curios.api.SlotTypeMessage;
import org.lwjgl.glfw.GLFW;

@Mod(RamessesCurseMod.MODID)
public class RamessesCurseMod {

    public static final String MODID = "ramesses_curse";
    public static final String CURSE_SLOT_ID = "ramesses_curse_curse";
    public static final String HEART_SLOT_ID = "ramesses_curse_heart";
    public static final String MUSIC_SLOT_ID = "ramesses_curse_music";
    public static final String HAND_SLOT_ID = "ramesses_curse_hand";
    public static final KeyMapping HEART_AWAKENING_KEY = new KeyMapping(
            "key.ramesses_curse.heart_awakening",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.ramesses_curse");
    public static final KeyMapping HEART_WARD_KEY = new KeyMapping(
            "key.ramesses_curse.heart_ward",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.ramesses_curse");
    public static final KeyMapping EDICT_OF_TWO_LANDS_KEY = new KeyMapping(
            "key.ramesses_curse.edict_of_two_lands",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.ramesses_curse");
    public static final KeyMapping EDICT_OF_TWO_LANDS_DOMAIN_KEY = new KeyMapping(
            "key.ramesses_curse.edict_of_two_lands_domain",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "key.categories.ramesses_curse");
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = ModEnchantments.ENCHANTMENTS;
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = ModMobEffects.MOB_EFFECTS;
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = ModSounds.SOUND_EVENTS;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> RAMESSES_CURSE_TAB =
            CREATIVE_MODE_TABS.register("epic_curse_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Ramesses II's Curse").withStyle(ChatFormatting.DARK_PURPLE))
                    .icon(() -> ModItems.SEAL_OF_KADESH.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SEAL_OF_KADESH.get());
                        output.accept(ModItems.RAMASSES_MANUAL.get());
                        output.accept(ModItems.PURIFICATION_SCROLL.get());
                        output.accept(ModItems.DYNASTY_PRESSURE_POTION.get());
                        output.accept(ModItems.SATIETY_CRYSTAL.get());
                        output.accept(ModItems.EMBER_OF_DUAT.get());
                        output.accept(ModItems.HEART_OF_DUAT.get());
                        output.accept(ModItems.HYMN_OF_THE_TWO_LANDS.get());
                        output.accept(ModItems.RESONANT_SCARAB.get());
                        output.accept(ModItems.URAEUS_NECKLACE.get());
                        output.accept(ModItems.EDICT_OF_TWO_LANDS.get());
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CORROSION_WARD.get(), 1)));
                    })
                    .build());

    public RamessesCurseMod(FMLJavaModLoadingContext context) {
        GeckoLib.initialize();

        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        registerOptionalL2LibraryCompat(modEventBus);

        ITEMS.register(modEventBus);
        ENCHANTMENTS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void registerOptionalL2LibraryCompat(IEventBus modEventBus) {
        if (!ModList.get().isLoaded("l2library")) {
            return;
        }
        try {
            Class.forName("com.eldren.wutao.ramesses_curse.integration.l2.L2LibraryCompat")
                    .getMethod("register", IEventBus.class)
                    .invoke(null, modEventBus);
            LOGGER.info("L2 Library compatibility enabled");
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("Failed to initialize L2 Library compatibility", exception);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerCuriosSlot(CURSE_SLOT_ID);
            registerCuriosSlot(HEART_SLOT_ID);
            registerCuriosSlot(MUSIC_SLOT_ID);
            registerCuriosSlot(HAND_SLOT_ID);
            registerCuriosSlot("necklace");
            registerBrewingRecipes();
            ModNetwork.register();
        });
    }

    private static void registerBrewingRecipes() {
        BrewingRecipeRegistry.addRecipe(new IBrewingRecipe() {
            private final Ingredient ingredient = Ingredient.of(Items.ECHO_SHARD);

            @Override
            public boolean isInput(ItemStack input) {
                return input.is(Items.POTION) && PotionUtils.getPotion(input) == Potions.AWKWARD;
            }

            @Override
            public boolean isIngredient(ItemStack ingredient) {
                return this.ingredient.test(ingredient);
            }

            @Override
            public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                if (!isInput(input) || !isIngredient(ingredient)) {
                    return ItemStack.EMPTY;
                }
                return new ItemStack(ModItems.DYNASTY_PRESSURE_POTION.get());
            }
        });
    }

    private static void registerCuriosSlot(String slotId) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
                new SlotTypeMessage.Builder(slotId)
                        .size(1)
                        .icon(new ResourceLocation("curios", "slot/empty_curio_slot"))
                        .build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            event.enqueueWork(() -> {
                EntityRenderers.register(ModEntities.URAEUS_FLARE.get(), UraeusFlareRenderer::new);
                EntityRenderers.register(ModEntities.URAEUS_RAIN_SHARD.get(), UraeusRainShardRenderer::new);
                EntityRenderers.register(ModEntities.URAEUS_STORM_STAR.get(), UraeusStormStarRenderer::new);
                EntityRenderers.register(ModEntities.SATIETY_CRYSTAL.get(), SatietyCrystalRenderer::new);
            });
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(HEART_AWAKENING_KEY);
            event.register(HEART_WARD_KEY);
            event.register(EDICT_OF_TWO_LANDS_KEY);
            event.register(EDICT_OF_TWO_LANDS_DOMAIN_KEY);
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> tintIndex == 0 ? 0x932423 : 0xFFFFFFFF,
                    ModItems.DYNASTY_PRESSURE_POTION.get());
        }
    }
}
