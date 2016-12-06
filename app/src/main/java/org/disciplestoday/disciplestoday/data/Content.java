package org.disciplestoday.disciplestoday.data;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by neil on 12/2/16.
 */
@Root(name = "content", strict = false)
public class Content {

    @Attribute
    public String url;

    @Attribute
    public String medium;


}
