package org.disciplestoday.disciplestoday;

import android.text.Html;
import android.util.Log;

import org.disciplestoday.disciplestoday.data.DTService;
import org.disciplestoday.disciplestoday.data.Feed;
import org.disciplestoday.disciplestoday.data.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neil on 5/28/16.
 */

public class Article {
    private static final String IMAGE_BASE_URL = DTService.DISCIPLES_TODAY_BASE_URL;
    public static final String DEFAULT_IMAGE_URL = "https://pbs.twimg.com/profile_images/186752127/DToday_logo_Gradient_Orange_400x400.jpg";
    private String title;
    private String imageLink;
    private String fullText;
    private String author;
    private String summary; //or description, byline, etc.

    private Article(String title, String imageLink, String author, String summary, String fullText) {
        this.title = title;
        this.imageLink = imageLink;
        this.fullText = fullText.replace("images/", IMAGE_BASE_URL + "/images/");;
        this.author = author;
        this.summary = summary;
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
        //String imagePath = getImageLink().replace(IMAGE_BASE_URL, "");
      //  if (text.contains(imagePath)) {
       //     text = getTextWithHiddenImage(imagePath);
      //  }

        return Html.escapeHtml(text);
    }

    /**
     *
     * @param imagePath an image's path such as 'images/myimage.jpg'
     * @return text with its image hidden with css display:none
     */
    private String getTextWithHiddenImage(String imagePath) {
        String s1 = "src=\"" + imagePath + "\"";
        String s2 = " style=\"display:none\" ";
        String fixedFullText = fullText.replace(s1, s2);
        return fixedFullText;
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
            return new Article(item.getTitle(), item.getImageUrl(), author, item.getIntroText(), item.getFulltext());
        }

        String title = item.getExtraFields().get(0).getValue();
        String description = item.getExtraFields().get(1).getValue();
        String image = item.getImageUrl();

        return new Article(title, image, author, description, item.getFulltext());
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
}
