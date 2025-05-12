package org.cortex.encounters.encounter;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class EncounterManager {

    private ArrayList<Encounter> encounters = new ArrayList<>();
    private HashMap<Player, Encounter> playerEncounterMap = new HashMap<>();

    public void addEncounter(Encounter encounter) {
        encounters.add(encounter);
    }

    public Encounter getEncounter(String name) {
        for (Encounter encounter : encounters) {
            if (encounter.getName().equalsIgnoreCase(name)) {
                return encounter;
            }
        }
        return null;
    }

    public Encounter getEncounter(Player player) {
        return playerEncounterMap.get(player);
    }

    public boolean isInEncounter(Player player) {
        return playerEncounterMap.containsKey(player);
    }

    public ArrayList<Encounter> getEncounters() {
        return encounters;
    }

    public HashMap<Player, Encounter> getPlayerEncounterMap() {
        return playerEncounterMap;
    }
}
