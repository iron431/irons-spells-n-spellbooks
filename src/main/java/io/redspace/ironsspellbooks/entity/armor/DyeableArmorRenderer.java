package io.redspace.ironsspellbooks.entity.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class DyeableArmorRenderer<T extends Item & GeoItem> extends GenericCustomArmorRenderer<T> {
    public DyeableArmorRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        color = 0xFFFFFFFF; // todo: default color?
        if (bone.getName().startsWith("dye") && this.currentStack != null) {
            color = Minecraft.getInstance().getItemColors().getColor(this.currentStack, 0) | 0xFF000000;
        }
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
    }
}
