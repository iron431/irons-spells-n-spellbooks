package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvasionLayer extends AbstractEnergySwirlLayer<Player, PlayerModel<Player>> {
    private static final ResourceLocation POWER_LOCATION = new ResourceLocation(TestMod.MODID, "textures/entity/evasion.png");
    private final PlayerModel<Player> model;

    public EvasionLayer(RenderLayerParent<Player, PlayerModel<Player>> pRenderer, EntityModelSet p_174472_) {
        super(pRenderer);
        this.model = new PlayerModel<>(p_174472_.bakeLayer(ModelLayers.PLAYER), false);
    }

    protected float xOffset(float p_116683_) {
        return p_116683_ * 0.01F;
    }

    protected ResourceLocation getTextureLocation() {
        return POWER_LOCATION;
    }

    protected EntityModel<Player> model() {
        return this.model;
    }

    @Override
    protected boolean shouldRender(Player entity) {
        return ClientMagicData.getPlayerSyncedData(entity.getId()).getHasEvasion();
    }
}