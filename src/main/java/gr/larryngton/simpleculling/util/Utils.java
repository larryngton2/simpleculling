package gr.larryngton.simpleculling.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.List;

public class Utils {
    public static Vec3 getMoveDeltaVector(Entity entity) {
        return new Vec3(entity.posX, entity.posY, entity.posZ).subtract(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
    }

    public static List<Vec3> getVertices(AxisAlignedBB bb) {
        return Arrays.asList(
                new Vec3(bb.minX, bb.minY, bb.minZ),
                new Vec3(bb.minX, bb.maxY, bb.minZ),
                new Vec3(bb.minX, bb.minY, bb.maxZ),
                new Vec3(bb.minX, bb.maxY, bb.maxZ),
                new Vec3(bb.maxX, bb.minY, bb.minZ),
                new Vec3(bb.maxX, bb.maxY, bb.minZ),
                new Vec3(bb.maxX, bb.minY, bb.maxZ),
                new Vec3(bb.maxX, bb.maxY, bb.maxZ)
        );
    }
}
