package org.disciplestoday.disciplestoday;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import org.disciplestoday.disciplestoday.data.ArticleResponse;
import org.disciplestoday.disciplestoday.data.Item;
import org.disciplestoday.disciplestoday.data.WordPressService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by neil on 5/28/16.
 */

public class Article {

    private static final String IMAGE_BASE_URL = WordPressService.BLOG_URL;
    public static final String DEFAULT_IMAGE_URL = "https://jeaniesjourneys.files.wordpress.com/2012/01/cropped-morning-cup-cover.jpg";
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

    public String getPubDate() {
        return pubDate;
    }

    private String pubDate;


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
    private Article(String moduleId, String articleId, String title, String imageLink, String author, String pubDate, String summary, String fullText, String link) {
        this.id = articleId;
        this.moduleId = moduleId;
        this.title = title;
        this.imageLink = imageLink;
        this.fullText = fullText.replace("images/", IMAGE_BASE_URL + "/images/");;
        this.author = author;
        //this.pubDate = pubDate;
        try {
            this.pubDate =  String.valueOf(((new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)).parse(pubDate)).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
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


    public static Article newArticle(int id,String pageNum, Item item) {
        String author = "gordon";
        String fullText = item.encoded;
        if (TextUtils.isEmpty(fullText)) {
            fullText = item.description;
        }

        String imageLink = "";

        imageLink = extractImageLink(fullText);
        String newFullText = removeImageHtml(fullText, imageLink);
        return new Article(pageNum, String.valueOf(id), item.title, imageLink,
                author, item.pubDate,
                item.description, newFullText, item.link);


        //TODO: Parse only once.

    }

    /***
     * Remove the html for the image so image is not duplicated
     * and so there isn't a huge image inside our webview.
     * @param fullText
     * @return
     */
    private static String removeImageHtml(String fullText, String imageLink) {
        Document doc = Jsoup.parse(fullText);
        String currentImageLink = "";
        for (Element e : doc.select("img")) {
            currentImageLink = e.attr("src");
            // Hide it as 0x0 if it's already the featured/thumbnail  image
            if (currentImageLink.equalsIgnoreCase(imageLink) ) {
                e.attr("width", "0");
                e.attr("height", "0");
            }
        }
        return doc.outerHtml();
    }

    private static String extractImageLink(String fullText) {
        Document doc = Jsoup.parse(fullText);
        String imageLink = "";
        Elements elements = doc.select("img");
        if (elements.size() > 0) {
            imageLink = elements.get(0).attr("src");
        }

        return imageLink;
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

    public static List<Article> getArticles(String page, ArticleResponse feed) {
        List<Article> articles = new ArrayList<>();
        if (feed.channel == null) {
            return null;
        }
        int i=0;
        for (Item item : feed.channel.items) {
            i++;
            articles.add(newArticle(i,page, item));
        }
        return  articles;
    }
}
