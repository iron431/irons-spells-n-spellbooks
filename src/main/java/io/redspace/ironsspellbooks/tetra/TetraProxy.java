package io.redspace.ironsspellbooks.tetra;

public class TetraProxy {
    public static ITetraProxy PROXY = new TetraDummyImpl();
}
