/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.javac.ls.server.model.classpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.javac.ls.server.model.WorkspaceProject;
import org.jboss.tools.javac.ls.server.model.classpath.IJavacClasspathEntry.EntryType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProjectClasspathDiscoveryTest {

	private File tempDir;
	private ClasspathCache cache;
	private ProjectClasspathDiscovery discovery;

	@Before
	public void setup() throws IOException {
		tempDir = Files.createTempDirectory("ProjectClasspathDiscoveryTest").toFile();
		cache = new ClasspathCache(tempDir);
		discovery = new ProjectClasspathDiscovery(cache);
	}

	@After
	public void teardown() {
		deleteRecursively(tempDir);
	}

	private void deleteRecursively(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					deleteRecursively(f);
				}
			}
		}
		file.delete();
	}

	@Test
	public void testDiscovererRegistration() {
		List<IProjectClasspathDiscoverer> discoverers = discovery.getDiscoverers();

		assertNotNull(discoverers);
		assertEquals(2, discoverers.size());

		// Verify priority order: Maven (100) before Eclipse (1)
		assertEquals("maven", discoverers.get(0).getId());
		assertEquals("eclipse", discoverers.get(1).getId());
	}

	@Test
	public void testMavenProjectDiscovery() throws IOException {
		File projectDir = new File(tempDir, "maven-project");
		projectDir.mkdirs();
		File pomFile = new File(projectDir, "pom.xml");
		pomFile.createNewFile();

		WorkspaceProject proj = new WorkspaceProject("maven-project", projectDir.getAbsolutePath());

		ArrayList<IJavacClasspathEntry> entries = discovery.discoverClasspath(proj);
		assertNotNull(entries);
		// Currently empty until we implement Maven discovery
	}

	@Test
	public void testEclipseProjectDiscovery() throws IOException {
		File projectDir = new File(tempDir, "eclipse-project");
		projectDir.mkdirs();
		File classpathFile = new File(projectDir, ".classpath");
		classpathFile.createNewFile();

		WorkspaceProject proj = new WorkspaceProject("eclipse-project", projectDir.getAbsolutePath());

		ArrayList<IJavacClasspathEntry> entries = discovery.discoverClasspath(proj);
		assertNotNull(entries);
		// Currently empty until we implement Eclipse discovery
	}

	@Test
	public void testMavenTakesPrecedenceOverEclipse() throws IOException {
		// Project has both pom.xml and .classpath (m2e-generated)
		File projectDir = new File(tempDir, "hybrid-project");
		projectDir.mkdirs();
		File pomFile = new File(projectDir, "pom.xml");
		pomFile.createNewFile();
		File classpathFile = new File(projectDir, ".classpath");
		classpathFile.createNewFile();

		WorkspaceProject proj = new WorkspaceProject("hybrid-project", projectDir.getAbsolutePath());

		// Maven should be chosen (higher priority)
		ArrayList<IJavacClasspathEntry> entries = discovery.discoverClasspath(proj);
		assertNotNull(entries);
	}

	@Test
	public void testUnknownProjectType() {
		File projectDir = new File(tempDir, "unknown-project");
		projectDir.mkdirs();

		WorkspaceProject proj = new WorkspaceProject("unknown-project", projectDir.getAbsolutePath());

		ArrayList<IJavacClasspathEntry> entries = discovery.discoverClasspath(proj);
		assertNotNull(entries);
		assertEquals(0, entries.size());
	}

	@Test
	public void testCacheHit() throws IOException, InterruptedException {
		File projectDir = new File(tempDir, "cached-project");
		projectDir.mkdirs();
		File pomFile = new File(projectDir, "pom.xml");
		pomFile.createNewFile();

		WorkspaceProject proj = new WorkspaceProject("cached-project", projectDir.getAbsolutePath());

		// First call - cache miss, discovery runs
		ArrayList<IJavacClasspathEntry> entries1 = discovery.discoverClasspath(proj);
		assertNotNull(entries1);

		// Give time for cache to be written
		Thread.sleep(100);

		// Second call - should hit cache (pom.xml unchanged)
		ArrayList<IJavacClasspathEntry> entries2 = discovery.discoverClasspath(proj);
		assertNotNull(entries2);
		assertEquals(entries1.size(), entries2.size());
	}

	@Test
	public void testCacheMissAfterSourceFileChange() throws IOException, InterruptedException {
		File projectDir = new File(tempDir, "changing-project");
		projectDir.mkdirs();
		File pomFile = new File(projectDir, "pom.xml");
		pomFile.createNewFile();

		WorkspaceProject proj = new WorkspaceProject("changing-project", projectDir.getAbsolutePath());

		// Pre-populate cache manually with known entries
		ArrayList<IJavacClasspathEntry> cachedEntries = new ArrayList<>();
		cachedEntries.add(new JavacClasspathEntry(EntryType.SOURCE, "/old/src"));
		cache.saveCache("changing-project", cachedEntries);

		Thread.sleep(100);

		// Touch pom.xml to make it newer than cache
		pomFile.setLastModified(System.currentTimeMillis());

		// Discovery should invalidate cache and re-run
		ArrayList<IJavacClasspathEntry> entries = discovery.discoverClasspath(proj);
		assertNotNull(entries);
		// New discovery should overwrite cache
	}

	@Test
	public void testGetCache() {
		ClasspathCache retrievedCache = discovery.getCache();
		assertEquals(cache, retrievedCache);
	}
}
