package io.redspace.ironsspellbooks.datafix;

import com.google.common.collect.Lists;
import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
public class IronsTagTraverser implements TagVisitor {
    private final AtomicInteger changeCount;

    public IronsTagTraverser() {
        changeCount = new AtomicInteger(0);
    }

    private IronsTagTraverser(AtomicInteger changeCount) {
        this.changeCount = changeCount;
    }

    public boolean changesMade() {
        return changeCount.get() > 0;
    }

    public int totalChanges() {
        return changeCount.get();
    }

    public void visit(@Nullable Tag pTag) {
        if (pTag != null) {
            pTag.accept(this);
        }
    }

    public void visitString(StringTag pTag) {
    }

    public void visitByte(ByteTag pTag) {
    }

    public void visitShort(ShortTag pTag) {
    }

    public void visitInt(IntTag pTag) {
    }

    public void visitLong(LongTag pTag) {
    }

    public void visitFloat(FloatTag pTag) {
    }

    public void visitDouble(DoubleTag pTag) {
    }

    public void visitByteArray(ByteArrayTag pTag) {
    }

    public void visitIntArray(IntArrayTag pTag) {
    }

    public void visitLongArray(LongArrayTag pTag) {
    }

    public void visitList(ListTag pTag) {
        for (int i = 0; i < pTag.size(); ++i) {
            new IronsTagTraverser(changeCount).visit(pTag.get(i));
        }
    }

    public void visitCompound(CompoundTag pTag) {
        if (DataFixerHelpers.doFixUps(pTag)) {
            changeCount.incrementAndGet();
        }

        List<String> list = Lists.newArrayList(pTag.getAllKeys());
        Collections.sort(list);

        for (String s : list) {
            new IronsTagTraverser(changeCount).visit(pTag.get(s));
        }
    }

    public void visitEnd(EndTag pTag) {
    }
}
