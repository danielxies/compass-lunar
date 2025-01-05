package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerFinalsCommand extends Command {
    public PlayerFinalsCommand() {
        super("playerfinals");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        try {
            Map<String, Integer> reverseSortedMap = weavefks.getChatMessageParser().getAllPlayers()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new));

            StringBuilder stringBuilder = new StringBuilder();

            int i = 1;
            for (Map.Entry<String, Integer> entry : reverseSortedMap.entrySet()) {
                String prefix = "";

                if (weavefks.getChatMessageParser().getBlue().containsKey(entry.getKey())) {
                    prefix = weavefks.getChatMessageParser().getBluePrefix();
                } else if (weavefks.getChatMessageParser().getGreen().containsKey(entry.getKey())) {
                    prefix = weavefks.getChatMessageParser().getGreenPrefix();
                } else if (weavefks.getChatMessageParser().getRed().containsKey(entry.getKey())) {
                    prefix = weavefks.getChatMessageParser().getRedPrefix();
                } else if (weavefks.getChatMessageParser().getYellow().containsKey(entry.getKey())) {
                    prefix = weavefks.getChatMessageParser().getYellowPrefix();
                }

                stringBuilder.append(i).append(". ").append(prefix).append(entry.getKey()).append(": ").append("\u00A7f").append(entry.getValue()).append("\n");
                i++;
            }

            if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.replace(stringBuilder.lastIndexOf("\n"), stringBuilder.lastIndexOf("\n") + 1, "");
            }

            weavefks.addChatComponentText(stringBuilder.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
