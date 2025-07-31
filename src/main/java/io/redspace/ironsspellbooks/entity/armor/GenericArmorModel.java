package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.ExtendedArmorItem;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.HashMap;
import java.util.Map;

public class GenericArmorModel<T extends ExtendedArmorItem> extends DefaultedItemGeoModel<T> {
    record ModelVariantResult(ResourceLocation location, boolean validated) {
    }

    private final ResourceLocation model; //(IronsSpellbooks.MODID, "geo/shadowwalker_armor.geo.json");

    private final ResourceLocation texture; //(IronsSpellbooks.MODID, "textures/models/armor/shadowwalker.png");

    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    private final Map<String, ModelVariantResult> modelVariants;

    public GenericArmorModel(String modid, String name) {
        super(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, ""));
        this.model = ResourceLocation.fromNamespaceAndPath(modid, String.format("geo/%s_armor.geo.json", name));
        this.texture = ResourceLocation.fromNamespaceAndPath(modid, String.format("textures/models/armor/%s.png", name));
        this.modelVariants = new HashMap<>();
    }

    public GenericArmorModel(String name) {
        this(IronsSpellbooks.MODID, name);
    }

    public GenericArmorModel<T> variants(Map<String, ResourceLocation> modelVariants) {
        modelVariants.forEach((string, location) -> this.modelVariants.put(string, new ModelVariantResult(location, false)));
        return this;
    }

    @Override
    public ResourceLocation getModelResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        if (renderer instanceof GeoArmorRenderer<?> armorRenderer && armorRenderer.getCurrentStack() != null) {
            String transmogVariant = armorRenderer.getCurrentStack().get(ComponentRegistry.CLOTHING_VARIANT);
            if (transmogVariant != null) {
                var result = modelVariants.get(transmogVariant);
                if (result != null) {
                    if (validateModelLocation(result, transmogVariant)) {
                        return result.location;
                    }
                }
            }
        }
        return model;
    }

    private boolean validateModelLocation(ModelVariantResult result, String transmogVariant) {
        if (result.validated) {
            return true;
        } else {
            if (GeckoLibCache.getBakedModels().get(result.location) != null) {
                modelVariants.put(transmogVariant, new ModelVariantResult(result.location, true));
                return true;
            } else {
                modelVariants.remove(transmogVariant);
                IronsSpellbooks.LOGGER.error("Could not find model variant location \"{}\", ignoring for the future!", result.location);
            }
        }
        return false;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ANIMATION;
    }
}