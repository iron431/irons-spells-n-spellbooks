package io.redspace.ironsspellbooks.worldgen;

public interface IExtendedNoiseChunk {

    record AquifierNuke(){}

    AquifierNuke getAquifierStatus();
    void setAquifierStatus(AquifierNuke nuke);
}
