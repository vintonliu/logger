/*
 * Copyright (c) 2019 51talk. All rights reserved.
 * FormatStrategy.java
 * Creator: Vinton.Liu
 * Create Date: 2019-01-25 18:12:24
 */

package com.orhanobut.logger;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Used to determine how messages should be printed or saved.
 *
 * @see PrettyFormatStrategy
 * @see CsvFormatStrategy
 */
public interface FormatStrategy {

  void log(int priority, @Nullable String tag, @NonNull String message);
}
