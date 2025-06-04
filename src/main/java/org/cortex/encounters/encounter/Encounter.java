package org.cortex.encounters.encounter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.cortex.core.RpCore;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.core.scoreboard.GameScoreboard;
import org.cortex.core.util.RollResult;
import org.cortex.core.weapons.SpellType;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.listener.MovementListener;
import org.cortex.encounters.listener.PlayerDamageListener;
import org.cortex.encounters.util.EnChatUtil;
import org.cortex.rpchat.util.ChatUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Encounter {

    private final String name;
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> deadPlayers = new ArrayList<>();
    private final GameScoreboard gameScoreboard;
    private GameState gameState = GameState.WAITING;
    private Player attacker;
    private int attackTurn = -1;
    private AttackAction currentAttackAction;
    private boolean attackCompleted = false;
    private ArrayList<Player> defensiveStance = new ArrayList<>();
    private BukkitTask startTimer;
    private BukkitTask endTimer;
    private BukkitTask passTimer;

    public Encounter(String name) {
        this.name = name;
        gameScoreboard = new GameScoreboard(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: " + ChatColor.GRAY + name);
        gameScoreboard.addBlankLine();
    }

    public void preStart() {
        gameState = GameState.COUNTDOWN;
        AtomicInteger i = new AtomicInteger(14*4); //TODO put back to 45 sec
        final BukkitTask[] task = new BukkitTask[1];

        task[0] = Bukkit.getScheduler().runTaskTimerAsynchronously(Encounters.getInstance(), () -> {
            int current = i.get();
            int secondsLeft = current / 4;

            players.forEach(player -> {
                player.sendTitle(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Starting in",
                        secondsLeft + " seconds",
                        0, 22, 0);

                // SOUND LOGIC
                if (secondsLeft >= 10 && current % 4 == 0) {
                    // 1 time per second
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                } else if (secondsLeft < 10 && secondsLeft >= 3 && current % 2 == 0) {
                    // 2 times per second
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                } else if (secondsLeft < 3) {
                    // 4 times per second
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                }
            });

            if (i.decrementAndGet() == 2) {
                Bukkit.getScheduler().cancelTask(task[0].getTaskId());
                players.forEach(player -> {
                    player.sendTitle(
                        ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Encounter",
                        name + " has started",
                        0, 30, 20);
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1, 2);
                });
                Bukkit.getScheduler().runTask(RpCore.getInstance(), this::start);
            }
        }, 0L, 5L);
        startTimer = task[0];
    }

    public void start() {
        gameState = GameState.STARTED;
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

        nextAttacker(true);
    }

    public void completeAttackAction() {
        getCurrentAttackAction().setFinished(true);
        RpCharacter characterAttacker = RpCore.getInstance().getPlayerManager().getRpPlayer(currentAttackAction.getAttacker()).getCharacter();
        RpCharacter characterDefender = RpCore.getInstance().getPlayerManager().getRpPlayer(currentAttackAction.getDefender()).getCharacter();
        if (currentAttackAction.getDamageSuccessRoll() > currentAttackAction.getDefensiveRoll()) {
            //sendMessageToAll(characterAttacker.getName() + " successfully attacked " + characterDefender.getName());
            EnChatUtil.sendRollAttackMessage(currentAttackAction, characterAttacker, characterDefender, currentAttackAction.getDamageRollResult(), currentAttackAction.getWeaponDamageRoll(), getAllPlayers());
            PlayerDamageListener.allowNextDamage(characterDefender.getAssignedPlayer());

            int armorDamageReduction = 0;
            if (currentAttackAction.getDefender().getInventory().getBoots() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(currentAttackAction.getDefender().getInventory().getBoots()))
                armorDamageReduction = armorDamageReduction + RpCore.getInstance().getWeaponManager().getCustomArmor(currentAttackAction.getDefender().getInventory().getBoots()).getDamageReduction();
            if (currentAttackAction.getDefender().getInventory().getLeggings() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(currentAttackAction.getDefender().getInventory().getLeggings()))
                armorDamageReduction = armorDamageReduction + RpCore.getInstance().getWeaponManager().getCustomArmor(currentAttackAction.getDefender().getInventory().getLeggings()).getDamageReduction();
            if (currentAttackAction.getDefender().getInventory().getChestplate() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(currentAttackAction.getDefender().getInventory().getChestplate()))
                armorDamageReduction = armorDamageReduction + RpCore.getInstance().getWeaponManager().getCustomArmor(currentAttackAction.getDefender().getInventory().getChestplate()).getDamageReduction();
            if (currentAttackAction.getDefender().getInventory().getHelmet() != null && RpCore.getInstance().getWeaponManager().isCustomArmor(currentAttackAction.getDefender().getInventory().getHelmet()))
                armorDamageReduction = armorDamageReduction + RpCore.getInstance().getWeaponManager().getCustomArmor(currentAttackAction.getDefender().getInventory().getHelmet()).getDamageReduction();

            //Bypass armor reduction with spell attacks
            if (currentAttackAction.getCustomSpell() != null)
                armorDamageReduction = 0;

            if (currentAttackAction.getCustomSpell() != null && currentAttackAction.getCustomSpell().getSpellType() == SpellType.HEAL) {
                double currentHealth = currentAttackAction.getDefender().getHealth();
                double maxHealth = Objects.requireNonNull(currentAttackAction.getDefender().getAttribute(Attribute.MAX_HEALTH)).getValue();
                double newHealth = currentHealth + currentAttackAction.getDamageRoll();
                currentAttackAction.getDefender().setHealth(Math.min(newHealth, maxHealth));
            } else if (currentAttackAction.getDamageRoll() > armorDamageReduction) {
                int finalDamage = currentAttackAction.getDamageRoll() - armorDamageReduction;
                currentAttackAction.getDefender().damage(finalDamage, getAttacker());
                if (currentAttackAction.getCustomSpell() == null && armorDamageReduction > 0)
                 sendMessageToAll(characterDefender.getName() + " wears armor (" + armorDamageReduction + ") and takes " + finalDamage + " damage");
            } else if (currentAttackAction.getCustomSpell() == null && armorDamageReduction > 0) {
                    sendMessageToAll(characterDefender.getName() + " wears armor (" + armorDamageReduction + ") and takes no damage");
            }

            //Add spell effect
            if (currentAttackAction.getCustomSpell() != null) {
                currentAttackAction.getDefender().addPotionEffect(new PotionEffect(currentAttackAction.getCustomSpell().getPotionEffectType(), 140, 1, true, true, true));
                sendMessageToAll(characterDefender.getName() + " has been casted " + currentAttackAction.getCustomSpell().getPotionEffectTypeString().replace("_", " "));
                EnChatUtil.sendSpellAttackMessage(currentAttackAction, getAllPlayers());
            }
        } else {
            sendMessageToAll(characterDefender.getName() + " successfully defended from " + characterAttacker.getName() + "'s attack");
        }

        nextAttacker(false);
    }

    public void nextAttacker(boolean firstRound) {
        Bukkit.getScheduler().runTaskLater(Encounters.getInstance(), ()->{
            attackCompleted = false;
            if (attackTurn >= players.size()-1) {
                attackTurn = 0;
            } else {
                attackTurn++;
            }
            attacker = players.get(attackTurn);

            attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);

            int movementPoints = RpCore.getInstance().getPlayerManager().getRpPlayer(attacker).getCharacter().getAttributes().agility + RpCore.getInstance().getPlayerManager().getRpPlayer(attacker).getCharacter().getGeneralSkills().athletics;
            MovementListener.moveMap.put(attacker.getUniqueId(), movementPoints + 3);

            updateScoreboard();

            if (firstRound)
                sendMessageToAll(RpCore.getInstance().getPlayerManager().getRpPlayer(attacker).getCharacter().getName() + " is first to attack!");
            else
                sendMessageToAll(RpCore.getInstance().getPlayerManager().getRpPlayer(attacker).getCharacter().getName() + " is next to attack!");
            EnChatUtil.sendMovementPointsMessage(attacker, getAllPlayers());
        }, 80L);
    }

    public void endCountdown() {
        gameState = GameState.ENDING;
        AtomicInteger i = new AtomicInteger(30);
        final BukkitTask[] task = new BukkitTask[1];

        task[0] = Bukkit.getScheduler().runTaskTimerAsynchronously(Encounters.getInstance(), () -> {
            players.forEach(player -> {
                player.sendTitle(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Ending in",
                        i.get() + " seconds",
                        0, 22, 0);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "To cancel and resume the encounter: /encounter resume"));
            });

            if (i.decrementAndGet() == -1) {
                Bukkit.getScheduler().cancelTask(task[0].getTaskId());
                players.forEach(player -> {
                    player.sendTitle(
                            ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Encounter",
                            name + " has ended",
                            0, 30, 20);
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1, 1);
                });
                end();
            }
        }, 0L, 20L);
        endTimer = task[0];
    }

    public void end() {
        if (startTimer != null)
            startTimer.cancel();
        if (endTimer != null && !endTimer.isCancelled())
            endTimer.cancel();
        if (passTimer != null && !passTimer.isCancelled())
            passTimer.cancel();
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
            RpCore.getInstance().getScoreboardManager().removeGameScoreboard(player);
        });
        deadPlayers.forEach(player -> {
            Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().remove(player);
            RpCore.getInstance().getScoreboardManager().removeGameScoreboard(player);
        });
    }

    public void passTurn() {
        AtomicInteger i = new AtomicInteger(30);
        final BukkitTask[] task = new BukkitTask[1];

        task[0] = Bukkit.getScheduler().runTaskTimerAsynchronously(Encounters.getInstance(), () -> {
            attacker.sendTitle(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Turn passing in",
                    i.get() + " seconds",
                    0, 22, 0);
            attacker.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "To cancel and resume the encounter: /encounter cancel-pass"));

            if (i.decrementAndGet() == -1) {
                Bukkit.getScheduler().cancelTask(task[0].getTaskId());
                sendMessageToAll("Pass turned");
                nextAttacker(false);
                passTimer = null;
            }
        }, 0L, 20L);
        passTimer = task[0];
    }

    public void updateScoreboard() {
        gameScoreboard.clearScoreboard();
        gameScoreboard.addBlankLine();
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

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void setPassTimer(BukkitTask passTimer) {
        this.passTimer = passTimer;
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
        RpCore.getInstance().getScoreboardManager().removeGameScoreboard(player);
        sendMessageToAll(RpCore.getInstance().getPlayerManager().getRpPlayer(player).getCharacter().getName() + " left the encounter");

        if (players.isEmpty()) {
            Encounters.getInstance().getEncounterManager().getEncounters().remove(this);
            Encounters.getInstance().getEncounterManager().getPlayerEncounterMap().remove(player);
        }
    }

    public String getName() {
        return name;
    }

    public List<Player> getAllPlayers() {
        return Stream.concat(players.stream(), deadPlayers.stream()).toList();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Player> getDeadPlayers() {
        return deadPlayers;
    }

    public GameState getGameState() {
        return gameState;
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

    public BukkitTask getEndTimer() {
        return endTimer;
    }

    public BukkitTask getPassTimer() {
        return passTimer;
    }

    private String healthString(double value) {
        String fullHeart = "❤";
        String halfHeart = "💔";

        int renderedHealth = (int) Math.ceil(value);

        int fullHearts = renderedHealth / 2;
        boolean hasHalfHeart = (renderedHealth % 2) != 0;

        StringBuilder sb = new StringBuilder();
        sb.append(fullHeart.repeat(Math.max(0, fullHearts)));
        if (hasHalfHeart) {
            sb.append(halfHeart);
        }

        return sb.toString();
    }

}
