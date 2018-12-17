package com.wasabilee.moments.utils.navigators;

public class JournalStateNavigator {
    /**
     * Static flag class for expressing journal state.
     * Instead of enum, used integer values to utilize them as result codes.
     */
    public static final int JOURNAL_STATE_ADDED_NEW = 1;
    public static final int JOURNAL_STATE_EDITED = 2;
    public static final int JOURNAL_STATE_DELETED = 3;
    public static final int JOURNAL_STATE_UNCHANGED = 4;
    public static final int JOURNAL_STATE_LOAD_FAILED = 5;
}
