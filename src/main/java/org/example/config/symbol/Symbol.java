package org.example.config.symbol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StandardSymbol.class, name = "standard"),
        @JsonSubTypes.Type(value = BonusSymbol.class, name = "bonus")
})
public sealed interface Symbol permits StandardSymbol, BonusSymbol {
    String type();
}
