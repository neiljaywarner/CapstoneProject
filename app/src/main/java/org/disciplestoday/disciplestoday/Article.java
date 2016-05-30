package org.disciplestoday.disciplestoday;

import android.util.Log;

import org.disciplestoday.disciplestoday.data.Feed;
import org.disciplestoday.disciplestoday.data.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neil on 5/28/16.
 */

public class Article {
    public static final String IMAGE_BASE_URL = "http://disciplestoday.org/";
    private String title;
    private String imageLink;
    private String fullText;
    private String author;
    private String summary; //or description, byline, etc.

    private Article(String title, String imageLink, String author, String summary, String fullText) {
        this.title = title;
        this.imageLink = imageLink;
        this.fullText = fullText;
        this.author = author;
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public String getFullText() {
        return fullText;
    }

    public String getAuthor() {
        return author;
    }

    public String getSummary() {
        return summary;
    }

    /**
     * Given: Extra fields are title/description/image as of May 28,2016
     * They have name='title' etc,but is that better?
     * Waiting for API.
     * @param item
     * @return
     */
    public static Article newArticle(Item item) {
        String author = item.getCreated_by_alias();
        if (item.getExtraFields().size() == 0) {
            return new Article(item.getTitle(), item.getImageUrl(), author, item.getIntroText(), item.getFulltext());
        }

        String title = item.getExtraFields().get(0).getValue();
        String description = item.getExtraFields().get(1).getValue();
        String image = item.getImageUrl();
        //TODO: Skip 'intenrational news bulletin' if needed?
        Log.e("NJW", "title=" + title);

        if (title.contains("International News Bulletin"))
        {
            image = "http://www.disciplestoday.org/media/k2/items/cache/48feebd427d6991de4b81c1aad4efc08_L.jpg";
            //TODO: Can try M, XL, etc after this.

        }
        return new Article(title, image, author, description, item.getFulltext());
    }

    public String getImageLink() {
        return imageLink;
    }

    public static List<Article> getArticles(Feed feed) {
        List<Article> articles = new ArrayList<>();
        if (feed.getItems() == null) {
            return null;
        }

        for (Item item : feed.getItems()) {
            articles.add(newArticle(item));
        }
        return  articles;
    }
}
