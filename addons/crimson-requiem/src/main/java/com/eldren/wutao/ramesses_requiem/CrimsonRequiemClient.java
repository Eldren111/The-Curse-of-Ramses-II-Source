package com.eldren.wutao.ramesses_requiem;

import com.eldren.wutao.ramesses_curse.CurseNineSeal;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RequiemAddon.MODID, value = Dist.CLIENT)
public final class CrimsonRequiemClient {
    public static net.minecraft.world.entity.player.Player tooltipPlayer() {
        return Minecraft.getInstance().player;
    }

    private static int lastSkillTick = Integer.MIN_VALUE;
    private static boolean held() {
        var player = Minecraft.getInstance().player;
        return player != null && !CurseNineSeal.isSealed(player)
                && CrimsonRequiemActions.weapon(player).is(RequiemItems.CRIMSON_REQUIEM.get())
                && !CrimsonRequiemFlight.locked(CrimsonRequiemActions.weapon(player));
    }
    private static int lastUseTick = Integer.MIN_VALUE;
    public static boolean offhandUse() {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator() || CurseNineSeal.isSealed(player)
                || CrimsonRequiemActions.weaponHand(player) != net.minecraft.world.InteractionHand.OFF_HAND
                || !player.getOffhandItem().is(RequiemItems.CRIMSON_REQUIEM.get())) return false;
        if (!CrimsonRequiemFlight.locked(player.getOffhandItem())
                && (player.tickCount < lastUseTick || (long)player.tickCount - lastUseTick >= 4)) {
            lastUseTick = player.tickCount;
            RequiemNetwork.CHANNEL.sendToServer(new CrimsonRequiemAttackPacket(false, true));
            player.swing(net.minecraft.world.InteractionHand.OFF_HAND);
        }
        return true;
    }
    public static boolean shiftAttack() {
        var player = Minecraft.getInstance().player;
        if (player != null && CrimsonRequiemFlight.locked(player.getMainHandItem())) return true;
        if (!held() || !Minecraft.getInstance().player.isShiftKeyDown()) return false;
        int tick = Minecraft.getInstance().player.tickCount;
        if (tick < lastSkillTick || (long)tick - lastSkillTick >= 5) {
            lastSkillTick = tick;
            RequiemNetwork.CHANNEL.sendToServer(new CrimsonRequiemAttackPacket(true));
        }
        return true;
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && shiftAttack()) { event.setSwingHand(false); event.setCanceled(true); }
    }
    @SubscribeEvent
    public static void emptyAttack(PlayerInteractEvent.LeftClickEmpty event) {
        // Better Combat's Forge PlatformClient also posts this event on a missed swing.
        if (held()) RequiemNetwork.CHANNEL.sendToServer(new CrimsonRequiemAttackPacket(event.getEntity().isShiftKeyDown()));
    }

    @SubscribeEvent
    public static void hand(net.minecraftforge.client.event.RenderHandEvent event) {
        if (CrimsonRequiemFlight.locked(event.getItemStack())) event.setCanceled(true);
    }

    public static void emitSteamCross(CrimsonRequiemProjectile wave) {
        var minecraft = Minecraft.getInstance();
        // Don't emit directly in the camera on the first flight update.
        if (minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(wave.position()) < 1.5 * 1.5) return;
        var forward = wave.getDeltaMovement().normalize();
        if (forward.lengthSqr() < 1.0E-6) return;
        var reference = Math.abs(forward.y) > 0.95 ? new net.minecraft.world.phys.Vec3(0, 0, 1)
                : new net.minecraft.world.phys.Vec3(0, 1, 0);
        var right = forward.cross(reference).normalize();
        var up = right.cross(forward).normalize();
        for (int arm = -1; arm <= 1; arm += 2) {
            var diagonal = right.add(up.scale(arm)).normalize();
            for (int i = -3; i <= 3; i++) {
                if (arm == 1 && i == 0) continue;
                var point = wave.position().add(diagonal.scale(i * 0.12));
                var particle = minecraft.particleEngine.createParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        point.x, point.y, point.z, 0, 0.006, 0);
                if (particle != null) {
                    particle.scale(0.18F);
                    particle.setColor(0.88F, 0.93F, 0.96F);
                    particle.setLifetime(6);
                }
            }
        }
    }
}
