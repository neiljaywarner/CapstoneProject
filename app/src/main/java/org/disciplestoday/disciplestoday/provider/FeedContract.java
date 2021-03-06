/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.disciplestoday.disciplestoday.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for
 */
public class FeedContract {
    private FeedContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "org.disciplestoday.disciplestoday";

    /**
     * Base URI. (content://org.disciplestoday.disciplestoday)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "article"-type resources..
     */
    private static final String PATH_articles = "articles";

    /**
     * Columns supported by "articles" records.
     */
    public static class Entry implements BaseColumns {
        /**
         * MIME type for lists of articles.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.disciplestoday.articles";
        /**
         * MIME type for individual articles.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.disciplestoday.article";

        /**
         * Fully qualified URI for "article" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_articles).build();


        public static final String TABLE_NAME = "Article";
        /**
         * Article ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ARTICLE_ID = "id";

        public static final String COLUMN_NAME_MODULE_ID = "moduleId";

        public static final String COLUMN_NAME_TITLE = "title";

        public static final String COLUMN_NAME_IMAGE_LINK = "imageLink";

        public static final String COLUMN_NAME_FULL_TEXT = "fullText";

        public static final String COLUMN_NAME_AUTHOR = "author";

        public static final String COLUMN_NAME_SUMMARY = "summary";

        public static final String COLUMN_NAME_LINK = "link";

    }
}