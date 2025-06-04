package org.cortex.encounters.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.cortex.core.RpCore;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDamageListener implements Listener {

    private static final Set<UUID> pendingDamage = ConcurrentHashMap.newKeySet();

    public static void allowNextDamage(Player player) {
        UUID uuid = player.getUniqueId();
        pendingDamage.add(uuid);
        Bukkit.getScheduler().runTaskLater(Encounters.getInstance(), () -> pendingDamage.remove(uuid), 1L); // 1 tick later
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerReceiveDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();

        if (Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
            if (!pendingDamage.contains(uuid)) {
                event.setCancelled(true);
                return;
            }

            // Remove immediately so only one damage goes through
            pendingDamage.remove(uuid);

            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);

            if (event.getDamage() >= player.getHealth()) {
                RpCharacter defender = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
                RpCharacter attacker = RpCore.getInstance().getPlayerManager().getRpPlayer(encounter.getAttacker()).getCharacter();
                encounter.sendMessageToAll(defender.getName() + " was slain in combat by " + attacker.getName());
                encounter.setDeadPlayer(player);
                encounter.updateScoreboard();
            }
        }
    }
}
