package io.redspace.ironsspellbooks.util;

import java.util.Objects;
import java.util.function.Supplier;

public class MemoizedSupplier<T> implements Supplier<T> {
    T value;
    final Supplier<T> supplier;

    public MemoizedSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (value == null) {
            synchronized (supplier) {
                value = Objects.requireNonNull(supplier.get());
            }
        }
        return value;
    }
}
