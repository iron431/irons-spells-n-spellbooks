package io.redspace.ironsspellbooks.entity.armor;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

/**
 * Dying now native to {@link GenericCustomArmorRenderer}, use that and set dyeable instead.
 * @param <T>
 */
@Deprecated(forRemoval = true)
public class DyeableArmorRenderer<T extends Item & GeoItem> extends GenericCustomArmorRenderer<T> {
    public DyeableArmorRenderer(GeoModel<T> model) {
        super(model);
        dyeable();
    }
}
