/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.javac.ls.client.bindings;

import org.jboss.tools.javac.ls.api.JavacLSClient;
import org.jboss.tools.javac.ls.api.JavacLSServer;
import org.jboss.tools.javac.ls.api.dao.Status;

public class ServerManagementClientImpl implements JavacLSClient {

	private JavacLSServer server;

	public void initialize(JavacLSServer server) {
		this.server = server;
	}

	public JavacLSServer getProxy() {
		return server;
	}

	@Override
	public void statusChanged(Status status) {
		System.out.println("Status changed: " + status.toString());
	}

}
