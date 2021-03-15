package me.manaki.plugin.farms.tool;

import org.bukkit.Material;

import java.util.List;

public class Tool {

    private String id;
    private Material textureType;
    private int textureData;
    private int durability;
    private List<String> blocks;

    public Tool(String id, Material textureType, int textureData, int durability, List<String> blocks) {
        this.id = id;
        this.textureType = textureType;
        this.textureData = textureData;
        this.durability = durability;
        this.blocks = blocks;
    }

    public String getID() {
        return id;
    }

    public Material getTextureType() {
        return textureType;
    }

    public int getTextureData() {
        return textureData;
    }

    public int getDurability() {
        return durability;
    }

    public List<String> getBlocks() {
        return blocks;
    }
}
