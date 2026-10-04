package gr.larryngton.simpleculling.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.*;

public class CullingHandler {
    private static final ScheduledExecutorService CULLING_EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private static final Set<Entity> cullingQueue = ConcurrentHashMap.newKeySet();
    private static final Map<Entity, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private static Vec3 camPos = new Vec3(0, 0, 0);
    public static double camDist = 4; //todo properly update from entityrenderer like on demise
    public static double camDist1 = 4;
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (mc.theWorld == null || mc.getRenderViewEntity() == null) {
            return;
        }

        RenderManager renderManager = mc.getRenderManager();
        double x = renderManager.viewerPosX;
        double y = renderManager.viewerPosY + mc.getRenderViewEntity().getEyeHeight();
        double z = renderManager.viewerPosZ;

        if (mc.gameSettings.thirdPersonView == 0) {
            camPos = new Vec3(x, y, z);
        } else {
            float yaw = mc.getRenderViewEntity().rotationYaw;
            float pitch = mc.getRenderViewEntity().rotationPitch;

            if (mc.gameSettings.thirdPersonView == 2) {
                pitch += 180.0F;
            }

            float yawRad = yaw * (float) Math.PI / 180.0F;
            float pitchRad = pitch * (float) Math.PI / 180.0F;

            double d4 = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * camDist;
            double d5 = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * camDist;
            double d6 = -MathHelper.sin(pitchRad) * camDist;

            camPos = new Vec3(
                    x - d4 / camDist * camDist1,
                    y - d6 / camDist * camDist1,
                    z - d5 / camDist * camDist1
            );
        }
        camPos = camPos.add(getMoveDeltaVector(mc.getRenderViewEntity()));

        for (Entity entity : cullingQueue) {
            scheduleCulling(entity);
        }

        cullingQueue.clear();
    }

    private static Vec3 getMoveDeltaVector(Entity entity) {
        return new Vec3(entity.posX, entity.posY, entity.posZ).subtract(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
    }

    private static void scheduleCulling(Entity entity) {
        if (scheduledTasks.containsKey(entity) || entity.getUniqueID() == mc.thePlayer.getUniqueID()) {
            return;
        }

        long delay = (long) Math.min(500, mc.thePlayer.getDistanceToEntity(entity) * 10);
        ScheduledFuture<?> future = CULLING_EXECUTOR.schedule(() -> {
            try {
                updateCulling(entity);
            } finally {
                scheduledTasks.remove(entity);
            }
        }, delay, TimeUnit.MILLISECONDS);

        scheduledTasks.put(entity, future);
    }

    private static void updateCulling(Entity entity) {
        Vec3 delta = getMoveDeltaVector(entity);
        List<Vec3> hitboxVertices = getVertices(entity.getEntityBoundingBox().offset(delta.xCoord, delta.yCoord, delta.zCoord));

        boolean visible = false;
        for (Vec3 vec : hitboxVertices) {
            MovingObjectPosition ray = mc.theWorld.rayTraceBlocks(camPos, vec);
            if (ray == null || canRayPassThrough(ray.getBlockPos())) {
                visible = true;
                break;
            }
        }

        ((ICullingEntity) entity).simpleculling$setShouldRender(visible);
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

    public static boolean shouldRender(Entity entity) {
        cullingQueue.add(entity);
        return ((ICullingEntity) entity).simpleculling$shouldRender();
    }

    private static boolean canRayPassThrough(BlockPos pos) { // todo check for barrier
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return !block.isFullCube();
    }

    public static void postRender(Entity entity, float partialTicks) {
//        if (entity instanceof EntityPlayer) {
//            double d01 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
//            double d11 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
//            double d21 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
//
//            mc.getRenderManager().getEntityRenderObject(entity).renderName(entity, d01, d11, d21);
//        }

//        ChatUtils.sendMessageClient(entity.getName());
    }
}