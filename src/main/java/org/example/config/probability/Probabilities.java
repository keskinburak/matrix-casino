package org.example.config.probability;

import java.util.List;

public record Probabilities(
        List<StandardSymbolProbability> standardSymbols,
        BonusSymbolProbability bonusSymbols
){}
