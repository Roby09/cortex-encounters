package org.cortex.encounters.command.attack;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.cortex.core.RpCore;
import org.cortex.core.player.RpPlayer;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.core.util.RollResult;
import org.cortex.core.util.RollUtil;
import org.cortex.core.weapons.CustomWeapon;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.AttackAction;
import org.cortex.encounters.encounter.Encounter;
import org.cortex.encounters.encounter.EncounterManager;
import org.cortex.encounters.gui.ActionInventory;
import org.cortex.encounters.listener.ActionMenuListener;
import org.cortex.encounters.listener.MovementListener;
import org.cortex.encounters.util.EnChatUtil;

import java.util.ArrayList;
import java.util.List;

public class AttackthrowCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        RpPlayer rpPlayer = RpCore.getInstance().getPlayerManager().getRpPlayer(player);
        EncounterManager encounterManager = Encounters.getInstance().getEncounterManager();
        RpCharacter character = rpPlayer.getCharacter();

        if (!encounterManager.isInEncounter(player)) {
            player.sendMessage(ChatColor.RED + "You are not in an encounter.");
            return false;
        }
        Encounter encounter = encounterManager.getEncounter(player);
        if (encounterManager.getEncounter(player).getAttacker() != player) {
            player.sendMessage(ChatColor.RED + "You are not the attacking player");
            return false;
        }
        if (encounter.isAttackCompleted() || MovementListener.dash.contains(player)) {
            player.sendMessage(ChatColor.RED + "Your attack is already over");
            return false;
        }

        String targetName;

        if (arguments.length == 2) {
            targetName = arguments[0] + " " + arguments[1];
        } else if (arguments.length == 1) {
            targetName = arguments[0];
        } else {
            player.sendMessage("Correct usage: /attackthrow <character name>");
            return false;
        }

        RpCharacter target = RpCore.getInstance().getPlayerManager().getCharacter(targetName);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Character " + targetName + " does not exist");
            return false;
        }

        if (!encounter.getPlayers().contains(target.getAssignedPlayer())) {
            player.sendMessage(ChatColor.RED + "Character " + target.getName() + " is not part of this encounter");
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!RpCore.getInstance().getWeaponManager().isCustomWeapon(item)) {
            player.sendMessage(ChatColor.RED + "You must hold a custom weapon in your hand to use this attack");
            return false;
        }

        CustomWeapon customWeapon = RpCore.getInstance().getWeaponManager().getCustomWeapon(item);

        //Roll for success of attack
        int throwing = character.getSpecialtySkills().throwing;
        String diceThrowing = RollUtil.getDice(throwing);
        int throwingRoll = RollUtil.roll(throwing);

        RollResult successRoll = new RollResult(null, "throwing", diceThrowing, throwingRoll, 0);

        //Roll for damage
        String dice = customWeapon.getDamageDice();
        RollResult attackRoll = new RollResult();
        attackRoll.setAttribute("throwing");
        attackRoll.setSum("dice / 2");
        attackRoll.setDice(dice);
        if (dice.contains("+")) {
            String[] d = dice.split("\\+");
            int d1 = RollUtil.rollDie(Integer.parseInt(d[0].replace("d", "")));
            int d2 = RollUtil.rollDie(Integer.parseInt(d[1].replace("d", "")));
            attackRoll.setDice1Result(d1);
            attackRoll.setDice2Result(d2);
        } else {
            int d1 = RollUtil.rollDie(Integer.parseInt(dice.replace("d", "")));
            attackRoll.setDice1Result(d1);
        }

        target.getAssignedPlayer().getInventory().addItem(item);
        target.getAssignedPlayer().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1,1);

        player.getInventory().remove(item);
        player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_THROWN, 1,1);

        EnChatUtil.sendRollThrowingSuccessMessage(character, target, successRoll, encounter.getAllPlayers());
        encounter.setCurrentAttackAction(new AttackAction(player, target.getAssignedPlayer(), successRoll.getResult(), attackRoll.getResult()/2, attackRoll));

        ActionMenuListener.playersInMenu.add(target.getAssignedPlayer());
        target.getAssignedPlayer().openInventory(new ActionInventory().getInventory());
        encounter.setAttackCompleted(true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        if (!(commandSender instanceof Player player)) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            EncounterManager encounterManager = Encounters.getInstance().getEncounterManager();
            if (!encounterManager.isInEncounter(player)) {
                return List.of();
            }
            for (Player _pl : encounterManager.getEncounter(player).getPlayers()) {
                if (_pl != player) {
                    list.add(RpCore.getInstance().getPlayerManager().getRpPlayer(_pl).getCharacter().getName());
                }
            }
        }
        return list;
    }
}
