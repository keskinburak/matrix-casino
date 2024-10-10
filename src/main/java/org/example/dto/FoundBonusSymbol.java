package org.example.dto;

import org.example.config.symbol.BonusSymbol;

public record FoundBonusSymbol(String symbolName, BonusSymbol bonusSymbol, int quantity) {
}
