package io.redspace.ironsspellbooks.item;

import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class FurledMapItem extends Item {
    public static String FURLED_MAP_NBT = "furledMapData";
    public static String FURLED_MAP_LOCATION = "destination";
    public static String FURLED_MAP_DESCRIPTION = "description";

    public FurledMapItem() {
        super(ItemPropertiesHelper.material().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverlevel) {
            ItemStack itemStack = player.getItemInHand(hand);
            CompoundTag tag = itemStack.getTag();
            level.playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
            player.getCooldowns().addCooldown(ItemRegistry.FURLED_MAP.get(), 50);
            if (tag != null && tag.contains(FURLED_MAP_NBT, 10) && tag.getCompound(FURLED_MAP_NBT).contains(FURLED_MAP_LOCATION)) {
                ResourceLocation destinationResource = ResourceLocation.parse(tag.getCompound(FURLED_MAP_NBT).getString(FURLED_MAP_LOCATION));
                ResourceKey<Structure> structureResourceKey = ResourceKey.create(Registries.STRUCTURE, destinationResource);
                var holder = serverlevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(structureResourceKey).map(HolderSet::direct);
                //IronsSpellbooks.LOGGER.debug("FurledMapItem: found location: {}", structureResourceKey);
                if (holder.isPresent()) {
                    Pair<BlockPos, Holder<Structure>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapStructure(serverlevel, holder.get(), player.blockPosition(), 100, ServerConfigs.FURLED_MAPS_SKIP_CHUNKS.get());
                    if (pair != null) {
                        var blockpos = pair.getFirst();
                        ItemStack mapStack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true, true);
                        MapItem.renderBiomePreviewMap(serverlevel, mapStack);
                        MapItemSavedData.addTargetDecoration(mapStack, blockpos, "x", MapDecoration.Type.RED_X);

                        if (tag.getCompound(FURLED_MAP_NBT).contains(FURLED_MAP_DESCRIPTION)) {
                            Component mapTitle = Component.Serializer.fromJson(tag.getCompound(FURLED_MAP_NBT).getString(FURLED_MAP_DESCRIPTION));
                            mapStack.setHoverName(mapTitle);
                        }
                        replaceItem(player, mapStack, hand);
                        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
                    }
                }
            }
            replaceItem(player, new ItemStack(Items.MAP), hand);
        }
        return super.use(level, player, hand);
    }

    private static void replaceItem(Player player, ItemStack itemStack, InteractionHand hand) {
        boolean flag = player.getAbilities().instabuild;
        if (!flag) {
            //We set in hand because stack size is 1 and i don't wanna do logic. create filled result was bugging out in survival mode
            player.setItemInHand(hand, itemStack);
        } else {
            player.getInventory().add(itemStack);
        }
    }

    public static ItemStack of(ResourceLocation structure, MutableComponent descriptor) {
        ItemStack itemStack = new ItemStack(ItemRegistry.FURLED_MAP.get());
        itemStack.getOrCreateTagElement(FURLED_MAP_NBT).putString(FURLED_MAP_LOCATION, structure.toString());
        itemStack.getOrCreateTagElement(FURLED_MAP_NBT).putString(FURLED_MAP_DESCRIPTION, Component.Serializer.toJson(descriptor));
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", descriptor).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))));
        itemStack.getOrCreateTagElement("display").put("Lore", lore);
        return itemStack;
    }
}
