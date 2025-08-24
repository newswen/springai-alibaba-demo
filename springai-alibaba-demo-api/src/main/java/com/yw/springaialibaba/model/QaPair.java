package com.yw.springaialibaba.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"question", "answer"})
public record QaPair(String question, String answer) {
}
