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
 * Discovers classpath for Maven projects.
 */
public class MavenProjectClasspathDiscoverer implements IProjectClasspathDiscoverer {

	@Override
	public String getId() {
		return "maven";
	}

	@Override
	public boolean accepts(WorkspaceProject proj) {
		if (proj == null || proj.getPath() == null) {
			return false;
		}
		File pomFile = new File(proj.getPath(), "pom.xml");
		return pomFile.exists();
	}

	@Override
	public List<File> getSourceFilesForCacheValidation(WorkspaceProject proj) {
		if (proj == null || proj.getPath() == null) {
			return null;
		}
		File pomFile = new File(proj.getPath(), "pom.xml");
		// Check pom.xml timestamp for cache validation
		return Arrays.asList(pomFile);
	}

	@Override
	public ArrayList<IJavacClasspathEntry> discoverClasspath(WorkspaceProject proj) {
		ArrayList<IJavacClasspathEntry> entries = new ArrayList<>();
		// TODO: Implement Maven classpath discovery
		return entries;
	}
}
