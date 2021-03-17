package me.manaki.plugin.farms.tool;

import org.bukkit.Material;

import java.util.List;

public class Tool {

    private String id;
    private String name;
    private Material textureType;
    private int textureData;
    private int durability;
    private List<String> blocks;

    public Tool(String id, String name, Material textureType, int textureData, int durability, List<String> blocks) {
        this.id = id;
        this.name = name;
        this.textureType = textureType;
        this.textureData = textureData;
        this.durability = durability;
        this.blocks = blocks;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
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
