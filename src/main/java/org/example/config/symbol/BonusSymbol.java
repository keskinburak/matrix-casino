package org.example.config.symbol;

public record BonusSymbol(String type, double reward_multiplier, Integer extra, String impact) implements Symbol{}
