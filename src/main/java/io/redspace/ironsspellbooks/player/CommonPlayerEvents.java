package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CommonPlayerEvents {
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        IronsSpellbooks.LOGGER.debug("CommonPlayerEvents.onPlayerRightClickItem {}", event.getSide());
        var stack = event.getItemStack();
        if (Utils.canImbue(stack)) {
            var spellData = SpellData.getSpellData(stack);
            var result = Utils.onUseCastingHelper(event.getLevel(), event.getEntity(), event.getHand(), stack, spellData.getSpell());

            if (result != null) {
                event.setCancellationResult(result.getResult());
                event.setCanceled(true);
            }
        }
    }

    public static void onUseItemStop(LivingEntityUseItemEvent.Stop event) {
        IronsSpellbooks.LOGGER.debug("CommonPlayerEvents.onUseItemStop {} {}", event.getEntity().getLevel().isClientSide, event.getItem().getItem());
        var stack = event.getItem();
        if (Utils.canImbue(stack)) {
            var spell = SpellData.getSpellData(stack).getSpell();
            var entity = event.getEntity();

            if (spell.getSpellType() != SpellType.NONE_SPELL) {
                entity.stopUsingItem();
                Utils.releaseUsingHelper(entity);
            }
        }
    }

    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        IronsSpellbooks.LOGGER.debug("CommonPlayerEvents.onUseItemFinish {} {}", event.getEntity().getLevel().isClientSide, event.getItem().getItem());
    }
}
