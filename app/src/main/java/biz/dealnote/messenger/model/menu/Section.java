package biz.dealnote.messenger.model.menu;

import androidx.annotation.DrawableRes;

import biz.dealnote.messenger.model.Text;

public class Section {

    private final Text title;
    @DrawableRes
    private Integer icon;

    public Section(Text title) {
        this.title = title;
    }

    public Text getTitle() {
        return title;
    }

    public Integer getIcon() {
        return icon;
    }

    public Section setIcon(Integer icon) {
        this.icon = icon;
        return this;
    }
}
