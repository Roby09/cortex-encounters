package org.cortex.encounters.encounter;

import org.bukkit.entity.Player;
import org.cortex.core.util.RollResult;
import org.cortex.core.weapons.CustomSpell;
import org.cortex.encounters.util.WeaponDamageRoll;

public class AttackAction {

    private Player attacker;
    private Player defender;

    private int damageSuccessRoll;
    private int damageRoll;
    private int defensiveRoll;
    private RollResult damageRollResult;
    private boolean finished = false;
    private boolean crit = false;
    private CustomSpell customSpell;
    private WeaponDamageRoll weaponDamageRoll;

    public AttackAction(Player attacker, Player defender, int damageSuccessRoll, int damageRoll, RollResult damageRollResult) {
        this.attacker = attacker;
        this.defender = defender;
        this.damageSuccessRoll = damageSuccessRoll;
        this.damageRoll = damageRoll;
        this.damageRollResult = damageRollResult;
    }

    public void setDefensiveRoll(int defensiveRoll) {
        this.defensiveRoll = defensiveRoll;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setCrit(boolean crit) {
        this.crit = crit;
    }

    public void setCustomSpell(CustomSpell customSpell) {
        this.customSpell = customSpell;
    }

    public void setWeaponDamageRoll(WeaponDamageRoll weaponDamageRoll) {
        this.weaponDamageRoll = weaponDamageRoll;
    }

    public boolean isCrit() {
        return crit;
    }

    public Player getAttacker() {
        return attacker;
    }

    public Player getDefender() {
        return defender;
    }

    public int getDamageSuccessRoll() {
        return damageSuccessRoll;
    }

    public int getDamageRoll() {
        return damageRoll;
    }

    public RollResult getDamageRollResult() {
        return damageRollResult;
    }

    public int getDefensiveRoll() {
        return defensiveRoll;
    }

    public CustomSpell getCustomSpell() {
        return customSpell;
    }

    public WeaponDamageRoll getWeaponDamageRoll() {
        return weaponDamageRoll;
    }

    public boolean isFinished() {
        return finished;
    }
}
