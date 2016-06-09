package org.disciplestoday.disciplestoday.utils;

import org.disciplestoday.disciplestoday.Article;

import java.util.List;

/**
 * Created by neil on 6/8/16.
 */

public class ArticleUtils {
    public static boolean hasArticle(List<Article> articles, Article articleToSeachFor) {
        for (Article article : articles) {
            if (article.getId().contentEquals(articleToSeachFor.getId())) {
                return true;
            }
        }
        //TODO: Consider if it should be updated, and if this will need to do it or if this will be deprecated.
        return false;
    }
}
