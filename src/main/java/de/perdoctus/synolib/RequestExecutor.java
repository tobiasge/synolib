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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;

import de.perdoctus.synolib.exceptions.CommunicationException;
import de.perdoctus.synolib.exceptions.SynoException;
import de.perdoctus.synolib.requests.AddUrlRequest;
import de.perdoctus.synolib.requests.DownloadRedirectorRequest;
import de.perdoctus.synolib.requests.KeyValue;
import de.perdoctus.synolib.requests.LoginRequest;
import de.perdoctus.synolib.responses.DownloadRedirectorResponse;

/**
 * @author Christoph Giesche
 *
 * @author Tobias Genannt
 */
public class RequestExecutor {

    private HttpClient httpClient;

    private final URI loginURI;

    private final URI taskURI;

    public final static String LOGIN_API_PATH = "/webapi/auth.cgi";

    public final static String LOGIN_API_NAME = "SYNO.API.Auth";

    public final static String LOGIN_API_VERSION = "2";

    public final static String TASK_API_PATH = "/webapi/DownloadStation/task.cgi";

    public final static String TASK_API_NAME = "SYNO.DownloadStation.Task";

    public final static String TASK_API_VERSION = "1";

    private static final Logger LOG = Logger.getLogger(RequestExecutor.class);

    public RequestExecutor(final URI targetURI) throws URISyntaxException {
        this.loginURI = new URI(targetURI.toString() + LOGIN_API_PATH);
        this.taskURI = new URI(targetURI.toString() + TASK_API_PATH);
    }

    public <T extends DownloadRedirectorResponse> T executeRequest(final DownloadRedirectorRequest drRequest,
        final Class<T> clazz) throws SynoException {

        LOG.debug("Executing DR-Request: " + drRequest.getClass().getSimpleName());

        if (this.httpClient == null) {

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
            }).build();
        }

        final HttpUriRequest request;
        if (drRequest.getHttpMethod().equalsIgnoreCase("POST")) {
            final HttpPost postRequest;
            if (drRequest.getClass().equals(LoginRequest.class)) {
                postRequest = new HttpPost(loginURI);
            } else if (drRequest.getClass().equals(AddUrlRequest.class)) {
                postRequest = new HttpPost(taskURI);
            } else {
                throw new RuntimeException("Method " + drRequest.getClass() + " not supported yet!");
            }

            final List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (final KeyValue param : drRequest.getRequestParams()) {
                params.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }

            final HttpEntity entity;
            try {
                entity = new UrlEncodedFormEntity(params, "UTF-8");
            } catch (final UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }

            postRequest.setEntity(entity);
            request = postRequest;
        } else if (drRequest.getHttpMethod().equalsIgnoreCase("GET")) {
            final HttpGet getRequest;
            if (drRequest.getClass().equals(LoginRequest.class)) {
                String params = "?";
                for (KeyValue kv : drRequest.getRequestParams()) {
                    params = params + kv.getKey() + "=" + kv.getValue() + "&";
                }
                getRequest = new HttpGet(loginURI.toString() + params);
            } else if (drRequest.getClass().equals(AddUrlRequest.class)) {
                getRequest = new HttpGet(taskURI);
            } else {
                throw new RuntimeException("Method " + drRequest.getClass() + " not supported yet!");
            }
            request = getRequest;
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
        final T drResponse;
        try {
            content = EntityUtils.toString(entity);
            drResponse = mapper.readValue(content, clazz);
        } catch (final UnrecognizedPropertyException ex) {
            throw new CommunicationException("Failed to parse JSON response!", ex);
        } catch (final IOException ex) {
            throw new CommunicationException("Could not read response body!", ex);
        } catch (final IllegalStateException ex) {
            throw new CommunicationException("Could not read response body stream!", ex);
        }

        return drResponse;
    }
}
