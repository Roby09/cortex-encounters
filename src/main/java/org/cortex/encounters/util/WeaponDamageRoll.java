package org.cortex.encounters.util;

public class WeaponDamageRoll {

    private String attribute;
    private String sum;
    private String dice;
    private String finalSum;

    public WeaponDamageRoll(String attribute, String sum, String dice, String finalSum) {
        this.attribute = attribute;
        this.sum = sum;
        this.dice = dice;
        this.finalSum = finalSum;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getSum() {
        return sum;
    }

    public String getDice() {
        return dice;
    }

    public String getFinalSum() {
        return finalSum;
    }
}
