package org.example.config.wincombination;

import java.util.List;

public record WinCombination(
        Double rewardMultiplier,
        Integer count,
        String group,
        List<List<String>> coveredAreas,
        String when
){}
