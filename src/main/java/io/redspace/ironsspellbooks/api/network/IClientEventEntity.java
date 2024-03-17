package io.redspace.ironsspellbooks.api.network;

public interface IClientEventEntity {
    void handleClientEvent(byte eventId);
}
