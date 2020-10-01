package biz.dealnote.messenger.activity;

import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.SelectProfileCriteria;

public interface ProfileSelectable {

    void select(Owner owner);

    SelectProfileCriteria getAcceptableCriteria();
}