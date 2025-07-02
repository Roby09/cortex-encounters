package org.cortex.encounters.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.cortex.core.RpCore;
import org.cortex.core.player.RpPlayer;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.core.util.RollResult;
import org.cortex.core.util.RollSpecialtyResult;
import org.cortex.core.util.RollUtil;
import org.cortex.core.weapons.CustomArmor;
import org.cortex.core.weapons.CustomShield;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;
import org.cortex.encounters.gui.ActionInventory;
import org.cortex.encounters.util.EnChatUtil;

import java.util.ArrayList;

public class ActionMenuListener implements Listener {

    public static ArrayList<Player> playersInMenu = new ArrayList<>();

    public ActionMenuListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Encounters.getInstance(), () -> {
            playersInMenu.forEach(player -> {
                Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
                if (encounter == null) {
                    playersInMenu.remove(player);
                    return;
                }

                if (!encounter.getCurrentAttackAction().isFinished()) {
                    Bukkit.getScheduler().runTask(RpCore.getInstance(), () -> {
                        // Check again right before opening
                        if (!encounter.getCurrentAttackAction().isFinished() && playersInMenu.contains(player)) {
                            player.openInventory(new ActionInventory().getInventory());
                        }
                    });
                }
            });
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
        RpCharacter rpCharacter = rpPlayer.getCharacter();
        Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);

        event.setCancelled(true);

        if (encounter.getCurrentAttackAction().isFinished()) {
            player.closeInventory();
            return;
        }

        switch (event.getRawSlot()) {
            case 0 -> {
                RollSpecialtyResult rollSpecialtyResult = rpCharacter.getSpecialtySkills().roll("flexibility", rpCharacter.getSpecialtySkills().flexibility, "athletics", "agility", rpCharacter);

                int debuff = 0;
                if (player.getInventory().getBoots() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getBoots()))
                    debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getBoots()).getAgilityDebuff();
                if (player.getInventory().getLeggings() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getLeggings()))
                    debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getLeggings()).getAgilityDebuff();
                if (player.getInventory().getChestplate() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getChestplate()))
                    debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getChestplate()).getAgilityDebuff();
                if (player.getInventory().getHelmet() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getHelmet()))
                    debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getHelmet()).getAgilityDebuff();

                //Bypass debuff with spells
                if (encounter.getCurrentAttackAction().getCustomSpell() != null)
                    debuff = 0;

                int critBonus = 0;
                if (encounter.getCurrentAttackAction().isCrit())
                    critBonus = 4;

                if (encounter.getDefensiveStance().contains(player)) {
                    encounter.getCurrentAttackAction().setDefensiveRoll(rollSpecialtyResult.getResult() + 2 + critBonus - debuff);
                    EnChatUtil.sendDefenseRollMessage(rpPlayer.getCharacter(), rollSpecialtyResult, encounter.getCurrentAttackAction().getDefensiveRoll(), true, encounter.getCurrentAttackAction().isCrit(), debuff, encounter.getAllPlayers());
                    encounter.getDefensiveStance().remove(player);
                } else {
                    encounter.getCurrentAttackAction().setDefensiveRoll(rollSpecialtyResult.getResult() + critBonus - debuff);
                    EnChatUtil.sendDefenseRollMessage(rpPlayer.getCharacter(), rollSpecialtyResult, encounter.getCurrentAttackAction().getDefensiveRoll(),false, encounter.getCurrentAttackAction().isCrit(), debuff, encounter.getAllPlayers());
                }
                encounter.completeAttackAction();
                playersInMenu.remove(player);
                player.closeInventory();
            }
            case 1 -> {
                ItemStack item = player.getInventory().getItemInOffHand();
                if (!RpCore.getInstance().getWeaponManager().isCustomShield(item)) {
                    player.sendMessage(ChatColor.RED + "You need a custom shield in your off-hand for this action");
                    break;
                }

                int critBonus = 0;
                if (encounter.getCurrentAttackAction().isCrit())
                    critBonus = 4;

                RollResult rollResult = new RollResult();
                CustomShield customShield = RpCore.getInstance().getWeaponManager().getCustomShield(item);
                rollResult.setDice(customShield.getActionDifficulty());
                String actionDiff = customShield.getActionDifficulty();
                if (actionDiff.contains("+")) {
                    String[] d = actionDiff.split("\\+");
                    rollResult.setDice1Result(RollUtil.rollDie(Integer.parseInt(d[0].replace("d", ""))));
                    rollResult.setDice2Result(RollUtil.rollDie(Integer.parseInt(d[1].replace("d", ""))));
                } else {
                    rollResult.setDice1Result(RollUtil.rollDie(Integer.parseInt(actionDiff.replace("d", ""))));
                }
                if (encounter.getDefensiveStance().contains(player)) {
                    encounter.getCurrentAttackAction().setDefensiveRoll(rollResult.getResult() + critBonus + 2);
                    EnChatUtil.sendRollDefenseShieldMessage(rpCharacter, rollResult, encounter.getCurrentAttackAction().getDefensiveRoll(),true, encounter.getCurrentAttackAction().isCrit(), encounter.getAllPlayers());
                    encounter.getDefensiveStance().remove(player);
                } else {
                    encounter.getCurrentAttackAction().setDefensiveRoll(rollResult.getResult() + critBonus);
                    EnChatUtil.sendRollDefenseShieldMessage(rpCharacter, rollResult, encounter.getCurrentAttackAction().getDefensiveRoll(), false, encounter.getCurrentAttackAction().isCrit(), encounter.getAllPlayers());
                }
                rpCharacter.transaction(customShield.getLabourCost(), true);
                encounter.completeAttackAction();
                playersInMenu.remove(player);
                player.closeInventory();
            }
            case 2 -> {
                int critBonus = 0;
                if (encounter.getCurrentAttackAction().isCrit())
                    critBonus = 4;
                if (encounter.getDefensiveStance().contains(player)) {
                    encounter.getCurrentAttackAction().setDefensiveRoll(3 + 2 + critBonus);
                    EnChatUtil.sendBraceMessage(rpCharacter, encounter.getCurrentAttackAction().getDefensiveRoll(), true, encounter.getCurrentAttackAction().isCrit(), encounter.getAllPlayers());
                    encounter.getDefensiveStance().remove(player);
                } else {
                    encounter.getCurrentAttackAction().setDefensiveRoll(3 + critBonus);
                    EnChatUtil.sendBraceMessage(rpCharacter, encounter.getCurrentAttackAction().getDefensiveRoll(), false, encounter.getCurrentAttackAction().isCrit(), encounter.getAllPlayers());
                }
                encounter.completeAttackAction();
                playersInMenu.remove(player);
                player.closeInventory();
            }
        }
    }
}
