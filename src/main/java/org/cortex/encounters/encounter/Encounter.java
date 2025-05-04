package org.cortex.encounters.encounter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.cortex.core.RpCore;
import org.cortex.core.scoreboard.GameScoreboard;
import org.cortex.encounters.Encounters;

import java.util.ArrayList;

public class Encounter {

    private String name;
    private ArrayList<Player> players = new ArrayList<>();
    private GameScoreboard gameScoreboard;

    public Encounter(String name) {
        this.name = name;
        gameScoreboard = new GameScoreboard("ENCOUNTER: " + name);
    }

    public void addPlayer(Player player) {
        players.add(player);
        RpCore.getInstance().getScoreboardManager().setScoreboard(player, gameScoreboard);
        gameScoreboard.addLine(player.getName());
    }

    public void removePlayer(Player player) {

    }

    public String getName() {
        return name;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
