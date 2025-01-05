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
    // (No fixed MIN_BOX_WIDTH; we will let the box grow dynamically.)
    private static final float BOX_HEIGHT = 15.0f;
    private static final int HEAD_SIZE = 15;
    private static final int BOX_BG_COLOR = 0x80404040;
    private static final int BOX_BORDER_COLOR = 0xFF000000;
    private static final int BOX_TOP_MARGIN = -2;
    private static final int BOX_LEFT_MARGIN = -2;
    private static final int PADDING_RIGHT = 9;
    private static final int ARROW_SIZE = 7;
    private static final int ARROW_GAP_LEFT = 7;
    private static final int ARROW_GAP_RIGHT = 4;
    private static final int HEAD_GAP = 4;
    private static final int COLUMN_GAP = 16;

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
        float maxHP = mc.thePlayer == null ? 20 : mc.thePlayer.getMaxHealth();
        return getHPColor(maxHP, healthPoints);
    }

    private String getPlayerName(NetworkPlayerInfo netInfo) {
        String raw = ScorePlayerTeam.formatPlayerName(
            netInfo.getPlayerTeam(),
            netInfo.getGameProfile().getName()
        );
        return squadSuffixPattern.matcher(raw).replaceFirst("");
    }

    private float getDirectionAngle(EntityPlayer local, EntityPlayer target) {
        double dx = target.posX - local.posX;
        double dz = target.posZ - local.posZ;

        float angleToTarget = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float relativeAngle = angleToTarget - local.rotationYaw;

        while (relativeAngle < 0)   { relativeAngle += 360; }
        while (relativeAngle >= 360){ relativeAngle -= 360; }

        return relativeAngle;
    }

    private static class DistYDiff {
        final String distStr;
        final String yDiffStr;
        DistYDiff(String distStr, String yDiffStr) {
            this.distStr = distStr;
            this.yDiffStr = yDiffStr;
        }
    }

    private DistYDiff getDistanceStrings(EntityPlayer local, EntityPlayer target) {
        double dist = local.getDistanceToEntity(target);
        int distInt = (int)Math.round(dist);

        int yDiff = (int)Math.round(target.posY - local.posY);
        String distColored = EnumChatFormatting.LIGHT_PURPLE + (distInt + "m");

        int absDiff = Math.abs(yDiff);
        String color;
        if (absDiff <= 5) {
            color = "\u00A7c"; // red
        } else if (absDiff <= 15) {
            color = "\u00A76"; // gold
        } else {
            color = "\u00A72"; // dark green
        }

        String yDiffPrefix = (yDiff >= 0) ? "+" : "";
        String yDiffColored = color + (yDiffPrefix + yDiff);

        return new DistYDiff(distColored, yDiffColored);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent event) {
        if (!weavefks.getConfig().displayPartyHUD) {
            return;
        }

        renderPartyHUD();
    }

    private void renderPartyHUD() {
        try {
            if (mc.thePlayer == null || mc.theWorld == null) return;

            Scoreboard sb = mc.theWorld.getScoreboard();
            ScoreObjective sbObj = sb.getObjectiveInDisplaySlot(0);

            if (mc.isIntegratedServerRunning() && sbObj == null) return;

            weavefks.addSelfToPartyMembers();
            List<String> partyMembers = weavefks.getPartyMembers();
            if (partyMembers == null || partyMembers.isEmpty()) return;

            boolean inFocus = mc.inGameHasFocus;
            boolean debug = mc.gameSettings.showDebugInfo;
            if (!weavefks.getConfig().displayPartyHUD || !inFocus || debug) {
                return;
            }

            FontRenderer fr = mc.fontRendererObj;
            EntityPlayer local = mc.thePlayer;

            int maxNameW  = 0;
            int maxFinalW = 0;
            int maxHPW    = 0;
            int maxParenW = 0;
            final float TEXT_SCALE = 1.5f;

            class Row {
                NetworkPlayerInfo info;
                String nameString;
                String finalsString;
                String healthString;
                // We keep them separate so we can measure the left vs. right exactly how we draw it:
                String parenOpen;
                String distString;
                String yDiffString;
                String parenClose;
            }

            List<Row> rows = new ArrayList<>();

            // ------------------ PASS 1: gather & measure all text so we know how wide to make the box
            for (String pName : partyMembers) {
                NetworkPlayerInfo pInfo = mc.getNetHandler().getPlayerInfo(pName);
                if (pInfo == null) continue;

                Row row = new Row();
                row.info = pInfo;
                row.nameString   = getPlayerName(pInfo);
                row.finalsString = weavefks.getChatMessageParser()
                    .getFinalsPlayer(pInfo.getGameProfile().getName());

                // HP from scoreboard, if not spectator and not hearts
                String hpStr = "";
                if (sbObj != null
                    && pInfo.getGameType() != WorldSettings.GameType.SPECTATOR
                    && sbObj.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS)
                {
                    int points = sbObj.getScoreboard()
                        .getValueFromObjective(pInfo.getGameProfile().getName(), sbObj)
                        .getScorePoints();
                    hpStr = getColoredHP(points) + " " + points;
                }
                row.healthString = hpStr;

                // Distance strings / parentheses
                if (!pInfo.getGameProfile().getName().equalsIgnoreCase(local.getName())) {
                    EntityPlayer targetEnt = mc.theWorld.getPlayerEntityByName(
                        pInfo.getGameProfile().getName()
                    );
                    DistYDiff distPair;
                    if (targetEnt != null) {
                        distPair = getDistanceStrings(local, targetEnt);
                    } else {
                        // Show 65+m and +0 when player is out of render distance
                        distPair = new DistYDiff(EnumChatFormatting.LIGHT_PURPLE + "65+m", "\u00A72+0");
                    }
                    row.parenOpen = EnumChatFormatting.WHITE + "(";
                    row.distString = distPair.distStr;
                    row.yDiffString = distPair.yDiffStr;
                    row.parenClose = EnumChatFormatting.WHITE + ")";
                } else {
                    row.parenOpen   = "";
                    row.distString  = "";
                    row.yDiffString = "";
                    row.parenClose  = "";
                }

                rows.add(row);

                // Now measure each piece at TEXT_SCALE:
                // 1) name
                int rawNameW  = fr.getStringWidth(row.nameString);
                // 2) finals
                int rawFinalW = fr.getStringWidth(row.finalsString);
                // 3) HP
                int rawHPW    = fr.getStringWidth(row.healthString);

                int scaledNameW  = (int)Math.ceil(rawNameW  * TEXT_SCALE);
                int scaledFinalW = (int)Math.ceil(rawFinalW * TEXT_SCALE);
                int scaledHPW    = (int)Math.ceil(rawHPW    * TEXT_SCALE);

                maxNameW  = Math.max(maxNameW,  scaledNameW);
                maxFinalW = Math.max(maxFinalW, scaledFinalW);
                maxHPW    = Math.max(maxHPW,    scaledHPW);

                // 4) parentheses/distance: measure exactly as we draw it
                //    leftPart = ( + distString
                //    arrow    = arrowSize if present
                //    rightPart = yDiff + )
                String leftPart = row.parenOpen + row.distString;
                String rightPart = row.yDiffString + row.parenClose;

                int rawLeftWidth  = fr.getStringWidth(leftPart);
                int rawRightWidth = fr.getStringWidth(rightPart);

                int scaledLeftWidth  = (int)Math.ceil(rawLeftWidth  * TEXT_SCALE);
                int scaledRightWidth = (int)Math.ceil(rawRightWidth * TEXT_SCALE);

                // Arrow is only drawn if parenOpen is non-empty (meaning there's a real target)
                int arrowW = 0;
                if (!row.parenOpen.isEmpty()) {
                    arrowW = ARROW_SIZE + ARROW_GAP_LEFT + ARROW_GAP_RIGHT;
                }

                // total distance text width
                int totalParenW = scaledLeftWidth + scaledRightWidth + arrowW;

                maxParenW = Math.max(maxParenW, totalParenW);
            }

            // Now compute total box width from these columns
            int gap = 10;
            int totalWidth = HEAD_SIZE + gap
                    + maxNameW + gap
                    + maxFinalW + gap
                    + maxHPW + gap
                    + maxParenW
                    + PADDING_RIGHT;

            // Add a small margin so text doesn't bump the box edge
            int boxWidth = totalWidth + 10;

            // Position & scale
            float rawX = (float) weavefks.getConfig().partyHUDX;
            float rawY = (float) weavefks.getConfig().partyHUDY;
            double hudScale = weavefks.getConfig().partyHUDScale / 100.0;

            GlStateManager.pushMatrix();
            GlStateManager.scale(hudScale, hudScale, 1.0);

            float drawX = (float) (rawX / hudScale);
            float drawY = (float) (rawY / hudScale);

            // measure total HUD height
            int totalRows = rows.size();
            float totalHUDHeight = totalRows * BOX_HEIGHT;

            int boundingLeft = (int) drawX + BOX_LEFT_MARGIN;
            int boundingTop = (int) drawY + BOX_TOP_MARGIN;
            int boundingRight = boundingLeft + boxWidth;
            int boundingBottom = (int) (boundingTop + totalHUDHeight);

            // draw one big background
            Gui.drawRect(boundingLeft, boundingTop, boundingRight, boundingBottom, BOX_BG_COLOR);

            // draw one big border
            Gui.drawRect(boundingLeft - 1, boundingTop - 1,
                    boundingRight + 1, boundingTop,
                    BOX_BORDER_COLOR);
            Gui.drawRect(boundingLeft - 1, boundingBottom,
                    boundingRight + 1, boundingBottom + 1,
                    BOX_BORDER_COLOR);
            Gui.drawRect(boundingLeft - 1, boundingTop,
                    boundingLeft, boundingBottom,
                    BOX_BORDER_COLOR);
            Gui.drawRect(boundingRight, boundingTop,
                    boundingRight + 1, boundingBottom,
                    BOX_BORDER_COLOR);

            // ------------------ PASS 2: actually render each row of data
            float currentY = drawY;
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);

                int boxLeft = (int) drawX + BOX_LEFT_MARGIN;
                int boxTop = (int) currentY + BOX_TOP_MARGIN;
                int boxRight = boxLeft + boxWidth;
                int boxBottom = (int) (boxTop + BOX_HEIGHT);

                // row background (no border lines here)
                Gui.drawRect(boxLeft, boxTop, boxRight, boxBottom, BOX_BG_COLOR);

                // Draw the player head
                int headX = boxLeft;
                int headY = (int) (boxTop + (BOX_HEIGHT - HEAD_SIZE) / 2);
                mc.getTextureManager().bindTexture(row.info.getLocationSkin());
                GlStateManager.color(1f, 1f, 1f, 1f);

                // base head layer
                Gui.drawScaledCustomSizeModalRect(
                        headX, headY, // Screen coords
                        8f, 8f,       // UV start
                        8, 8,         // UV size
                        HEAD_SIZE, HEAD_SIZE, // Draw size
                        64f, 64f      // Texture size
                );

                // overlay
                Gui.drawScaledCustomSizeModalRect(
                        headX, headY,
                        40f, 8f,      // overlay is at 40,8
                        8, 8,
                        HEAD_SIZE, HEAD_SIZE,
                        64f, 64f
                );

                // Now columns
                int colX_name   = headX + HEAD_SIZE + HEAD_GAP;
                int colX_final  = colX_name + maxNameW + COLUMN_GAP;
                int colX_hp     = colX_final + maxFinalW + COLUMN_GAP;
                int colX_paren  = colX_hp + maxHPW + COLUMN_GAP;

                int textY = (int) (boxTop + (BOX_HEIGHT - fr.FONT_HEIGHT) / 2 + 1);
                float textScale = TEXT_SCALE;
                float textCenterY = textY + (fr.FONT_HEIGHT / 2.0f);

                // 1) name
                GlStateManager.pushMatrix();
                GlStateManager.translate(colX_name, textCenterY, 0);
                GlStateManager.scale(textScale, textScale, 1.0f);
                GlStateManager.translate(-colX_name, -textCenterY, 0);
                String rawName = row.info.getGameProfile().getName();
                String displayName = weavefks.getPlayerNickname(rawName);
                // Get everything before the actual name in the original string
                String prefix = "";
                int nameIndex = row.nameString.toLowerCase().indexOf(rawName.toLowerCase());
                if (nameIndex > 0) {
                    prefix = row.nameString.substring(0, nameIndex);
                }
                fr.drawStringWithShadow(prefix + displayName, colX_name, textY, 0xFFFFFF);
                GlStateManager.popMatrix();

                // 2) finals
                GlStateManager.pushMatrix();
                GlStateManager.translate(colX_final, textCenterY, 0);
                GlStateManager.scale(textScale, textScale, 1.0f);
                GlStateManager.translate(-colX_final, -textCenterY, 0);
                fr.drawStringWithShadow(row.finalsString, colX_final, textY, 0xFFFFFF);
                GlStateManager.popMatrix();

                // 3) HP
                GlStateManager.pushMatrix();
                GlStateManager.translate(colX_hp, textCenterY, 0);
                GlStateManager.scale(textScale, textScale, 1.0f);
                GlStateManager.translate(-colX_hp, -textCenterY, 0);
                fr.drawStringWithShadow(row.healthString, colX_hp, textY, 0xFFFFFF);
                GlStateManager.popMatrix();

                // 4) parentheses/distance/arrow, if any
                if (!row.parenOpen.isEmpty()) {
                    // left part
                    String leftPart = row.parenOpen + row.distString;
                    String rightPart = row.yDiffString + row.parenClose;

                    // Draw left part
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(colX_paren, textCenterY, 0);
                    GlStateManager.scale(textScale, textScale, 1.0f);
                    GlStateManager.translate(-colX_paren, -textCenterY, 0);
                    fr.drawStringWithShadow(leftPart, colX_paren, textY, 0xFFFFFF);
                    GlStateManager.popMatrix();

                    // figure out arrow placement
                    int scaledLeftWidth = (int)(fr.getStringWidth(leftPart) * textScale);
                    int arrowX = colX_paren + scaledLeftWidth + ARROW_GAP_LEFT;
                    int arrowY = textY + (fr.FONT_HEIGHT - ARROW_SIZE) / 2;

                    // arrow
                    EntityPlayer targetEnt = mc.theWorld.getPlayerEntityByName(
                            row.info.getGameProfile().getName());
                    if (targetEnt != null) {
                        float angle = getDirectionAngle(local, targetEnt);
                        // Regular purple arrow
                        float r = 1.0f;
                        float g = 0.5f;
                        float b = 1.0f;
                        float a = 1.0f;
                        drawRotatedArrow(arrowX, arrowY, ARROW_SIZE, angle, r, g, b, a);
                    } else {
                        // Blue arrow (\u00A79) pointing up
                        float r = 0.0f;
                        float g = 0.6f;
                        float b = 1.0f;
                        float a = 1.0f;
                        drawRotatedArrow(arrowX, arrowY, ARROW_SIZE, 0f, r, g, b, a);
                    }

                    // right part
                    int arrowSpace = ARROW_SIZE + ARROW_GAP_LEFT + ARROW_GAP_RIGHT;
                    int rightPartX = arrowX + arrowSpace;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(rightPartX, textCenterY, 0);
                    GlStateManager.scale(textScale, textScale, 1.0f);
                    GlStateManager.translate(-rightPartX, -textCenterY, 0);
                    fr.drawStringWithShadow(rightPart, rightPartX, textY, 0xFFFFFF);
                    GlStateManager.popMatrix();
                }

                // Horizontal divider between rows
                if (i < rows.size() - 1) {
                    int separatorY = boxBottom;
                    Gui.drawRect(boundingLeft + 1, separatorY,
                            boundingRight - 1, separatorY + 1,
                            BOX_BORDER_COLOR);
                }

                currentY += BOX_HEIGHT;
            }

            GlStateManager.popMatrix();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void drawHollowCaretArrow(int arrowSize) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        GL11.glLineWidth(2.1F);

        float r = 1.0f;
        float g = 0.5f;
        float b = 1.0f;
        float a = 1.0f;

        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(0, arrowSize, 0).color(r, g, b, a).endVertex();
        wr.pos(arrowSize / 2.0, 0, 0).color(r, g, b, a).endVertex();
        wr.pos(arrowSize, arrowSize, 0).color(r, g, b, a).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glLineWidth(1.0F);
    }

    private void drawRotatedArrow(int x, int y, int size, float angle, float r, float g, float b, float a) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + size/2f, y + size/2f, 0);
        GlStateManager.rotate(angle, 0, 0, 1);
        GlStateManager.translate(-(size/2f), -(size/2f), 0);
        drawHollowCaretArrow(size);
        GlStateManager.popMatrix();
    }
}
