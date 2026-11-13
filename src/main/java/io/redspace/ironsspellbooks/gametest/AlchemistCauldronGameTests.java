package io.redspace.ironsspellbooks.gametest;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(IronsSpellbooks.MODID)
@PrefixGameTestTemplate(false)
public class AlchemistCauldronGameTests {
    private static final BlockPos CAULDRON = new BlockPos(2, 2, 2);

    @GameTest(template = "floor_5x5", timeoutTicks = 100)
    public static void droppedValidItemsFillFreeSlots(GameTestHelper helper) {
        AlchemistCauldronTile tile = placeCauldron(helper);
        ItemEntity item = drop(helper, new ItemStack(ItemRegistry.SCROLL.get(), 3), 0, 1.5, 0);
        helper.succeedWhen(() -> {
            helper.assertTrue(item.isRemoved(), "fully inserted stack should be discarded");
            helper.assertTrue(scrollSlots(tile) == 3, "expected 3 filled slots, got " + scrollSlots(tile));
        });
    }

    @GameTest(template = "floor_5x5", timeoutTicks = 100)
    public static void overflowBouncesOutside(GameTestHelper helper) {
        AlchemistCauldronTile tile = placeCauldron(helper);
        fillSlots(tile, 3);
        ItemEntity item = drop(helper, new ItemStack(ItemRegistry.SCROLL.get(), 3), 0, 1.5, 0);
        helper.succeedWhen(() -> {
            helper.assertTrue(scrollSlots(tile) == 4, "expected 4 filled slots, got " + scrollSlots(tile));
            helper.assertTrue(item.getItem().getCount() == 2, "expected 2 bounced scrolls, got " + item.getItem().getCount());
            assertRestsOutside(helper, item);
        });
    }

    @GameTest(template = "floor_5x5", timeoutTicks = 100)
    public static void invalidItemBouncesOutside(GameTestHelper helper) {
        AlchemistCauldronTile tile = placeCauldron(helper);
        ItemEntity item = drop(helper, new ItemStack(Items.DIRT), 0, 1.5, 0);
        helper.succeedWhen(() -> {
            helper.assertTrue(tile.isEmpty(), "invalid item should not be inserted");
            assertRestsOutside(helper, item);
        });
    }

    @GameTest(template = "floor_5x5", timeoutTicks = 100)
    public static void restingItemInFullCauldronBouncesOnce(GameTestHelper helper) {
        AlchemistCauldronTile tile = placeCauldron(helper);
        fillSlots(tile, 4);
        Vec3 cauldronCenter = Vec3.atCenterOf(helper.absolutePos(CAULDRON));
        AtomicInteger bounces = new AtomicInteger();
        Consumer<PlayLevelSoundEvent.AtPosition> onSound = event -> {
            if (event.getLevel() == helper.getLevel() && event.getSound() != null && event.getSound().value() == SoundRegistry.FORCE_IMPACT.get()
                    && event.getPosition().distanceTo(cauldronCenter) < 1) {
                bounces.incrementAndGet();
            }
        };
        NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.AtPosition.class, onSound);
        ItemEntity item = drop(helper, new ItemStack(ItemRegistry.SCROLL.get()), 0.25, 0.3, 0.25);
        helper.succeedWhen(() -> {
            assertRestsOutside(helper, item);
            helper.assertTrue(bounces.get() == 1, "expected one bounce, got " + bounces.get());
            NeoForge.EVENT_BUS.unregister(onSound);
        });
    }

    private static AlchemistCauldronTile placeCauldron(GameTestHelper helper) {
        helper.setBlock(CAULDRON, BlockRegistry.ALCHEMIST_CAULDRON.get());
        return helper.getBlockEntity(CAULDRON);
    }

    private static ItemEntity drop(GameTestHelper helper, ItemStack stack, double dx, double dy, double dz) {
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(CAULDRON)).add(dx, dy, dz);
        ItemEntity item = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, stack, 0, 0, 0);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    private static void fillSlots(AlchemistCauldronTile tile, int n) {
        for (int i = 0; i < n; i++) {
            tile.inputItems.set(i, new ItemStack(ItemRegistry.SCROLL.get()));
        }
    }

    private static int scrollSlots(AlchemistCauldronTile tile) {
        return (int) tile.inputItems.stream().filter(stack -> stack.is(ItemRegistry.SCROLL.get()) && stack.getCount() == 1).count();
    }

    private static void assertRestsOutside(GameTestHelper helper, ItemEntity item) {
        BlockPos cauldron = helper.absolutePos(CAULDRON);
        helper.assertTrue(item.isAlive(), "bounced item should survive");
        helper.assertTrue(item.onGround(), "bounced item still moving");
        helper.assertTrue(item.getY() < cauldron.getY() + 0.5, "bounced item should land on the floor, not the rim");
        helper.assertFalse(item.getBoundingBox().intersects(new AABB(cauldron)), "bounced item should land outside the cauldron");
        helper.assertTrue(item.position().subtract(Vec3.atBottomCenterOf(cauldron)).horizontalDistance() < 2.5, "bounced item should land close");
    }
}
