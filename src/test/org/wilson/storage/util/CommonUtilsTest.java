package org.wilson.storage.util;

import java.net.URLEncoder;

import org.junit.Test;

public class CommonUtilsTest {

    @Test
    public void testUrlEncoding() throws Exception {
        String url = URLEncoder.encode("http://www.ywcaws.org/wp-content/uploads/water1.jpg", "UTF-8");
        System.out.println(url);
    }

}
