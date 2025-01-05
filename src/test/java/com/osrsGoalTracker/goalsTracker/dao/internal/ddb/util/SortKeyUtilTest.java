package com.osrsGoalTracker.goalsTracker.dao.internal.ddb.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the SortKeyUtil class.
 */
class SortKeyUtilTest {

    @Test
    void testGetUserMetadataSortKey() {
        assertThat(SortKeyUtil.getUserMetadataSortKey())
                .isEqualTo("METADATA");
    }
}
