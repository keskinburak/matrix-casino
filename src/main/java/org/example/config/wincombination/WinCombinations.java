package org.example.config.wincombination;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public record WinCombinations(@JsonProperty("same_symbol_3_times") WinCombination sameSymbol3times,
                              @JsonProperty("same_symbol_4_times") WinCombination sameSymbol4Times,
                              @JsonProperty("same_symbol_5_times") WinCombination sameSymbol5Times,
                              @JsonProperty("same_symbol_6_times") WinCombination sameSymbol6Times,
                              @JsonProperty("same_symbol_7_times") WinCombination sameSymbol7Times,
                              @JsonProperty("same_symbol_8_times") WinCombination sameSymbol8Times,
                              @JsonProperty("same_symbol_9_times") WinCombination sameSymbol9Times,
                              WinCombination sameSymbolsHorizontally, WinCombination sameSymbolsVertically,
                              WinCombination sameSymbolsDiagonallyLeftToRight,
                              WinCombination sameSymbolsDiagonallyRightToLeft) {
    public Map<String, WinCombination> toMap() {
        Map<String, WinCombination> map = new HashMap<>();
        map.put("same_symbol_3_times", sameSymbol3times);
        map.put("same_symbol_4_times", sameSymbol4Times);
        map.put("same_symbol_5_times", sameSymbol5Times);
        map.put("same_symbol_6_times", sameSymbol6Times);
        map.put("same_symbol_7_times", sameSymbol7Times);
        map.put("same_symbol_8_times", sameSymbol8Times);
        map.put("same_symbol_9_times", sameSymbol9Times);
        map.put("same_symbols_horizontally", sameSymbolsHorizontally);
        map.put("same_symbols_vertically", sameSymbolsVertically);
        map.put("same_symbols_diagonally_left_to_right", sameSymbolsDiagonallyLeftToRight);
        map.put("same_symbols_diagonally_right_to_left", sameSymbolsDiagonallyRightToLeft);
        return map;
    }
}
