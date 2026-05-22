/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.javac.ls.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jboss.tools.javac.ls.api.JavacLSClient;
import org.jboss.tools.javac.ls.api.JavacLSServer;
import org.jboss.tools.javac.ls.api.dao.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavacLSServerImpl implements JavacLSServer {
	private static final Logger LOG = LoggerFactory.getLogger(JavacLSServerImpl.class);

	private final JavacLsServerLauncher launcher;
	private final List<JavacLSClient> clients;

	public JavacLSServerImpl(JavacLsServerLauncher launcher) {
		this.launcher = launcher;
		this.clients = new ArrayList<>();
	}

	public void addClient(JavacLSClient client) {
		synchronized(clients) {
			clients.add(client);
		}
	}

	public void removeClient(JavacLSClient client) {
		synchronized(clients) {
			clients.remove(client);
		}
	}

	public List<JavacLSClient> getClients() {
		synchronized(clients) {
			return new ArrayList<>(clients);
		}
	}

	@Override
	public CompletableFuture<Status> ping() {
		LOG.info("Received ping request");
		Status status = new Status(Status.OK, "Server is alive", null);
		return CompletableFuture.completedFuture(status);
	}

	@Override
	public void shutdown() {
		LOG.info("Received shutdown request");
		launcher.shutdown();
	}

	public void notifyStatusChanged(Status status) {
		for (JavacLSClient client : getClients()) {
			try {
				client.statusChanged(status);
			} catch (Exception e) {
				LOG.error("Error notifying client of status change", e);
			}
		}
	}
}
