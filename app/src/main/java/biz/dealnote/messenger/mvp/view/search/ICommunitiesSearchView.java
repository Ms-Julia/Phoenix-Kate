package biz.dealnote.messenger.mvp.view.search;

import biz.dealnote.messenger.model.Community;


public interface ICommunitiesSearchView extends IBaseSearchView<Community> {
    void openCommunityWall(int accountId, Community community);
}