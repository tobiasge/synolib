package de.perdoctus.synolib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import de.perdoctus.synolib.exceptions.SynoException;

public class AddUrlTest {

    private static String user;

    private static String pass;

    private static String URL;

    @BeforeClass
    public static void initTestConfig() throws IOException {
        final Properties properties = new Properties();
        try (final InputStream stream = AddUrlTest.class.getResourceAsStream("/local.properties")) {
            properties.load(stream);
            user = properties.getProperty("username", "");
            pass = properties.getProperty("password", "");
            URL = properties.getProperty("synoUri", "");
        }
    }

    @Test
    public void addUrl() throws URISyntaxException, SynoException {
        DownloadRedirectorClient drc = new DownloadRedirectorClient(user, pass, URL);
        drc.addDownloadUrl(new URI("http://www.test.com/test.html"));
    }
}
