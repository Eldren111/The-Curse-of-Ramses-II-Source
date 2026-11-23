package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class VoidGatewayRitualEvents {
    private static final int EMBER_COST = 15;
    private static final int DRAGON_BREATH_COST = 5;
    private static final int CRYSTAL_COST = 3;
    private static final int RITUAL_DURATION = 80;
    private static final AABB FOUNTAIN_AREA = new AABB(-12.0D, 36.0D, -12.0D, 13.0D, 108.0D, 13.0D);
    private static final DustParticleOptions CRIMSON_DUST =
            new DustParticleOptions(new Vector3f(0.95F, 0.04F, 0.18F), 1.45F);
    private static final DustParticleOptions GOLD_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.58F, 0.05F), 1.25F);
    private static final Map<ServerLevel, ActivationRitual> ACTIVE_RITUALS = new WeakHashMap<>();

    private VoidGatewayRitualEvents() {
    }

    @SubscribeEvent
    public static void onOfferingTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || !Level.END.equals(itemEntity.level().dimension())
                || !Level.OVERWORLD.equals(event.getDimension())
                || !FOUNTAIN_AREA.contains(itemEntity.position())
                || !isRitualOffering(itemEntity.getItem())) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.level instanceof ServerLevel level)
                || !Level.END.equals(level.dimension())) {
            return;
        }

        ActivationRitual active = ACTIVE_RITUALS.get(level);
        if (active != null) {
            if (active.tick(level)) {
                ACTIVE_RITUALS.remove(level);
            }
            return;
        }
        if (level.getGameTime() % 10L != 0L) {
            return;
        }

        List<ItemEntity> offerings = level.getEntitiesOfClass(ItemEntity.class, FOUNTAIN_AREA,
                entity -> entity.isAlive() && entity.tickCount > 10);
        if (!hasCost(offerings, ModItems.EMBER_OF_DUAT.get(), EMBER_COST)
                || !hasCost(offerings, Items.DRAGON_BREATH, DRAGON_BREATH_COST)
                || !hasCost(offerings, ModItems.HEKA_RADIANT_CRYSTAL.get(), CRYSTAL_COST)) {
            return;
        }

        EndDragonFight fight = level.getDragonFight();
        if (fight == null || !fight.hasPreviouslyKilledDragon()) {
            notifyNearby(level, "message.ramesses_curse.void_gateway.requires_dragon");
            return;
        }

        ServerPlayer crownBearer = level.getPlayers(player -> FOUNTAIN_AREA.inflate(8.0D).contains(player.position())
                && player.isAlive() && !player.isSpectator()
                && DuatSunEaterCrownItem.hasEquippedEmpoweredCrown(player)).stream().findFirst().orElse(null);
        if (crownBearer == null) {
            notifyNearby(level, "message.ramesses_curse.void_gateway.requires_crown");
            return;
        }

        List<BlockPos> endPortalBlocks = findCentralPortalBlocks(level);
        if (endPortalBlocks.isEmpty()) {
            return;
        }

        consume(offerings, ModItems.EMBER_OF_DUAT.get(), EMBER_COST);
        consume(offerings, Items.DRAGON_BREATH, DRAGON_BREATH_COST);
        consume(offerings, ModItems.HEKA_RADIANT_CRYSTAL.get(), CRYSTAL_COST);
        ACTIVE_RITUALS.put(level, new ActivationRitual(endPortalBlocks, crownBearer.getUUID()));
        level.playSound(null, crownBearer.blockPosition(), SoundEvents.WITHER_SPAWN,
                SoundSource.BLOCKS, 0.75F, 0.52F);
        level.playSound(null, crownBearer.blockPosition(), SoundEvents.PORTAL_TRIGGER,
                SoundSource.BLOCKS, 1.4F, 0.58F);
    }

    private static List<BlockPos> findCentralPortalBlocks(ServerLevel level) {
        List<BlockPos> result = new ArrayList<>();
        for (int y = 40; y <= 100; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                        result.add(pos.immutable());
                    }
                }
            }
        }
        return result;
    }

    private static boolean hasCost(List<ItemEntity> entities, Item item, int cost) {
        return entities.stream().filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount()).sum() >= cost;
    }

    private static boolean isRitualOffering(ItemStack stack) {
        return stack.is(ModItems.EMBER_OF_DUAT.get())
                || stack.is(Items.DRAGON_BREATH)
                || stack.is(ModItems.HEKA_RADIANT_CRYSTAL.get());
    }

    private static void consume(List<ItemEntity> entities, Item item, int amount) {
        int remaining = amount;
        for (ItemEntity entity : entities) {
            if (remaining <= 0 || !entity.isAlive() || !entity.getItem().is(item)) {
                continue;
            }
            ItemStack stack = entity.getItem();
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (stack.isEmpty()) {
                entity.discard();
            } else {
                entity.setItem(stack);
            }
        }
    }

    private static void notifyNearby(ServerLevel level, String translationKey) {
        if (level.getGameTime() % 40L != 0L) {
            return;
        }
        for (ServerPlayer player : level.getPlayers(player -> FOUNTAIN_AREA.inflate(8.0D).contains(player.position()))) {
            player.displayClientMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED), true);
        }
    }

    private static final class ActivationRitual {
        private final List<BlockPos> portalBlocks;
        private final UUID crownBearer;
        private final double centerX;
        private final double centerY;
        private final double centerZ;
        private int age;

        private ActivationRitual(List<BlockPos> portalBlocks, UUID crownBearer) {
            this.portalBlocks = List.copyOf(portalBlocks);
            this.crownBearer = crownBearer;
            this.centerX = portalBlocks.stream().mapToInt(BlockPos::getX).average().orElse(0.0D) + 0.5D;
            this.centerY = portalBlocks.stream().mapToInt(BlockPos::getY).average().orElse(64.0D) + 0.75D;
            this.centerZ = portalBlocks.stream().mapToInt(BlockPos::getZ).average().orElse(0.0D) + 0.5D;
        }

        private boolean tick(ServerLevel level) {
            this.age++;
            double progress = this.age / (double) RITUAL_DURATION;
            if (this.age % 3 == 0) {
                emitSpiral(level, progress);
            }
            if (this.age == 18 || this.age == 38 || this.age == 58) {
                emitVisualLightning(level, this.age / 20);
                emitShockwave(level, 5.0D + this.age * 0.055D);
            }
            if (this.age % 16 == 0) {
                level.playSound(null, BlockPos.containing(this.centerX, this.centerY, this.centerZ),
                        SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS,
                        1.25F, 0.45F + this.age / 150.0F);
            }
            if (this.age < RITUAL_DURATION) {
                return false;
            }
            complete(level);
            return true;
        }

        private void emitSpiral(ServerLevel level, double progress) {
            double radius = 10.0D * (1.0D - progress) + 1.2D;
            for (int index = 0; index < 18; index++) {
                double angle = index * Math.PI * 2.0D / 18.0D + this.age * 0.19D;
                double x = this.centerX + Math.cos(angle) * radius;
                double z = this.centerZ + Math.sin(angle) * radius;
                double y = this.centerY + 0.4D + progress * 6.5D
                        + Math.sin(angle * 2.0D + this.age * 0.12D) * 1.4D;
                level.sendParticles(index % 2 == 0 ? CRIMSON_DUST : GOLD_DUST,
                        x, y, z, 1, 0.04D, 0.08D, 0.04D, 0.02D);
                level.sendParticles(ParticleTypes.DRAGON_BREATH,
                        x, y, z, 1, 0.05D, 0.12D, 0.05D, 0.015D);
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.centerX, this.centerY + progress * 4.0D, this.centerZ,
                    18, radius * 0.22D, 0.45D, radius * 0.22D, 0.13D);
        }

        private void emitVisualLightning(ServerLevel level, int phase) {
            for (int index = 0; index < 4; index++) {
                double angle = index * Math.PI * 0.5D + phase * 0.45D;
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.setVisualOnly(true);
                    bolt.moveTo(this.centerX + Math.cos(angle) * 6.5D,
                            this.centerY, this.centerZ + Math.sin(angle) * 6.5D);
                    level.addFreshEntity(bolt);
                }
            }
        }

        private void emitShockwave(ServerLevel level, double radius) {
            for (int index = 0; index < 56; index++) {
                double angle = index * Math.PI * 2.0D / 56.0D;
                double x = this.centerX + Math.cos(angle) * radius;
                double z = this.centerZ + Math.sin(angle) * radius;
                level.sendParticles(index % 3 == 0 ? ParticleTypes.END_ROD : CRIMSON_DUST,
                        x, this.centerY + 0.35D, z, 1,
                        0.02D, 0.02D, 0.02D, 0.01D);
            }
            level.sendParticles(ParticleTypes.SONIC_BOOM,
                    this.centerX, this.centerY + 1.0D, this.centerZ,
                    2, 0.15D, 0.1D, 0.15D, 0.0D);
        }

        private void complete(ServerLevel level) {
            for (BlockPos pos : this.portalBlocks) {
                if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                    level.setBlock(pos, ModBlocks.VOID_GATEWAY_PORTAL.get().defaultBlockState()
                            .setValue(NetherPortalBlock.AXIS, Direction.Axis.X), 18);
                }
            }
            VoidGatewaySavedData.activate(level);
            level.sendParticles(ParticleTypes.FLASH, this.centerX, this.centerY + 1.0D, this.centerZ,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.centerX, this.centerY + 0.5D, this.centerZ,
                    2, 1.0D, 0.25D, 1.0D, 0.0D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.centerX, this.centerY + 1.0D, this.centerZ,
                    220, 5.2D, 2.2D, 5.2D, 0.28D);
            level.sendParticles(ParticleTypes.END_ROD,
                    this.centerX, this.centerY + 1.0D, this.centerZ,
                    110, 4.2D, 1.4D, 4.2D, 0.18D);
            emitShockwave(level, 11.5D);
            BlockPos center = BlockPos.containing(this.centerX, this.centerY, this.centerZ);
            level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.BLOCKS, 2.0F, 0.62F);
            level.playSound(null, center, SoundEvents.WARDEN_SONIC_BOOM,
                    SoundSource.BLOCKS, 1.45F, 0.7F);
            for (ServerPlayer player : level.getPlayers(candidate -> FOUNTAIN_AREA.inflate(32.0D)
                    .contains(candidate.position()))) {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.void_gateway.opened",
                        VoidGatewaySavedData.MAX_ENTRIES).withStyle(ChatFormatting.DARK_PURPLE), false);
            }
            ServerPlayer bearer = level.getServer().getPlayerList().getPlayer(this.crownBearer);
            if (bearer != null) {
                bearer.displayClientMessage(Component.translatable(
                        "message.ramesses_curse.void_gateway.remaining",
                        VoidGatewaySavedData.MAX_ENTRIES), true);
            }
        }
    }
}
