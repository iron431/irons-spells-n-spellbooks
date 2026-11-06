package io.redspace.ironsspellbooks.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributeInstance.class)
public interface AttributeInstanceAccessor {
    @Accessor("modifiersByOperation")
    Map<AttributeModifier.Operation, Map<ResourceLocation, AttributeModifier>> irons_spellbooks$modifiersByOperation();
}
