package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ArcaneAnvilJeiRecipe {
    enum Type {
        Scroll_Upgrade,
        Item_Upgrade,
        Imbue,
        Affinity_Ring_Attune
    }

    @NotNull Type type;
    @Nullable
    Item leftItem;
    @Nullable
    Item rightItem;
    @Nullable
    AbstractSpell spell;
    @Nullable
    int level;

    public ArcaneAnvilJeiRecipe(Item leftItem, Item rightItem) {
        this.leftItem = leftItem;
        this.rightItem = rightItem;
        this.type = Type.Item_Upgrade;
    }

    public ArcaneAnvilJeiRecipe(Item leftItem, AbstractSpell spell) {
        this.leftItem = leftItem;
        this.spell = spell;
        this.type = Type.Imbue;
    }

    public ArcaneAnvilJeiRecipe(AbstractSpell spell, int baseLevel) {
        this.spell = spell;
        this.level = baseLevel;
        this.type = Type.Scroll_Upgrade;
    }

    public ArcaneAnvilJeiRecipe(AbstractSpell spell) {
        this.spell = spell;
        this.type = Type.Affinity_Ring_Attune;
    }

    public Tuple<List<ItemStack>, List<ItemStack>, List<ItemStack>> getRecipeItems() {
        return switch (this.type) {
            case Scroll_Upgrade -> {
                var scroll1 = new ItemStack(ItemRegistry.SCROLL.get());
                var scroll2 = new ItemStack(ItemRegistry.SCROLL.get());
                var ink = new ItemStack(InkItem.getInkForRarity(spell.getRarity(level + 1)));
                ISpellContainer.createScrollContainer(spell, level, scroll1);
                ISpellContainer.createScrollContainer(spell, level + 1, scroll2);
                yield new Tuple<>(List.of(scroll1), List.of(ink), List.of(scroll2));
            }
            case Imbue -> {
                var tuple = new Tuple<List<ItemStack>, List<ItemStack>, List<ItemStack>>(new ArrayList<ItemStack>(), new ArrayList<ItemStack>(), new ArrayList<ItemStack>());
                tuple.a.add(new ItemStack(leftItem));
                SpellRegistry.getEnabledSpells().forEach(spell -> {
                    IntStream.rangeClosed(spell.getMinLevel(), spell.getMaxLevel()).forEach(i -> {
                        var scroll = new ItemStack(ItemRegistry.SCROLL.get());
                        ISpellContainer.createScrollContainer(spell, i, scroll);
                        var result = new ItemStack(leftItem);
                        ISpellContainer.createScrollContainer(spell, i, result);
                        tuple.b.add(scroll);
                        tuple.c.add(result);
                    });
                });

                yield tuple;
            }
            case Item_Upgrade -> {
                var tuple = new Tuple<List<ItemStack>, List<ItemStack>, List<ItemStack>>(new ArrayList<ItemStack>(), new ArrayList<ItemStack>(), new ArrayList<ItemStack>());
                tuple.a.add(new ItemStack(leftItem));
                var upgradeStack = new ItemStack(rightItem);
                var result = new ItemStack(leftItem);
                UpgradeData.set(result, UpgradeData.NONE.addUpgrade(result, Minecraft.getInstance().level.registryAccess().holderOrThrow(
                        upgradeStack.get(ComponentRegistry.UPGRADE_ORB_TYPE)
                ), UpgradeUtils.getRelevantEquipmentSlot(result)));
                tuple.b.add(upgradeStack);
                tuple.c.add(result);
                yield tuple;
            }
            case Affinity_Ring_Attune -> {
                var tuple = new Tuple<List<ItemStack>, List<ItemStack>, List<ItemStack>>(new ArrayList<ItemStack>(), new ArrayList<ItemStack>(), new ArrayList<ItemStack>());
                var result = new ItemStack(ItemRegistry.AFFINITY_RING.get());
                AffinityData.set(result, new AffinityData(this.spell));
                SpellRegistry.getEnabledSpells().forEach(randomSpell -> {
                    var baseRing = new ItemStack(ItemRegistry.AFFINITY_RING.get());
                    AffinityData.set(baseRing, new AffinityData(randomSpell));
                    tuple.a.add(baseRing);
                });
                IntStream.rangeClosed(this.spell.getMinLevel(), this.spell.getMaxLevel()).forEach(i -> {
                    var scroll = new ItemStack(ItemRegistry.SCROLL);
                    ISpellContainer.createScrollContainer(this.spell, i, scroll);

                    tuple.b.add(scroll);
                    //tuple.c.add(result);
                });
                tuple.c.add(result);
                yield tuple;
            }
        };
    }

    public record Tuple<A, B, C>(A a, B b, C c) {

    }
}
