package org.cortex.encounters.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.cortex.core.RpCore;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;

import java.util.ArrayList;

public class PlayerDamageListener implements Listener {

    public static ArrayList<Player> exempt = new ArrayList<>();

    @EventHandler
    public void onPlayerReceiveDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
            if (!exempt.contains(player))
                event.setCancelled(true);
            else {
                exempt.remove(player);
                Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
                if (event.getDamage() >= player.getHealth()) {
                    RpCharacter defender = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
                    RpCharacter attacker = RpCore.getInstance().getPlayerManager().getRpPlayer(encounter.getAttacker()).getCharacter();
                    encounter.sendMessageToAll(defender.getName() + " was slain in combat by " + attacker.getName());
                    encounter.setDeadPlayer(player);
                }
            }
        }
    }
}
