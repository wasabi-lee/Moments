package com.wasabilee.moments.data.models;

public abstract class JournalData {

    public static final int TYPE_DATE = 0;
    public static final int TYPE_JOURNAL = 1;

    abstract public int getType();

}
