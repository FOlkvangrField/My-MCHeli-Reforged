package mcheli;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mcheli.vector.Vector3f;
import mcheli.vector.Vector4f;
import mcheli.weapon.MCH_RenderUtil;
import mcheli.wrapper.W_MOD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MCH_RenderBVRLockBox {

    private static final ResourceLocation FRAME = new ResourceLocation(W_MOD.DOMAIN, "textures/BVRLockBox.png");
    private static final int BOX_SIZE = 24;

    FloatBuffer screen = BufferUtils.createFloatBuffer(4);

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        World world = mc.theWorld;

        if (player == null || world == null) return;
        if (mc.gameSettings.thirdPersonView != 0) return;

        List<Entity> entities = new ArrayList<>(world.loadedEntityList);

        GL11.glPushMatrix();
        prepareRenderState();

     //   for (Entity entity : entities) {
     //       if (shouldSkipEntity(entity, player)) continue;

//            double x = interpolate(entity.posX, entity.lastTickPosX, event.partialTicks);
//            double y = interpolate(entity.posY, entity.lastTickPosY, event.partialTicks) + entity.height * 0.5;
//            double z = interpolate(entity.posZ, entity.lastTickPosZ, event.partialTicks);

  //          Vec3 entityPos = Vec3.createVectorHelper(x, y, z);

            Vec3 entityPos = Vec3.createVectorHelper(10, 10, 10);

            double[] screenPos = worldToScreen(entityPos);

            double sx = screenPos[0];
            double sy = screenPos[1];

            if(player.ticksExisted % 100 == 0) {
                System.out.println("-----------------------");
                System.out.println("worldpos   " + entityPos);
                System.out.println("viewport   " + Arrays.toString(bufferToArray(MCH_RenderUtil.viewport)));
                System.out.println("modelview  " + Arrays.toString(bufferToArray(MCH_RenderUtil.modelview)));
                System.out.println("projection " + Arrays.toString(bufferToArray(MCH_RenderUtil.projection)));
                System.out.println(sx + " " + sy);
                System.out.println("-----------------------");
            }

            drawEntityMarker(sx, sy);
            // 调试信息
            Minecraft.getMinecraft().fontRenderer.drawString(
                    String.format("[%.1f,%.1f,%.1fm]", sx, sy, player.getDistance(10, 10, 10)),
                    (int) (sx + 10), (int) sy,
                    0xFFFFFF
            );

            restoreRenderState();
            GL11.glPopMatrix();
    //    }
    }

    public static Vector4f Multiply(Vector4f vec, float[] mat) {
        return new Vector4f(
                vec.x * mat[0] + vec.y * mat[4] + vec.z * mat[8] + vec.w * mat[12],
                vec.x * mat[1] + vec.y * mat[5] + vec.z * mat[9] + vec.w * mat[13],
                vec.x * mat[2] + vec.y * mat[6] + vec.z * mat[10] + vec.w * mat[14],
                vec.x * mat[3] + vec.y * mat[7] + vec.z * mat[11] + vec.w * mat[15]
        );
    }

    public static float[] bufferToArray(FloatBuffer buffer) {
        if (buffer.capacity() < 16) {
            throw new IllegalArgumentException("FloatBuffer must have at least 16 elements.");
        }
        int originalPosition = buffer.position(); // 保存原始位置
        buffer.position(0); // 重置到起始位置
        float[] array = new float[16];
        buffer.get(array); // 读取16个元素
        buffer.position(originalPosition); // 恢复原始位置
        return array;
    }

    public static int[] bufferToArray(IntBuffer buffer) {
        if (buffer.capacity() < 16) {
            throw new IllegalArgumentException("IntBuffer must have at least 16 elements.");
        }
        int originalPosition = buffer.position();
        buffer.position(0);
        int[] array = new int[16];
        buffer.get(array);
        buffer.position(originalPosition);
        return array;
    }

    private double[] worldToScreen(Vec3 pos) {
        Vector4f clipSpacePos = Multiply(new Vector4f((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord, 1.0f), bufferToArray(MCH_RenderUtil.modelview));
        clipSpacePos = Multiply(clipSpacePos, bufferToArray(MCH_RenderUtil.projection));
        Vector3f ndcSpacePos = new Vector3f(clipSpacePos.x / clipSpacePos.w, clipSpacePos.y / clipSpacePos.w, clipSpacePos.z / clipSpacePos.w);
        int[] viewPort = bufferToArray(MCH_RenderUtil.viewport);
        if (ndcSpacePos.z < -1.0f || ndcSpacePos.z > 1.0f) {
            return new double[]{0, 0};
        }
        return new double[] {
                ((ndcSpacePos.x + 1.0f) / 2.0f) * viewPort[2],
                ((1.0f - ndcSpacePos.y) / 2.0f) * viewPort[3]
        };
    }

    private void drawEntityMarker(double x, double y) {
        Minecraft.getMinecraft().renderEngine.bindTexture(FRAME);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        double halfSize = BOX_SIZE / 2.0;
        tess.addVertexWithUV(x - halfSize, y + halfSize, 0, 0, 1);
        tess.addVertexWithUV(x + halfSize, y + halfSize, 0, 1, 1);
        tess.addVertexWithUV(x + halfSize, y - halfSize, 0, 1, 0);
        tess.addVertexWithUV(x - halfSize, y - halfSize, 0, 0, 0);
        tess.draw();
    }

    private double interpolate(double now, double old, float partialTicks) {
        return old + (now - old) * partialTicks;
    }

    // 修改后的渲染状态设置
    private void prepareRenderState() {
        GL11.glEnable(3042);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glBlendFunc(770, 771);
    }

    private void restoreRenderState() {
        int srcBlend = GL11.glGetInteger(3041);
        int dstBlend = GL11.glGetInteger(3040);
        GL11.glBlendFunc(srcBlend, dstBlend);
        GL11.glDisable(3042);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean shouldSkipEntity(Entity entity, EntityPlayer player) {
        return entity.isDead || entity == player;
    }

}