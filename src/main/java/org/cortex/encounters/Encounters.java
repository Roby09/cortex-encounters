package org.cortex.encounters;

import org.bukkit.plugin.java.JavaPlugin;
import org.cortex.encounters.command.EncounterCommand;
import org.cortex.encounters.encounter.EncounterManager;

import java.util.Objects;

public class Encounters extends JavaPlugin {

    private static Encounters i;
    private EncounterManager encounterManager;

    @Override
    public void onEnable() {
        i = this;

        encounterManager = new EncounterManager();

        Objects.requireNonNull(getCommand("encounter")).setExecutor(new EncounterCommand());
    }

    public static Encounters getInstance() {
        return i;
    }

    public EncounterManager getEncounterManager() {
        return encounterManager;
    }
}
