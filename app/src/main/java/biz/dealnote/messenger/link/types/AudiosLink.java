package biz.dealnote.messenger.link.types;

import org.jetbrains.annotations.NotNull;

public class AudiosLink extends AbsLink {

    public int ownerId;

    public AudiosLink(int ownerId) {
        super(AUDIOS);
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "AudiosLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
