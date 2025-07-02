package org.cortex.encounters.command;

import org.bukkit.Bukkit;
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
import org.cortex.encounters.encounter.GameState;
import org.cortex.encounters.listener.MovementListener;
import org.cortex.rpchat.util.ChatUtil;

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
            if (!(arguments.length > 1)) {
                player.sendMessage(ChatColor.RED + "Encounter name can not be empty.");
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
            ChatUtil.sendMessageInVicinity(player, 20, true, ChatColor.DARK_AQUA + character.getName() + " created encounter " + ChatColor.BOLD + encounter.getName());
            //player.sendMessage(ChatColor.GREEN + "Created encounter " + encounter.getName());
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
            if (encounter.getGameState() != GameState.WAITING && encounter.getGameState() != GameState.COUNTDOWN) {
                encounter.sendMessageToAll(character.getName() + " joined this ongoing encounter.");
                Bukkit.getLogger().info(character.getName() + "/" + player.getName() + " joined ongoing encounter: " + encounter.getName());
            } else
                encounter.sendMessageToAll(character.getName() + " joined this encounter.");

            encounter.addPlayer(player);
            player.sendMessage(ChatColor.GREEN + "You joined encounter " + encounter.getName());
        } else if (arguments[0].equalsIgnoreCase("start")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            if (encounter.getGameState() != GameState.WAITING) {
                player.sendMessage(ChatColor.RED + "This encounter has already begun.");
                return false;
            }
            //TODO remove
            /*if (encounter.getPlayers().size() == 1) {
                player.sendMessage(ChatColor.RED + "You need one more player to start this encounter.");
                return false;
            }*/
            encounter.preStart();
        } else if (arguments[0].equalsIgnoreCase("leave")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);

            if (encounter.getGameState() != GameState.WAITING) {
                boolean canLeave = true;
                for (Player player_ : encounter.getPlayers()) {
                    if (player != player_ && player.getLocation().distance(player_.getLocation()) <= 20)
                        canLeave = false;
                }

                if (!canLeave) {
                    player.sendMessage(ChatColor.RED + "You are too close to another player in this encounter. You must be at least 20 blocks away from any player.");
                    return false;
                }
                if (encounter.getAttacker() != player) {
                    player.sendMessage(ChatColor.RED + "You can only leave an encounter when it's your turn.");
                    return false;
                }
            }

            encounter.removePlayer(player);
            if (encounter.getAttacker() == player)
                encounter.nextAttacker(false);
            player.sendMessage(ChatColor.GRAY + "You left encounter " + encounter.getName());
        } else if (arguments[0].equalsIgnoreCase("end")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            if (encounter.getGameState() == GameState.ENDING) {
                player.sendMessage(ChatColor.RED + "Encounter is already ending");
                return false;
            }
            encounter.endCountdown();
        } else if (arguments[0].equalsIgnoreCase("info")) {
            player.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "________ Encounter Info ________");
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "- To attack another player: /attack <target name>");
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "- To crit attack another player: /attackcrit <target name>");
            player.sendMessage(ChatColor.GRAY + "(costs 4x labour points)");
            player.sendMessage(ChatColor.GRAY + "(multiplies damage by x1.5 if successfully)");
            player.sendMessage("");
            Bukkit.getScheduler().runTaskLaterAsynchronously(Encounters.getInstance(), () -> {
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "- To throw your weapon to another player: /attackthrow <target name>");
                player.sendMessage(ChatColor.GRAY + "(rolls 'throwing' specialty skill)");
                player.sendMessage(ChatColor.GRAY + "(transfers the weapon to the defended regardless of success)");
                player.sendMessage("");
            }, 80L);
            Bukkit.getScheduler().runTaskLaterAsynchronously(Encounters.getInstance(), () -> {
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "- When the encounter has started movement is limited to 5 blocks per turn");
                player.sendMessage("");
            }, 160L);
            Bukkit.getScheduler().runTaskLaterAsynchronously(Encounters.getInstance(), () -> {
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "- To take the defensive stance and pass your turn: /ds");
                player.sendMessage(ChatColor.GRAY + "(pass your turn and get 2 extra defense points when attacked)");
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "- To pash your turn and get double movement points: /dash");
                player.sendMessage(ChatColor.GRAY + "- You can only move around if you are the attacking player");
                player.sendMessage(ChatColor.GRAY + "- You will get 3 movement points (blocks) + your agility and athletics level");
                player.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "_____________________________________________________");
            }, 240L);
        } else if (arguments[0].equalsIgnoreCase("resume")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            if (encounter.getGameState() != GameState.ENDING) {
                player.sendMessage(ChatColor.RED + "Encounter is not ending.");
                return false;
            }
            encounter.getEndTimer().cancel();
            encounter.sendMessageToAll("Encounter resumed");
            encounter.setGameState(GameState.STARTED);
        } else if (arguments[0].equalsIgnoreCase("pass-turn")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter.");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            if (encounter.getGameState() != GameState.STARTED) {
                player.sendMessage(ChatColor.RED + "Encounter has not started or is ending.");
                return false;
            }
            if (encounter.getPassTimer() != null) {
                player.sendMessage(ChatColor.RED + "A pass turn has already been initiated");
                return false;
            }
            if (encounter.isAttackCompleted() || MovementListener.dash.contains(encounter.getAttacker())) {
                player.sendMessage(ChatColor.RED + "An attack has already been initiated");
                return false;
            }
            encounter.passTurn();
            RpCharacter attacker = RpCore.getInstance().getPlayerManager().getRpPlayer(encounter.getAttacker()).getCharacter();
            encounter.sendMessageToAll("A countdown of 30 seconds has started to pass the turn of " + attacker.getName());
        } else if (arguments[0].equalsIgnoreCase("cancel-pass")) {
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(player)) {
                player.sendMessage(ChatColor.RED + "You are not in an encounter");
                return false;
            }
            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(player);
            if (player != encounter.getAttacker()) {
                player.sendMessage(ChatColor.RED + "Only the attacker can use this command");
                return false;
            }
            if (encounter.getPassTimer() == null) {
                player.sendMessage(ChatColor.RED + "A turn pass has not been initiated");
                return false;
            }
            encounter.getPassTimer().cancel();
            encounter.setPassTimer(null);
            RpCharacter attacker = RpCore.getInstance().getPlayerManager().getRpPlayer(encounter.getAttacker()).getCharacter();
            encounter.sendMessageToAll(attacker.getName() + " has canceled the turn pass");
        } else if (arguments[0].equalsIgnoreCase("kick")) {
            if (!player.hasPermission("cortex.admin")) {
                player.sendMessage("You do not have permission to use this command");
                return false;
            }
            String targetName;

            if (arguments.length == 3) {
                targetName = arguments[1] + " " + arguments[2];
            } else if (arguments.length == 2) {
                targetName = arguments[1];
            } else {
                player.sendMessage("Correct usage: /encounter kick <character name>");
                return false;
            }

            RpCharacter target = RpCore.getInstance().getPlayerManager().getCharacter(targetName);

            if (target == null) {
                player.sendMessage(ChatColor.RED + "Character " + targetName + " does not exist");
                return false;
            }

            Encounter encounter = Encounters.getInstance().getEncounterManager().getEncounter(target.getAssignedPlayer());
            if (!Encounters.getInstance().getEncounterManager().isInEncounter(target.getAssignedPlayer())) {
                player.sendMessage(ChatColor.RED + target.getName() + " is not in an encounter");
                return false;
            }

            //Set attack turn -1 else next player will be skipped
            encounter.setAttackTurn(encounter.getAttackTurn()-1);
            encounter.removePlayer(target.getAssignedPlayer());
            if (encounter.getAttacker() == target.getAssignedPlayer() && !encounter.isAttackCompleted())
                encounter.nextAttacker(false);

            player.sendMessage(ChatColor.GREEN + "Removed " + target.getName() + " from their encounter");
        } else if (arguments[0].equalsIgnoreCase("list")) {
            if ( Encounters.getInstance().getEncounterManager().getEncounters().isEmpty()) {
                player.sendMessage(ChatColor.RED + "There are no active encounters");
                return false;
            }
            player.sendMessage(ChatColor.BLUE + "Active encounters:");
            for (Encounter activeEncounter : Encounters.getInstance().getEncounterManager().getEncounters()) {
                player.sendMessage(ChatColor.GRAY + "- " + activeEncounter.getName());
            }
        } else if (arguments[0].equalsIgnoreCase("force")) {
            if (!player.hasPermission("cortex.admin")) {
                player.sendMessage(ChatColor.RED + "You can not use this command");
                return false;
            }
            if (Encounters.getInstance().getEncounterManager().getEncounters().isEmpty()) {
                player.sendMessage(ChatColor.RED + "There are no active encounters");
                return false;
            }
            if (arguments.length < 3) {
                player.sendMessage(ChatColor.RED + "Correct usage: /encounter force <player> <encounter name>");
                return false;
            }
            Player target = Bukkit.getServer().getPlayer(arguments[1]);
            if (target == null) {
                commandSender.sendMessage(ChatColor.RED + "That player is not found");
                return false;
            }

            RpPlayer rpPlayer_ = RpCore.getInstance().getPlayerManager().getRpPlayer(target);
            RpCharacter character_ = rpPlayer_.getCharacter();

            StringBuilder sb = new StringBuilder();
            String[] args = Arrays.copyOfRange(arguments, 2, arguments.length);
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
            if (Encounters.getInstance().getEncounterManager().isInEncounter(target) && Encounters.getInstance().getEncounterManager().getEncounter(target) == encounter) {
                player.sendMessage(ChatColor.GREEN + "Player " + rpPlayer_.getOriginalPlayerName() + "/" + target.getName() + " is already part of encounter " + encounter.getName());
                return false;
            }
            if (Encounters.getInstance().getEncounterManager().isInEncounter(target)) {
                Encounters.getInstance().getEncounterManager().getEncounter(player).removePlayer(player);
            }

            if (encounter.getGameState() != GameState.WAITING && encounter.getGameState() != GameState.COUNTDOWN) {
                encounter.sendMessageToAll(character_.getName() + " joined this ongoing encounter.");
                Bukkit.getLogger().info(character_.getName() + "/" + target.getName() + " joined ongoing encounter: " + encounter.getName());
            } else
                encounter.sendMessageToAll(character_.getName() + " joined this encounter.");

            target.sendMessage(ChatColor.GREEN + "You joined encounter " + encounter.getName());
            encounter.addPlayer(target);
        } else if (arguments[0].equalsIgnoreCase("force-end")) {
            if (!player.hasPermission("cortex.admin")) {
                player.sendMessage(ChatColor.RED + "You can not use this command");
                return false;
            }
            if (Encounters.getInstance().getEncounterManager().getEncounters().isEmpty()) {
                player.sendMessage(ChatColor.RED + "There are no active encounters");
                return false;
            }
            if (arguments.length < 2) {
                player.sendMessage(ChatColor.RED + "Correct usage: /encounter force-end <encounter name>");
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
            encounter.end();
            player.sendMessage(ChatColor.GREEN + "You force ended encounter " + encounter.getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        if (!(commandSender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1 && commandSender.hasPermission("cortex.admin"))
            return List.of("create", "join", "start", "list", "leave", "end", "info", "resume", "pass-turn", "cancel-pass", "kick", "force", "force-end");
        else if (args.length == 1)
            return List.of("create", "join", "start", "list", "leave", "end", "info", "resume", "pass-turn", "cancel-pass");
        else if (args.length == 2 && args[0].equalsIgnoreCase("force") && commandSender.hasPermission("cortex.admin")) {
            List<String> list = new ArrayList<>();
            RpCore.getInstance().getPlayerManager().getRpPlayers().forEach(rpPlayer -> list.add(rpPlayer.getOriginalPlayerName()));
            return list;
        }
        return List.of();
    }
}
