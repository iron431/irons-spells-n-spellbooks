package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvasionLayer extends AbstractEnergySwirlLayer<Player, PlayerModel<Player>> {
    private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/evasion.png");
    private final PlayerModel<Player> model;

    public EvasionLayer(RenderLayerParent<Player, PlayerModel<Player>> pRenderer) {
        super(pRenderer);
        this.model = pRenderer.getModel();
    }

    protected float xOffset(float offset) {
        return offset * 0.01F;
    }

    protected ResourceLocation getTextureLocation() {
        return EVASION_TEXTURE;
    }

    protected EntityModel<Player> model() {
        return this.model;
    }

    @Override
    protected boolean shouldRender(Player entity) {
        return ClientMagicData.getPlayerSyncedData(entity.getId()).getHasEvasion();
    }
}