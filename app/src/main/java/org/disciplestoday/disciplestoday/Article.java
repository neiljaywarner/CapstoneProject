package org.disciplestoday.disciplestoday;

import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.util.Log;

import org.disciplestoday.disciplestoday.data.DTService;
import org.disciplestoday.disciplestoday.data.Feed;
import org.disciplestoday.disciplestoday.data.Item;
import org.disciplestoday.disciplestoday.utils.ArticleUtils;

import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by neil on 5/28/16.
 */

public class Article {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_LINK = "link";

    public static final String FIELD_NUMBEROFCOMMENTS = "numberOfComments";
        //save on teh reflection

    private static final String IMAGE_BASE_URL = DTService.DISCIPLES_TODAY_BASE_URL;
    public static final String DEFAULT_IMAGE_URL = "https://pbs.twimg.com/profile_images/186752127/DToday_logo_Gradient_Orange_400x400.jpg";
    public static final String TRACK_TYPE_ARTICLE="article";


    private Long _id; // for db/cupboard.
    private String id;
    private String title;
    private String imageLink;
    private String moduleId; //353 for highlighted, etc.


    public String getCategoryId() {
        return categoryId;
    }

    public Article setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    private String categoryId; //Use this info to display hope info that has no rss feed...




    public String getModuleId() {
        return moduleId;
    }

    public Article setModuleId(String moduleId) {
        this.moduleId = moduleId;
        return this;
    }


    public String getLink() {
        return link;
    }

    private String link;

    private String fullText;
    private String author;
    private String summary; //or description, byline, etc.

    // zero arg constructor for sqllite/cupboard?
    public Article() {

    }

    private Article(String id, String title, String imageLink, String author, String summary, String fullText, String link) {
        this.id = id;
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
        return title;
    }





    /**
     *
     * @return html-escaped text with duplicate images hidden. (for use in webview)
     */
    String getFullText() {
        String text = fullText;
        return Html.escapeHtml(text);
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
        if ((item.getExtraFields() == null) || item.getExtraFields().size() == 0) {
            return new Article(item.getId(), item.getTitle(), item.getImageUrl(), author,
                    item.getIntroText(), item.getFulltext(), item.getLink());
        }

        String title = item.getExtraFields().get(0).getValue();
        String description = item.getExtraFields().get(1).getValue();
        String image = item.getImageUrl();

        return new Article(item.getId(), title, image, author, description, item.getFulltext(), item.getLink());
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

    public boolean isInList(List<Article> articles) {
        return ArticleUtils.hasArticle(articles, this);
    }



}
