package com.yw.springaialibabademobiz.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"question", "answer"})
public record QaPair(String question, String answer) {
}
