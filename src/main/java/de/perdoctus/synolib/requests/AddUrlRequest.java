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

import de.perdoctus.synolib.RequestExecutor;
import de.perdoctus.synolib.responses.LoginResponse;

import java.net.URI;
import java.net.URL;

/**
 * @author Christoph Giesche
 */
public class AddUrlRequest extends DownloadRedirectorRequest {

    public AddUrlRequest(final URI uri, final String sessionId) {
        super("POST");

        setParams(uri.toString(), sessionId);
    }

    public AddUrlRequest(final URI uri, final LoginResponse loginResponse) {
        super("POST");

        setParams(uri.toString(), loginResponse.getSid());
    }

    public AddUrlRequest(final URL url, final String sessionId) {
        super("POST");

        setParams(url.toString(), sessionId);
    }

    public AddUrlRequest(final URL url, final LoginResponse loginResponse) {
        super("POST");

        setParams(url.toString(), loginResponse.getSid());
    }

    private void setParams(final String url, final String sessionId) {
        
        requestParams.add(new KeyValue("_sid", sessionId));
        requestParams.add(new KeyValue("uri", url));
        requestParams.add(new KeyValue("method", "create"));
        requestParams.add(new KeyValue("version", RequestExecutor.TASK_API_VERSION));
        requestParams.add(new KeyValue("api", RequestExecutor.TASK_API_NAME));
        
        this.setRequestURI("/webapi/DownloadStation/task.cgi");
    }
}
