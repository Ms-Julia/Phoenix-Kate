package biz.dealnote.messenger.mvp.view;

import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IPollView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayQuestion(String title);

    void displayPhoto(String photo_url);

    void displayType(boolean anonymous);

    void displayCreationTime(long unixtime);

    void displayVoteCount(int count);

    void displayVotesList(List<Poll.Answer> answers, boolean canCheck, boolean multiply, Set<Integer> checked);

    void displayLoading(boolean loading);

    void setupButton(boolean voted);
}