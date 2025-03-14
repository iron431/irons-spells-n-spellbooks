package io.redspace.ironsspellbooks.item.weapons.pyrium_staff;

import io.redspace.ironsspellbooks.item.weapons.StaffItem;

public class PyriumStaffItem extends StaffItem {

    public PyriumStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCustomRendering() {
        return true;
    }
}
