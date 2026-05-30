package io.redspace.skillcasting.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.selection.SkillSelection;
import io.redspace.skillcasting.api.selection.SkillSelectionEntry;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.api.cast.EntityCasterRef;
import io.redspace.skillcasting.api.selection.SelectionAccumulator;
import io.redspace.skillcasting.api.selection.SkillSelectionProvider;
import io.redspace.skillcasting.network.SkillSelectionSyncPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans equipped items for {@link ISkillContainer} components and builds the flattened skill bar.
 * One instance is held per player on {@link SkillcastingData}.
 */
public final class SkillSelectionManager {
    private static final List<SkillSelectionProvider> EXTRA_PROVIDERS = new ArrayList<>();

    public static void registerProvider(SkillSelectionProvider provider) {
        EXTRA_PROVIDERS.add(provider);
    }

    public static final String MAINHAND = EquipmentSlot.MAINHAND.getName();
    public static final String OFFHAND = EquipmentSlot.OFFHAND.getName();
    @Deprecated
    public static final String SPELLBOOK_SLOT = "spellbook";

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSelectionManager> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Codec.list(SkillOption.CODEC)),
            m -> m.skillOptions,
            SkillSelectionManager::fromNetwork
    );

    public record SkillOption(SkillData skill, int globalIndex, String slotSource) {
        public static final Codec<SkillOption> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                SkillData.CODEC.fieldOf("skill").forGetter(SkillOption::skill),
                Codec.INT.fieldOf("globalIndex").forGetter(SkillOption::globalIndex),
                Codec.STRING.fieldOf("slotSource").forGetter(SkillOption::slotSource)
        ).apply(builder, SkillOption::new));
    }

    private static SkillSelectionManager fromNetwork(List<SkillOption> skillOptions) {
        var manager = new SkillSelectionManager();
        manager.skillOptions.addAll(skillOptions);
        return manager;
    }

    private final List<SkillOption> skillOptions = new ArrayList<>();
    private int globalSize;

    public SkillSelectionManager() {
    }

    public void refresh(Player player) {
        if (player == null) {
            return;
        }
        globalSize = 0;
        skillOptions.clear();

        initItem(player.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD.getName());
        initItem(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST.getName());
        initItem(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS.getName());
        initItem(player.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET.getName());
        initItem(player.getItemBySlot(EquipmentSlot.MAINHAND), EquipmentSlot.MAINHAND.getName());
        initItem(player.getItemBySlot(EquipmentSlot.OFFHAND), EquipmentSlot.OFFHAND.getName());

        if (!EXTRA_PROVIDERS.isEmpty()) {
            SelectionAccumulator acc = new SelectionAccumulator();
            var caster = new EntityCasterRef(player);
            for (SkillSelectionProvider provider : EXTRA_PROVIDERS) {
                provider.collect(caster, acc);
            }
            for (SkillSelectionEntry entry : acc.entries()) {
                skillOptions.add(new SkillOption(
                        new SkillData(entry.skillId(), entry.level(), false),
                        globalSize,
                        entry.source()));
                globalSize++;
            }
        }
    }

    /**
     * Rebuilds {@link SkillcastingData#selection()} entries from this manager and reconciles the
     * selected index after equipment changes.
     */
    public void applyToData(Player player, SkillcastingData data) {
        refresh(player);
        List<SkillSelectionEntry> entries = new ArrayList<>();
        for (SkillOption option : skillOptions) {
            entries.add(new SkillSelectionEntry(option.skill().skillId(), option.skill().getLevel(), option.slotSource()));
        }
        data.selection().replaceEntries(entries);
        validateSelection(player, data);
    }

    private void validateSelection(Player player, SkillcastingData data) {
        SkillSelection current = data.selection();
        if (isEmpty()) {
            return;
        }
        boolean changed = false;
        int currentIndex = current.selectedIndex();
        if (currentIndex < 0) {
            current.withSelection(0, skillOptions.getFirst().slotSource());
            changed = true;
        } else if (currentIndex >= skillOptions.size()) {
            int newIndex;
            int lastIndex = current.lastSelectedIndex();
            if (lastIndex >= 0 && lastIndex < skillOptions.size()) {
                newIndex = lastIndex;
            } else {
                newIndex = skillOptions.size() - 1;
            }
            current.withSelection(newIndex, skillOptions.get(newIndex).slotSource());
            changed = true;
        } else if (current.lastSelectedIndex() >= 0
                && current.lastSelectedIndex() < skillOptions.size()
                && current.selectedIndex() != current.lastSelectedIndex()) {
            current.withSelection(current.lastSelectedIndex(), current.lastSelectedSource());
            changed = true;
        }
        if (changed && player instanceof ServerPlayer serverPlayer) {
            SkillSelectionSyncPacket.sendToPlayer(serverPlayer, this, data.selection());
        }
    }

    private void initItem(@Nullable ItemStack itemStack, String equipmentSlot) {
        if (!ISkillContainer.isSkillContainer(itemStack)) {
            return;
        }
        ISkillContainer container = ISkillContainer.get(itemStack);
        if (!container.isSpellWheel()) {
            return;
        }
        if (container.mustEquip()
                && !equipmentSlot.equals(MAINHAND)
                && !equipmentSlot.equals(OFFHAND)) {
            return;
        }
        var activeSpells = container.getActiveSpells();
        for (int i = 0; i < activeSpells.size(); i++) {
            var spellSlot = activeSpells.get(i);
            skillOptions.add(new SkillOption(spellSlot.skillData(), globalSize + i, equipmentSlot));
        }
        globalSize += container.getMaxSpellCount();
    }

    public List<SkillOption> getAllSkills() {
        return skillOptions;
    }

    @Nullable
    public SkillData getSkillAt(int index) {
        if (index >= 0 && index < skillOptions.size()) {
            return skillOptions.get(index).skill();
        }
        return null;
    }

    @Nullable
    public SkillOption getOptionAt(int index) {
        if (index >= 0 && index < skillOptions.size()) {
            return skillOptions.get(index);
        }
        return null;
    }

    @Nullable
    public SkillOption getOptionAtGlobalIndex(int globalIndex) {
        for (SkillOption option : skillOptions) {
            if (option.globalIndex() == globalIndex) {
                return option;
            }
        }
        return null;
    }

    public int getSkillCount() {
        return skillOptions.size();
    }

    public boolean isEmpty() {
        return skillOptions.isEmpty();
    }
}
