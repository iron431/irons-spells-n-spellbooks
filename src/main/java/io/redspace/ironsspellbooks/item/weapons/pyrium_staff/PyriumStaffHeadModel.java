package io.redspace.ironsspellbooks.item.weapons.pyrium_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PyriumStaffHeadModel extends Model {
    public static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/item/pyrium_staff_head.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(IronsSpellbooks.id("pyrium_staff_head"), "main");
    private final ModelPart root;

    public PyriumStaffHeadModel(ModelPart pRoot) {
        super(RenderType::entityCutout);
        this.root = pRoot;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition forespike_r1 = bb_main.addOrReplaceChild("forespike_r1", CubeListBuilder.create().texOffs(13, 10).addBox(0.0F, -9.0F, 0.0F, 0.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition forespike_r2 = bb_main.addOrReplaceChild("forespike_r2", CubeListBuilder.create().texOffs(13, 10).addBox(0.0F, -9.0F, 0.0F, 0.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition forespike_r3 = bb_main.addOrReplaceChild("forespike_r3", CubeListBuilder.create().texOffs(13, 10).addBox(0.0F, -9.0F, 0.0F, 0.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public RenderType renderType() {
        return renderType(TEXTURE);
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int pColor) {
        this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pColor);
    }
}
