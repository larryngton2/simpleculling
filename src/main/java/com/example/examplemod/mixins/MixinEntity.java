package com.example.examplemod.mixins;

import com.example.examplemod.util.ICullingEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class MixinEntity implements ICullingEntity {
    @Unique
    private boolean simpleculling$shouldRender = true;

    @Override
    public boolean simpleculling$shouldRender() {
        return simpleculling$shouldRender;
    }

    @Override
    public void simpleculling$setShouldRender(boolean shouldRender) {
        this.simpleculling$shouldRender = shouldRender;
    }
}