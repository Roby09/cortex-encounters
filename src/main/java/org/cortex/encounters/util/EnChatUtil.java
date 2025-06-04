package org.cortex.encounters.util;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cortex.core.RpCore;
import org.cortex.core.player.character.RpCharacter;
import org.cortex.core.util.RollResult;
import org.cortex.core.util.RollSpecialtyResult;
import org.cortex.encounters.Encounters;
import org.cortex.encounters.encounter.AttackAction;
import org.cortex.encounters.encounter.Encounter;
import org.cortex.encounters.listener.MovementListener;

import java.util.List;

public class EnChatUtil {

    public static void sendMovementPointsMessage(Player attacker, List<Player> sendTo) {
        RpCharacter rpCharacter = RpCore.getInstance().getPlayerManager().getRpPlayer(attacker).getCharacter();

        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " has " + MovementListener.moveMap.get(attacker.getUniqueId()) + " ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "movement points");
        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("agility + athletics" + "\n" + rpCharacter.getAttributes().agility + " + " + rpCharacter.getGeneralSkills().athletics).create()));

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent));
    }

    public static void sendRollFistSuccessMessage(RpCharacter attacker, RpCharacter defender, RollResult rollResult, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " attacks " + ChatColor.GOLD + defender.getName()
                + ChatColor.GRAY + " and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result() + " + " + rollResult.getDice2Result()).create()));
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + rollResult.getResult() + ChatColor.GRAY + ">");

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

    public static void sendRollCritWeaponSuccessMessage(RpCharacter attacker, RpCharacter defender, RollSpecialtyResult rollResult, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " is attempting a crit attack against " + ChatColor.GOLD + defender.getName()
                + ChatColor.GRAY + " using a special weapon and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result() + " + (" + rollResult.getDice2Result() + " + " + rollResult.getDice3Result() + ")").create()));
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + rollResult.getResult() + ChatColor.GRAY + ">");

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

    public static void sendRollCritFistSuccessMessage(RpCharacter attacker, RpCharacter defender, RollResult rollResult, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " is attempting a crit attack against " + ChatColor.GOLD + defender.getName()
                + ChatColor.GRAY + " and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result() + " + " + rollResult.getDice2Result()).create()));
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + rollResult.getResult() + ChatColor.GRAY + ">");

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

    public static void sendRollSpecialWeaponSuccessMessage(RpCharacter attacker, RpCharacter defender, RollSpecialtyResult rollResult, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " attacks " + ChatColor.GOLD + defender.getName()
                + ChatColor.GRAY + " with a special weapon and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        diceComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result() + " + (" + rollResult.getDice2Result() + " + " + rollResult.getDice3Result() + ")").create()));
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + rollResult.getResult() + ChatColor.GRAY + ">");

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

    public static void sendRollForcedSpecialtySpellSuccessMessage(RpCharacter attacker, RpCharacter defender, String forcedSpecialty, RollSpecialtyResult rollResult, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " casts a spell towards " + ChatColor.GOLD + defender.getName()
                + ChatColor.GRAY + " and forces them to roll for " + forcedSpecialty);
        TextComponent textComponent_ = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + " ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        diceComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result() + " + (" + rollResult.getDice2Result() + " + " + rollResult.getDice3Result() + ")").create()));
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + rollResult.getResult() + ChatColor.GRAY + ">");

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent));
        sendTo.forEach(player -> player.spigot().sendMessage(textComponent_, diceComponent, textComponent2));
    }

    public static void sendRollSpellSuccessMessage(RpCharacter attacker, RpCharacter defender, RollSpecialtyResult rollResult, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " casts a spell towards " + ChatColor.GOLD + defender.getName()
                + ChatColor.GRAY + " and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        diceComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result() + " + (" + rollResult.getDice2Result() + " + " + rollResult.getDice3Result() + ")").create()));
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + rollResult.getResult() + ChatColor.GRAY + ">");

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

    public static void sendRollThrowingSuccessMessage(RpCharacter attacker, RpCharacter defender, RollResult rollResult, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " attacks " + ChatColor.GOLD + defender.getName()
                + ChatColor.GRAY + " by throwing a special weapon and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        diceComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result()).create()));
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + rollResult.getResult() + ChatColor.GRAY + ">");

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

    public static void sendRollAttackMessage(AttackAction attackAction, RpCharacter attacker, RpCharacter defender, RollResult rollResult, WeaponDamageRoll weaponDamageRoll, List<Player> sendTo) {
        //Throwing item message
        if (rollResult.getAttribute() != null && rollResult.getAttribute().equalsIgnoreCase("throwing"))  {
            TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                    + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " successfully thrown a weapon to " + ChatColor.GOLD + defender.getName()
                    + ChatColor.GRAY + " and does" + ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + attackAction.getDamageRoll() + ChatColor.GRAY + "> ");
            TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "damage");
            if (rollResult.getDice2Result() != 0)
                diceComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n(" + rollResult.getDice1Result() + " + " + rollResult.getDice2Result() + ") / 2").create()));
            else
                diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rollResult.getSum() + "\n" + rollResult.getDice() + "\n(" + rollResult.getDice1Result() + ") / 2").create()));
            sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent));
        }

        //Default item message
        else if (rollResult.getAttribute() != null && rollResult.getAttribute().equalsIgnoreCase("default"))  {
            TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                    + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " successfully attacked " + ChatColor.GOLD + defender.getName()
                    + ChatColor.GRAY + " and does" + ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + attackAction.getDamageRoll() + ChatColor.GRAY + "> ");
            TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "damage");
            diceComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result()).create()));
            sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent));
        }

        //Special weapon message
        else if (rollResult.getAttribute() != null && rollResult.getAttribute().equalsIgnoreCase("default weapon"))  {
            TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                    + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " successfully attacked " + ChatColor.GOLD + defender.getName()
                    + ChatColor.GRAY + " with a special weapon and does" + ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + attackAction.getDamageRoll() + ChatColor.GRAY + "> ");
            TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "damage");
            diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(weaponDamageRoll.getSum() + "\n" + weaponDamageRoll.getDice() + "\n" + weaponDamageRoll.getFinalSum()).create()));

            sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent));
        }

        //Special weapon crit message
        else if (rollResult.getAttribute() != null && rollResult.getAttribute().equalsIgnoreCase("weapon crit"))  {
            TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                    + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " successfully crit attacked " + ChatColor.GOLD + defender.getName()
                    + ChatColor.GRAY + " using a special weapon and does" + ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + attackAction.getDamageRoll() + ChatColor.GRAY + "> ");
            TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "damage");
            diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(weaponDamageRoll.getSum() + "\n" + weaponDamageRoll.getDice() + "\n(x1.5 crit bonus)\n" + weaponDamageRoll.getFinalSum()).create()));

            sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent));
        }

        //default item crit message
        else if (rollResult.getAttribute() != null && rollResult.getAttribute().equalsIgnoreCase("default crit"))  {
            TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                    + ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + " successfully crit attacked " + ChatColor.GOLD + defender.getName()
                    + ChatColor.GRAY + " and does" + ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + attackAction.getDamageRoll() + ChatColor.GRAY + "> ");
            TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "damage");
            diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("(x1.5 crit bonus)\n" + rollResult.getSum() + "\n" + rollResult.getDice() + "\n" + rollResult.getDice1Result()).create()));


            sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent));
            }
    }

    public static void sendSpellAttackMessage(AttackAction attackAction, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: " + ChatColor.GRAY + "Spell type: ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + attackAction.getCustomSpell().getSpellType().toString().toLowerCase());

        String msg = attackAction.getDamageRollResult().getDice() + "\n" + attackAction.getDamageRollResult().getDice1Result();
        if (attackAction.getDamageRollResult().getDice2Result() != 0)
            msg = msg + " + " + attackAction.getDamageRollResult().getDice2Result();

        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + attackAction.getDamageRoll() + ChatColor.GRAY + ">");
        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msg).create()));

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));


    }

    public static void sendDefenseRollMessage(RpCharacter defender, RollSpecialtyResult rollSpecialtyResult, int defensePoints, boolean defensiveStance, boolean crit, int debuff, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " chose to dodge and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + defensePoints + ChatColor.GRAY + ">");
        String msg = rollSpecialtyResult.getSum() + "\n" + rollSpecialtyResult.getDice()
                + "\n" + rollSpecialtyResult.getDice1Result() + " + " + rollSpecialtyResult.getDice2Result() + " + " + rollSpecialtyResult.getDice3Result();
        if (defensiveStance) {
            msg = msg + "\n" + "+ 2 for defensive stance";
        } if (debuff > 0)
            msg = msg + "\n" + "- " + debuff + " armor debuff";
        if (crit)
            msg = msg + "\n" + "+ 4 crit bonus";

        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msg).create()));

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent,diceComponent, textComponent2));
    }

    public static void sendDefenseForcedSpellMessage(RpCharacter defender, RollSpecialtyResult rollSpecialtyResult, boolean defensiveStance, int debuff, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " is forced to roll for " + rollSpecialtyResult.getSpecialty() + " and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        TextComponent textComponent2;
        String msg = rollSpecialtyResult.getSum() + "\n" + rollSpecialtyResult.getDice()
                + "\n" + rollSpecialtyResult.getDice1Result() + " + " + rollSpecialtyResult.getDice2Result() + " + " + rollSpecialtyResult.getDice3Result();
        if (defensiveStance) {
            msg = msg + "\n" + "+ 2 for defensive stance";
            textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + (rollSpecialtyResult.getResult() + 2 - debuff) + ChatColor.GRAY + ">");
        } else {
            textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + (rollSpecialtyResult.getResult()-debuff) + ChatColor.GRAY + ">");
        } if (debuff > 0)
            msg = msg + "\n" + "- " + debuff + " armor debuff";

        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msg).create()));

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent,diceComponent, textComponent2));
    }

    public static void sendRollDefenseShieldMessage(RpCharacter defender, RollResult rollResult, int defensePoints, boolean defensiveStance, boolean crit, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD  + "ENCOUNTER: "
                + ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " is raising a shield and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + defensePoints + ChatColor.GRAY + ">");
        String msg = rollResult.getDice() + "\n" + rollResult.getDice1Result();
        if (rollResult.getDice2Result() != 0) {
            msg = msg + " + " + rollResult.getDice2Result();;
            if (defensiveStance) {
                msg = msg + "\n+ 2 for defensive stance";
            }
        } else {
            if (defensiveStance) {
                msg = msg + "\n+ 2 points for defensive stance";
            }
        }
        if (crit)
            msg = msg + "\n" + "+ 4 crit bonus";

        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msg).create()));

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

    public static void sendBraceMessage(RpCharacter defender, int i, boolean defensiveStance, boolean crit, List<Player> sendTo) {
        TextComponent textComponent = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ENCOUNTER: "
                + ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " used a brace defense and ");
        TextComponent diceComponent = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "rolls:");
        TextComponent textComponent2 = new TextComponent(ChatColor.GRAY + " <" + ChatColor.GREEN + ChatColor.BOLD + i + ChatColor.GRAY + ">");
        String msg = "Default brace defence points: 3";
        if (defensiveStance)
            msg = msg + "\n+2 points for defensive stance";
        if (crit)
            msg = msg + "\n" + "+ 4 crit bonus";

        diceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msg).create()));

        sendTo.forEach(player -> player.spigot().sendMessage(textComponent, diceComponent, textComponent2));
    }

}
