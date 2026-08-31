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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import org.lwjgl.glfw.GLFW;

@Mod(RamessesCurseMod.MODID)
public class RamessesCurseMod {

    public static final String MODID = "ramesses_curse";
    public static final String CURSE_SLOT_ID = "ramesses_curse_curse";
    public static final String HEART_SLOT_ID = "ramesses_curse_heart";
    public static final String MUSIC_SLOT_ID = "ramesses_curse_music";
    public static final String HAND_SLOT_ID = "ramesses_curse_hand";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = ModMobEffects.MOB_EFFECTS;
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = ModSounds.SOUND_EVENTS;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RAMESSES_CURSE_TAB =
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

    public RamessesCurseMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModNetwork::registerPayloads);
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

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(
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
            ModItems.ITEMS.getEntries().forEach(holder -> {
                Item item = holder.get();
                if (item instanceof ICurioItem curioItem) {
                    CuriosApi.registerCurio(item, curioItem);
                }
            });
            registerCuriosSlot(CURSE_SLOT_ID);
            registerCuriosSlot(HEART_SLOT_ID);
            registerCuriosSlot(MUSIC_SLOT_ID);
            registerCuriosSlot(HAND_SLOT_ID);
            registerCuriosSlot("necklace");
            registerCuriosSlot("charm");
            registerCuriosSlot(ModItems.EternalSilenceTotem.BODY_SLOT_ID);
            registerCuriosSlot(ModItems.TwilightCrystalRing.RING_SLOT_ID);
            registerCuriosSlot(InscriptionBagItem.SLOT_ID);
            ModNetwork.register();
        });
    }

    private static void registerCuriosSlot(String slotId) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
                new SlotTypeMessage.Builder(slotId)
                        .size(1)
                        .icon(ResourceLocation.fromNamespaceAndPath("curios", "slot/empty_curio_slot"))
                        .build());
    }

    @SubscribeEvent
    public void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(new IBrewingRecipe() {
            private final Ingredient ingredient = Ingredient.of(Items.ECHO_SHARD);

            @Override
            public boolean isInput(ItemStack input) {
                PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
                return input.is(Items.POTION) && contents != null && contents.is(Potions.AWKWARD);
            }

            @Override
            public boolean isIngredient(ItemStack stack) {
                return ingredient.test(stack);
            }

            @Override
            public ItemStack getOutput(ItemStack input, ItemStack ingredientStack) {
                return isInput(input) && isIngredient(ingredientStack)
                        ? new ItemStack(ModItems.DYNASTY_PRESSURE_POTION.get()) : ItemStack.EMPTY;
            }
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.INSCRIPTION_BAG.get(), InscriptionBagScreen::new);
            event.register(ModMenus.COMPOUND_BOW_AMMO.get(), CompoundBowAmmoScreen::new);
        }

        @SubscribeEvent
        public static void addEntityRenderLayers(EntityRenderersEvent.AddLayers event) {
            GoetyMonolithAuraLayer.register(event);
        }

        private static void registerBowPullProperties(Item bow) {
            ItemProperties.register(bow, ResourceLocation.parse("pulling"), (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(bow, ResourceLocation.parse("pull"), (stack, level, entity, seed) -> {
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
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> tintIndex == 0 ? 0xFF932423 : 0xFFFFFFFF,
                    ModItems.DYNASTY_PRESSURE_POTION.get());
            event.register((stack, tintIndex) -> LegacyItemData.has(stack)
                            && LegacyItemData.get(stack).getBoolean(ModItems.FISSION_WRAITH_HEART_RITUAL_ACTIVE)
                            ? 0xFF3A123F
                            : 0xFFFFFFFF,
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

