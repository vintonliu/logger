/*
 * Copyright (c) 2019 51talk. All rights reserved.
 * TextFormatStrategy.java
 * Creator: Vinton.Liu
 * Create Date: 2019-01-25 18:12:24
 */

package com.orhanobut.logger;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.orhanobut.logger.Utils.checkNotNull;

public class TextFormatStrategy implements FormatStrategy {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String NEW_LINE_REPLACEMENT = " <br> ";
    private static final String SEPARATOR = " ";

    @NonNull
    private final Date date;
    @NonNull
    private final SimpleDateFormat dateFormat;
    @NonNull
    private final LogStrategy logStrategy;
    @Nullable
    private final String tag;

    private TextFormatStrategy(@NonNull TextFormatStrategy.Builder builder) {
        checkNotNull(builder);

        date = builder.date;
        dateFormat = builder.dateFormat;
        logStrategy = builder.logStrategy;
        tag = builder.tag;
    }

    @NonNull
    public static TextFormatStrategy.Builder newBuilder() {
        return new TextFormatStrategy.Builder();
    }

    @Override
    public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {
        checkNotNull(message);

        String tag = formatTag(onceOnlyTag);

        date.setTime(System.currentTimeMillis());

        StringBuilder builder = new StringBuilder();

        // machine-readable date/time
//        builder.append(Long.toString(date.getTime()));

        // human-readable date/time
//        builder.append(SEPARATOR);
        builder.append(dateFormat.format(date));

        // level
        builder.append(SEPARATOR);
        builder.append(Utils.logLevel(priority));

        // thread
        builder.append("/[");
        builder.append(Thread.currentThread().getName());
        builder.append("]");

        // tag
        builder.append("[");
        builder.append(tag);
        builder.append("]:");

        // message
//        if (message.contains(NEW_LINE)) {
//            // a new line would break the CSV format, so we replace it here
//            message = message.replaceAll(NEW_LINE, NEW_LINE_REPLACEMENT);
//        }
        builder.append(SEPARATOR);
        builder.append(message);

        // new line
        builder.append(NEW_LINE);

        logStrategy.log(priority, tag, builder.toString());
    }

    @Nullable
    private String formatTag(@Nullable String tag) {
        if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
            return this.tag + "-" + tag;
        }
        return this.tag;
    }

    public static final class Builder {
        private static final int MAX_BYTES = 500 * 1024; // 500K averages to a 4000 lines per file

        Date date;
        SimpleDateFormat dateFormat;
        LogStrategy logStrategy;
        String tag = "PRETTY_LOGGER";
        String absPath;
        String fileName;
        boolean append = false;

        private Builder() {
        }

        @NonNull
        public TextFormatStrategy.Builder date(@Nullable Date val) {
            date = val;
            return this;
        }

        @NonNull
        public TextFormatStrategy.Builder dateFormat(@Nullable SimpleDateFormat val) {
            dateFormat = val;
            return this;
        }

        @NonNull
        public TextFormatStrategy.Builder logStrategy(@Nullable LogStrategy val) {
            logStrategy = val;
            return this;
        }

        @NonNull
        public TextFormatStrategy.Builder tag(@Nullable String tag) {
            this.tag = tag;
            return this;
        }

        @NonNull
        public TextFormatStrategy.Builder path(@Nullable String absPath) {
            this.absPath = absPath;
            return this;
        }

        @NonNull
        public TextFormatStrategy.Builder append(final boolean append) {
            this.append = append;
            return this;
        }

        @NonNull
        public TextFormatStrategy.Builder fileName(@Nullable String fileName) {
            this.fileName = fileName;
            return this;
        }

        @NonNull
        public TextFormatStrategy build() {
            if (date == null) {
                date = new Date();
            }
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.UK);
            }
            if (logStrategy == null) {
                String folder;
                if (absPath == null) {
                    String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    folder = diskPath + File.separatorChar + "logger";
                } else {
                    folder = absPath;
                }

                String fileName = "logs";
                if (this.fileName != null) {
                    fileName = this.fileName;
                }
                HandlerThread ht = new HandlerThread("AndroidFileLogger." + folder);
                ht.start();
                Handler handler = new DiskLogStrategy.WriteHandler(ht.getLooper(),
                        folder, MAX_BYTES, DiskLogStrategy.TEXT_FORMAT, fileName);
                logStrategy = new DiskLogStrategy(handler);
            }
            return new TextFormatStrategy(this);
        }
    }
}
