package com.eldren.wutao.ramesses_requiem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public final class CrimsonSwordStormRenderer extends EntityRenderer<CrimsonSwordStorm> {
    private final ItemRenderer items;
    public CrimsonSwordStormRenderer(EntityRendererProvider.Context context) { super(context); items = context.getItemRenderer(); }

    @Override public boolean shouldRender(CrimsonSwordStorm sword, Frustum frustum, double x, double y, double z) {
        return sword.isController() || super.shouldRender(sword, frustum, x, y, z);
    }

    @Override public void render(CrimsonSwordStorm sword, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        if (sword.isController()) {
            var owner = sword.visualOwner();
            if (owner != null) {
                var offset = owner.getPosition(partial).subtract(sword.getPosition(partial));
                pose.translate(offset.x, offset.y, offset.z);
            }
            for (int i = 0; i < sword.remaining(); i++) {
                pose.pushPose();
                double angle = Math.toRadians((sword.tickCount + partial) * 22F + i * 60F);
                double x = Math.cos(angle) * 2.2, z = -Math.sin(angle) * 2.2;
                pose.translate(x, 0.85, z);
                var center = owner != null ? owner.getPosition(partial) : sword.getPosition(partial);
                faceViewer(pose, center.add(x, 0.85, z));
                pose.mulPose(Axis.ZP.rotationDegrees(-135));
                renderItem(sword, pose, buffer);
                pose.popPose();
            }
        } else {
            pointAtTarget(pose, sword, partial);
            renderItem(sword, pose, buffer);
        }
        pose.popPose();
        super.render(sword, yaw, partial, pose, buffer, light);
    }

    private void pointAtTarget(PoseStack pose, CrimsonSwordStorm sword, float partial) {
        var motion = sword.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-6) motion = new net.minecraft.world.phys.Vec3(0, -1, 0);
        var forward = motion.normalize().toVector3f();
        var view = entityRenderDispatcher.camera.getPosition().subtract(sword.getPosition(partial)).toVector3f();
        var normal = new org.joml.Vector3f(view).sub(new org.joml.Vector3f(forward).mul(view.dot(forward)));
        if (normal.lengthSquared() < 1.0E-6F) {
            normal.set(Math.abs(forward.y) < 0.9F ? new org.joml.Vector3f(0, 1, 0) : new org.joml.Vector3f(0, 0, 1));
            normal.sub(new org.joml.Vector3f(forward).mul(normal.dot(forward)));
        }
        normal.normalize();
        var right = new org.joml.Vector3f(forward).cross(normal).normalize();
        var basis = new org.joml.Matrix3f().setColumn(0, right).setColumn(1, forward).setColumn(2, normal);
        pose.mulPose(new org.joml.Quaternionf().setFromNormalized(basis));
        // Upper-right texture tip -> local +Y -> direction of travel, broad face toward viewer.
        pose.mulPose(Axis.ZP.rotationDegrees(45));
    }

    private void faceViewer(PoseStack pose, net.minecraft.world.phys.Vec3 position) {
        var direction = entityRenderDispatcher.camera.getPosition().subtract(position);
        // Yaw only: show the broad face to the viewer while the tip remains vertically downward.
        pose.mulPose(Axis.YP.rotationDegrees((float)Math.toDegrees(Math.atan2(direction.x, direction.z))));
    }

    private void renderItem(CrimsonSwordStorm sword, PoseStack pose, MultiBufferSource buffer) {
        pose.scale(1.25F, 1.25F, 1.25F);
        // NONE avoids inherited item-frame transforms; the texture's upper-right tip rotates to -Y.
        items.renderStatic(sword.weapon(), ItemDisplayContext.NONE, 15728880, OverlayTexture.NO_OVERLAY,
                pose, buffer, sword.level(), sword.getId());
    }

    @Override public ResourceLocation getTextureLocation(CrimsonSwordStorm sword) { return TextureAtlas.LOCATION_BLOCKS; }
}
