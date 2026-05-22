/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.javac.ls.server.model.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.tools.javac.ls.server.model.WorkspaceProject;

/**
 * Discovers classpath for Eclipse projects.
 * This is a fallback discoverer with the lowest priority.
 */
public class EclipseProjectClasspathDiscoverer implements IProjectClasspathDiscoverer {

	@Override
	public String getId() {
		return "eclipse";
	}

	@Override
	public boolean accepts(WorkspaceProject proj) {
		if (proj == null || proj.getPath() == null) {
			return false;
		}
		File classpathFile = new File(proj.getPath(), ".classpath");
		return classpathFile.exists();
	}

	@Override
	public List<File> getSourceFilesForCacheValidation(WorkspaceProject proj) {
		if (proj == null || proj.getPath() == null) {
			return null;
		}
		File classpathFile = new File(proj.getPath(), ".classpath");
		// Check .classpath timestamp for cache validation
		return Arrays.asList(classpathFile);
	}

	@Override
	public ArrayList<IJavacClasspathEntry> discoverClasspath(WorkspaceProject proj) {
		ArrayList<IJavacClasspathEntry> entries = new ArrayList<>();
		// TODO: Implement Eclipse classpath discovery
		return entries;
	}
}
