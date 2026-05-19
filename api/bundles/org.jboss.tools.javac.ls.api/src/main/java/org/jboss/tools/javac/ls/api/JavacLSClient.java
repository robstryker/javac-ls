/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.javac.ls.api;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.jboss.tools.javac.ls.api.dao.Status;

@JsonSegment("client")
public interface JavacLSClient {

	/**
	 * The `client/statusChanged` notification is sent by the server to all
	 * clients when the server status changes. This is a simple stub for testing.
	 */
	@JsonNotification
	void statusChanged(Status status);

}
