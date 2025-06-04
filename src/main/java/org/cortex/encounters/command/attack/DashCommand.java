package org.cortex.encounters.command.attack;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.cortex.core.RpCore;
import org.cortex.core.player.RpPlayer;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;
import org.cortex.encounters.encounter.EncounterManager;
import org.cortex.encounters.listener.MovementListener;

import java.util.List;

public class DashCommand implements CommandExecutor, TabExecutor {

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
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("pass")) {
            if (MovementListener.dash.contains(player)) {
                MovementListener.dash.remove(player);
                encounter.sendMessage(player, "Turn passed");
                encounter.setAttackCompleted(true);
                encounter.nextAttacker(false);
            } else {
                player.sendMessage(ChatColor.RED + "You have not enabled a dash");
            }
            return false;
        }
        if (MovementListener.dash.contains(player)) {
            player.sendMessage(ChatColor.RED + "Your attack is already over");
            return false;
        }
        int extraPoints = (character.getAttributes().agility + character.getGeneralSkills().athletics + 3);
        MovementListener.moveMap.put(player.getUniqueId(), MovementListener.moveMap.get(player.getUniqueId())+extraPoints);
        MovementListener.dash.add(player);
        encounter.sendMessageToAll(character.getName() + " chose to dash and gets double movement points");
        encounter.sendMessage(player, "You can not attack while dashing. Your turn will pass if you use all your movement points or if you type /dash pass");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        return List.of();
    }
}
