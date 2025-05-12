package org.cortex.encounters.encounter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.cortex.core.RpCore;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.core.scoreboard.GameScoreboard;
import org.cortex.core.util.RollResult;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.listener.PlayerDamageListener;
import org.cortex.rpchat.util.ChatUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Encounter {

    private final String name;
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> deadPlayers = new ArrayList<>();
    private final GameScoreboard gameScoreboard;
    private boolean hasStarted = false;
    private Player attacker;
    private int attackTurn = 0;
    private AttackAction currentAttackAction;
    private boolean attackCompleted = false;
    private ArrayList<Player> defensiveStance = new ArrayList<>();

    public Encounter(String name) {
        this.name = name;
        gameScoreboard = new GameScoreboard(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: " + ChatColor.GRAY + name);
        gameScoreboard.addBlankLine();
    }

    public void preStart() {
        hasStarted = true;
        AtomicInteger i = new AtomicInteger(45);
        final BukkitTask[] task = new BukkitTask[1];

        task[0] = Bukkit.getScheduler().runTaskTimerAsynchronously(Encounters.getInstance(), () -> {
            players.forEach(player -> player.sendTitle(
                    ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Starting in",
                    i.get() + " seconds",
                    0, 22, 0
            ));

            if (i.decrementAndGet() == -1) {
                Bukkit.getScheduler().cancelTask(task[0].getTaskId());
                players.forEach(player -> player.sendTitle(
                        ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Encounter",
                        name + " has started",
                        0, 30, 20
                ));
                Bukkit.getScheduler().runTask(RpCore.getInstance(), this::start);
            }
        }, 0L, 20L);
    }

    public void start() {
        sendMessageToAll(name + " has started.");

        Map<Player, Integer> initiativeRolled = new HashMap<>();
        for (Player player : players) {
            RpCharacter rpCharacter = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
            RollResult rollResult = rpCharacter.getAttributes().rollInitiative();
            ChatUtil.sendRollMessage(player, rollResult);
            int initiative = rollResult.getResult();
            initiativeRolled.put(player, initiative);
        }
        //SORT turn order
        Map<Player, Integer> sortedMap = new LinkedHashMap<>();
        initiativeRolled.entrySet()
                .stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        players.clear();
        players = new ArrayList<>(sortedMap.keySet().stream().toList());

        gameScoreboard.clearScoreboard();
        gameScoreboard.addBlankLine();

        attacker = (Player) sortedMap.keySet().toArray()[attackTurn];

        for (Player player : players) {
            RpCharacter rpCharacter = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
            if (player == attacker)
                gameScoreboard.addLine(ChatColor.GREEN + rpCharacter.getName() + " | " + ChatColor.RED + healthString(player.getHealth()));
            else
                gameScoreboard.addLine(rpCharacter.getName() + " | " + ChatColor.RED + healthString(player.getHealth()));
        }
        sendMessageToAll(RpCore.getInstance().getPlayerManager().getRpPlayer(attacker).getCharacter().getName() + " is the first to attack!");
        attacker.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "_____________________________________________________");
        attacker.sendMessage(ChatColor.GRAY + "It is your turn to attack or to hold a defensive stance");
        attacker.sendMessage(ChatColor.GRAY + "- To attack another player: /attack <target name>");
        attacker.sendMessage(ChatColor.GRAY + "- To take the defensive stance and pass your turn: /ds");
        attacker.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "_____________________________________________________");
    }

    public void completeAttackAction() {
        RpCharacter characterAttacker = RpCore.getInstance().getPlayerManager().getRpPlayer(currentAttackAction.getAttacker()).getCharacter();
        RpCharacter characterDefender = RpCore.getInstance().getPlayerManager().getRpPlayer(currentAttackAction.getDefender()).getCharacter();
        if (currentAttackAction.getDamageAttacker() > currentAttackAction.getDefensiveRoll()) {
            sendMessageToAll(characterAttacker.getName() + " successfully attacked " + characterDefender.getName());
            PlayerDamageListener.exempt.add(characterDefender.getAssignedPlayer());
            currentAttackAction.getDefender().damage(currentAttackAction.getDamageAttacker(), getAttacker());
        } else {
            sendMessageToAll(characterAttacker.getName() + " successfully defended from " + characterDefender.getName() + "'s attack");
        }

        nextAttacker();
    }

    public void nextAttacker() {
        Bukkit.getScheduler().runTaskLater(Encounters.getInstance(), ()->{
            attackCompleted = false;
            if (attackTurn >= players.size()-1) {
                attackTurn = 0;
            } else {
                attackTurn++;
            }
            attacker = players.get(attackTurn);

            updateScoreboard();

            sendMessageToAll(RpCore.getInstance().getPlayerManager().getRpPlayer(attacker).getCharacter().getName() + " is next to attack!");
            attacker.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "_____________________________________________________");
            attacker.sendMessage(ChatColor.GRAY + "It is your turn to attack or to hold a defensive stance");
            attacker.sendMessage(ChatColor.GRAY + "- To attack another player: /attack <target name>");
            attacker.sendMessage(ChatColor.GRAY + "- To take the defensive stance and pass your turn: /ds");
            attacker.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "_____________________________________________________");
        }, 80L);
    }

    public void end() {
        if (!deadPlayers.isEmpty()) {
            sendMessageToAllWithoutPrefix(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "_____________________________________________________");
            sendMessageToAllWithoutPrefix(ChatColor.GRAY + "Fallen people:");
            deadPlayers.forEach(player -> sendMessageToAllWithoutPrefix(ChatColor.GRAY + "- " + RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter().getName()));
            sendMessageToAllWithoutPrefix(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "_____________________________________________________");
        }
        sendMessageToAll(name + " has been settled and is now over");

        Encounters.getInstance().getEncounterManager().getEncounters().remove(this);
        players.forEach(player -> {
            Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().remove(player);
            RpCore.getInstance().getScoreboardManager().playerScoreboard.remove(player);
            player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
        });
        deadPlayers.forEach(player -> {
            Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().remove(player);
            RpCore.getInstance().getScoreboardManager().playerScoreboard.remove(player);
            player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
        });
    }

    public void updateScoreboard() {
        gameScoreboard.clearScoreboard();
        for (Player player : players) {
            RpCharacter rpCharacter = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
            if (player == attacker)
                gameScoreboard.addLine(ChatColor.GREEN + rpCharacter.getName() + " | " + ChatColor.RED + healthString(player.getHealth()));
            else
                gameScoreboard.addLine(rpCharacter.getName() + " | " + ChatColor.RED + healthString(player.getHealth()));
        }
        for (Player player : deadPlayers) {
            RpCharacter rpCharacter = RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter();
            gameScoreboard.addLine(rpCharacter.getName() + " | " + ChatColor.RED + "☠");
        }
    }

    public void setDeadPlayer(Player player) {
        players.remove(player);
        deadPlayers.add(player);
    }

    public void sendMessageToAll(String message) {
        players.forEach(player ->player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: " + ChatColor.GRAY + message));
    }

    public void sendMessageToAllWithoutPrefix(String message) {
        players.forEach(player ->player.sendMessage(message));
    }

    public void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: " + ChatColor.GRAY + message);
    }

    public void setAttackCompleted(boolean attackCompleted) {
        this.attackCompleted = attackCompleted;
    }

    public void setCurrentAttackAction(AttackAction currentAttackAction) {
        this.currentAttackAction = currentAttackAction;
    }

    public AttackAction getCurrentAttackAction() {
        return currentAttackAction;
    }

    public void addPlayer(Player player) {
        Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().put(player, this);
        players.add(player);
        RpCore.getInstance().getScoreboardManager().setScoreboard(player, gameScoreboard);
        updateScoreboard();
    }

    public void removePlayer(Player player) {
        Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().remove(player);
        players.remove(player);
        deadPlayers.remove(player);
        updateScoreboard();
        sendMessageToAll(RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter().getName() + " left the encounter");
    }

    public String getName() {
        return name;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Player> getDeadPlayers() {
        return deadPlayers;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public Player getAttacker() {
        return attacker;
    }

    public boolean isAttackCompleted() {
        return attackCompleted;
    }

    public ArrayList<Player> getDefensiveStance() {
        return defensiveStance;
    }

    private String healthString(double value) {
        String fullHeart = "❤";
        String halfHeart = "💔";
        StringBuilder sb = new StringBuilder();

        int fullHearts = (int) value / 2;
        boolean hasHalfHeart = ((int) value) % 2 != 0;

        sb.append(fullHeart.repeat(Math.max(0, fullHearts)));

        if (hasHalfHeart) {
            sb.append(halfHeart);
        }
        return sb.toString();
    }

}
