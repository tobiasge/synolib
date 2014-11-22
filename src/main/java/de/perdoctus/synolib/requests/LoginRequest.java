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

/**
 * @author Christoph Giesche
 */
public class LoginRequest extends DownloadRedirectorRequest {

    public LoginRequest(final String username, final String password) {
        super("GET");

        requestParams.add(new KeyValue("account", username));
        requestParams.add(new KeyValue("passwd", password));
        requestParams.add(new KeyValue("session", "DownloadStation"));
        requestParams.add(new KeyValue("format", "sid"));
        requestParams.add(new KeyValue("method", "login"));
        requestParams.add(new KeyValue("version", RequestExecutor.LOGIN_API_VERSION));
        requestParams.add(new KeyValue("api", RequestExecutor.LOGIN_API_NAME));
    }

}
