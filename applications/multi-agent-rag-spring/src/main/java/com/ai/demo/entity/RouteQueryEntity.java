package com.ai.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RouteQueryEntity(@JsonProperty("data_source") String dataSource) {
}
