package com.wasabilee.moments.Data.Models;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JournalListDisplayItem {

    /**
     * A class that contains displayable items of a journal.
     * It holds randomly selected fields of the journal and get passed to each list items for DataBinding.
     */

    private static final String TAG = JournalListDisplayItem.class.getSimpleName();

    private static final int INDEX_TOPIC_1 = 0;
    private static final int INDEX_TOPIC_2 = 1;
    private static final int INDEX_TOPIC_3 = 2;
    private static final int INDEX_TOPIC_4 = 3;
    private static final int INDEX_TOPIC_5 = 4;
    private static final int[] TOPIC_INDICES = new int[]{INDEX_TOPIC_1, INDEX_TOPIC_2, INDEX_TOPIC_3, INDEX_TOPIC_4, INDEX_TOPIC_5};


    public String imageUrl;
    public String textHeader;
    public String textContent;


    public JournalListDisplayItem(Journal journal) {
        this.imageUrl = getAvailableImageUrl(journal);
        setHeaderAndContent(journal);

    }

    private void setHeaderAndContent(Journal journal) {
        int selectedHeaderIndex = getRandomlySelectedHeaderIndex(journal);
        if (selectedHeaderIndex != -1) {
            this.textHeader = getHeader(selectedHeaderIndex);
            this.textContent = getContent(getTopic(selectedHeaderIndex, journal));
        } else {
            this.textHeader = "";
            this.textContent = "";
        }
    }

    private String getAvailableImageUrl(Journal journal) {
        if (journal.getDay_image_thumbnail_url() != null && journal.getNight_image_thumbnail_url() != null) {
            return journal.getDay_image_thumbnail_url();
        } else if (journal.getDay_image_thumbnail_url() != null) {
            return journal.getDay_image_thumbnail_url();
        } else if (journal.getNight_image_thumbnail_url() != null) {
            return journal.getNight_image_thumbnail_url();
        } else {
            return null;
        }
    }

    private int getRandomlySelectedHeaderIndex(Journal journal) {

        // 1. Iterate through each topic, store any populated topic indices to ArrayList<Integer>, and exclude any empty topics
        // 2. After the iteration, pick any random item in the ArrayList<Integer> and return the corresponding header.

        if (journal == null)
            return -1;

        Random random = new Random();
        ArrayList<Integer> completedTopicIndices = getCompletedTopicIndices(journal);
        if (completedTopicIndices.size() == 0)
            return -1;
        return completedTopicIndices.get(random.nextInt(completedTopicIndices.size()));
    }

    private ArrayList<Integer> getCompletedTopicIndices(Journal journal) {
        ArrayList<Integer> completedTopicIndices = new ArrayList<>();
        for (int currentTopicIndex : TOPIC_INDICES) {
            List<String> topic = getTopic(currentTopicIndex, journal);
            if (topic != null) {
                boolean isTopicEmpty = true;
                for (String topicContent : topic) {
                    if (!TextUtils.isEmpty(topicContent)) {
                        isTopicEmpty = false;
                        break;
                    }
                }
                if (!isTopicEmpty) {
                    completedTopicIndices.add(currentTopicIndex);
                }
            }
        }
        return completedTopicIndices;
    }

    private String getHeader(int selectedHeaderIndex) {
        switch (selectedHeaderIndex) {
            case INDEX_TOPIC_1:
                return Journal.TOPIC_1_DISPLAY_TEXT;
            case INDEX_TOPIC_2:
                return Journal.TOPIC_2_DISPLAY_TEXT;
            case INDEX_TOPIC_3:
                return Journal.TOPIC_3_DISPLAY_TEXT;
            case INDEX_TOPIC_4:
                return Journal.TOPIC_4_DISPLAY_TEXT;
            case INDEX_TOPIC_5:
                return Journal.TOPIC_5_DISPLAY_TEXT;
        }
        return "";
    }

    private List<String> getTopic(int selectedHeaderIndex, Journal journal) {
        switch (selectedHeaderIndex) {
            case INDEX_TOPIC_1:
                return journal.getTopic_1();
            case INDEX_TOPIC_2:
                return journal.getTopic_2();
            case INDEX_TOPIC_3:
                return journal.getTopic_3();
            case INDEX_TOPIC_4:
                return journal.getTopic_4();
            case INDEX_TOPIC_5:
                return journal.getTopic_5();
        }
        return null;
    }

    private String getContent(List<String> topic) {
        if (topic != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < topic.size(); i++) {
                String contentItem = topic.get(i);
                if (!TextUtils.isEmpty(contentItem)) {
                    sb.append(contentItem);
                    if (i != topic.size() - 1) {
                        sb.append("\n");
                    }
                }
            }
            String content = sb.toString();
            return content.trim();
        }
        return "";
    }


}
