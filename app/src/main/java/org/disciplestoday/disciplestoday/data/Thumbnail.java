package org.disciplestoday.disciplestoday.data;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by neil on 12/2/16.
 */
@Root(name = "thumbnail", strict = false)
public class Thumbnail {

    @Attribute
    public String url;


}
