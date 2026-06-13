package io.redspace.skillcasting.api.event;

public enum SkillSelectionPriority {
    PRIMARY_SKILL_SOURCE(0),
    CURIO(100),
    ARMOR(200),
    HANDHELD(300),
    OTHER(1000);

    private final int sortOrder;

    SkillSelectionPriority(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int sortOrder() {
        return sortOrder;
    }
}
