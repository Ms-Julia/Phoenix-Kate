package biz.dealnote.messenger.view.steppers.base;

import java.util.EventListener;

public interface BaseHolderListener extends EventListener {
    void onNextButtonClick(int step);

    void onCancelButtonClick(int step);
}