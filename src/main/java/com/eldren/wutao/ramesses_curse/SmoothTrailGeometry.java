package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class SmoothTrailGeometry {
    private SmoothTrailGeometry() {
    }

    public static List<Vec3> catmullRom(List<Vec3> controlPoints, int subdivisions) {
        if (controlPoints.size() < 3 || subdivisions <= 1) {
            return List.copyOf(controlPoints);
        }

        List<Vec3> result = new ArrayList<>((controlPoints.size() - 1) * subdivisions + 1);
        int last = controlPoints.size() - 1;
        for (int segment = 0; segment < last; segment++) {
            Vec3 p0 = controlPoints.get(Math.max(0, segment - 1));
            Vec3 p1 = controlPoints.get(segment);
            Vec3 p2 = controlPoints.get(segment + 1);
            Vec3 p3 = controlPoints.get(Math.min(last, segment + 2));
            for (int step = 0; step < subdivisions; step++) {
                result.add(sample(p0, p1, p2, p3, step / (double) subdivisions));
            }
        }
        result.add(controlPoints.get(last));
        return result;
    }

    private static Vec3 sample(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return new Vec3(
                component(p0.x, p1.x, p2.x, p3.x, t, t2, t3),
                component(p0.y, p1.y, p2.y, p3.y, t, t2, t3),
                component(p0.z, p1.z, p2.z, p3.z, t, t2, t3));
    }

    private static double component(double p0, double p1, double p2, double p3,
                                    double t, double t2, double t3) {
        return 0.5D * ((2.0D * p1)
                + (-p0 + p2) * t
                + (2.0D * p0 - 5.0D * p1 + 4.0D * p2 - p3) * t2
                + (-p0 + 3.0D * p1 - 3.0D * p2 + p3) * t3);
    }
}
