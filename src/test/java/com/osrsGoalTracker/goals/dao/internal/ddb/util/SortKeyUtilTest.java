package com.osrsGoalTracker.goals.dao.internal.ddb.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SortKeyUtilTest {
    @Test
    void testGetUserMetadataSortKey() {
        assertThat(SortKeyUtil.getUserMetadataSortKey())
                .isEqualTo("METADATA");
    }
}
