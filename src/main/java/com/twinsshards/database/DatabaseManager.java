package com.twinsshards.database;

import java.util.List;
import java.util.UUID;

public interface DatabaseManager {

    void init() throws Exception;

    void close();

    double loadBalance(UUID uuid, String name, double defaultBalance);

    void saveBalance(UUID uuid, String name, double balance);

    List<TopEntry> getTop(int limit);

    UUID getUuidByName(String name);

    record TopEntry(UUID uuid, String name, double balance) {}
}
