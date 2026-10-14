package gr.larryngton.simpleculling.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.*;

public class EntityCullingHandler {
    private static final Set<Entity> cullingQueue = ConcurrentHashMap.newKeySet();
    private static final Map<Entity, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (mc.theWorld == null || mc.getRenderViewEntity() == null) {
            return;
        }

        for (Entity entity : cullingQueue) {
            scheduleCulling(entity);
        }
        cullingQueue.clear();
    }

    private static void scheduleCulling(Entity entity) {
        if (scheduledTasks.containsKey(entity) || entity.getUniqueID() == mc.thePlayer.getUniqueID()) {
            return;
        }

        long delay = (long) Math.min(200, mc.thePlayer.getDistanceToEntity(entity) * 2);
        ScheduledFuture<?> future = MainCullingHandler.CULLING_EXECUTOR.schedule(() -> {
            try {
                updateCulling(entity);
            } finally {
                scheduledTasks.remove(entity);
            }
        }, delay, TimeUnit.MILLISECONDS);
        scheduledTasks.put(entity, future);
    }

    private static void updateCulling(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        Vec3 delta = Utils.getMoveDeltaVector(entity);
        List<Vec3> hitboxVertices = Utils.getVertices(bb.addCoord(0.1, 0, 0.1).offset(delta.xCoord, delta.yCoord, delta.zCoord));
        hitboxVertices.addAll(Utils.getVertices(bb));

        Vec3 cam = MainCullingHandler.camPos;

        boolean visible = false;
        for (Vec3 vec : hitboxVertices) {
            if (bb.contract(0.005, 0.005, 0.005).calculateIntercept(cam, vec) != null) {
                continue;
            }

            MovingObjectPosition ray = mc.theWorld.rayTraceBlocks(cam, vec);
            if (ray == null || MainCullingHandler.canRayPassThrough(ray.getBlockPos())) {
                visible = true;
                break;
            }
        }

        ((ICullingEntity) entity).simpleculling$setShouldRender(visible);
    }

    public static boolean shouldRender(Entity entity) {
        if (entity == mc.thePlayer || entity instanceof EntityArmorStand) {
            return true;
        }

        cullingQueue.add(entity);
        return ((ICullingEntity) entity).simpleculling$shouldRender();
    }
}