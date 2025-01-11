package com.osrsGoalTracker.shared.dao.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SortKeyUtilTest {

    @Test
    void testGetUserMetadataSortKeyReturnsCorrectFormat() {
        assertThat(SortKeyUtil.getUserMetadataSortKey())
                .isEqualTo("METADATA");
    }

    @Test
    void testGetGoalMetadataSortKeyReturnsCorrectFormat() {
        String characterName = "testChar";
        String goalId = "goal123";

        assertThat(SortKeyUtil.getGoalMetadataSortKey(characterName, goalId))
                .isEqualTo("CHARACTER#testChar#GOAL#METADATA#goal123");
    }

    @Test
    void testGetLatestGoalProgressSortKeyReturnsCorrectFormat() {
        String characterName = "testChar";
        String goalId = "goal123";

        assertThat(SortKeyUtil.getLatestGoalProgressSortKey(characterName, goalId))
                .isEqualTo("CHARACTER#testChar#GOAL#goal123#LATEST");
    }

    @Test
    void testGetEarliestGoalProgressSortKeyReturnsCorrectFormat() {
        String characterName = "testChar";
        String goalId = "goal123";

        assertThat(SortKeyUtil.getEarliestGoalProgressSortKey(characterName, goalId))
                .isEqualTo("CHARACTER#testChar#GOAL#goal123#EARLIEST");
    }

    @Test
    void testGetNotificationChannelSortKeyReturnsCorrectFormat() {
        String channelType = "DISCORD";

        assertThat(SortKeyUtil.getNotificationChannelSortKey(channelType))
                .isEqualTo("NOTIFICATION#DISCORD");
    }

    @Test
    void testGetCharacterMetadataSortKeyReturnsCorrectFormat() {
        String characterName = "testChar";

        assertThat(SortKeyUtil.getCharacterMetadataSortKey(characterName))
                .isEqualTo("CHARACTER#METADATA#testChar");
    }

    @Test
    void testCharacterMetadataPrefixHasCorrectValue() {
        assertThat(SortKeyUtil.CHARACTER_METADATA_PREFIX)
                .isEqualTo("CHARACTER#METADATA#");
    }
}