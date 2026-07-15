package io.redspace.ironsspellbooks.render;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.skillcasting.data.ISkillContainer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ScrollModel extends NBTOverrideItemModel {
    public ScrollModel(BakedModel original, ModelBakery loader) {
        super(original, loader);
    }

    @Override
    Optional<ResourceLocation> getModelFromStack(ItemStack itemStack) {
        if (ISkillContainer.isSkillContainer(itemStack)) {
            var skillData = ISkillContainer.get(itemStack).getSkillAtIndex(0);
            if (skillData != null && skillData.getSkill() instanceof AbstractSpell spellSkill) {
                return Optional.of(getScrollModelLocation(spellSkill.getSchoolType()));
            }
        }
        return Optional.empty();
    }

    public static ResourceLocation getScrollModelLocation(SchoolType schoolType) {
        return ResourceLocation.fromNamespaceAndPath(schoolType.getId().getNamespace(), String.format("item/scroll_%s", schoolType.getId().getPath()));
    }
}
