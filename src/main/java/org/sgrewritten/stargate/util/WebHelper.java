package org.sgrewritten.stargate.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class WebHelper {
    public static void downloadFile(String link, File file) throws IOException {
        URL url = new URL(link);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(file);
        is.transferTo(os);
        is.close();
        os.close();
    }
}
