package io.redspace.ironsspellbooks.item.weapons.pyrium_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.RenderHelper;
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
public class PyriumStaffOrbModel extends Model {
    public static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/item/pyrium_staff_orb.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(IronsSpellbooks.id("pyrium_staff_orb"), "main");
    private final ModelPart root;

    public PyriumStaffOrbModel(ModelPart pRoot) {
        super(RenderHelper.CustomerRenderType.PYRIUM_STAFF_ORB);
        this.root = pRoot;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create()
                .texOffs(12, 16).addBox(1.5F, 1.5F, 1.5F, -3.0F, -3.0F, -3.0F, new CubeDeformation(0.0F))
                .texOffs(8, 8).addBox(1.0F, 1.0F, 1.0F, -2.0F, -2.0F, -2.0F, new CubeDeformation(0.0F))
                .texOffs(4, 3).addBox(0.5F, 0.5F, 0.5F, -1.0F, -1.0F, -1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public RenderType renderType() {
        return renderType(TEXTURE);
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int pColor) {
        this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pColor);
    }
}
