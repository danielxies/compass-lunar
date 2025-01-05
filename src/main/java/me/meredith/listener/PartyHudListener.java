package me.meredith.listener;

import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSettings;
import me.meredith.WeaveFks;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;

// For 1.8.9:
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PartyHudListener {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final WeaveFks weavefks = WeaveFks.getInstance();

    // Removes "[S]" suffix if present
    private static final Pattern squadSuffixPattern = Pattern.compile(
        "^" + EnumChatFormatting.GOLD + "\\["
             + EnumChatFormatting.DARK_GREEN + "S"
             + EnumChatFormatting.GOLD + "\\] "
    );

    // Layout constants
    private static final int MIN_BOX_WIDTH = 225;        // increased from 150 to 200 for longer names
    private static final float BOX_HEIGHT = 15.0f;       // Changed from int 15 to float 15.1
    private static final int HEAD_SIZE = 15;             
    private static final int BOX_BG_COLOR = 0x80404040;  
    private static final int BOX_BORDER_COLOR = 0xFF000000;
    private static final int BOX_TOP_MARGIN = -2;        
    private static final int BOX_LEFT_MARGIN = -2;
    private static final int PADDING_RIGHT = 9;          
    private static final int ARROW_SIZE = 7;             
    private static final int ARROW_GAP_LEFT = 7;    
    private static final int ARROW_GAP_RIGHT = 7;   
    private static final int HEAD_GAP = 4;       // New constant: smaller gap after head
    private static final int COLUMN_GAP = 16;    // Renamed from 'gap': larger gap between columns

    /**
     * Returns the color code for HP relative to the local player's max HP.
     */
    private static EnumChatFormatting getHPColor(float maxHealthPoints, float healthPoints) {
        if (healthPoints > maxHealthPoints) {
            return EnumChatFormatting.GREEN;
        } else if (healthPoints > maxHealthPoints * 3f / 4f) {
            return EnumChatFormatting.GREEN;
        } else if (healthPoints > maxHealthPoints / 2f) {
            return EnumChatFormatting.YELLOW;
        } else if (healthPoints > maxHealthPoints / 4f) {
            return EnumChatFormatting.RED;
        } else {
            return EnumChatFormatting.DARK_RED;
        }
    }

    private static EnumChatFormatting getColoredHP(int healthPoints) {
        float maxHP = mc.thePlayer.getMaxHealth();
        return getHPColor(maxHP, healthPoints);
    }

    /**
     * Strips squad suffix from a player name, if present.
     */
    private String getPlayerName(NetworkPlayerInfo netInfo) {
        String raw = ScorePlayerTeam.formatPlayerName(
            netInfo.getPlayerTeam(),
            netInfo.getGameProfile().getName()
        );
        return squadSuffixPattern.matcher(raw).replaceFirst("");
    }

    /**
     * Compute the angle from local->target, subtracting local yaw.
     */
    private float getDirectionAngle(EntityPlayer local, EntityPlayer target) {
        double dx = target.posX - local.posX;
        double dz = target.posZ - local.posZ;

        float angleToTarget = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float relativeAngle = angleToTarget - local.rotationYaw;

        // normalize to [0..360)
        while (relativeAngle < 0)   { relativeAngle += 360; }
        while (relativeAngle >= 360){ relativeAngle -= 360; }

        return relativeAngle;
    }

    /**
     * Small helper to store distance & colored vertical difference as separate pieces.
     */
    private static class DistYDiff {
        final String distStr;  
        final String yDiffStr; 
        DistYDiff(String distStr, String yDiffStr) {
            this.distStr = distStr;
            this.yDiffStr = yDiffStr;
        }
    }

    /**
     * Returns DistYDiff such that dist is always pink, and yDiff is colored per your custom logic:
     *  - If |yDiff| ≤ 5 => \u00A7c (RED)
     *  - If 6 ≤ |yDiff| ≤ 15 => \u00A76 (GOLD)
     *  - Otherwise => \u00A72 (DARK GREEN)
     */
    private DistYDiff getDistanceStrings(EntityPlayer local, EntityPlayer target) {
        double dist = local.getDistanceToEntity(target);
        int distInt = (int)Math.round(dist);

        int yDiff = (int)Math.round(target.posY - local.posY);
        // color the distance pink
        String distColored = EnumChatFormatting.LIGHT_PURPLE + (distInt + "m");

        // figure out color for yDiff
        int absDiff = Math.abs(yDiff);
        String color;
        if (absDiff <= 5) {
            color = "\u00A7c"; // red
        } else if (absDiff <= 15) {
            color = "\u00A76"; // gold
        } else {
            color = "\u00A72"; // dark green
        }

        // prefix with + if positive
        String yDiffPrefix = (yDiff >= 0) ? "+" : "";
        String yDiffColored = color + (yDiffPrefix + yDiff);

        return new DistYDiff(distColored, yDiffColored);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        renderPartyHUD();
    }

    private void renderPartyHUD() {
        try {
            if (mc.thePlayer == null || mc.theWorld == null) return;

            Scoreboard sb = mc.theWorld.getScoreboard();
            ScoreObjective sbObj = sb.getObjectiveInDisplaySlot(0);

            // In singleplayer, if there's no scoreboard objective, no need to render
            if (mc.isIntegratedServerRunning() && sbObj == null) return;

            // Ensure we add ourselves to party members
            weavefks.addSelfToPartyMembers();
            List<String> partyMembers = weavefks.getPartyMembers();
            if (partyMembers == null || partyMembers.isEmpty()) return;

            // If not in focus or debug screen is up, do not render
            boolean inFocus = mc.inGameHasFocus;
            boolean debug = mc.gameSettings.showDebugInfo;
            if (!weavefks.getConfig().displayPartyHUD || !inFocus || debug) {
                return;
            }

            FontRenderer fr = mc.fontRendererObj;
            EntityPlayer local = mc.thePlayer;

            // We'll measure columns: name, finals, hp, plus the parentheses portion
            int maxNameW  = 0;
            int maxFinalW = 0;
            int maxHPW    = 0;
            int maxParenW = 0;  // We'll measure the entire "(...)" piece + arrow space

            class Row {
                NetworkPlayerInfo info;
                String nameString;
                String finalsString;
                String healthString;

                // For the parentheses chunk
                String parenOpen;
                String distString;  
                String yDiffString; 
                String parenClose;
            }

            List<Row> rows = new ArrayList<>();

            // ------------------ PASS 1: gather & measure
            for (String pName : partyMembers) {
                NetworkPlayerInfo pInfo = mc.getNetHandler().getPlayerInfo(pName);
                if (pInfo == null) continue;

                Row row = new Row();
                row.info = pInfo;
                row.nameString   = getPlayerName(pInfo);
                row.finalsString = weavefks.getChatMessageParser()
                    .getFinalsPlayer(pInfo.getGameProfile().getName());

                // HP
                String hpStr = "";
                if (sbObj != null
                    && pInfo.getGameType() != WorldSettings.GameType.SPECTATOR
                    && sbObj.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS)
                {
                    int points = sbObj.getScoreboard()
                        .getValueFromObjective(pInfo.getGameProfile().getName(), sbObj)
                        .getScorePoints();
                    hpStr = getColoredHP(points) + String.valueOf(points);
                }
                row.healthString = hpStr;

                // If not local, compute distance & yDiff
                if (!pInfo.getGameProfile().getName().equalsIgnoreCase(local.getName())) {
                    EntityPlayer targetEnt = mc.theWorld.getPlayerEntityByName(
                        pInfo.getGameProfile().getName()
                    );
                    if (targetEnt != null) {
                        DistYDiff distPair = getDistanceStrings(local, targetEnt);
                        row.parenOpen   = EnumChatFormatting.WHITE + "(";
                        row.distString  = distPair.distStr;  
                        row.yDiffString = distPair.yDiffStr;  
                        row.parenClose  = EnumChatFormatting.WHITE + ")";
                    } else {
                        // If the target is not in the loaded world
                        row.parenOpen   = "";
                        row.distString  = "";
                        row.yDiffString = "";
                        row.parenClose  = "";
                    }
                } else {
                    // local => no parentheses
                    row.parenOpen   = "";
                    row.distString  = "";
                    row.yDiffString = "";
                    row.parenClose  = "";
                }

                rows.add(row);

                // measure name, finals, hp
                int nameW  = fr.getStringWidth(row.nameString);
                int finalW = fr.getStringWidth(row.finalsString);
                int hpW    = fr.getStringWidth(row.healthString);
                maxNameW  = Math.max(maxNameW, nameW);
                maxFinalW = Math.max(maxFinalW, finalW);
                maxHPW    = Math.max(maxHPW, hpW);

                // measure parentheses + arrow space if present
                String combined = row.parenOpen + row.distString + " " + row.yDiffString + row.parenClose;
                int parenW = fr.getStringWidth(combined);
                if (!row.parenOpen.isEmpty()) {
                    parenW += (ARROW_SIZE + ARROW_GAP_LEFT + ARROW_GAP_RIGHT); 
                }
                maxParenW = Math.max(maxParenW, parenW);
            }

            // total width
            int gap = 10;
            int totalWidth = HEAD_SIZE
                             + gap
                             + maxNameW
                             + gap
                             + maxFinalW
                             + gap
                             + maxHPW
                             + gap
                             + maxParenW
                             + PADDING_RIGHT;

            int boxWidth = Math.max(MIN_BOX_WIDTH, totalWidth);

            float rawX = (float) weavefks.getConfig().partyHUDX;
            float rawY = (float) weavefks.getConfig().partyHUDY;
            double hudScale = weavefks.getConfig().partyHUDScale / 100.0;

            GlStateManager.pushMatrix();
            GlStateManager.scale(hudScale, hudScale, 1.0);

            float drawX = (float) (rawX / hudScale);
            float drawY = (float) (rawY / hudScale);

            // ------------------ PASS 2: render
            for (Row row : rows) {
                int boxLeft   = (int) drawX + BOX_LEFT_MARGIN;
                int boxTop    = (int) drawY + BOX_TOP_MARGIN;
                int boxRight  = boxLeft + boxWidth;
                int boxBottom = (int)(boxTop + BOX_HEIGHT);

                // background
                Gui.drawRect(boxLeft, boxTop, boxRight, boxBottom, BOX_BG_COLOR);
                // border
                Gui.drawRect(boxLeft - 1, boxTop - 1, boxRight + 1, boxTop, BOX_BORDER_COLOR);
                Gui.drawRect(boxLeft - 1, boxBottom, boxRight + 1, boxBottom + 1, BOX_BORDER_COLOR);
                Gui.drawRect(boxLeft - 1, boxTop, boxLeft, boxBottom, BOX_BORDER_COLOR);
                Gui.drawRect(boxRight, boxTop, boxRight + 1, boxBottom, BOX_BORDER_COLOR);

                // draw the player head
                int headX = boxLeft;
                int headY = (int)(boxTop + (BOX_HEIGHT - HEAD_SIZE) / 2);
                mc.getTextureManager().bindTexture(row.info.getLocationSkin());
                GlStateManager.color(1f, 1f, 1f, 1f);
                Gui.drawScaledCustomSizeModalRect(
                    headX, headY,
                    8, 8, 8, 8,
                    HEAD_SIZE, HEAD_SIZE,
                    64f, 64f
                );

                // define column offsets
                int colX_name  = headX + HEAD_SIZE + HEAD_GAP;
                int colX_final = colX_name + maxNameW + COLUMN_GAP;
                int colX_hp    = colX_final + maxFinalW + COLUMN_GAP;
                int colX_paren = colX_hp + maxHPW + COLUMN_GAP;

                // Calculate base text Y position
                int textY = (int)(boxTop + (BOX_HEIGHT - fr.FONT_HEIGHT) / 2 + 1);

                // Scale factor (1.1 = 10% bigger)
                float textScale = 1.1f;

                // Calculate the center point for scaling
                float textCenterY = textY + (fr.FONT_HEIGHT / 2.0f);

                // Draw each text element with scaling
                GlStateManager.pushMatrix();
                // Translate to center, scale, then translate back
                GlStateManager.translate(colX_name, textCenterY, 0);
                GlStateManager.scale(textScale, textScale, 1.0f);
                GlStateManager.translate(-colX_name, -textCenterY, 0);
                fr.drawStringWithShadow(row.nameString, colX_name, textY, 0xFFFFFF);
                GlStateManager.popMatrix();

                // Repeat for finals
                GlStateManager.pushMatrix();
                GlStateManager.translate(colX_final, textCenterY, 0);
                GlStateManager.scale(textScale, textScale, 1.0f);
                GlStateManager.translate(-colX_final, -textCenterY, 0);
                fr.drawStringWithShadow(row.finalsString, colX_final, textY, 0xFFFFFF);
                GlStateManager.popMatrix();

                // Repeat for HP
                GlStateManager.pushMatrix();
                GlStateManager.translate(colX_hp, textCenterY, 0);
                GlStateManager.scale(textScale, textScale, 1.0f);
                GlStateManager.translate(-colX_hp, -textCenterY, 0);
                fr.drawStringWithShadow(row.healthString, colX_hp, textY, 0xFFFFFF);
                GlStateManager.popMatrix();

                // And for parentheses section if present
                if (!row.parenOpen.isEmpty()) {
                    String leftPart = row.parenOpen + row.distString;
                    String rightPart = row.yDiffString + row.parenClose;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(colX_paren, textCenterY, 0);
                    GlStateManager.scale(textScale, textScale, 1.0f);
                    GlStateManager.translate(-colX_paren, -textCenterY, 0);
                    fr.drawStringWithShadow(leftPart, colX_paren, textY, 0xFFFFFF);
                    GlStateManager.popMatrix();

                    // The arrow is a 7x7 box now
                    int arrowX = colX_paren + fr.getStringWidth(leftPart) + ARROW_GAP_LEFT;
                    int arrowY = textY + (fr.FONT_HEIGHT - ARROW_SIZE) / 2;

                    // figure out angle
                    EntityPlayer targetEnt = mc.theWorld.getPlayerEntityByName(
                        row.info.getGameProfile().getName()
                    );
                    if (targetEnt != null) {
                        float angle = getDirectionAngle(local, targetEnt);

                        GlStateManager.pushMatrix();
                        // Center pivot at (3.5,3.5) => half of 7
                        GlStateManager.translate(arrowX + (ARROW_SIZE / 2f), arrowY + (ARROW_SIZE / 2f), 0);
                        GlStateManager.rotate(angle, 0, 0, 1);
                        GlStateManager.translate(-(ARROW_SIZE / 2f), -(ARROW_SIZE / 2f), 0);

                        drawHollowCaretArrow(ARROW_SIZE);

                        GlStateManager.popMatrix();
                    }

                    // Draw the remainder
                    int arrowSpace = ARROW_SIZE + ARROW_GAP_LEFT + ARROW_GAP_RIGHT;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(arrowX + arrowSpace - ARROW_SIZE, textCenterY, 0);
                    GlStateManager.scale(textScale, textScale, 1.0f);
                    GlStateManager.translate(-(arrowX + arrowSpace - ARROW_SIZE), -textCenterY, 0);
                    fr.drawStringWithShadow(rightPart, arrowX + arrowSpace - ARROW_SIZE, textY, 0xFFFFFF);
                    GlStateManager.popMatrix();
                }

                // move down for next row 
                // (slightly bigger BOX_HEIGHT gives a bit more vertical space)
                drawY += BOX_HEIGHT;
            }

            GlStateManager.popMatrix();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Draws a hollow "caret"/"A" shape in a fixed NxN box. 
     * With size=7, it's basically (0,7)->(3.5,0)->(7,7).
     */
    private void drawHollowCaretArrow(int arrowSize) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        // Changed from 2.0F to 2.6F (30% thicker)
        GL11.glLineWidth(2.6F);

        // Pink color (like distance)
        float r = 1.0f;
        float g = 0.5f;
        float b = 1.0f;
        float a = 1.0f;

        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        // bottom-left
        wr.pos(0, arrowSize, 0).color(r, g, b, a).endVertex();
        // top tip
        wr.pos(arrowSize / 2.0, 0, 0).color(r, g, b, a).endVertex();
        // bottom-right
        wr.pos(arrowSize, arrowSize, 0).color(r, g, b, a).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glLineWidth(1.0F);  // Reset line width
    }
}