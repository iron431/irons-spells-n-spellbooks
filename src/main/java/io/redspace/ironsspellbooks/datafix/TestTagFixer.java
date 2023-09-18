package io.redspace.ironsspellbooks.datafix;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.StringTagVisitor;

public class TestTagFixer extends StringTagVisitor {
    String query;

    TestTagFixer(String stringToSearchFor) {
        super();
        this.query = stringToSearchFor;
    }

    @Override
    public void visitString(StringTag pTag) {
        super.visitString(pTag);
        if (pTag.getAsString().equals(query))
            IronsSpellbooks.LOGGER.debug("TestTagFixer found: {}", query);
    }
}
