package org.disciplestoday.disciplestoday;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import org.disciplestoday.disciplestoday.data.ArticleResponse;
import org.disciplestoday.disciplestoday.data.Feed;
import org.disciplestoday.disciplestoday.data.Item;
import org.disciplestoday.disciplestoday.data.WordPressService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neil on 5/28/16.
 */

public class Article {

    private static final String IMAGE_BASE_URL = WordPressService.JEANIE_SHAW_BLOG_URL;
    public static final String DEFAULT_IMAGE_URL = "https://pbs.twimg.com/profile_images/186752127/DToday_logo_Gradient_Orange_400x400.jpg";
    //TODO: FIX THIS PART
    public static final String TRACK_TYPE_ARTICLE="article";


    private Long _id; // for db/cupboard.
    private String id;
    private String categoryId; //Use this info to display hope info that has no rss feed...

    private String title;
    private String link;
    private String imageLink;
    private String moduleId; //353 for highlighted, etc.

    private String fullText;
    private String author;
    private String summary; //or description, byline, etc.


    public String getCategoryId() {
        return categoryId;
    }

    public Article setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getLink() {
        return link;
    }

    // zero arg constructor for sqllite/cupboard?
    public Article() {

    }

    //TODO: Add categoryid so we can add a feed for hope.
    private Article(String moduleId, String articleId, String title, String imageLink, String author, String summary, String fullText, String link) {
        this.id = articleId;
        this.moduleId = moduleId;
        this.title = title;
        this.imageLink = imageLink;
        this.fullText = fullText.replace("images/", IMAGE_BASE_URL + "/images/");;
        this.author = author;
        this.summary = summary;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return Html.fromHtml(title).toString();
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
    public static Article newArticle(String pageNum, Item item) {
        String author = "jeanie";
        String fullText = item.encoded;
        if (TextUtils.isEmpty(fullText)) {
            fullText = item.description;
        }
        String imageLink = item.contentList.get(1).url;

        return new Article(pageNum, "no_id", item.title, imageLink,
                    author,
                    item.description, fullText, item.link);



    }

    public static Article newArticle(int id,String pageNum, Item item) {
        String author = "jeanie";
        String fullText = item.encoded;
        if (TextUtils.isEmpty(fullText)) {
            fullText = item.description;
        }
        String imageLink = item.contentList.get(1).url;
        Log.e("NJW", "in newArticle:" +  imageLink);

        return new Article(pageNum, String.valueOf(id), item.title, imageLink,
                author,
                item.description, fullText, item.link);



    }

    public String getImageLink() {
        return imageLink;
    }

    public String getDetailImageLink() {
        String s1 = fullText.replace("/images", IMAGE_BASE_URL + "images");
        if (s1.contains(imageLink)) {
            return "duplicate_image:'" + imageLink + "'";
        } else {
            return imageLink;
        }
    }

    public static List<Article> getArticles(String moduleId, ArticleResponse feed) {
        List<Article> articles = new ArrayList<>();
        if (feed.channel == null) {
            return null;
        }
        int i=0;
        for (Item item : feed.channel.items) {
            i++;
            articles.add(newArticle(i,moduleId, item));
        }
        return  articles;
    }
}
