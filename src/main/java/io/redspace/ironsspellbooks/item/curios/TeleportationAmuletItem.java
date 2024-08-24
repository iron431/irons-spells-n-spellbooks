package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class TeleportationAmuletItem extends SimpleDescriptiveCurio {
    private static final Component VANITY_DESCRIPTION = Component.translatable("item.irons_spellbooks.teleportation_amulet.desc.alt").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    public TeleportationAmuletItem(Properties properties) {
        super(properties, Curios.NECKLACE_SLOT);
    }

    private void handleCurse(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        if (entity != null && !slotContext.entity().level.isClientSide && !canUse(entity)) {
            CuriosApi.getCuriosInventory(slotContext.entity()).ifPresent(
                    handler ->
                    {
                        var equippedStack = handler.getEquippedCurios().getStackInSlot(slotContext.index());
                        if (ItemStack.matches(stack, equippedStack)) {
                            handler.setEquippedCurio(Curios.NECKLACE_SLOT, slotContext.index(), ItemStack.EMPTY);
                            createItemEntity(slotContext.entity().level, stack, slotContext.entity().position());
                        }
                    }
            );
        }
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(VANITY_DESCRIPTION);
        var player = MinecraftInstanceHelper.getPlayer();
        if (player != null) {
            if (canUse(player)) {
                return super.getSlotsTooltip(tooltips, stack);
            }
        }
        return tooltips;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        //Using short modulo on tick because #onEquip fires before the living entity's attributes are calculated on relog/entity creation, meaning the curse will erroneously fire
        if (slotContext.entity().tickCount % 5 == 0) {
            handleCurse(slotContext, stack);
        }
    }

    private boolean canUse(LivingEntity livingEntity) {
        return livingEntity.getAttributeValue(AttributeRegistry.ENDER_SPELL_POWER) > 1.25;
    }

    private void createItemEntity(Level level, ItemStack stack, Vec3 center) {
        Vec3 target = center.add(new Vec3(Utils.random.nextIntBetweenInclusive(4, 8) + Utils.random.nextFloat(), 0, 0).yRot(Utils.random.nextFloat() * Mth.TWO_PI));
        Vec3 clipped = Utils.raycastForBlock(level,center.add(0,0.5,0),target.add(0,0.5,0), ClipContext.Fluid.NONE).getLocation();
        Vec3 placement = Utils.moveToRelativeGroundLevel(level, clipped, 5).add(0, 0.75, 0);
        var item = new ItemEntity(level, placement.x, placement.y, placement.z, stack);
        level.addFreshEntity(item);
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, placement.x, placement.y, placement.z, 20, 0.2, 0.2, 0.2, 0.2, false);
        level.playSound(null, BlockPos.containing(placement), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        level.playSound(null, BlockPos.containing(center), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
    }
}
