package org.cortex.encounters.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;
import org.cortex.encounters.encounter.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MovementListener implements Listener {

    public static HashMap<UUID, Integer> moveMap = new HashMap<>();
    public static ArrayList<Player> dash = new ArrayList<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(!Encounters.getInstance().getEncounterManager().isInEncounter(event.getPlayer()))
            return;

        Player player = event.getPlayer();
        Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);

        if (encounter.getGameState() != GameState.STARTED && encounter.getGameState() != GameState.ENDING)
            return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double x = Math.floor(from.getX());
        double z = Math.floor(from.getZ());


        if (moveMap.containsKey(player.getUniqueId()) && moveMap.get(player.getUniqueId()) > 0 && encounter.getAttacker() == player && !encounter.isAttackCompleted()) {
            if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
                moveMap.put(player.getUniqueId(), moveMap.get(player.getUniqueId()) - 1);
                encounter.sendMessage(player, "Movement points (blocks): " + moveMap.get(player.getUniqueId()));
                if (dash.contains(player) && moveMap.get(player.getUniqueId()) == 0) {
                    encounter.nextAttacker(false);
                    dash.remove(player);
                }
            }
        } else {
            if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
                x += .5;
                z += .5;
                event.getPlayer().teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
            }
        }
    }
}
