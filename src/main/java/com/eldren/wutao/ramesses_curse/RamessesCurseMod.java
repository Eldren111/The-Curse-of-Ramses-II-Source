package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.dynamictext.config.DynamicTextClientConfig;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = ModMobEffects.MOB_EFFECTS;
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = ModSounds.SOUND_EVENTS;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> RAMESSES_CURSE_TAB =
            CREATIVE_MODE_TABS.register("epic_curse_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Ramesses II's Curse"))
                    .icon(() -> ModItems.SEAL_OF_KADESH.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SEAL_OF_KADESH.get());
                        output.accept(ModItems.RAMASSES_MANUAL.get());
                        output.accept(ModItems.PURIFICATION_SCROLL.get());
                        output.accept(ModItems.DYNASTY_PRESSURE_POTION.get());
                        output.accept(ModItems.SATIETY_CRYSTAL.get());
                        output.accept(ModItems.HEKA_RADIANT_CRYSTAL.get());
                        output.accept(ModItems.SUN_EATER_BEACON.get());
                        output.accept(ModItems.DUAT_SUN_EATER_CROWN.get());
                        output.accept(ModItems.EMBER_OF_DUAT.get());
                        output.accept(ModItems.ETERNAL_SILENCE_GRANULE.get());
                        output.accept(ModItems.ETERNAL_SILENCE_INGOT.get());
                        output.accept(ModItems.FRACTURED_PHASE_ORE.get());
                        output.accept(ModItems.NETHER_FRACTURED_PHASE_ORE.get());
                        output.accept(ModItems.END_FRACTURED_PHASE_ORE.get());
                        output.accept(ModItems.FRACTURED_PHASE_ORE_CHUNK.get());
                        output.accept(ModItems.FRACTURED_PHASE_INGOT.get());
                        output.accept(ModItems.ERODED_LAW_INGOT.get());
                        output.accept(ModItems.BOUND_PHASE_INGOT.get());
                        output.accept(ModItems.ORDERED_PHASE_INGOT.get());
                        output.accept(ModItems.CONSTANT_LAW_INGOT.get());
                        output.accept(ModItems.ETERNAL_CONSTANT_INGOT.get());
                        output.accept(ModItems.STAR_LAW_DIVINE_INGOT.get());
                        output.accept(ModItems.DRACONIC_AUTHORITY_INGOT.get());
                        output.accept(ModItems.CALMING_TIBIA.get());
                        output.accept(ModItems.PITCHFIRE_CRYSTAL.get());
                        output.accept(ModItems.RAW_GROUP_U.get());
                        output.accept(ModItems.COOKED_GROUP_U.get());
                        output.accept(ModItems.HEART_DEVOURING_REMNANT.get());
                        output.accept(ModItems.RAVENOUS_CLAW_FRAGMENT.get());
                        output.accept(ModItems.FAMINE_CORE.get());
                        output.accept(ModItems.VENOM_ORB.get());
                        output.accept(ModItems.KA_REMNANT_BREATH.get());
                        output.accept(ModItems.LIFE_BINDING_REMNANT_CRYSTAL.get());
                        output.accept(ModItems.AUTHORITY_FRAGMENT.get());
                        output.accept(ModItems.TRUTH.get());
                        output.accept(ModItems.REVERSE_SCALE.get());
                        output.accept(ModItems.FISSION_WRAITH_HEART.get());
                        output.accept(ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get());
                        output.accept(ModItems.ARMOR_PIERCING_ARROW.get());
                        output.accept(ModItems.CHANCE_DIE_SIX_SIDED.get());
                        output.accept(ModItems.CHANCE_DIE_LARGE.get());
                        output.accept(ModItems.ETERNAL_SILENCE_TOTEM.get());
                        output.accept(ModItems.ETERNAL_SILENCE_REBIRTH_TOTEM.get());
                        output.accept(ModItems.TWILIGHT_CRYSTAL_RING.get());
                        output.accept(ModItems.SILENT_SEPULCHER_WITHERED_VINE.get());
                        output.accept(ModItems.ETERNAL_SILENCE_RITUAL_STAFF.get());
                        output.accept(ModItems.ETERNAL_SILENCE_UPGRADE_SMITHING_TEMPLATE.get());
                        output.accept(ModItems.HEART_OF_DUAT.get());
                        output.accept(ModItems.HYMN_OF_THE_TWO_LANDS.get());
                        output.accept(ModItems.RESONANT_SCARAB.get());
                        output.accept(ModItems.URAEUS_NECKLACE.get());
                        output.accept(ModItems.HUSHED_DEPTHS_AMULET.get());
                        output.accept(ModItems.ETERNAL_CONQUEST.get());
                        output.accept(ModItems.RUIN_TALISMAN.get());
                        output.accept(ModItems.EDICT_OF_TWO_LANDS.get());
                        output.accept(ModItems.DUAT_DUNGEON_TILE.get());
                        output.accept(ModItems.DUAT_DUNGEON_TILE_WALL.get());
                        output.accept(ModItems.DUAT_DUNGEON_TILE_SLAB.get());
                        output.accept(ModItems.DUAT_DUNGEON_TILE_STAIRS.get());
                        output.accept(ModItems.DUAT_DUNGEON_PILLAR.get());
                        output.accept(ModItems.SOLAR_RITE_BRICK.get());
                        output.accept(ModItems.DATE_PALM_LOG.get());
                        output.accept(ModItems.DATE_PALM_LEAVES.get());
                        output.accept(ModItems.DATE_PALM_SAPLING.get());
                        output.accept(ModItems.DATE_PALM_FRUIT.get());
                        output.accept(ModItems.INSCRIPTION_BAG.get());
                        ModItems.INSCRIPTION_FRAGMENTS.forEach(fragment -> output.accept(fragment.get()));
                        ModItems.AFFIX_KNOWLEDGE_BOOKS.values().forEach(book -> output.accept(book.get()));
                    })
                    .build());

    public RamessesCurseMod(FMLJavaModLoadingContext context) {
        GeckoLib.initialize();

        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        registerOptionalL2LibraryCompat(modEventBus);

        ITEMS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModStructures.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        context.registerConfig(
                ModConfig.Type.CLIENT,
                DynamicTextClientConfig.SPEC,
                "ramesses_curse-dynamic-text-client.toml"
        );
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
            registerCuriosSlot("charm");
            registerCuriosSlot(ModItems.EternalSilenceTotem.BODY_SLOT_ID);
            registerCuriosSlot(ModItems.TwilightCrystalRing.RING_SLOT_ID);
            registerCuriosSlot(InscriptionBagItem.SLOT_ID);
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
        public static final KeyMapping COMPOUND_BOW_AMMO_KEY = new KeyMapping(
                "key.ramesses_curse.compound_bow_ammo",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.ramesses_curse");
        public static final KeyMapping COMPOUND_BOW_MODE_KEY = new KeyMapping(
                "key.ramesses_curse.compound_bow_mode",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key.categories.ramesses_curse");

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            event.enqueueWork(() -> {
                registerOptionalL2Tabs();
                MenuScreens.register(ModMenus.INSCRIPTION_BAG.get(), InscriptionBagScreen::new);
                MenuScreens.register(ModMenus.COMPOUND_BOW_AMMO.get(), CompoundBowAmmoScreen::new);
                EntityRenderers.register(ModEntities.URAEUS_FLARE.get(), UraeusFlareRenderer::new);
                EntityRenderers.register(ModEntities.URAEUS_RAIN_SHARD.get(), UraeusRainShardRenderer::new);
                EntityRenderers.register(ModEntities.URAEUS_STORM_STAR.get(), UraeusStormStarRenderer::new);
                EntityRenderers.register(ModEntities.SATIETY_CRYSTAL.get(), SatietyCrystalRenderer::new);
                EntityRenderers.register(ModEntities.NEBUKA_GUARDIAN.get(), NebukaGuardianRenderer::new);
                EntityRenderers.register(ModEntities.NEBUKA_SUMMONING_RITUAL.get(), NebukaSummoningRitualRenderer::new);
                EntityRenderers.register(ModEntities.INSCRIPTION_FRAGMENT.get(), net.minecraft.client.renderer.entity.ItemFrameRenderer::new);
                EntityRenderers.register(ModEntities.DRAGON_BREATH_ARROW.get(), DragonBreathArrowRenderer::new);
                EntityRenderers.register(ModEntities.FISSION_HEART_DROP.get(), FissionHeartDropRenderer::new);
                EntityRenderers.register(ModEntities.DUAT_SUN_EATER_CROWN.get(), net.minecraft.client.renderer.entity.ItemEntityRenderer::new);
                EntityRenderers.register(ModEntities.RAMESSES_END_CRYSTAL.get(), RamessesEndCrystalRenderer::new);
                EntityRenderers.register(ModEntities.DUAT_SUN_EATER.get(), DuatSunEaterRenderer::new);
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.DUAT_PORTAL.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.SILENT_SEPULCHER_WITHERED_VINE.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.DATE_PALM_LEAVES.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.DATE_PALM_SAPLING.get(), RenderType.cutout());
                registerBowPullProperties(ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get());
                registerBowPullProperties(ModItems.DRAGON_BREATH_COMPOUND_BOW.get());
            });
        }

        private static void registerOptionalL2Tabs() {
            if (!ModList.get().isLoaded("l2library") || !ModList.get().isLoaded("l2tabs")) {
                return;
            }
            try {
                Class.forName("com.eldren.wutao.ramesses_curse.integration.l2.L2LibraryClientCompat")
                        .getMethod("registerTab")
                        .invoke(null);
            } catch (ReflectiveOperationException exception) {
                LOGGER.warn("Failed to register the Ramesses Curse L2 tab", exception);
            }
        }

        private static void registerBowPullProperties(Item bow) {
            ItemProperties.register(bow, new ResourceLocation("pulling"), (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(bow, new ResourceLocation("pull"), (stack, level, entity, seed) -> {
                return ModItems.EternalSilenceCompoundBow.getDrawProgress(stack, entity);
            });
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(ClientModEvents.HEART_AWAKENING_KEY);
            event.register(ClientModEvents.HEART_WARD_KEY);
            event.register(ClientModEvents.EDICT_OF_TWO_LANDS_KEY);
            event.register(ClientModEvents.EDICT_OF_TWO_LANDS_DOMAIN_KEY);
            event.register(ClientModEvents.COMPOUND_BOW_AMMO_KEY);
            event.register(ClientModEvents.COMPOUND_BOW_MODE_KEY);
        }

        @SubscribeEvent
        public static void addEntityRenderLayers(EntityRenderersEvent.AddLayers event) {
            GoetyMonolithAuraLayer.register(event);
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> tintIndex == 0 ? 0x932423 : 0xFFFFFFFF,
                    ModItems.DYNASTY_PRESSURE_POTION.get());
            event.register((stack, tintIndex) -> stack.hasTag()
                            && stack.getTag().getBoolean(ModItems.FISSION_WRAITH_HEART_RITUAL_ACTIVE)
                            ? 0x3A123F
                            : 0xFFFFFF,
                    ModItems.FISSION_WRAITH_HEART.get());
        }

        @SubscribeEvent
        public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
            event.register(ModItems.CALMING_TIBIA.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.PITCHFIRE_CRYSTAL.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.HEART_DEVOURING_REMNANT.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.RAVENOUS_CLAW_FRAGMENT.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.FAMINE_CORE.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.VENOM_ORB.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.KA_REMNANT_BREATH.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.LIFE_BINDING_REMNANT_CRYSTAL.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.AUTHORITY_FRAGMENT.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.TRUTH.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(ModItems.REVERSE_SCALE.get(), DynastyAffixMaterialDecorator.INSTANCE);
            event.register(
                    ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get(),
                    DynastyAffixMaterialDecorator.DEEP_PURPLE);
        }
    }
}
