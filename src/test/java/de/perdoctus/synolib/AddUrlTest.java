package de.perdoctus.synolib;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import de.perdoctus.synolib.exceptions.SynoException;

public class AddUrlTest {

    @Test
    public void addUrl() throws URISyntaxException, SynoException {

        DownloadRedirectorClient drc = new DownloadRedirectorClient("", "", "https://ds.kappa-velorum.net:5001");
        drc.addDownloadUrl(new URI("http://www.test.com/test.html"));

    }
}
