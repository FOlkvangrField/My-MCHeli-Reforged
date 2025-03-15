package mcheli;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mcheli.network.packets.PacketEntitySync;
import mcheli.weapon.MCH_IEntityLockChecker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static mcheli.network.packets.PacketEntitySync.*;

public class MCH_EntityInfoManager {

    public static final Map<Integer, MCH_EntityInfo> serverEntities = new ConcurrentHashMap<>();

    public MCH_EntityInfoManager() {
        FMLCommonHandler.instance().bus().register(this);
    }

    private int tickCounter;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            serverTick();
        }
    }

    public void serverTick() {

        //更新实体到集合
        for (WorldServer world : MinecraftServer.getServer().worldServers) {
            for (Entity entity : (List<Entity>) world.loadedEntityList) {
                if (shouldTrack(entity)) {
                    serverEntities.put(entity.getEntityId(), MCH_EntityInfo.createInfo(entity));
                }
            }
        }

        if(tickCounter % 10 == 0) {
            //删除过期实体
            Iterator<Map.Entry<Integer, MCH_EntityInfo>> it = serverEntities.entrySet().iterator();
            List<MCH_EntityInfo> removed = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry<Integer, MCH_EntityInfo> entry = it.next();
                MCH_EntityInfo info = entry.getValue();
                Entity entity = serverGetEntity(info.entityId);
                if (entity == null || entity.isDead) {
                    removed.add(info);
                    it.remove();
                }
            }
            //发送移除包
            if (!removed.isEmpty()) {
//                System.out.println("向客户端移除了" + removed.size() + "个实体信息");
                sendEntityPacket(removed, OPERATION_REMOVE);
            }
        }

        //发送实体数据包
        List<MCH_EntityInfo> list = new ArrayList<>(serverEntities.values());
//        System.out.println("向客户端发送了" + list.size() + "个实体信息");
        sendEntityPacket(list, OPERATION_UPDATE);
    }

    private Entity serverGetEntity(int entityId) {
        for (WorldServer world : MinecraftServer.getServer().worldServers) {
            for (Entity entity : (List<Entity>)world.loadedEntityList) {
                if(entity.getEntityId() == entityId) {
                    return entity;
                }
            }
        }
        return null;
    }

    private boolean shouldTrack(Entity entity) {
        return entity instanceof EntityPlayer || entity instanceof MCH_IEntityLockChecker;
    }

    private void sendEntityPacket(List<MCH_EntityInfo> infos, byte operation) {
        if (!infos.isEmpty()) {
            MCH_MOD.getPacketHandler().sendToAll(new PacketEntitySync(infos, operation));
        }
    }


}