package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.effect.AbyssalShroudEffect;
import io.redspace.ironsspellbooks.effect.AscensionEffect;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientPlayerEvents {
    //
    //  Handle (Client Side) cast duration
    //
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END && event.player == Minecraft.getInstance().player) {
            var level = Minecraft.getInstance().level;

            ClientMagicData.getCooldowns().tick(1);
            if (ClientMagicData.getCastDuration() > 0) {
                ClientMagicData.handleCastDuration();
            }

            if (level != null) {
                List<Entity> spellcasters = level.getEntities((Entity) null, event.player.getBoundingBox().inflate(64), (mob) -> mob instanceof Player || mob instanceof AbstractSpellCastingMob);
                spellcasters.forEach((entity) -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    var spellData = ClientMagicData.getSyncedSpellData(livingEntity);
                    /*
                    Status Effect Visuals
                     */
                    if (spellData.hasEffect(SyncedSpellData.ABYSSAL_SHROUD)) {
                        AbyssalShroudEffect.ambientParticles(level, livingEntity);
                    }
                    if (spellData.hasEffect(SyncedSpellData.ASCENSION)) {
                        AscensionEffect.ambientParticles(level, livingEntity);
                    }
                    /*
                    Current Casting Spell Visuals
                     */
                    SpellType currentSpell = SpellType.getTypeFromValue(spellData.getCastingSpellId());
                    if (currentSpell == SpellType.RAY_OF_SIPHONING_SPELL) {
                        //RayOfSiphoningSpell.doRayParticles(livingEntity, spellData.getCastingSpellLevel());
                    }
                });
            }

        }
    }

    @SubscribeEvent
    public static void beforeLivingRender(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;

        var livingEntity = event.getEntity();
        if (livingEntity instanceof Player || livingEntity instanceof AbstractSpellCastingMob) {

            var syncedData = ClientMagicData.getSyncedSpellData(livingEntity);
            if (syncedData.hasEffect(SyncedSpellData.TRUE_INVIS) && livingEntity.isInvisibleTo(player)) {
                event.setCanceled(true);
            }
            if (syncedData.getCastingSpellType() == SpellType.RAY_OF_SIPHONING_SPELL) {
                // player.position().add(0, player.getEyeHeight()/2,0),player.getEyePosition().add(player.getForward().normalize().scale(32))
                SpellRenderingHelper.renderRay(livingEntity, event.getPoseStack(), event.getMultiBufferSource(), 255, 0, 0, 255, event.getPartialTick());
            }
        }
    }

    @SubscribeEvent
    public static void imbuedWeaponTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        /*
        Universal info to display:
        - Unique Info
        - Cast Time
        - Mana Cost
        - Cooldown Time
        Scrolls show:
        - Level w/ rarity
        - School
        Spellbooks and Imbued weapons show:
        - [*name* *lvl*]
         */

        if (SpellData.hasSpellData(stack)) {
            //Scrolls take care of themselves
            if (!(stack.getItem() instanceof Scroll)) {
                var additionalLines = TooltipsUtils.formatActiveSpellTooltip(stack, CastSource.SWORD) ;
                //Add header to sword tooltip
                additionalLines.add(1, Component.translatable("tooltip.irons_spellbooks.imbued_tooltip").withStyle(ChatFormatting.GRAY));
                //Indent the title because we have an additional header
                additionalLines.set(2, Component.literal(" ").append(additionalLines.get(2)));
                //Make room for the stuff the advanced tooltips add to the tooltip
                if (event.getFlags().isAdvanced())
                    event.getToolTip().addAll(event.getToolTip().size() - 2, additionalLines);
                else
                    event.getToolTip().addAll(additionalLines);
                //event.getToolTip().add(Component.literal(additionalLines.size() + "").withStyle(ChatFormatting.BOLD));
            }

        }
    }


//    @SubscribeEvent
//    public static void createSpellTooltips(RenderTooltipEvent.GatherComponents event) {
//        //List<Either<FormattedText, TooltipComponent>> eventTooltipElements = event.getTooltipElements();
//        ItemStack stack = event.getItemStack();
//
//        List<Component> additionalLines;
//        boolean flag = false;
//        if (stack.getItem() instanceof SpellBook) {
//            additionalLines = formatActiveSpellTooltip(stack, Minecraft.getInstance().player);
//            flag = true;
//            //lines.forEach((line) -> eventTooltipElements.add(Either.left(line)));
//
//        } else if (SpellData.hasSpellData(stack)) {
//
//            if (stack.getItem() instanceof Scroll) {
//
//            } else {
//                additionalLines = formatActiveSpellTooltip(stack, Minecraft.getInstance().player);
//                flag = true;
//                //lines.forEach((line) -> eventTooltipElements.add(Either.left(line)));
//            }
//        }
//        if(flag){
//            if()
//        }
//    }


}