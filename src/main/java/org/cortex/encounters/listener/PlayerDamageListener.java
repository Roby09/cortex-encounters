package org.cortex.encounters.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.cortex.core.RpCore;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.encounters.Encounters;

public class PlayerDamageListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerReceiveDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        event.setCancelled(Encounters.getInstance().getEncounterManager().isInEncounter(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageOther(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Player attacker = null;

        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }
        if (attacker == null) return;

        if (Encounters.getInstance().getEncounterManager().isInEncounter(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player player) {
            event.setCancelled(Encounters.getInstance().getEncounterManager().isInEncounter(player));
        }
    }

}
