package mcheli.flare;

import mcheli.MCH_Explosion;
import mcheli.MCH_FMURUtil;
import mcheli.aircraft.MCH_EntityAircraft;
import mcheli.weapon.*;
import mcheli.wrapper.W_McClient;
import mcheli.wrapper.W_WorldFunc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.List;

public class MCH_APS {

    //冷却时长 0代表冷却结束
    public int tick;
    //生效时长 0代表使用结束
    public int useTick;
    //APS生效时间
    public int useTime;
    //APS等待时间
    public int waitTime;

    public World worldObj;

    public MCH_EntityAircraft aircraft;

    public int range;

    public Entity user;

    public MCH_APS(World w, MCH_EntityAircraft ac) {
        this.worldObj = w;
        this.aircraft = ac;
    }

    public boolean onUse(Entity user) {
        boolean result = false;
        System.out.println("MCH_APS.onUse");
        this.user = user;
        if (worldObj.isRemote) {
            if (tick == 0) {
                tick = waitTime;
                useTick = useTime;
                result = true;
                W_WorldFunc.MOD_playSoundEffect(worldObj, aircraft.posX, aircraft.posY, aircraft.posZ, "aps_activate", 10.0F, 1.0F);
            }
        } else {
            result = true;
            tick = waitTime;
            useTick = useTime;
            aircraft.getEntityData().setBoolean("APSUsing", true);
            W_WorldFunc.MOD_playSoundEffect(worldObj, aircraft.posX, aircraft.posY, aircraft.posZ, "aps_activate", 10.0F, 1.0F);
        }
        return result;
    }

    public void onUpdate() {
        if (this.aircraft != null && !this.aircraft.isDead) {
            if (this.tick > 0) {
                --this.tick;
            }
            if (this.useTick > 0) {
                --this.useTick;
                if(useTick == 0) {
                    W_WorldFunc.MOD_playSoundEffect(worldObj, aircraft.posX, aircraft.posY, aircraft.posZ, "aps_deactivate", 10.0F, 1.0F);
                }
            }
            if(this.useTick > 0) {
                this.onUsing();
            }
            if (!this.isUsing() && this.aircraft.getEntityData().getBoolean("APSUsing")) {
                this.aircraft.getEntityData().setBoolean("APSUsing", false);
            }
        }
    }

    private void onUsing() {
        if(worldObj.isRemote) {
        } else {
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(aircraft, aircraft.boundingBox.expand(range, range, range));
            for (Object obj : list) {
                Entity entity = (Entity) obj;

                if(entity.getClass().getName().contains("EntityBullet")) {
                    if(MCH_FMURUtil.bulletDestructedByAPS(entity, (EntityLivingBase) user)) {
                        W_WorldFunc.MOD_playSoundEffect(worldObj, aircraft.posX, aircraft.posY, aircraft.posZ, "aps_shoot", 10.0F, 1.0F);
                    }
                }

                if(entity.getClass().getName().contains("EntityGrenade")) {
                    if(MCH_FMURUtil.grenadeDestructedByAPS(entity, (EntityLivingBase) user)) {
                        W_WorldFunc.MOD_playSoundEffect(worldObj, aircraft.posX, aircraft.posY, aircraft.posZ, "aps_shoot", 10.0F, 1.0F);
                        MCH_Explosion.newExplosion(worldObj, user, user, entity.posX, entity.posY, entity.posZ,
                                2, 0, true, true, false, true, 0, null);
                    }
                }

                if(entity instanceof MCH_EntityAAMissile
                        || entity instanceof MCH_EntityRocket
                        || entity instanceof MCH_EntityATMissile 
                        || entity instanceof MCH_EntityASMissile
                        || entity instanceof MCH_EntityTvMissile
                ) {
                    MCH_EntityBaseBullet bullet = (MCH_EntityBaseBullet) entity;
                    if(bullet.shootingEntity instanceof EntityPlayer && !((EntityPlayer) user).isOnSameTeam((EntityLivingBase) bullet.shootingEntity)) {
                        bullet.setDead();
                        W_WorldFunc.MOD_playSoundEffect(worldObj, aircraft.posX, aircraft.posY, aircraft.posZ, "aps_shoot", 10.0F, 1.0F);
                        MCH_FMURUtil.sendAPSMarker((EntityPlayerMP) bullet.shootingEntity);
                        MCH_Explosion.newExplosion(worldObj, user, user, entity.posX, entity.posY, entity.posZ,
                                3, 0, true, true, false, true, 0, null);
                    }
                }

            }
        }
    }


    public boolean isInPreparation() {
        return this.tick != 0;
    }

    public boolean isUsing() {
        return this.useTick > 0;
    }
}
