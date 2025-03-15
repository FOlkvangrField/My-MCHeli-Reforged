package mcheli;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class MCH_EntityInfo {
    public int entityId;
    public String worldName;
    public String entityClassName;
    public double posX;
    public double posY;
    public double posZ;
    public double lastTickPosX;
    public double lastTickPosY;
    public double lastTickPosZ;

    public MCH_EntityInfo(int entityId, String worldName, String entityClassName, double posX, double posY, double posZ, double lastTickPosX, double lastTickPosY, double lastTickPosZ) {
        this.entityId = entityId;
        this.worldName = worldName;
        this.entityClassName = entityClassName;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.lastTickPosX = lastTickPosX;
        this.lastTickPosY = lastTickPosY;
        this.lastTickPosZ = lastTickPosZ;
    }

    public static MCH_EntityInfo createInfo(Entity e) {
        return new MCH_EntityInfo(e.getEntityId(),
                e.worldObj.getWorldInfo().getWorldName(),
                e.getClass().getName(),
                e.posX, e.posY, e.posZ,
                e.lastTickPosX, e.lastTickPosY, e.lastTickPosZ
        );
    }

    public double getDistanceToEntity(Entity e) {
        return Math.sqrt((e.posX - posX) * (e.posX - posX) + (e.posY - posY) * (e.posY - posY) + (e.posZ - posZ) * (e.posZ - posZ));
    }

    public double getDistanceSqToEntity(Entity e) {
        return (e.posX - posX) * (e.posX - posX) + (e.posY - posY) * (e.posY - posY) + (e.posZ - posZ) * (e.posZ - posZ);
    }
}