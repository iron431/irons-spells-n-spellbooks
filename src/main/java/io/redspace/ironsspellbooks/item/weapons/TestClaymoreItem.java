package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.render.SpecialItemRenderer;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;


public class TestClaymoreItem extends ExtendedSwordItem {
    public TestClaymoreItem() {
        super(ExtendedWeaponTiers.CLAYMORE, 9, -2.7, Map.of(), ItemPropertiesHelper.hidden(1));
    }
}
