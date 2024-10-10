package org.example;

import org.example.config.Config;
import org.example.config.probability.BonusSymbolProbability;
import org.example.config.probability.Probabilities;
import org.example.config.probability.StandardSymbolProbability;
import org.example.config.symbol.BonusSymbol;
import org.example.config.symbol.StandardSymbol;
import org.example.config.wincombination.WinCombination;
import org.example.config.wincombination.WinCombinations;
import org.example.dto.MatrixElementProbability;
import org.example.dto.OutputDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void testCreateRandomProbabilityArray() {
        Config config = Mockito.mock(Config.class);

        Probabilities probabilities = Mockito.mock(Probabilities.class);
        BonusSymbolProbability bonusSymbols = Mockito.mock(BonusSymbolProbability.class);
        StandardSymbolProbability standardSymbolProbability = Mockito.mock(StandardSymbolProbability.class);

        Map<String, Double> standardSymbolMap = Map.of("A", 1.0, "B", 3.0);
        Map<String, Double> bonusSymbolMap = Map.of("+100", 1.0);

        Mockito.when(config.probabilities()).thenReturn(probabilities);
        Mockito.when(probabilities.bonusSymbols()).thenReturn(bonusSymbols);
        Mockito.when(bonusSymbols.symbols()).thenReturn(bonusSymbolMap);
        Mockito.when(probabilities.standardSymbols()).thenReturn(List.of(standardSymbolProbability));
        Mockito.when(standardSymbolProbability.symbols()).thenReturn(standardSymbolMap);

        List<MatrixElementProbability> result = Main.createRandomProbabilityArray(config);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getProbability().length);
    }

    @Test
    void testFillMatrix() {
        // Mock Config ve matrixElementProbabilities listesi
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.rows()).thenReturn(3);
        Mockito.when(config.columns()).thenReturn(3);

        List<MatrixElementProbability> matrixElementProbabilities = List.of(
                new MatrixElementProbability(0, 0, new String[]{"A", "B"}),
                new MatrixElementProbability(0, 1, new String[]{"A", "B"}),
                new MatrixElementProbability(0, 2, new String[]{"A", "B"}),
                new MatrixElementProbability(1, 0, new String[]{"A", "B"}),
                new MatrixElementProbability(1, 1, new String[]{"A", "B"}),
                new MatrixElementProbability(1, 2, new String[]{"A", "B"}),
                new MatrixElementProbability(2, 0, new String[]{"A", "B"}),
                new MatrixElementProbability(2, 1, new String[]{"A", "B"}),
                new MatrixElementProbability(2, 2, new String[]{"A", "B"})
        );

        String[][] matrix = Main.fillMatrix(matrixElementProbabilities, config);

        assertNotNull(matrix);
        assertEquals(3, matrix.length);
        assertEquals(3, matrix[0].length);
    }

    @Test
    void testCalculatePrize() {
        // Mock Config, BonusSymbol, StandardSymbol, WinCombination
        Config config = Mockito.mock(Config.class);
        BonusSymbol bonusSymbol = Mockito.mock(BonusSymbol.class);
        StandardSymbol standardSymbol = Mockito.mock(StandardSymbol.class);
        WinCombination winCombination = Mockito.mock(WinCombination.class);
        WinCombination winCombination2 = Mockito.mock(WinCombination.class);
        WinCombinations configWinCombinations = Mockito.mock(WinCombinations.class);

        Map<String, BonusSymbol> bonusSymbols = Map.of("+100", bonusSymbol);
        Map<String, StandardSymbol> standardSymbols = Map.of("A", standardSymbol, "B", standardSymbol);
        Map<String, WinCombination> winCombinations = Map.of("same_symbols", winCombination, "linear_symbols", winCombination2);

        Mockito.when(config.filterSymbols(BonusSymbol.class)).thenReturn(bonusSymbols);
        Mockito.when(config.filterSymbols(StandardSymbol.class)).thenReturn(standardSymbols);
        Mockito.when(config.winCombinations()).thenReturn(configWinCombinations);
        Mockito.when(config.winCombinations().toMap()).thenReturn(winCombinations);

        // Matrix
        String[][] matrix = {
                {"A", "MISS", "B"},
                {"A", "+100", "C"},
                {"A", "MISS", "B"}
        };

        Mockito.when(standardSymbol.reward_multiplier()).thenReturn(1.0);
        Mockito.when(bonusSymbol.impact()).thenReturn("extra_bonus");
        Mockito.when(bonusSymbol.extra()).thenReturn(100);
        Mockito.when(winCombination.rewardMultiplier()).thenReturn(2.0);
        Mockito.when(winCombination.when()).thenReturn("same_symbols");
        Mockito.when(winCombination.count()).thenReturn(3);
        Mockito.when(winCombination2.rewardMultiplier()).thenReturn(2.0);
        Mockito.when(winCombination2.when()).thenReturn("linear_symbols");
        Mockito.when(winCombination2.coveredAreas()).thenReturn(List.of(List.of("0:0", "1:0", "2:0"), List.of("0:1", "1:1", "2:1"), List.of("0:2", "1:2", "2:2")));

        OutputDTO output = Main.calculatePrize(matrix, config, "100");

        assertNotNull(output);
        assertEquals(500, output.reward());
        assertTrue(output.appliedBonusSymbols().contains("+100"));
        assertEquals(output.appliedWinningCombinations().toString(),"{A=[linear_symbols, same_symbols]}");
    }
}
