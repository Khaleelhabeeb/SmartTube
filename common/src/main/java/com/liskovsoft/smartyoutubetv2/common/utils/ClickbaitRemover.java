package com.liskovsoft.smartyoutubetv2.common.utils;

import android.os.Build.VERSION;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;

import java.util.regex.Pattern;

public class ClickbaitRemover {
    public static final int THUMB_QUALITY_DEFAULT = 0;
    public static final int THUMB_QUALITY_START = 1;
    public static final int THUMB_QUALITY_MIDDLE = 2;
    public static final int THUMB_QUALITY_END = 3;

    private static final Pattern THUMB_QUALITY_PATTERN = Pattern.compile("/(hq1|hq2|hq3|hqdefault|mqdefault|sddefault|hq720)\\.");

    public static String updateThumbnail(String thumbUrl, int thumbQuality) {
        if (thumbUrl == null || thumbQuality == THUMB_QUALITY_DEFAULT) {
            return thumbUrl;
        }

        String quality = "hqdefault";

        switch (thumbQuality) {
            case THUMB_QUALITY_START:
                quality = "hq1";
                break;
            case THUMB_QUALITY_MIDDLE:
                quality = "hq2";
                break;
            case THUMB_QUALITY_END:
                quality = "hq3";
                break;
        }

        return Helpers.replace(thumbUrl, THUMB_QUALITY_PATTERN, "/" + quality + ".");
    }

    public static String updateThumbnail(Video video, int thumbQuality) {
        if (video == null) {
            return null;
        }

        if (video.isLive || video.isUpcoming || video.altCardImageUrl != null) { // priority to DeArrow
            return video.getCardImageUrl();
        }

        // Android 4.4 and below: search may return signed, webp/avif or oversized thumbnails that fail to load.
        // Use the plain medium-size jpg that is always available and decodable.
        if (VERSION.SDK_INT <= 19 && video.videoId != null) {
            return getLegacyThumbnail(video.videoId, thumbQuality);
        }

        return updateThumbnail(video.getCardImageUrl(), thumbQuality);
    }

    private static String getLegacyThumbnail(String videoId, int thumbQuality) {
        String quality = "hqdefault";

        switch (thumbQuality) {
            case THUMB_QUALITY_START:
                quality = "hq1";
                break;
            case THUMB_QUALITY_MIDDLE:
                quality = "hq2";
                break;
            case THUMB_QUALITY_END:
                quality = "hq3";
                break;
        }

        return String.format("https://i.ytimg.com/vi/%s/%s.jpg", videoId, quality);
    }
}
