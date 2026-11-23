package com.eldren.wutao.ramesses_requiem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** World-space, depth-tested geometry only: no framebuffer copies or screen-wide shader state. */
public final class CrimsonRiftExecutionRenderer extends EntityRenderer<CrimsonRiftExecution> {
    private static final ResourceLocation RIFT = new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "textures/effect/crimson_rift.png");
    private static final ResourceLocation SLASH = new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "textures/effect/crimson_rift_slash.png");
    private final ItemRenderer items;

    public CrimsonRiftExecutionRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
    }

    @Override public boolean shouldRender(CrimsonRiftExecution cast, Frustum frustum, double x, double y, double z) {
        // The giant blade extends well beyond the small controller's bounding box.
        return cast.distanceToSqr(x, y, z) < 128 * 128;
    }

    @Override public void render(CrimsonRiftExecution cast, float yaw, float partial, PoseStack pose,
            MultiBufferSource buffers, int light) {
        float age = cast.visualAge(partial);
        var target = cast.visualTarget();
        Vec3 center = cast.getPosition(partial);
        if (target != null && target.isAlive()) center = target.getPosition(partial).add(0, target.getBbHeight() * 0.5, 0);
        Vec3 correction = center.subtract(cast.getPosition(partial));
        pose.pushPose();
        pose.translate(correction.x, correction.y, correction.z);
        if (age < CrimsonRiftExecution.FINAL_TICK) {
            for (int i = 0; i < CrimsonRiftExecution.BLADES; i++) {
                float localAge = age - CrimsonRiftExecution.launchTick(i);
                if (localAge < -8) continue;
                Vec3 origin = cast.portalOffset(i);
                Vec3 destination = cast.bodyOffset(i);
                Vec3 direction = destination.subtract(origin).normalize();
                float flight = CrimsonRiftExecution.flightTicks(i);
                if (localAge < flight + 7) {
                    float opening = smooth((localAge + 8) / 8F);
                    float closing = smooth((flight + 7 - localAge) / 7F);
                    pose.pushPose();
                    pose.translate(origin.x, origin.y, origin.z);
                    faceNormal(pose, direction);
                    // Tear open as a thin slit, then unfold a rotating corona at different depths.
                    pose.mulPose(Axis.ZP.rotationDegrees(i * 53));
                    float width = 0.08F + 0.82F * opening * opening;
                    texturedQuad(pose, buffers, RIFT, width, 1.5F * opening, closing);
                    for (int ring = 0; ring < 3; ring++) {
                        pose.pushPose();
                        pose.translate(0, 0, 0.025 + ring * 0.035);
                        pose.mulPose(Axis.ZP.rotationDegrees(age * (ring == 1 ? -7 : 5) + ring * 120));
                        texturedQuad(pose, buffers, SLASH, width * (1.2F + ring * 0.12F),
                                opening * (1.65F + ring * 0.1F), closing * (0.65F - ring * 0.12F));
                        pose.popPose();
                    }
                    pose.popPose();
                }
                if (localAge < 0) continue;
                float progress = Mth.clamp(localAge / flight, 0, 1);
                float eased = progress * progress * (1.1F - 0.1F * progress);
                Vec3 pathEnd = destination.subtract(direction.scale(0.35));
                Vec3 pos = origin.subtract(direction.scale(0.65)).lerp(pathEnd, eased);
                // Leave the blade's hilt outside the body after insertion.
                if (progress >= 1) pos = destination.subtract(direction.scale(0.35));
                pose.pushPose();
                pose.translate(pos.x, pos.y, pos.z);
                orientBlade(pose, direction, center.add(pos), 0);
                pose.scale(1.35F, 1.35F, 1.35F);
                renderSword(cast, pose, buffers);
                pose.popPose();
                if (progress < 1 && localAge > 2) {
                    ribbon(pose, buffers, center, pos, pos.subtract(direction.scale(0.8 + progress * 1.8)),
                            0.12F + progress * 0.12F, 0.75F);
                }
                float hitAge = localAge - flight;
                if (hitAge >= 0 && hitAge < 6) {
                    pose.pushPose();
                    pose.translate(destination.x, destination.y, destination.z);
                    faceNormal(pose, entityRenderDispatcher.camera.getPosition().subtract(center.add(destination)));
                    pose.mulPose(Axis.ZP.rotationDegrees(i * 73 + hitAge * 11));
                    texturedQuad(pose, buffers, SLASH, 0.4F + hitAge * 0.2F, 0.4F + hitAge * 0.2F, 1 - hitAge / 6);
                    pose.popPose();
                }
            }
        }
        if (age >= CrimsonRiftExecution.GIANT_START && age < CrimsonRiftExecution.FINAL_TICK) {
            float local = age - CrimsonRiftExecution.GIANT_START;
            float charge = smooth(local / CrimsonRiftExecution.GIANT_CHARGE_TICKS);
            float dive = Mth.clamp((local - CrimsonRiftExecution.GIANT_CHARGE_TICKS) / CrimsonRiftExecution.GIANT_DIVE_TICKS, 0, 1);
            double y = 15.5 - 13 * Math.pow(dive, 1.55);
            pose.pushPose();
            pose.translate(0, 16, 0);
            faceNormal(pose, new Vec3(0, -1, 0));
            pose.mulPose(Axis.ZP.rotationDegrees(local * 12));
            texturedQuad(pose, buffers, RIFT, 3.2F * charge, 3.2F * charge, 1 - dive);
            for (int ring = 0; ring < 3; ring++) {
                pose.translate(0, 0, 0.04);
                pose.mulPose(Axis.ZP.rotationDegrees(local * 3 + ring * 120));
                texturedQuad(pose, buffers, SLASH, (3.6F + ring * 0.3F) * charge,
                        (3.6F + ring * 0.3F) * charge, (1 - dive) * 0.7F);
            }
            pose.popPose();
            pose.pushPose();
            pose.translate(0, y, 0);
            // Rotate around a stable world axis so camera motion cannot make the blade wobble.
            pose.mulPose(Axis.YP.rotationDegrees(local * 58 + local * local * 0.65F));
            pose.mulPose(Axis.ZP.rotationDegrees(180));
            pose.scale(5.2F * charge, 8F * charge, 5.2F * charge);
            pose.mulPose(Axis.ZP.rotationDegrees(45));
            renderSword(cast, pose, buffers);
            pose.popPose();
            if (dive > 0) {
                ribbon(pose, buffers, center, new Vec3(0, y + 1, 0), new Vec3(0, 17, 0), 0.7F, 0.65F);
            }
            for (int arc = 0; arc < 5; arc++) {
                pose.pushPose();
                pose.translate(0, y - 1.5 + arc * 0.85, 0);
                pose.mulPose(Axis.YP.rotationDegrees(local * 67 + arc * 72));
                pose.mulPose(Axis.XP.rotationDegrees(78 + arc * 4));
                texturedQuad(pose, buffers, SLASH, 2.4F + arc * 0.35F, 1.4F + arc * 0.2F, 0.7F * charge);
                pose.popPose();
            }
        }

        if (age >= CrimsonRiftExecution.FINAL_TICK) {
            float t = Mth.clamp((age - CrimsonRiftExecution.FINAL_TICK) / 14F, 0, 1);
            pose.pushPose();
            for (int ring = 0; ring < 3; ring++) {
                pose.pushPose();
                pose.mulPose(Axis.XP.rotationDegrees(90 - ring * 25));
                pose.mulPose(Axis.ZP.rotationDegrees(age * (7 + ring * 4)));
                float size = 0.8F + (float)Math.sqrt(t) * (6 - ring);
                texturedQuad(pose, buffers, SLASH, size, size, (1 - t) * (1 - ring * 0.2F));
                pose.popPose();
            }
            pose.popPose();
        }
        pose.popPose();
        super.render(cast, yaw, partial, pose, buffers, light);
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private void ribbon(PoseStack pose, MultiBufferSource buffers, Vec3 center, Vec3 head, Vec3 tail,
            float width, float alpha) {
        Vec3 along = head.subtract(tail);
        if (along.lengthSqr() < 0.001) return;
        pose.pushPose();
        Vec3 midpoint = head.add(tail).scale(0.5);
        pose.translate(midpoint.x, midpoint.y, midpoint.z);
        orientBlade(pose, along, center.add(midpoint), 0);
        pose.mulPose(Axis.ZP.rotationDegrees(-45));
        texturedQuad(pose, buffers, SLASH, width, (float)along.length() * 0.5F, alpha);
        pose.popPose();
    }

    private void orientBlade(PoseStack pose, Vec3 direction, Vec3 position, float spin) {
        Vector3f forward = direction.normalize().toVector3f();
        Vector3f view = entityRenderDispatcher.camera.getPosition().subtract(position).toVector3f();
        Vector3f normal = new Vector3f(view).sub(new Vector3f(forward).mul(view.dot(forward)));
        if (normal.lengthSquared() < 1.0E-6F) {
            normal.set(Math.abs(forward.y) < 0.9F ? new Vector3f(0, 1, 0) : new Vector3f(0, 0, 1));
            normal.sub(new Vector3f(forward).mul(normal.dot(forward)));
        }
        normal.normalize();
        Vector3f right = new Vector3f(forward).cross(normal).normalize();
        pose.mulPose(new Quaternionf().setFromNormalized(new Matrix3f().setColumn(0, right).setColumn(1, forward).setColumn(2, normal)));
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.mulPose(Axis.ZP.rotationDegrees(45));
    }

    private static void faceNormal(PoseStack pose, Vec3 normal) {
        if (normal.lengthSqr() > 1.0E-6) pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 0, 1), normal.normalize().toVector3f()));
    }

    private void renderSword(CrimsonRiftExecution cast, PoseStack pose, MultiBufferSource buffers) {
        items.renderStatic(cast.weapon(), ItemDisplayContext.NONE, 15728880, OverlayTexture.NO_OVERLAY, pose, buffers, cast.level(), cast.getId());
    }

    private static void texturedQuad(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
            float width, float height, float alpha) {
        var vertices = buffers.getBuffer(RenderType.entityTranslucentEmissive(texture));
        var matrix = pose.last().pose();
        var normal = pose.last().normal();
        vertices.vertex(matrix, -width, -height, 0).color(1F, 1F, 1F, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
        vertices.vertex(matrix, width, -height, 0).color(1F, 1F, 1F, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
        vertices.vertex(matrix, width, height, 0).color(1F, 1F, 1F, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
        vertices.vertex(matrix, -width, height, 0).color(1F, 1F, 1F, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
    }

    @Override public ResourceLocation getTextureLocation(CrimsonRiftExecution cast) { return TextureAtlas.LOCATION_BLOCKS; }
}
