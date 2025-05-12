package org.cortex.encounters.command;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncounterCommand implements CommandExecutor, TabExecutor {

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

        if (!(arguments.length > 0)) {
            commandSender.sendMessage("Correct usage: /encounter <subcommand> (input)");
            return false;
        }
        
        if (arguments[0].equalsIgnoreCase("create")) {
            if (Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are already in an encounter. You must leave the current encounter first.");
                return false;
            }

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

            if(Encounters.getInstance().getEncounterManager().getEncounter(name) != null) {
                player.sendMessage(ChatColor.RED + "An encounter with that name already exists.");
                return false;
            }

            Encounter encounter = new Encounter(name);
            encounter.addPlayer(player);
            Encounters.getInstance().getEncounterManager().addEncounter(encounter);
            player.sendMessage(ChatColor.GREEN + "Created encounter " + encounter.getName());
        } else if (arguments[0].equalsIgnoreCase("join")) {
            if (Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are already in an encounter. You must leave the current encounter first.");
                return false;
            }

            StringBuilder sb = new StringBuilder();
            String[] args = Arrays.copyOfRange(arguments, 1, arguments.length);
            for (String argument : args) {
                sb.append(" ").append(argument);
            }
            String name = sb.toString().trim();

            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(name);
            if (encounter == null) {
                player.sendMessage(ChatColor.RED + "Encounter not found, these are the active encounters:");
                for (Encounter activeEncounter : Encounters.getInstance().getEncounterManager().getEncounters()) {
                    player.sendMessage(ChatColor.GRAY + "- " + activeEncounter.getName());
                }
                return false;
            }
            encounter.sendMessageToAll(character.getName() + " joined this encounter.");
            encounter.addPlayer(player);
            player.sendMessage(ChatColor.GREEN + "You joined encounter " + encounter.getName());
        } else if (arguments[0].equalsIgnoreCase("start")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            if (encounter.hasStarted()) {
                player.sendMessage(ChatColor.RED + "This encounter has already begun.");
                return false;
            }
            encounter.preStart();
        } else if (arguments[0].equalsIgnoreCase("leave")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            encounter.removePlayer(player);
            if (encounter.getAttacker() == player)
                encounter.nextAttacker();
            player.sendMessage(ChatColor.GRAY + "You left encounter " + encounter.getName());
        } else if (arguments[0].equalsIgnoreCase("end")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            encounter.end();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        if (!(commandSender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1)
            return List.of("create", "join", "start", "leave", "end");
        return List.of();
    }
}
