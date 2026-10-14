package gr.larryngton.simpleculling.mixins;

import gr.larryngton.simpleculling.util.EntityCullingHandler;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Shadow
    private int countEntitiesRendered;

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z"))
    private boolean simpleculling$redirectRenderEntity(RenderManager renderManager, Entity entity, float partialTicks) {
        if (EntityCullingHandler.shouldRender(entity)) {
            boolean flag = renderManager.renderEntitySimple(entity, partialTicks);
            if (!flag) {
                countEntitiesRendered--;
            }
            return flag;
        }

        return false;
    }
}