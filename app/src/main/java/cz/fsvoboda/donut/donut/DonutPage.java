package cz.fsvoboda.donut.donut;

import android.view.View;

public class DonutPage {
    private View layout;
    private int maxValue;
    private int value;
    private int color = 0;

    public DonutPage(View layout, int maxValue, int value) {
        this.layout = layout;
        this.maxValue = maxValue;
        this.value = value;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public View getLayout() {
        return this.layout;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
