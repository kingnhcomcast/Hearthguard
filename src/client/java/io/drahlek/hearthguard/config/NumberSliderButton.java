package io.drahlek.hearthguard.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class NumberSliderButton extends AbstractSliderButton {
    // private double initialValue;
    private final double min;
    private final double max;
    private final ValueChangedListener listener;
    private final String label;
    private final double step;
    private double currentValue;
    private boolean wholeNumber;

    public NumberSliderButton(String label, int x, int y, int width, int height, double initialValue, double min, double max, boolean wholeNumber, double step, ValueChangedListener listener) {
        super(x, y, width, height, Component.literal(""), initialValue);
        this.label = label;
        //this.initialValue = initialValue;
        this.currentValue = initialValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.wholeNumber = wholeNumber;
        this.listener = listener;

        this.updateMessage(); // set initial label
        // Initialize the slider thumb position correctly
        this.setValue((initialValue - min) / (max - min));
    }

    @Override
    protected void updateMessage() {
        if (wholeNumber) {
            this.setMessage(Component.literal(label + ": " + (int) currentValue));
        } else {
            this.setMessage(Component.literal(String.format("%s: %.1f", label, currentValue)));
        }
    }

    @Override
    protected void applyValue() {
        double rawValue = value * (max - min) + min;

        if (wholeNumber) {
            currentValue = Math.round(rawValue);
        } else {
            // Snap to nearest step
            currentValue = Math.round(rawValue / step) * step;
        }

        // Notify listener
        listener.onValueChanged(currentValue);

        // Update the label
        updateMessage();
    }

    // Functional interface for callback when value changes
    public interface ValueChangedListener {
        void onValueChanged(double newValue);
    }

//    public int getInitiaValue() {
//        return initialValue;
//    }
//
//    public void setInitiaValue(int initiaValue) {
//        this.initialValue = Math.max(min, Math.min(max, initiaValue));
//        this.setValue((double)(this.initialValue - min) / (max - min));
//        updateMessage();
//    }
}
