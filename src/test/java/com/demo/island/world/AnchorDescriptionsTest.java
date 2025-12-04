package com.demo.island.world;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AnchorDescriptionsTest {

    @Test
    void allAnchorsHaveNonEmptyDescriptions() {
        List<String> descriptions = AnchorTiles.all().stream()
                .map(AnchorTile::getDescription)
                .collect(Collectors.toList());

        descriptions.forEach(desc -> {
            assertThat(desc).isNotNull();
            assertThat(desc.length()).isGreaterThanOrEqualTo(40);
        });

        // No duplicates
        assertThat(descriptions).doesNotHaveDuplicates();
    }

    @Test
    void descriptionsContainRoleKeywords() {
        Map<String, List<String>> expectedKeywords = Map.ofEntries(
                Map.entry("T_WRECK_BEACH", List.of("beach", "shore")),
                Map.entry("T_TIDEPOOL_ROCKS", List.of("rock", "tide")),
                Map.entry("T_HIDDEN_COVE", List.of("cove")),
                Map.entry("T_CAMP", List.of("camp", "clearing")),
                Map.entry("T_STREAM", List.of("stream", "water")),
                Map.entry("T_VINE_FOREST", List.of("vine")),
                Map.entry("T_BAMBOO_GROVE", List.of("bamboo")),
                Map.entry("T_WATERFALL_POOL", List.of("waterfall", "pool")),
                Map.entry("T_CAVE_ENTRANCE", List.of("cave", "opening")),
                Map.entry("T_CLIFF_EDGE", List.of("cliff", "edge", "drop")),
                Map.entry("T_OLD_RUINS", List.of("ruin", "stone")),
                Map.entry("T_SIGNAL_HILL", List.of("hill", "signal", "high"))
        );

        expectedKeywords.forEach((tileId, keywords) -> {
            String desc = AnchorTiles.byId(tileId).orElseThrow().getDescription().toLowerCase(Locale.ROOT);
            boolean matched = keywords.stream().anyMatch(desc::contains);
            assertThat(matched).as("Description for %s should contain one of %s", tileId, keywords).isTrue();
        });
    }

    @Test
    void directionalHintsPresentOnKeyTiles() {
        Set<String> required = Set.of("T_CAMP", "T_BAMBOO_GROVE", "T_WATERFALL_POOL", "T_CLIFF_EDGE");
        List<String> directionalTokens = List.of("north", "south", "east", "west", "uphill", "downhill", "above", "below", "up", "down");

        required.forEach(id -> {
            String desc = AnchorTiles.byId(id).orElseThrow().getDescription().toLowerCase(Locale.ROOT);
            boolean hasDir = directionalTokens.stream().anyMatch(desc::contains);
            assertThat(hasDir).as("Tile %s should hint directions", id).isTrue();
        });
    }

    @Test
    void spawnDescriptionAvailable() {
        PlayerLocation loc = PlayerLocation.spawn();
        AnchorTile tile = AnchorTiles.byId(loc.getTileId()).orElseThrow();
        assertThat(tile.getTileId()).isEqualTo("T_WRECK_BEACH");
        assertThat(tile.getDescription().toLowerCase(Locale.ROOT)).contains("beach");
    }
}
