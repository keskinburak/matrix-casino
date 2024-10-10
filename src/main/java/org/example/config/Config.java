package org.example.config;

import org.example.config.probability.Probabilities;
import org.example.config.symbol.Symbol;
import org.example.config.wincombination.WinCombinations;

import java.util.Map;
import java.util.stream.Collectors;

public record Config(
        Integer columns,
        Integer rows,
        Map<String, Symbol> symbols,
        Probabilities probabilities,
        WinCombinations winCombinations
) {
    public <T extends Symbol> Map<String, T> filterSymbols(Class<T> type) {
        return this.symbols.entrySet().stream()
                .filter(entry -> type.isInstance(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> type.cast(entry.getValue())));
    }
}

enum Impact {
    MULTIPLY_REWARD("multiply_reward"),
    EXTRA_BONUS("extra_bonus"),
    MISS("miss");

    private final String name;

    Impact(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

enum WinCombinationGroup {
    SAME_SYMBOL("same_symbols"),
    HORIZONTALLY_LINEAR_SYMBOLS("horizontally_linear_symbols"),
    VERTICALLY_LINEAR_SYMBOLS("vertically_linear_symbols"),
    LTR_DIAGONALLY_LINEAR_SYMBOLS("ltr_diagonally_linear_symbols"),
    RTL_DIAGONALLY_LINEAR_SYMBOLS("rtl_diagonally_linear_symbols");

    private final String name;

    WinCombinationGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

enum WinCombinationWhen {
    LINEAR_SYMBOLS("linear_symbols"),
    SAME_SYMBOLS("same_symbols");

    private final String name;

    WinCombinationWhen(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}