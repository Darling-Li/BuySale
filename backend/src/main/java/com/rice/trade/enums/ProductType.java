package com.rice.trade.enums;

public enum ProductType {
    RICE("稻谷"),
    SEED("稻种"),
    FERTILIZER("化肥");

    private final String label;

    ProductType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

