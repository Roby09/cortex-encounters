package org.cortex.encounters.encounter;

import java.util.ArrayList;

public class EncounterManager {

    private ArrayList<Encounter> encounters = new ArrayList<>();

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

    public ArrayList<Encounter> getEncounters() {
        return encounters;
    }
}
