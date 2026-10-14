package gr.larryngton.simpleculling.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

//todo block culling
public class MainCullingHandler {
    public static final ScheduledExecutorService CULLING_EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    public static Vec3 camPos = new Vec3(0, 0, 0);
    public static double camDist = 4;
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

        float f1 = mc.getRenderViewEntity().rotationYaw;
        float f2 = mc.getRenderViewEntity().rotationPitch;
        if (mc.gameSettings.thirdPersonView == 2) {
            f2 += 180.0F;
        }

        //todo find a way to get the value from mc calcs
        double camDist2 = camDist;
        double d4 = (double)(-MathHelper.sin(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * camDist2;
        double d5 = (double)(MathHelper.cos(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * camDist2;
        double d6 = (double)(-MathHelper.sin(f2 / 180.0F * (float)Math.PI)) * camDist2;

        for(int i = 0; i < 8; ++i) {
            float f3 = (float)((i & 1) * 2 - 1);
            float f4 = (float)((i >> 1 & 1) * 2 - 1);
            float f5 = (float)((i >> 2 & 1) * 2 - 1);
            f3 *= 0.1F;
            f4 *= 0.1F;
            f5 *= 0.1F;
            MovingObjectPosition movingobjectposition = mc.theWorld.rayTraceBlocks(new Vec3(x + (double)f3, y + (double)f4, z + (double)f5), new Vec3(x - d4 + (double)f3 + (double)f5, y - d6 + (double)f4, z - d5 + (double)f5));
            if (movingobjectposition != null) {
                double d7 = movingobjectposition.hitVec.distanceTo(new Vec3(x, y, z));
                if (d7 < camDist2) {
                    camDist2 = d7;
                }
            }
        }
        camDist1 = camDist2;

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

            d4 = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * camDist;
            d5 = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * camDist;
            d6 = -MathHelper.sin(pitchRad) * camDist;

            camPos = new Vec3(
                    x - d4 / camDist * camDist1,
                    y - d6 / camDist * camDist1,
                    z - d5 / camDist * camDist1
            );
        }
        camPos = camPos.add(Utils.getMoveDeltaVector(mc.getRenderViewEntity()));
    }

    public static boolean canRayPassThrough(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        if (!block.isFullCube()) {
            return true;
        }
        return !block.isOpaqueCube();
    }
}
