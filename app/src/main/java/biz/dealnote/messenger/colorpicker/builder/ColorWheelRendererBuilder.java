package biz.dealnote.messenger.colorpicker.builder;

import biz.dealnote.messenger.colorpicker.ColorPickerView;
import biz.dealnote.messenger.colorpicker.renderer.ColorWheelRenderer;
import biz.dealnote.messenger.colorpicker.renderer.FlowerColorWheelRenderer;
import biz.dealnote.messenger.colorpicker.renderer.SimpleColorWheelRenderer;

public class ColorWheelRendererBuilder {
    public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
        switch (wheelType) {
            case CIRCLE:
                return new SimpleColorWheelRenderer();
            case FLOWER:
                return new FlowerColorWheelRenderer();
        }
        throw new IllegalArgumentException("wrong WHEEL_TYPE");
    }
}