package me.manaki.plugin.farms.restrict;

public class Restrict {

    private final String blockType;
    private final int amount;
    private final int seconds;

    public Restrict(String blockType, int amount, int seconds) {
        this.blockType = blockType;
        this.amount = amount;
        this.seconds = seconds;
    }

    public String getBlockType() {
        return blockType;
    }

    public int getAmount() {
        return amount;
    }

    public int getSeconds() {
        return seconds;
    }
}
