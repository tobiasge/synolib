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

package de.perdoctus.synolib.requests;

import java.net.URI;
import java.net.URL;

import org.apache.http.message.BasicNameValuePair;

import de.perdoctus.synolib.RequestExecutor;
import de.perdoctus.synolib.responses.LoginResponse;

/**
 * @author Christoph Giesche
 */
public class AddUrlRequest extends DownloadRedirectorRequest {

    public AddUrlRequest(final URI uri, final String sessionId) {
        super("POST", "/webapi/DownloadStation/task.cgi");

        setParams(uri.toString(), sessionId);
    }

    public AddUrlRequest(final URI uri, final LoginResponse loginResponse) {
        super("POST", "/webapi/DownloadStation/task.cgi");

        setParams(uri.toString(), loginResponse.getSid());
    }

    public AddUrlRequest(final URL url, final String sessionId) {
        super("POST", "/webapi/DownloadStation/task.cgi");

        setParams(url.toString(), sessionId);
    }

    public AddUrlRequest(final URL url, final LoginResponse loginResponse) {
        super("POST", "/webapi/DownloadStation/task.cgi");

        setParams(url.toString(), loginResponse.getSid());
    }

    private void setParams(final String url, final String sessionId) {
        
        requestParams.add(new BasicNameValuePair("_sid", sessionId));
        requestParams.add(new BasicNameValuePair("uri", url));
        requestParams.add(new BasicNameValuePair("method", "create"));
        requestParams.add(new BasicNameValuePair("version", RequestExecutor.TASK_API_VERSION));
        requestParams.add(new BasicNameValuePair("api", RequestExecutor.TASK_API_NAME));
    }
}
