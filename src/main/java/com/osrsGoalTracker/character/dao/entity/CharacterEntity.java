package com.osrsGoalTracker.character.dao.entity;

import com.osrsGoalTracker.shared.dao.entity.AbstractEntity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a RuneScape character in the system.
 * Extends AbstractEntity to inherit common fields like createdAt and updatedAt.
 */
@Getter
@SuperBuilder
public class CharacterEntity extends AbstractEntity {
    private String name;
}