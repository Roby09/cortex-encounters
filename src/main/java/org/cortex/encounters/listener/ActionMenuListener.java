package org.cortex.encounters.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.cortex.core.RpCore;
import org.cortex.core.player.RpPlayer;
import org.cortex.core.util.RollUtil;
import org.cortex.core.weapons.CustomShield;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;
import org.cortex.encounters.gui.ActionInventory;
import org.cortex.rpchat.RpChat;
import org.cortex.rpchat.gui.EmoteInventory;

import java.util.ArrayList;

public class ActionMenuListener implements Listener {

    public static ArrayList<Player> playersInMenu = new ArrayList<>();

    public ActionMenuListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Encounters.getInstance(), () -> {
            playersInMenu.forEach(player -> Bukkit.getScheduler().runTask(RpCore.getInstance(), () -> player.openInventory(new ActionInventory().getInventory())));
        }, 0L, 20L);
    }

    @EventHandler
    public void onMainMenuClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        if (!(event.getClickedInventory().getHolder() instanceof ActionInventory))
            return;
        if (event.getCurrentItem() == null)
            return;

        Player player = (Player) event.getWhoClicked();
        RpPlayer rpPlayer = RpCore.getInstance().getPlayerManager().getRpPlayer(player);
        Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);

        event.setCancelled(true);

        switch (event.getRawSlot()) {
            case 0 -> {
                int roll = RollUtil.roll(rpPlayer.getCharacter().getSpecialtySkills().flexibility);
                if (encounter.getDefensiveStance().contains(player)) {
                    encounter.getCurrentAttackAction().setDefensiveRoll(roll + 2);
                    encounter.sendMessageToAll(rpPlayer.getCharacter().getName() + " rolls " + roll + " for flexibility and gets 2 additional points from the defensive stance");
                    encounter.getDefensiveStance().remove(player);
                } else {
                    encounter.getCurrentAttackAction().setDefensiveRoll(roll);
                    encounter.sendMessageToAll(rpPlayer.getCharacter().getName() + " rolls " + roll + " for flexibility");
                }
                encounter.completeAttackAction();
                playersInMenu.remove(player);
                player.closeInventory();
            }
            case 1 -> {
                if (player.getInventory().getItemInOffHand().getType() != Material.SHIELD) {
                    player.sendMessage(ChatColor.RED + "You need a shield in your off-hand for this action");
                    break;
                }
                ItemStack item = player.getInventory().getItemInOffHand();
                int roll = 2;
                if (RpCore.getInstance().getWeaponManager().isCustomShield(item)) {
                    CustomShield customShield = RpCore.getInstance().getWeaponManager().getCustomShield(item);
                    String actionDiff = customShield.getActionDifficulty();
                    if (actionDiff.contains("+")) {
                        String[] d = actionDiff.split("\\+");
                        roll = roll + RollUtil.roll(Integer.parseInt(d[0].replace("d", ""))) + Integer.parseInt(d[1].replace("d", ""));
                    } else {
                        roll = roll + RollUtil.roll(Integer.parseInt(actionDiff.replace("d", "")));
                    }
                }
                if (encounter.getDefensiveStance().contains(player)) {
                    encounter.getCurrentAttackAction().setDefensiveRoll(roll + 2);
                    encounter.sendMessageToAll(rpPlayer.getCharacter().getName() + " rolls " + roll + " for raising a shield and gets 2 additional points from the defensive stance");
                    encounter.getDefensiveStance().remove(player);
                } else {
                    encounter.getCurrentAttackAction().setDefensiveRoll(roll);
                    encounter.sendMessageToAll(rpPlayer.getCharacter().getName() + " rolls " + roll + " for raising a shield");
                }
                encounter.completeAttackAction();
                playersInMenu.remove(player);
                player.closeInventory();
            }
            case 2 -> {
                if (encounter.getDefensiveStance().contains(player)) {
                    encounter.getCurrentAttackAction().setDefensiveRoll(3 + 2);
                    encounter.sendMessageToAll(rpPlayer.getCharacter().getName() + " got 3 points with a brace defense and gets 2 additional points from the defensive stance");
                    encounter.getDefensiveStance().remove(player);
                } else {
                    encounter.getCurrentAttackAction().setDefensiveRoll(3);
                    encounter.sendMessageToAll(rpPlayer.getCharacter().getName() + " got 3 points with a brace defense");
                }
                encounter.completeAttackAction();
                playersInMenu.remove(player);
                player.closeInventory();
            }
        }
    }

}
