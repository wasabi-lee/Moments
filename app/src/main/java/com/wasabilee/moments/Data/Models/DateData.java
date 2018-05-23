package com.wasabilee.moments.Data.Models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateData extends JournalData {

    public static final int MONTHLY_FORMAT = 0; // FOR LIST HEADER (i.e. JUL 2018)
    public static final int DAILY_FORMAT = 1; // FOR EACH JOURNALS (i.e. JUL 4, 2018)
    public static final int TIME_FORMAT = 2; // FOR THE EXACT TIME (Locale default format)
    public static final int TIME_ONLY_FORMAT = 3; // FOR ONLY THE TIME (i.e. 07:02 AM)

    private Date date;
    public String dateHeaderFormat;

    public DateData(Date date) {
        this.date = date;
        this.dateHeaderFormat = createFormattedDate(MONTHLY_FORMAT, date);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateHeaderFormat() {
        return dateHeaderFormat;
    }

    public static String createFormattedDate(int format, Date date) {
        switch (format) {
            case MONTHLY_FORMAT:
                return new SimpleDateFormat("MMMM yyyy").format(date);
            case DAILY_FORMAT:
                return new SimpleDateFormat("MMMM d, yyyy").format(date).toUpperCase();
            case TIME_FORMAT:
                return DateFormat.getInstance().format(date).toUpperCase();
            case TIME_ONLY_FORMAT:
                return new SimpleDateFormat("hh:m a").format(date).toUpperCase();
        }
        return new SimpleDateFormat("MMM d, yyyy").format(date).toUpperCase();
    }

    @Override
    public int getType() {
        return TYPE_DATE;
    }
}
