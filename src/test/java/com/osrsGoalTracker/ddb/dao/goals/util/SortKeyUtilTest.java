package com.osrsGoalTracker.ddb.dao.goals.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SortKeyUtilTest {
    private static final String TEST_RSN = "testRsn";

    @Test
    void shouldReturnMetadataForUserMetadataSortKey() {
        assertThat(SortKeyUtil.getUserMetadataSortKey())
                .isEqualTo("METADATA");
    }

    @Test
    void shouldReturnFormattedKeyForRsnMetadataSortKey() {
        assertThat(SortKeyUtil.getRsnMetadataSortKey(TEST_RSN))
                .isEqualTo("RSN#METADATA#testRsn");
    }

    @Test
    void shouldReturnPrefixForRsnMetadataPrefix() {
        assertThat(SortKeyUtil.getRsnMetadataPrefix())
                .isEqualTo("RSN#METADATA#");
    }
}

