package gr.larryngton.simpleculling;

import gr.larryngton.simpleculling.util.EntityCullingHandler;
import gr.larryngton.simpleculling.util.MainCullingHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "simpleculling", name = "Simple culling", version = "1.0")
public class SimpleCulling {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EntityCullingHandler());
        MinecraftForge.EVENT_BUS.register(new MainCullingHandler());
    }
}
