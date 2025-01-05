package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SayFks extends Command {
    public SayFks() {
        super("fks");
    }

    @Override
    public void handle(@NotNull String[] args) {
        if (args.length == 0 || !"say".equalsIgnoreCase(args[0])) {
            return; // No subcommand provided or incorrect subcommand
        }

        WeaveFks weavefks = WeaveFks.getInstance();

        try {
            int blueFinals = weavefks.getChatMessageParser().getBlue()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);

            int greenFinals = weavefks.getChatMessageParser().getGreen()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);

            int redFinals = weavefks.getChatMessageParser().getRed()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);

            int yellowFinals = weavefks.getChatMessageParser().getYellow()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);
            
            int[] finalsArray = {blueFinals, greenFinals, redFinals, yellowFinals};
            String[] teamNames = {"Blue", "Green", "Red", "Yellow"};
            String[] sortedTeams = Arrays.stream(teamNames)
            .sorted(Comparator.comparingInt(team -> -finalsArray[Arrays.asList(teamNames).indexOf(team)]))
            .toArray(String[]::new);

            String sortedFinals = Arrays.stream(sortedTeams)
            .map(team -> team + ": " + finalsArray[Arrays.asList(teamNames).indexOf(team)])
            .collect(Collectors.joining(" "));

            weavefks.sendMessage(sortedFinals);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
