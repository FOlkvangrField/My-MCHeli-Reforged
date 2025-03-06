package mcheli.weapon;

import mcheli.wrapper.W_WorldFunc;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

import static mcheli.weapon.MCH_WeaponASMissile.rayTraceAllBlocks;

public class MCH_LaserGuidanceSystem implements MCH_IGuidanceSystem {

    public World worldObj;
    protected Entity user;
    public double targetPosX;
    public double targetPosY;
    public double targetPosZ;

    @Override
    public double getLockPosX() {
        return targetPosX;
    }

    @Override
    public double getLockPosY() {
        return targetPosY;
    }

    @Override
    public double getLockPosZ() {
        return targetPosZ;
    }

    @Override
    public void update() {

        if(worldObj.isRemote) {

            float yaw = user.rotationYaw;  // 获取玩家的偏航角度
            float pitch = user.rotationPitch;  // 获取玩家的俯仰角度

            // 计算目标方向的三维坐标变化量
            double targetX = -MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);
            double targetZ = MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);
            double targetY = -MathHelper.sin(pitch / 180.0F * (float) Math.PI);

            // 计算方向的距离
            double dist = MathHelper.sqrt_double(targetX * targetX + targetY * targetY + targetZ * targetZ);
            double maxDist = 1500.0;
            double segmentLength = 100.0;  // 每段的长度
            int numSegments = (int) (maxDist / segmentLength);  // 计算需要的段数

            // 在客户端和服务器端都将目标方向进行归一化处理
            targetX = targetX * maxDist / dist;
            targetY = targetY * maxDist / dist;
            targetZ = targetZ * maxDist / dist;

            // 计算发射源
            Vec3 src = W_WorldFunc.getWorldVec3(this.worldObj, RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);

            // 射线检测
            MovingObjectPosition hitResult = null;

            for (int i = 1; i <= numSegments; i++) {
                // 计算当前分段的目标点，确保每段都从上一个段的终点开始
                Vec3 currentDst = W_WorldFunc.getWorldVec3(this.worldObj,
                        RenderManager.renderPosX + targetX * i / numSegments,
                        RenderManager.renderPosY + 1.62D + targetY * i / numSegments,
                        RenderManager.renderPosZ + targetZ * i / numSegments);

                // 执行射线检测
                List<MovingObjectPosition> hitResults = rayTraceAllBlocks(this.worldObj, src, currentDst, false, true, true);

                if (hitResults != null && !hitResults.isEmpty()) {
                    hitResult = hitResults.get(0);
                    break;  // 找到碰撞结果后，退出循环
                }

                // 更新src为当前检测的dst
                src = currentDst;  // 当前段的dst成为下一段的src
            }

            // 如果没有检测到碰撞，则返回默认的目标位置
            if (hitResult == null) {
                hitResult = new MovingObjectPosition(null, src.addVector(targetX, targetY, targetZ));  // 使用目标点作为默认值
            }

            // 设置导弹的目标位置
            targetPosX = hitResult.hitVec.xCoord;
            targetPosY = hitResult.hitVec.yCoord;
            targetPosZ = hitResult.hitVec.zCoord;
        }
    }
}
