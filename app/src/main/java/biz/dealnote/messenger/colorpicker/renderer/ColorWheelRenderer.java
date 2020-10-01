package biz.dealnote.messenger.colorpicker.renderer;

import java.util.List;

import biz.dealnote.messenger.colorpicker.ColorCircle;

public interface ColorWheelRenderer {
    float GAP_PERCENTAGE = 0.025f;

    void draw();

    ColorWheelRenderOption getRenderOption();

    void initWith(ColorWheelRenderOption colorWheelRenderOption);

    List<ColorCircle> getColorCircleList();
}
