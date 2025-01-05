package me.meredith.finalscounter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.weavemc.loader.api.event.*;
import me.meredith.events.MegaWallsGameEvent;

import me.meredith.WeaveFks;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;
import net.minecraft.util.IChatComponent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.appender.db.jpa.converter.MessageAttributeConverter;


public class ChatMessageParser {
    private final WeaveFks weavefks;
    private static final String[] KILL_MESSAGES = {
        /*Banana messages, put those messages at the top to not conflict with the other pattern (\w{1,16}) was killed by (\w{1,16})*/
        "(\\w{1,16}) got banana pistol'd by (\\w{1,16}).*",
        "(\\w{1,16}) was peeled by (\\w{1,16}).*",
        "(\\w{1,16}) was mushed by (\\w{1,16}).*",
        "(\\w{1,16}) was hit by a banana split from (\\w{1,16}).*",
        "(\\w{1,16}) was killed by an explosive banana from (\\w{1,16}).*",
        "(\\w{1,16}) was killed by a magic banana from (\\w{1,16}).*",
        "(\\w{1,16}) was turned into mush by (\\w{1,16}).*",
        /*Default messages*/
        "(\\w{1,16}) was shot and killed by (\\w{1,16}).*",
        "(\\w{1,16}) was snowballed to death by (\\w{1,16}).*",
        "(\\w{1,16}) was killed by (\\w{1,16}).*",
        "(\\w{1,16}) was killed with a potion by (\\w{1,16}).*",
        "(\\w{1,16}) was killed with an explosion by (\\w{1,16}).*",
        "(\\w{1,16}) was killed with magic by (\\w{1,16}).*",
        /*Western messages*/
        "(\\w{1,16}) was filled full of lead by (\\w{1,16}).*",
        "(\\w{1,16}) was iced by (\\w{1,16}).*",
        "(\\w{1,16}) met their end by (\\w{1,16}).*",
        "(\\w{1,16}) lost a drinking contest with (\\w{1,16}).*",
        "(\\w{1,16}) was killed with dynamite by (\\w{1,16}).*",
        "(\\w{1,16}) lost the draw to (\\w{1,16}).*",
        /*Fire messages*/
        "(\\w{1,16}) was struck down by (\\w{1,16}).*",
        "(\\w{1,16}) was turned to dust by (\\w{1,16}).*",
        "(\\w{1,16}) was turned to ash by (\\w{1,16}).*",
        "(\\w{1,16}) was melted by (\\w{1,16}).*",
        "(\\w{1,16}) was incinerated by (\\w{1,16}).*",
        "(\\w{1,16}) was vaporized by (\\w{1,16}).*",
        /*Love messages*/
        "(\\w{1,16}) was struck with Cupid's arrow by (\\w{1,16}).*",
        "(\\w{1,16}) was given the cold shoulder by (\\w{1,16}).*",
        "(\\w{1,16}) was hugged too hard by (\\w{1,16}).*",
        "(\\w{1,16}) drank a love potion from (\\w{1,16}).*",
        "(\\w{1,16}) was hit by a love bomb from (\\w{1,16}).*",
        "(\\w{1,16}) was no match for (\\w{1,16}).*",
        /*Paladin messages*/
        "(\\w{1,16}) was smote from afar by (\\w{1,16}).*",
        "(\\w{1,16}) was justly ended by (\\w{1,16}).*",
        "(\\w{1,16}) was purified by (\\w{1,16}).*",
        "(\\w{1,16}) was killed with holy water by (\\w{1,16}).*",
        "(\\w{1,16}) was dealt vengeful justice by (\\w{1,16}).*",
        "(\\w{1,16}) was returned to dust by (\\w{1,16}).*",
        /*Pirate messages*/
        "(\\w{1,16}) be shot and killed by (\\w{1,16}).*",
        "(\\w{1,16}) be snowballed to death by (\\w{1,16}).*",
        "(\\w{1,16}) be sent to Davy Jones' locker by (\\w{1,16}).*",
        "(\\w{1,16}) be killed with rum by (\\w{1,16}).*",
        "(\\w{1,16}) be shot with cannon by (\\w{1,16}).*",
        "(\\w{1,16}) be killed with magic by (\\w{1,16}).*",
        /*BBQ messages*/
        "(\\w{1,16}) was glazed in BBQ sauce by (\\w{1,16}).*",
        "(\\w{1,16}) was sprinkled with chilli powder by (\\w{1,16}).*",
        "(\\w{1,16}) was sliced up by (\\w{1,16}).*",
        "(\\w{1,16}) was overcooked by (\\w{1,16}).*",
        "(\\w{1,16}) was deep fried by (\\w{1,16}).*",
        "(\\w{1,16}) was boiled by (\\w{1,16}).*",
        /*Squeak messages*/
        "(\\w{1,16}) was squeaked from a distance by (\\w{1,16}).*",
        "(\\w{1,16}) was hit by frozen cheese from (\\w{1,16}).*",
        "(\\w{1,16}) was chewed up by (\\w{1,16}).*",
        "(\\w{1,16}) was chemically cheesed by (\\w{1,16}).*",
        "(\\w{1,16}) was turned into cheese whiz by (\\w{1,16}).*",
        "(\\w{1,16}) was magically squeaked by (\\w{1,16}).*",
        /*Bunny messages*/
        "(\\w{1,16}) was hit by a flying bunny by (\\w{1,16}).*",
        "(\\w{1,16}) was hit by a bunny thrown by (\\w{1,16}).*",
        "(\\w{1,16}) was turned into a carrot by (\\w{1,16}).*",
        "(\\w{1,16}) was hit by a carrot from (\\w{1,16}).*",
        "(\\w{1,16}) was bitten by a bunny from (\\w{1,16}).*",
        "(\\w{1,16}) was magically turned into a bunny by (\\w{1,16}).*",
        "(\\w{1,16}) was fed to a bunny by (\\w{1,16}).*",
        /*Natural deaths messages*/
        "(\\w{1,16}) starved to death\\.",
        "(\\w{1,16}) hit the ground too hard\\.",
        "(\\w{1,16}) blew up\\.",
        "(\\w{1,16}) exploded\\.",
        "(\\w{1,16}) tried to swim in lava\\.",
        "(\\w{1,16}) went up in flames\\.",
        "(\\w{1,16}) burned to death\\.",
        "(\\w{1,16}) suffocated in a wall\\.",
        "(\\w{1,16}) suffocated\\.",
        "(\\w{1,16}) fell out of the world\\.",
        "(\\w{1,16}) had a block fall on them\\.",
        "(\\w{1,16}) drowned\\.",
        "(\\w{1,16}) died from a cactus\\." };
    public int withersDeadCount = 0;
    private final Map<String, Integer> allPlayers = new HashMap<>();
    private final Map<String, Integer> blue = new HashMap<>();
    private final Map<String, Integer> green = new HashMap<>();
    private final Map<String, Integer> red = new HashMap<>();
    private final Map<String, Integer> yellow = new HashMap<>();
    private final List<String> deadPlayers = new ArrayList<>();
    private String bluePrefix = "\u00A79";
    private String greenPrefix = "\u00A7a";
    private String redPrefix = "\u00A7c";
    private String yellowPrefixOld = "\u00A7e";
    private String yellowPrefix = "\u00A76";
    private boolean blueWitherDead = false;
    private boolean greenWitherDead = false;
    private boolean redWitherDead = false;
    private boolean yellowWitherDead = false;
    private boolean blueWitherProcessed = false;
    private boolean greenWitherProcessed = false;
    private boolean redWitherProcessed = false;
    private boolean yellowWitherProcessed = false;
    private static Pattern[] KILL_PATTERNS;

    public ChatMessageParser(WeaveFks weavefks) {
        this.weavefks = weavefks;
        KILL_PATTERNS = new Pattern[KILL_MESSAGES.length];
        for (int i = 0; i < KILL_MESSAGES.length; i++) {
            KILL_PATTERNS[i] = Pattern.compile(KILL_MESSAGES[i]);
        }
    }

    public Map<String, Integer> getAllPlayers() {
        return allPlayers;
    }

    public Map<String, Integer> getBlue() {
        return blue;
    }

    public Map<String, Integer> getGreen() {
        return green;
    }

    public Map<String, Integer> getRed() {
        return red;
    }

    public Map<String, Integer> getYellow() {
        return yellow;
    }

    public String getBluePrefix() {
        return bluePrefix;
    }

    public String getGreenPrefix() {
        return greenPrefix;
    }

    public String getRedPrefix() {
        return redPrefix;
    }

    public String getYellowPrefix() {
        return yellowPrefix;
    }

    public String getFinalsInTabString(String playerName) {
        if (weavefks.getConfig().finalsInTab) {
            if (allPlayers.containsKey(playerName)) {
                return " " + "\u00A76" + allPlayers.get(playerName);
            }
        }
        return "";
    }

    public String getFinalsPlayer(String playerName) {
        if (allPlayers.containsKey(playerName)) {
            return " " + "\u00A76" + allPlayers.get(playerName);
        }
        return "";
    }

    public int getWithersDeadCount() {
        return withersDeadCount;
    }

    public void reset() {
        allPlayers.clear();
        blue.clear();
        green.clear();
        red.clear();
        yellow.clear();
        deadPlayers.clear();
        blueWitherDead = false;
        greenWitherDead = false;
        redWitherDead = false;
        yellowWitherDead = false;
        blueWitherProcessed = false;
        greenWitherProcessed = false;
        redWitherProcessed = false;
        yellowWitherProcessed = false;
        withersDeadCount = 0;
        weavefks.getFinalsCounterRenderer().update();
    }

    private List<String> getScoreboardLines() {
        List<String> lines = new ArrayList<>();

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null) {
            return lines;
        }

        Scoreboard scoreboard = mc.theWorld.getScoreboard();

        if (scoreboard == null) {
            return lines;
        }

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

        if (objective == null) {
            return lines;
        }

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = scores
                .stream()
                .filter(input -> {
                    if (input != null) {
                        String playerName = input.getPlayerName();

                        return playerName != null && !playerName.startsWith("#");
                    }
                    return false;
                }).collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            String playerName = score.getPlayerName();
            ScorePlayerTeam team = scoreboard.getPlayersTeam(playerName);
            lines.add(ScorePlayerTeam.formatPlayerName(team, playerName));
        }

        return lines;

    }

    public void onChat(IChatComponent iChatComponent) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getCurrentServerData() == null) {
                return;
            }
            String serverIP = mc.getCurrentServerData().serverIP;
            if (!serverIP.toLowerCase().endsWith("hypixel.net")) {
                return;
            }
            weavefks.getFinalsCounterRenderer().update();
            String unformattedText = iChatComponent.getUnformattedText();

            if (unformattedText.equals("                                 Mega Walls")) {
                reset();
                return;
            }
            if (mc.theWorld == null) {
                return;
            }
            Scoreboard scoreboard = mc.theWorld.getScoreboard();

            if (scoreboard == null) {
                return;
            }

            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

            if (objective == null) {
                return;
            }

            String scoreboardTitle = objective.getDisplayName();
            scoreboardTitle = StringUtils.stripControlCodes(scoreboardTitle); 
            if (!scoreboardTitle.contains("MEGA WALLS")) {
                return;
            }

            for (Pattern pattern : KILL_PATTERNS) {
                Matcher matcher = pattern.matcher(unformattedText);

                if (matcher.matches()) {
                    for (String line : getScoreboardLines()) {
                        if (line.contains("[B]")) {
                            bluePrefix = line.substring(0, 2);
                            blueWitherDead = !line.contains("Wither");
                        } else if (line.contains("[G]")) {
                            greenPrefix = line.substring(0, 2);
                            greenWitherDead = !line.contains("Wither");
                        } else if (line.contains("[R]")) {
                            redPrefix = line.substring(0, 2);
                            redWitherDead = !line.contains("Wither");
                        } else if (line.contains("[Y]")) {
                            yellowPrefix = line.substring(0, 2);
                            yellowWitherDead = !line.contains("Wither");
                        }
                    }
                    if (blueWitherDead && !blueWitherProcessed) {
                        EventBus.callEvent(new MegaWallsGameEvent(MegaWallsGameEvent.EventType.BLUE_WITHER_DEAD));
                        blueWitherProcessed = true;
                        withersDeadCount++;
                    }
                    if (greenWitherDead && !greenWitherProcessed) {
                        EventBus.callEvent(new MegaWallsGameEvent(MegaWallsGameEvent.EventType.GREEN_WITHER_DEAD));
                        greenWitherProcessed = true;
                        withersDeadCount++;
                    }
                    if (redWitherDead && !redWitherProcessed) {
                        EventBus.callEvent(new MegaWallsGameEvent(MegaWallsGameEvent.EventType.RED_WITHER_DEAD));
                        redWitherProcessed = true;
                        withersDeadCount++;
                    }
                    if (yellowWitherDead && !yellowWitherProcessed) {
                        EventBus.callEvent(new MegaWallsGameEvent(MegaWallsGameEvent.EventType.YELLOW_WITHER_DEAD));
                        yellowWitherProcessed = true;
                        withersDeadCount++;
                    }

                    if (matcher.groupCount() == 2) {
                        String killed = matcher.group(1);
                        String killer = matcher.group(2);

                        String formattedText = iChatComponent.getFormattedText();

                        String killedPrefix = formattedText.substring(formattedText.indexOf(killed) - 2, formattedText.indexOf(killed));
                        String killerPrefix = formattedText.substring(formattedText.indexOf(killer) - 2, formattedText.indexOf(killer));

                        if (killedPrefix.equals(bluePrefix) && blueWitherDead) {
                            blue.remove(killed);
                        } else if (killedPrefix.equals(greenPrefix) && greenWitherDead) {
                            green.remove(killed);
                        } else if (killedPrefix.equals(redPrefix) && redWitherDead) {
                            red.remove(killed);
                        } else if (killedPrefix.equals(yellowPrefix) && yellowWitherDead) {
                            yellow.remove(killed);
                        } else {
                            return;
                        }

                        allPlayers.remove(killed);
                        deadPlayers.add(killed);

                        if (!killed.equals(killer) && !deadPlayers.contains(killer)) {
                            if (killerPrefix.equals(bluePrefix)) {
                                blue.put(killer, blue.getOrDefault(killer, 0) + 1);
                            } else if (killerPrefix.equals(greenPrefix)) {
                                green.put(killer, green.getOrDefault(killer, 0) + 1);
                            } else if (killerPrefix.equals(redPrefix)) {
                                red.put(killer, red.getOrDefault(killer, 0) + 1);
                            } else if (killerPrefix.equals(yellowPrefix)) {
                                yellow.put(killer, yellow.getOrDefault(killer, 0) + 1);
                            }

                            allPlayers.put(killer, allPlayers.getOrDefault(killer, 0) + 1);
                        }

                        weavefks.getFinalsCounterRenderer().update();
                        return;
                    }
                    if (matcher.groupCount() == 1) {
                        String killed = matcher.group(1);

                        String formattedText = iChatComponent.getFormattedText();

                        String killedPrefix = formattedText.substring(formattedText.indexOf(killed) - 2, formattedText.indexOf(killed));

                        if (killedPrefix.equals(bluePrefix) && blueWitherDead) {
                            blue.remove(killed);
                        } else if (killedPrefix.equals(greenPrefix) && greenWitherDead) {
                            green.remove(killed);
                        } else if (killedPrefix.equals(redPrefix) && redWitherDead) {
                            red.remove(killed);
                        } else if (killedPrefix.equals(yellowPrefix) && yellowWitherDead) {
                            yellow.remove(killed);
                        } else {
                            return;
                        }

                        allPlayers.remove(killed);
                        deadPlayers.add(killed);

                        weavefks.getFinalsCounterRenderer().update();
                        return;
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}