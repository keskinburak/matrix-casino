package org.example.config.probability;

import java.util.Map;

public record StandardSymbolProbability(Integer column, Integer row, Map<String, Double> symbols) {
}
