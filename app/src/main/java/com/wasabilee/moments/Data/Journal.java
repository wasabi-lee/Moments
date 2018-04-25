package com.wasabilee.moments.Data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Journal {

    private String user_id;

    @ServerTimestamp
    private Date timestamp;

    @ServerTimestamp
    private Date user_designated_timestamp;

    private boolean day_journal_exists;
    private boolean night_journal_exists;

    private String day_image_url;
    private String day_image_thumbnail_url;
    private List<String> topic_1;
    private List<String> topic_2;
    private List<String> topic_3;

    private String night_image_url;
    private String night_image_thumbnail_url;
    private List<String> topic_4;
    private List<String> topic_5;

    public Journal(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDay_journal_exists() {
        return day_journal_exists;
    }

    public void setDay_journal_exists(boolean day_journal_exists) {
        this.day_journal_exists = day_journal_exists;
    }

    public boolean isNight_journal_exists() {
        return night_journal_exists;
    }

    public void setNight_journal_exists(boolean night_journal_exists) {
        this.night_journal_exists = night_journal_exists;
    }

    public String getDay_image_url() {
        return day_image_url;
    }

    public void setDay_image_url(String day_image_url) {
        this.day_image_url = day_image_url;
    }

    public String getDay_image_thumbnail_url() {
        return day_image_thumbnail_url;
    }

    public void setDay_image_thumbnail_url(String day_image_thumbnail_url) {
        this.day_image_thumbnail_url = day_image_thumbnail_url;
    }

    public String getNight_image_url() {
        return night_image_url;
    }

    public void setNight_image_url(String night_image_url) {
        this.night_image_url = night_image_url;
    }

    public String getNight_image_thumbnail_url() {
        return night_image_thumbnail_url;
    }

    public void setNight_image_thumbnail_url(String night_image_thumbnail_url) {
        this.night_image_thumbnail_url = night_image_thumbnail_url;
    }

    public Date getUser_designated_timestamp() {
        return user_designated_timestamp;
    }

    public void setUser_designated_timestamp(Date user_designated_timestamp) {
        this.user_designated_timestamp = user_designated_timestamp;
    }

    public List<String> getTopic_1() {
        return topic_1;
    }

    public void setTopic_1(List<String> topic_1) {
        this.topic_1 = topic_1;
    }

    public List<String> getTopic_2() {
        return topic_2;
    }

    public void setTopic_2(List<String> topic_2) {
        this.topic_2 = topic_2;
    }

    public List<String> getTopic_3() {
        return topic_3;
    }

    public void setTopic_3(List<String> topic_3) {
        this.topic_3 = topic_3;
    }

    public List<String> getTopic_4() {
        return topic_4;
    }

    public void setTopic_4(List<String> topic_4) {
        this.topic_4 = topic_4;
    }

    public List<String> getTopic_5() {
        return topic_5;
    }

    public void setTopic_5(List<String> topic_5) {
        this.topic_5 = topic_5;
    }
}
