package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

public class SummonEnderChestSpell extends AbstractSpell {

    private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(5)
            .build();

    public SummonEnderChestSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof ServerPlayer player) {
//            Scroll.attemptRemoveScrollAfterCast(player);
            PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> ChestMenu.threeRows(containerId, inventory, enderChest),
                    CONTAINER_TITLE));
        }
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
