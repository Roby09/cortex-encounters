package org.cortex.encounters.command.attack;

import org.bukkit.ChatColor;
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
import org.cortex.core.util.RollSpecialtyResult;
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
import org.cortex.encounters.util.WeaponDamageRoll;

import java.util.ArrayList;
import java.util.List;

public class AttackcritCommand implements CommandExecutor, TabExecutor {

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
            player.sendMessage("Correct usage: /attack <character name>");
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
        if (RpCore.getInstance().getWeaponManager().isCustomSpell(item)) {
            player.sendMessage(ChatColor.RED + "Spell usage is not allowed with a crit attack");
            return false;
        }
        if (RpCore.getInstance().getWeaponManager().isCustomWeapon(item)) {
            CustomWeapon customWeapon = RpCore.getInstance().getWeaponManager().getCustomWeapon(item);

            if (player.getLocation().distance(target.getAssignedPlayer().getLocation()) > customWeapon.getRange()+0.5) {
                player.sendMessage(ChatColor.RED + target.getName() + " is out of range for this weapon!");
                return false;
            }

            String weaponAttribute = character.getSpecialtySkills().getParentAttributeName(customWeapon.getSpecialtySkill());

            //Roll for success of attack
            int attribute = character.getAttributes().getAttribute(weaponAttribute);
            int general = character.getSpecialtySkills().getParentGeneralSkill(customWeapon.getSpecialtySkill(), character);
            int specialty = character.getSpecialtySkills().getSpecialtySkill(customWeapon.getSpecialtySkill());

            String diceAttribute = RollUtil.getDice(attribute);
            String diceGeneral = RollUtil.getDice(general);
            String diceSpecialty = RollUtil.getDice(specialty);

            int attributeRoll = RollUtil.roll(attribute);
            int generalRoll = RollUtil.roll(general);
            int specialtyRoll = RollUtil.roll(specialty);

            RollSpecialtyResult successRoll = new RollSpecialtyResult(customWeapon.getSpecialtySkill().replace("_", " "), weaponAttribute.replace("_", " "), character.getSpecialtySkills().getParentGeneralSkillName(customWeapon.getSpecialtySkill()).replace("_", " "), diceAttribute + " + (" + diceGeneral + " + " + diceSpecialty + ")", attributeRoll, generalRoll, specialtyRoll);

            //Roll for damage
            String dice = customWeapon.getDamageDice();
            RollResult attackRoll = new RollResult();
            attackRoll.setAttribute("weapon crit");
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
            int damageAttributeRoll = RollUtil.roll(character.getAttributes().getAttribute(weaponAttribute));
            String sum = RollUtil.getDice(character.getAttributes().getAttribute(weaponAttribute)) + " + " + customWeapon.getDamageDice();
            WeaponDamageRoll dmgRoll;
            if (attackRoll.getDice2Result() != 0)
                dmgRoll = new WeaponDamageRoll(weaponAttribute, weaponAttribute + " + " + customWeapon.getDamageDice(), sum, damageAttributeRoll + " + " + attackRoll.getDice1Result() + " + " + attackRoll.getDice2Result());
            else
                dmgRoll = new WeaponDamageRoll(weaponAttribute, weaponAttribute + " + " + customWeapon.getDamageDice(), sum, damageAttributeRoll + " + " + attackRoll.getDice1Result());

            EnChatUtil.sendRollCritWeaponSuccessMessage(character, target, successRoll, encounter.getAllPlayers());
            AttackAction attackAction = new AttackAction(player, target.getAssignedPlayer(), successRoll.getResult(), (int) ((attackRoll.getResult()+damageAttributeRoll)*1.5), attackRoll);
            attackAction.setWeaponDamageRoll(dmgRoll);
            attackAction.setCrit(true);
            encounter.setCurrentAttackAction(attackAction);
        } else {
            if (player.getLocation().distance(target.getAssignedPlayer().getLocation()) > 2.5) {
                player.sendMessage(ChatColor.RED + target.getName() + " is out of range for this attack! The default range is 2 blocks.");
                return false;
            }

            //Roll for success
            String dice = RollUtil.getDice(character.getAttributes().agility) + " + " + RollUtil.getDice(character.getGeneralSkills().athletics);
            int agility = RollUtil.roll(character.getAttributes().agility);
            int athletics = RollUtil.roll(character.getGeneralSkills().athletics);
            RollResult rollResult = new RollResult(null, "agility + athletics", dice, agility, athletics);

            EnChatUtil.sendRollCritFistSuccessMessage(character, target, rollResult, encounter.getAllPlayers());

            //Roll for damage
            String diceDmg = RollUtil.getDice(character.getAttributes().strength);
            int strength = RollUtil.roll(character.getAttributes().strength);
            //int melee = RollUtil.roll(character.getGeneralSkills().melee);
            RollResult rollResultDmg = new RollResult("default crit","strength", diceDmg, strength, 0);

            AttackAction attackAction = new AttackAction(player, target.getAssignedPlayer(), rollResult.getResult(), (int) (rollResultDmg.getResult()*1.5), rollResultDmg);
            attackAction.setCrit(true);
            encounter.setCurrentAttackAction(attackAction);
        }
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
