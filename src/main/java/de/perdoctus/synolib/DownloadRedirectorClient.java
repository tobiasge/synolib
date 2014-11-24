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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

import de.perdoctus.synolib.exceptions.LoginException;
import de.perdoctus.synolib.exceptions.SynoException;
import de.perdoctus.synolib.requests.AddUrlRequest;
import de.perdoctus.synolib.requests.LoginRequest;
import de.perdoctus.synolib.responses.AddUrlResponse;
import de.perdoctus.synolib.responses.LoginResponse;

/**
 * @author Christoph Giesche
 */
public class DownloadRedirectorClient {

    private String username = "";
    private String password = "";

    private RequestExecutor executor;
    private String sessionId;

    public DownloadRedirectorClient() {

    }

    public DownloadRedirectorClient(final String username, final String password, final String synoUri)
        throws URISyntaxException {

        this.password = password;
        this.username = username;

        this.executor = new RequestExecutor(synoUri);
    }

    public AddUrlResponse addDownloadUrl(final URI uri) throws SynoException {
        if (sessionId == null) {
            login();
        }

        final AddUrlRequest request = new AddUrlRequest(uri, sessionId);

        return executor.executeRequest(request, AddUrlResponse.class);
    }

    public void forceReloginOnNextRequest() {
        this.sessionId = null;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setSynoUri(final String uri) throws URISyntaxException {
        this.executor = new RequestExecutor(uri);
    }

    private void login() throws SynoException {
        final LoginRequest request = new LoginRequest(username, password);
        final LoginResponse response = executor.executeRequest(request, LoginResponse.class);

        if (response.isSuccess()) {
            this.sessionId = response.getSid();
        } else {
            throw new LoginException("Login failed; Errorcode: " + response.getErrcode());
        }

    }

    @Override
    public String toString() {
        return "DownloadRedirectorClient [username=" + username + ", password="
            + StringUtils.repeat('*', password.length()) + ", sessionId=" + sessionId + "]";
    }

}
