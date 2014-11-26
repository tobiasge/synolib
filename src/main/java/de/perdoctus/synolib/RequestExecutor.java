/*
 * Copyright 2014 Christoph Giesche
 *
 * This file is part of synolib.
 *
 * synolib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * synolib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with synolib.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.perdoctus.synolib;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import de.perdoctus.synolib.exceptions.CommunicationException;
import de.perdoctus.synolib.exceptions.SynoException;
import de.perdoctus.synolib.requests.DownloadRedirectorRequest;
import de.perdoctus.synolib.responses.DownloadRedirectorResponse;

/**
 * @author Christoph Giesche
 *
 * @author Tobias Genannt
 */
public class RequestExecutor {

    private HttpClient httpClient;

    private String targetURI;

    public final static String LOGIN_API_NAME = "SYNO.API.Auth";

    public final static String LOGIN_API_VERSION = "2";

    public final static String TASK_API_NAME = "SYNO.DownloadStation.Task";

    public final static String TASK_API_VERSION = "1";

    private static final Logger LOG = LogManager.getLogger(RequestExecutor.class);

    public RequestExecutor(String targetURI) throws URISyntaxException {
        this.targetURI = targetURI;
    }

    public <T extends DownloadRedirectorResponse> T executeRequest(final DownloadRedirectorRequest drRequest,
        final Class<T> clazz) throws SynoException {

        LOG.debug("Executing DR-Request: " + drRequest.getClass().getSimpleName());

        if (this.httpClient == null) {
            this.buildHTTPClient();
        }

        final HttpUriRequest request;
        if (drRequest.getHttpMethod().equalsIgnoreCase("POST")) {

            final HttpPost postRequest = new HttpPost(this.targetURI + drRequest.getRequestURI());
            final HttpEntity entity = new UrlEncodedFormEntity(drRequest.getRequestParams(), Charset.forName("UTF-8"));
            postRequest.setEntity(entity);
            request = postRequest;
        } else if (drRequest.getHttpMethod().equalsIgnoreCase("GET")) {

            String params = "?";
            params += URLEncodedUtils.format(drRequest.getRequestParams(), Charset.forName("UTF-8"));
            request = new HttpGet(this.targetURI + drRequest.getRequestURI() + params);
            LOG.trace("URL is now:\n" + request.getURI());
        } else {
            throw new RuntimeException("Method " + drRequest.getHttpMethod() + " not supported yet!");
        }

        final HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (final IOException ex) {
            throw new CommunicationException("Failed to execute http call.", ex);
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new CommunicationException(response.getStatusLine().getReasonPhrase());
        }

        final ObjectMapper mapper = new ObjectMapper();
        final HttpEntity entity = response.getEntity();

        if (entity == null) {
            throw new CommunicationException("Got no response body!");
        }

        String content = "";
        try {
            content = EntityUtils.toString(entity);
        } catch (ParseException | IOException ex) {
            throw new CommunicationException("Could not read response body from HTTP entity!", ex);
        }

        final T drResponse;

        try {
            drResponse = mapper.readValue(content, clazz);
        } catch (IOException ex) {
            throw new CommunicationException("Failed to parse JSON response!" + content, ex);
        }

        return drResponse;
    }

    private void buildHTTPClient() throws CommunicationException {
        try {
            this.httpClient = HttpClientBuilder.create().setHostnameVerifier(new X509HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }
            }).setSslcontext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build()).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
            throw new CommunicationException("Could not build HTTP Client!", ex);
        }
    }
}
