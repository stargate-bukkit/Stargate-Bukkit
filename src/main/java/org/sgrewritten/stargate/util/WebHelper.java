package org.sgrewritten.stargate.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class WebHelper {
    public static void downloadFile(String link, File file) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            URL url = new URL(link);
            is = url.openStream();
            os = new FileOutputStream(file);
            is.transferTo(os);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }
}
