package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class ClientRenderCache {

    public static SpinAttackType lastSpinAttack = SpinAttackType.RIPTIDE;


    public enum SpinAttackType {
        RIPTIDE,
        FIRE;
    }

    /**
     * SPELL BAR RENDER CACHING
     *************************/
    public static List<Vec2> relativeSpellBarSlotLocations = Lists.newArrayList();

    public static void generateRelativeLocations(SpellBookData spellBookData, int boxSize, int spriteSize) {
        relativeSpellBarSlotLocations.clear();
        if (spellBookData == null)
            return;
        int spellCount = spellBookData.getSpellSlots();

        int[] row1 = new int[SPELL_LAYOUT[spellCount - 1][0]];
        int[] row2 = new int[SPELL_LAYOUT[spellCount - 1][1]];
        int[] row3 = new int[SPELL_LAYOUT[spellCount - 1][2]];

        int[] rowWidth = {
                boxSize * row1.length,
                boxSize * row2.length,
                boxSize * row3.length
        };
        int[] rowHeight = {
                row1.length > 0 ? boxSize : 0,
                row2.length > 0 ? boxSize : 0,
                row3.length > 0 ? boxSize : 0
        };

        int[][] display = {row1, row2, row3};
        int overallHeight = rowHeight[0] + rowHeight[1] + rowHeight[2];
        for (int row = 0; row < display.length; row++) {
            for (int column = 0; column < display[row].length; column++) {
                int offset = -rowWidth[row] / 2;
                Vec2 location = new Vec2(offset + column * boxSize, (row) * boxSize - (overallHeight / 2));
                location.add(-spriteSize / 2);
                relativeSpellBarSlotLocations.add(location);
            }
        }
    }

    /**
     * HELPER
     *************************/
    public static final int[][] SPELL_LAYOUT = {
            {1, 0, 0}, //1
            {2, 0, 0}, //2
            {2, 1, 0}, //3
            {2, 2, 0}, //4
            {3, 2, 0}, //5
            {3, 3, 0}, //6
            {4, 3, 0}, //7
            {4, 4, 0}, //8
            {3, 3, 3}, //9
            {3, 4, 3}, //10
            {4, 4, 3}, //11
            {4, 4, 4}, //12
            {4, 5, 4}, //13
            {5, 5, 4}, //14
            {5, 5, 5}  //15
    };
}
