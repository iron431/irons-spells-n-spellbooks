package io.redspace.ironsspellbooks.gametest;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.api.event.GatherSkillSelectionEvent;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.selection.SkillSelection;
import io.redspace.skillcasting.data.selection.SkillSelectionManager;
import io.redspace.skillcasting.data.selection.SkillSelectionPriority;
import io.redspace.skillcasting.data.skill.SkillContainer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(IronsSpellbooks.MODID)
@PrefixGameTestTemplate(false)
public class SkillcastingGameTests {

    @GameTest(template = "empty")
    public static void equalPrioritySortsBySource(GameTestHelper helper) {
        var container = SkillContainer.fromSerialized(1, false, false, List.of());
        var b = new GatherSkillSelectionEvent.Source(container, CastSource.of("b", "head"), SkillSelectionPriority.ARMOR);
        var a = new GatherSkillSelectionEvent.Source(container, CastSource.of("a", "chest"), SkillSelectionPriority.ARMOR);

        var forward = new ArrayList<>(List.of(b, a));
        var reversed = new ArrayList<>(List.of(a, b));
        forward.sort(SkillSelectionPriority.SOURCE_ORDER);
        reversed.sort(SkillSelectionPriority.SOURCE_ORDER);

        helper.assertTrue(forward.equals(List.of(a, b)), "[b, a] should sort to [a, b]");
        helper.assertTrue(reversed.equals(List.of(a, b)), "[a, b] should stay [a, b]");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void managerCopyDoesNotShareSelection(GameTestHelper helper) {
        var original = new SkillSelectionManager(new SkillSelection("mainhand", 0));
        var copy = original.copy();

        copy.getSkillSelection().makeSelection("offhand", 3);

        helper.assertTrue(original.getSkillSelection().index() == 0, "original index changed to " + original.getSkillSelection().index());
        helper.assertTrue("mainhand".equals(original.getSkillSelection().equipmentSlot()), "original slot changed to " + original.getSkillSelection().equipmentSlot());
        helper.succeed();
    }
}
