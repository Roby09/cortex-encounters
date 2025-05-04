package org.cortex.encounters.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cortex.core.RpCore;
import org.cortex.core.player.RpPlayer;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.Encounter;

import java.util.Arrays;

public class EncounterCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        RpPlayer rpPlayer = RpCore.getInstance().getPlayerManager().getRpPlayer(player);
        RpCharacter character = rpPlayer.getCharacter();

        if (character == null) {
            player.sendMessage(ChatColor.RED + "You do not have a character yet.");
            return false;
        }

        if (!(arguments.length > 1)) {
            commandSender.sendMessage("Correct usage: /encounter <subcommand> <input>");
            return false;
        }
        
        if (arguments[0].equalsIgnoreCase("create")) {
            StringBuilder sb = new StringBuilder();
            String[] args = Arrays.copyOfRange(arguments, 1, arguments.length);
            for (String argument : args) {
                sb.append(" ").append(argument);
            }
            if (sb.length() > 16) {
                player.sendMessage(ChatColor.RED + "Encounter name can not be greater than 16 characters.");
                return false;
            }
            String name = sb.toString().trim();

            Encounter encounter = new Encounter(name);
            encounter.addPlayer(player);
            Encounters.getInstance().getEncounterManager().addEncounter(encounter);
            player.sendMessage(ChatColor.GREEN + "Created encounter " + encounter.getName());
        } else if (arguments[0].equalsIgnoreCase("join")) {
            if (arguments.length != 2) {
                commandSender.sendMessage("Correct usage: /encounter join name");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(arguments[1]);
            if (encounter == null) {
                player.sendMessage(ChatColor.RED + "Encounter not found, these are the active encounters:");
                for (Encounter activeEncounter : Encounters.getInstance().getEncounterManager().getEncounters()) {
                    player.sendMessage(ChatColor.GRAY + "- " + activeEncounter.getName());
                }
                return false;
            }
            encounter.getPlayers().forEach(player1 -> player1.sendMessage(player.getName() + " joined this encounter."));
            encounter.addPlayer(player);
            player.sendMessage(ChatColor.GREEN + "You joined encounter " + encounter.getName());
        }

        return true;
    }
}
