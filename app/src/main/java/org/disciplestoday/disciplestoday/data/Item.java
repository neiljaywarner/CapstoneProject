package org.disciplestoday.disciplestoday.data;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by neil on 12/1/16.
 */
@Root(name = "item", strict = false) //strict=false keeps us from having to put everything
public class Item {
    //https://jeaniesjourneys.com/feed/?paged=3
    @Element(name = "title")
    public String title;

    @Element(name = "link")
    public String link;

    @ElementList(inline = true)
    public List<Content> contentList;


    @Element(name = "creator", data=true)
    public String creator;

    @Element(name = "description", data=true)
    public String description;

    @Element(name = "encoded", data=true) //content:encoded with cdata
    public String encoded;






    @Element(name = "pubDate")
    public String pubDate;



}