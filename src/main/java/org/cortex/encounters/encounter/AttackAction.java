package org.cortex.encounters.encounter;

import org.bukkit.entity.Player;

public class AttackAction {

    private Player attacker;
    private Player defender;

    private int damageAttacker;
    private int defensiveRoll;

    public AttackAction(Player attacker, Player defender, int damageAttacker) {
        this.attacker = attacker;
        this.defender = defender;
        this.damageAttacker = damageAttacker;
    }

    public void setDefensiveRoll(int defensiveRoll) {
        this.defensiveRoll = defensiveRoll;
    }

    public Player getAttacker() {
        return attacker;
    }

    public Player getDefender() {
        return defender;
    }

    public int getDamageAttacker() {
        return damageAttacker;
    }

    public int getDefensiveRoll() {
        return defensiveRoll;
    }

}
