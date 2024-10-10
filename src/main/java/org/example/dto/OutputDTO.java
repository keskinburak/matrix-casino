package org.example.dto;

import java.util.List;
import java.util.Map;

public record OutputDTO(String[][] matrix, Integer reward, Map<String, List<String>> appliedWinningCombinations,
                     List<String> appliedBonusSymbols) {
}
