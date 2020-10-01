package biz.dealnote.messenger;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Account_Types.BY_TYPE,
        Account_Types.KATE,
        Account_Types.KATE_HIDDEN})
@Retention(RetentionPolicy.SOURCE)
public @interface Account_Types {
    int BY_TYPE = 0;
    int KATE = 3;
    int KATE_HIDDEN = 4;
}

