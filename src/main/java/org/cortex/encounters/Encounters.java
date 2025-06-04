package org.cortex.encounters;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.cortex.encounters.command.EncounterCommand;
import org.cortex.encounters.command.attack.*;
import org.cortex.encounters.encounter.EncounterManager;
import org.cortex.encounters.listener.*;

import java.util.Objects;

public class Encounters extends JavaPlugin {

    private static Encounters i;
    private EncounterManager encounterManager;

    @Override
    public void onEnable() {
        i = this;

        encounterManager = new EncounterManager();

        Bukkit.getPluginManager().registerEvents(new ActionMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new MovementListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRegenAndStarveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitAndRejoinListener(), this);

        Objects.requireNonNull(getCommand("encounter")).setExecutor(new EncounterCommand());
        Objects.requireNonNull(getCommand("attack")).setExecutor(new AttackCommand());
        Objects.requireNonNull(getCommand("attackthrow")).setExecutor(new AttackthrowCommand());
        Objects.requireNonNull(getCommand("attackcrit")).setExecutor(new AttackcritCommand());
        Objects.requireNonNull(getCommand("ds")).setExecutor(new DsCommand());
        Objects.requireNonNull(getCommand("dash")).setExecutor(new DashCommand());
        //Objects.requireNonNull(getCommand("castspell")).setExecutor(new CastspellCommand());
    }

    public static Encounters getInstance() {
        return i;
    }

    public EncounterManager getEncounterManager() {
        return encounterManager;
    }
}
