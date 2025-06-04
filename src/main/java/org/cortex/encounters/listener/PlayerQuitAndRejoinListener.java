package org.cortex.encounters.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cortex.core.RpCore;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerQuitAndRejoinListener implements Listener {

    private HashMap<UUID, Encounter> leftPlayer = new HashMap<>();
    private HashMap<UUID, Encounter> deadPlayer = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!Encounters.getInstance().getEncounterManager().isInEncounter(player))
            return;

        Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
        RpCharacter character = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
        encounter.sendMessageToAll(character.getName() + " left the server, they will rejoin the encounter if they reconnect");
        if (encounter.getAttacker() == player && !encounter.isAttackCompleted()) {
            encounter.nextAttacker(false);
        }
        if (encounter.getPlayers().contains(player))
            leftPlayer.put(player.getUniqueId(), encounter);
        if (encounter.getDeadPlayers().contains(player))
            deadPlayer.put(player.getUniqueId(), encounter);

        encounter.removePlayer(player);
        Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().remove(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(Encounters.getInstance(), () -> {
            Player player = event.getPlayer();
            if (leftPlayer.containsKey(player.getUniqueId())) {
                RpCharacter character = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
                Encounter encounter = leftPlayer.get(player.getUniqueId());
                if (Encounters.getInstance().getEncounterManager().getEncounters().contains(encounter)) {
                    encounter.addPlayer(player);
                    Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().put(player, encounter);
                    encounter.sendMessageToAll(character.getName() + " rejoined the encounter");
                }
                leftPlayer.remove(player.getUniqueId());
            } else if (deadPlayer.containsKey(player.getUniqueId())) {
                RpCharacter character = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
                Encounter encounter = deadPlayer.get(player.getUniqueId());
                if (Encounters.getInstance().getEncounterManager().getEncounters().contains(encounter)) {
                    encounter.setDeadPlayer(player);
                    Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().put(player, encounter);
                    encounter.sendMessageToAll(character.getName() + " rejoined the encounter");
                }
                deadPlayer.remove(player.getUniqueId());
            }
        }, 3L);
    }

}
