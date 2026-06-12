package io.redspace.skillcasting.client;

import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * Cached spell-bar slot layout for the HUD, ported from the original skillcasting-api client.
 */
public final class ClientRenderCache {
    public static List<Vec2> relativeSpellBarSlotLocations = new ArrayList<>();

    private ClientRenderCache() {
    }

    public static int[] getRowCounts(int spellCount) {
        int topRow = 0;
        int middleRow = 0;
        int bottomRow = 0;
        if (spellCount <= 6) {
            topRow = SPELL_LAYOUT[spellCount - 1][0];
            middleRow = SPELL_LAYOUT[spellCount - 1][1];
            bottomRow = SPELL_LAYOUT[spellCount - 1][2];
        } else {
            for (int i = 0; i < spellCount; i++) {
                if (middleRow * 2 < (topRow + bottomRow + 1)) {
                    middleRow++;
                } else if (topRow <= bottomRow) {
                    topRow++;
                } else {
                    bottomRow++;
                }
            }
        }
        return new int[]{topRow, middleRow, bottomRow};
    }

    public static void generateRelativeLocations(SkillSelectionManager manager, int boxSize, int spriteSize) {
        relativeSpellBarSlotLocations.clear();
        int spellCount = manager.getSkillCount();
        if (spellCount == 0) {
            return;
        }
        int[] rowCounts = getRowCounts(spellCount);
        int[] row1 = new int[rowCounts[0]];
        int[] row2 = new int[rowCounts[1]];
        int[] row3 = new int[rowCounts[2]];
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
                relativeSpellBarSlotLocations.add(new Vec2(
                        offset + column * boxSize - spriteSize / 2f,
                        row * boxSize - overallHeight / 2f - spriteSize / 2f));
            }
        }
    }

    public static final int[][] SPELL_LAYOUT = {
            {1, 0, 0},
            {2, 0, 0},
            {2, 1, 0},
            {2, 2, 0},
            {3, 2, 0},
            {3, 3, 0},
            {4, 3, 0},
            {4, 4, 0},
            {3, 3, 3},
            {3, 4, 3},
            {4, 4, 3},
            {4, 4, 4},
            {4, 5, 4},
            {5, 5, 4},
            {5, 5, 5}
    };
}
