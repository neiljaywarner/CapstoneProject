package com.cba.data;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by neil on 12/1/16.
 */

@Root(name = "rss", strict = false)
public class ArticleResponse {

    @Element(name = "channel")
    public Channel channel;


    @Root(name = "channel", strict = false)
    public static class Channel {
        @Element
        public String title;

        @ElementList(inline = true)
        public List<Item> items;
    }

}

