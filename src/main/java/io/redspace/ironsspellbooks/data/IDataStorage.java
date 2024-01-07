package io.redspace.ironsspellbooks.data;

public interface IDataStorage {
    boolean isDirty();

    void clearDirty();
}
