package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.example.config.Config;
import org.example.config.wincombination.WinCombination;
import org.example.config.symbol.BonusSymbol;
import org.example.config.symbol.StandardSymbol;
import org.example.dto.MatrixElementProbability;
import org.example.dto.OutputDTO;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide the required parameters: --config <config-name> --betting-amount <betting-amount>");
            return;
        }

        String configFilePath = null;
        String bettingAmount = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--config")) {
                configFilePath = args[++i];
            } else if (args[i].equals("--betting-amount")) {
                bettingAmount = args[++i];
            }
        }

        if (configFilePath == null || bettingAmount == null) {
            System.out.println("Please provide both the config file and the betting amount.");
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(configFilePath)));
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            Config config = mapper.readValue(content, Config.class);

            List<MatrixElementProbability> matrixElementProbabilities = createRandomProbabilityArray(config);

            String[][] matrix = fillMatrix(matrixElementProbabilities, config);

            OutputDTO output = calculatePrize(matrix, config, bettingAmount);
            System.out.println(writer.writeValueAsString(output));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected static String[][] fillMatrix(List<MatrixElementProbability> matrixElementProbabilities, Config config) {
        String[][] matrix = new String[config.rows()][config.columns()];
        Random random = new Random();
        for (int i = 0; i < config.rows(); i++) {
            for (int j = 0; j < config.columns(); j++) {
                int finalI = i;
                int finalJ = j;
                MatrixElementProbability elementProbability = matrixElementProbabilities.stream().filter(matrixElementProbability -> matrixElementProbability.getRow() == finalI && matrixElementProbability.getColumn() == finalJ).findFirst().orElseThrow();
                int randomIndex = random.nextInt(elementProbability.getProbability().length);
                matrix[i][j] = elementProbability.getProbability()[randomIndex];
            }
        }

        return matrix;
    }

    protected static List<MatrixElementProbability> createRandomProbabilityArray(Config config) {
        List<MatrixElementProbability> matrixElementProbability = new ArrayList<>();
        double bonusSymbolProbabilitySum = config.probabilities().bonusSymbols().symbols().values().stream().mapToDouble(Double::doubleValue).sum();

        config.probabilities().standardSymbols().forEach(standardSymbolProbability -> {
            Double probabilitiesSum = standardSymbolProbability.symbols().values().stream().mapToDouble(Double::doubleValue).sum() + bonusSymbolProbabilitySum;

            Map<String, Long> probabilityMap = new HashMap<>();
            standardSymbolProbability.symbols().forEach((symbol, probability) -> probabilityMap.put(symbol, Math.round(probability / probabilitiesSum * 100)));
            config.probabilities().bonusSymbols().symbols().forEach((s, aDouble) -> probabilityMap.put(s, Math.round(aDouble / probabilitiesSum * 100)));

            int probabilityArraySize = probabilityMap.values().stream().mapToInt(Math::round).sum();
            String[] probabilityArray = new String[probabilityArraySize];

            Random random = new Random();

            probabilityMap.forEach((symbol, count) -> {
                for (int j = 0; j < count; j++) {
                    boolean placed = false;
                    while (!placed) {
                        int randomIndex = random.nextInt(probabilityArraySize);
                        if (probabilityArray[randomIndex] == null) {
                            probabilityArray[randomIndex] = symbol;
                            placed = true;
                        }
                    }
                }
            });
            matrixElementProbability.add(new MatrixElementProbability(standardSymbolProbability.column(), standardSymbolProbability.row(), probabilityArray));
        });

        return matrixElementProbability;
    }

    protected static OutputDTO calculatePrize(String[][] matrix, Config config, String bettingAmount) {
        Map<String, List<String>> positions = new HashMap<>();
        Map<String, Map<String, WinCombination>> winningCombinations = new HashMap<>();
        Map<String, BonusSymbol> bonusSymbols = config.filterSymbols(BonusSymbol.class);
        Map<String, StandardSymbol> standardSymbolMap = config.filterSymbols(StandardSymbol.class);
        List<String> appliedBonusSymbol = new ArrayList<>();
        Map<String, WinCombination> winCombinationMap = config.winCombinations().toMap();

        preparePositions(matrix, positions);

        findWiningCombinations(winCombinationMap, positions, winningCombinations);

        AtomicReference<Double> reward = calculateReward(bettingAmount, winningCombinations, standardSymbolMap);

        if (reward.get() != 0) {

            Map<String, List<BonusSymbol>> foundBonusSymbols = new HashMap<>();
            positions.keySet().stream().filter(symbol -> bonusSymbols.containsKey(symbol) && !symbol.equals("MISS"))
                    .forEach(symbol -> foundBonusSymbols.computeIfAbsent(symbol, k -> new ArrayList<>()).add(bonusSymbols.get(symbol)));
            appliedBonusSymbol = applyBonuses(foundBonusSymbols, reward);

        }

        Map<String, List<String>> appliedWinningCombinations = winningCombinations.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().keySet().stream().toList()));
        return new OutputDTO(matrix, reward.get().intValue(), appliedWinningCombinations, appliedBonusSymbol);
    }

    private static List<String> applyBonuses(Map<String, List<BonusSymbol>> foundBonusSymbols, AtomicReference<Double> reward) {
        foundBonusSymbols.forEach((name, bonusSymbolList) -> bonusSymbolList.forEach(bonusSymbol -> {
            switch (bonusSymbol.impact()) {
                case "multiply_reward":
                    reward.set(reward.get() * bonusSymbol.reward_multiplier());
                    break;
                case "extra_bonus":
                    reward.set(reward.get() + bonusSymbol.extra());
                    break;
                default:
                    break;
            }
        }));
        return foundBonusSymbols.keySet().stream().toList();
    }

    private static AtomicReference<Double> calculateReward(String bettingAmount, Map<String, Map<String, WinCombination>> winningCombinations, Map<String, StandardSymbol> standardSymbolMap) {
        AtomicReference<Double> reward = new AtomicReference<>(0.0);
        winningCombinations.forEach((symbol, stringWinCombinationMap) -> {
            double symbolMultiplier = standardSymbolMap.get(symbol).reward_multiplier();
            AtomicReference<Double> symbolPrize = new AtomicReference<>(Double.parseDouble(bettingAmount) * symbolMultiplier);
            stringWinCombinationMap.forEach((name, winCombination) ->
                    symbolPrize.set(symbolPrize.get() * winCombination.rewardMultiplier())
            );
            reward.set(reward.get() + symbolPrize.get());
        });

        return reward;
    }

    private static void findWiningCombinations(Map<String, WinCombination> winCombinationMap, Map<String, List<String>> positions, Map<String, Map<String, WinCombination>> winningCombinations) {
        winCombinationMap.forEach((name, winCombination) -> {
            switch (winCombination.when()) {
                case "same_symbols": {
                    positions.forEach((symbol, positionList) -> {
                        if (positionList.size() == winCombination.count())
                            winningCombinations.computeIfAbsent(symbol, k -> new HashMap<>()).put(name, winCombination);
                    });
                    break;
                }
                case "linear_symbols": {
                    positions.forEach((symbol, positionList) -> winCombination.coveredAreas().forEach(coveredArea -> {
                        if (new HashSet<>(positionList).containsAll(coveredArea))
                            winningCombinations.computeIfAbsent(symbol, k -> new HashMap<>()).put(name, winCombination);
                    }));
                    break;
                }
                default:
                    System.out.println("Undefined WinCombination When for: " + name);
            }
        });
    }

    private static void preparePositions(String[][] matrix, Map<String, List<String>> positions) {
        for (int row = 0; row < matrix.length; row++) {
            for (int column = 0; column < matrix[row].length; column++) {
                String symbol = matrix[row][column];
                positions.computeIfAbsent(symbol, k -> new ArrayList<>()).add(row + ":" + column);
            }
        }
    }
}