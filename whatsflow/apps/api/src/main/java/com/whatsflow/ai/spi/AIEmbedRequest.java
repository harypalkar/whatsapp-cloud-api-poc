package com.whatsflow.ai.spi;


import java.util.List;
public record AIEmbedRequest(String model, List<String> inputs) {}
