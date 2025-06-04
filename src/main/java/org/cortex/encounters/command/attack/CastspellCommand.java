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
import org.cortex.core.weapons.CustomSpell;
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

public class CastspellCommand implements CommandExecutor, TabExecutor {

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
            player.sendMessage("Correct usage: /castspell <character name>");
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

        if (!RpCore.getInstance().getWeaponManager().isCustomSpell(item)) {
            player.sendMessage(ChatColor.RED + "The item in your hand is not a spell");
            return false;
        }

        CustomSpell spell = RpCore.getInstance().getWeaponManager().getCustomSpell(item);

        if (player.getLocation().distance(target.getAssignedPlayer().getLocation()) > spell.getRange()+0.5) {
            player.sendMessage(ChatColor.RED + target.getName() + " is out of range for this weapon!");
            return false;
        }

        //Roll for success of attack
        RollSpecialtyResult specialtyResult = character.getSpecialtySkills().roll(spell.getSpecialtySkill(), character.getSpecialtySkills().getSpecialtySkill(spell.getSpecialtySkill()), character.getSpecialtySkills().getParentGeneralSkillName(spell.getSpecialtySkill()), character.getSpecialtySkills().getParentAttributeName(spell.getSpecialtySkill()), character);

        //Roll for damage
        String dice = spell.getDamageDice();
        RollResult attackRoll = new RollResult();
        attackRoll.setAttribute("spell");
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

        //ATTACK here if spell has opponent specialty skill
        if (spell.getOpponentSpecialty() != null) {
            RollSpecialtyResult attackSpecialtyResult = target.getSpecialtySkills().roll(spell.getOpponentSpecialty(), target.getSpecialtySkills().getSpecialtySkill(spell.getOpponentSpecialty()), target.getSpecialtySkills().getParentGeneralSkillName(spell.getOpponentSpecialty()), target.getSpecialtySkills().getParentAttributeName(spell.getOpponentSpecialty()), character);
            encounter.setCurrentAttackAction(new AttackAction(player, target.getAssignedPlayer(), specialtyResult.getResult(), attackRoll.getResult(), attackRoll));
            encounter.getCurrentAttackAction().setDefensiveRoll(attackSpecialtyResult.getResult());
            encounter.getCurrentAttackAction().setCustomSpell(spell);
            EnChatUtil.sendRollForcedSpecialtySpellSuccessMessage(character, target, spell.getOpponentSpecialty(), specialtyResult, encounter.getAllPlayers());
            attack(player, encounter, attackSpecialtyResult);
        //ELSE open defense action menu like normal
        } else {
            EnChatUtil.sendRollSpellSuccessMessage(character, target, specialtyResult, encounter.getAllPlayers());
            encounter.setCurrentAttackAction(new AttackAction(player, target.getAssignedPlayer(), specialtyResult.getResult(), attackRoll.getResult(), attackRoll));
            encounter.getCurrentAttackAction().setCustomSpell(spell);

            ActionMenuListener.playersInMenu.add(target.getAssignedPlayer());
            target.getAssignedPlayer().openInventory(new ActionInventory().getInventory());
        }
        encounter.setAttackCompleted(true);
        return true;
    }

    private void attack(Player player, Encounter encounter, RollSpecialtyResult rollSpecialtyResult) {
        RpCharacter  character = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
        int debuff = 0;
        if (player.getInventory().getBoots() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getBoots()))
            debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getBoots()).getAgilityDebuff();
        if (player.getInventory().getLeggings() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getLeggings()))
            debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getLeggings()).getAgilityDebuff();
        if (player.getInventory().getChestplate() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getChestplate()))
            debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getChestplate()).getAgilityDebuff();
        if (player.getInventory().getHelmet() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(player.getInventory().getHelmet()))
            debuff = debuff + RpCore.getInstance().getWeaponManager().getCustomArmor(player.getInventory().getHelmet()).getAgilityDebuff();

        if (encounter.getDefensiveStance().contains(player)) {
            encounter.getCurrentAttackAction().setDefensiveRoll(rollSpecialtyResult.getResult() + 2 - debuff);
            EnChatUtil.sendDefenseForcedSpellMessage(character, rollSpecialtyResult, true, debuff, encounter.getAllPlayers());
            encounter.getDefensiveStance().remove(player);
        } else {
            encounter.getCurrentAttackAction().setDefensiveRoll(rollSpecialtyResult.getResult() - debuff);
            EnChatUtil.sendDefenseForcedSpellMessage(character, rollSpecialtyResult, false, debuff, encounter.getAllPlayers());
        }
        encounter.completeAttackAction();
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
