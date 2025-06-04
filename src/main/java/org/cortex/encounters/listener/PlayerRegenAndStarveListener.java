package org.cortex.encounters.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.cortex.encounters.Encounters;

import java.util.ArrayList;

public class PlayerRegenAndStarveListener implements Listener {

    @EventHandler
    public void onPlayerRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
            event.setCancelled(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.EATING || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED);
        }
    }

    @EventHandler
    public void onPlayerStarve(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
            event.setCancelled(event.getFoodLevel() < player.getFoodLevel());
        }
    }
}
