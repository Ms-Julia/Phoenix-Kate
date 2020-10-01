package biz.dealnote.messenger.model.menu;

import androidx.annotation.DrawableRes;

import biz.dealnote.messenger.model.Icon;
import biz.dealnote.messenger.model.Text;

public class Item {

    private final int key;
    private final Text title;
    private Icon icon;
    private Section section;

    private int extra;

    public Item(int key, Text title) {
        this.key = key;
        this.title = title;
    }

    public int getExtra() {
        return extra;
    }

    public Item setExtra(int extra) {
        this.extra = extra;
        return this;
    }

    public int getKey() {
        return key;
    }

    public Section getSection() {
        return section;
    }

    public Item setSection(Section section) {
        this.section = section;
        return this;
    }

    public Icon getIcon() {
        return icon;
    }

    public Item setIcon(@DrawableRes int res) {
        icon = Icon.fromResources(res);
        return this;
    }

    public Item setIcon(String remoteUrl) {
        icon = Icon.fromUrl(remoteUrl);
        return this;
    }

    public Item setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public Text getTitle() {
        return title;
    }
}