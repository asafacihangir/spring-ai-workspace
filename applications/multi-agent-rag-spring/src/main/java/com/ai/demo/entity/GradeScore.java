package com.ai.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GradeScore(@JsonProperty("binary_score") String binaryScore) {
}
