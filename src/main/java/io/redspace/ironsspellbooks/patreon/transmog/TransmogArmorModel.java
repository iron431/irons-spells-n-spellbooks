package io.redspace.ironsspellbooks.patreon.transmog;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class TransmogArmorModel<T extends Item & GeoItem> extends DefaultedItemGeoModel<T> {
    final ResourceLocation model, texture, anim;


    public TransmogArmorModel(ResourceLocation model, ResourceLocation texture, ResourceLocation anim) {
        super(IronsSpellbooks.id(""));
        this.model = model;
        this.texture = texture;
        this.anim = anim;
    }



    public TransmogArmorModel(ResourceLocation model, ResourceLocation texture) {
        this(model, texture, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json"));
    }


    @Override
    public ResourceLocation getModelResource(T object) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T object) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return anim;
    }

}
