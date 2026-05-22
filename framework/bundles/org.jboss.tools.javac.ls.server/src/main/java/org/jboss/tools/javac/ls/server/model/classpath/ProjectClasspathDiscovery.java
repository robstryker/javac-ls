/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.javac.ls.server.model.classpath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jboss.tools.javac.ls.server.model.WorkspaceProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for discovering project classpaths.
 * Manages a prioritized list of classpath discoverers and caching.
 */
public class ProjectClasspathDiscovery {
	private static final Logger LOG = LoggerFactory.getLogger(ProjectClasspathDiscovery.class);

	private static class DiscovererWithPriority {
		final IProjectClasspathDiscoverer discoverer;
		final int priority;

		DiscovererWithPriority(IProjectClasspathDiscoverer discoverer, int priority) {
			this.discoverer = discoverer;
			this.priority = priority;
		}
	}

	private List<DiscovererWithPriority> discoverers;
	private ClasspathCache cache;

	public ProjectClasspathDiscovery(ClasspathCache cache) {
		this.discoverers = new ArrayList<>();
		this.cache = cache;
		registerDefaultDiscoverers();
	}

	private void registerDefaultDiscoverers() {
		// Higher priority values are checked first
		// Eclipse has lowest priority (fallback)
		discoverers.add(new DiscovererWithPriority(new MavenProjectClasspathDiscoverer(), 100));
		discoverers.add(new DiscovererWithPriority(new EclipseProjectClasspathDiscoverer(), 1));

		// Sort by priority (highest first)
		discoverers.sort(Comparator.comparingInt((DiscovererWithPriority d) -> d.priority).reversed());
	}

	/**
	 * Discovers the classpath for a given project.
	 * First checks the cache, and only performs discovery if cache is invalid.
	 * @param proj the workspace project
	 * @return list of classpath entries, or empty list if no discoverer accepts the project
	 */
	public ArrayList<IJavacClasspathEntry> discoverClasspath(WorkspaceProject proj) {
		// Try each discoverer in priority order
		for (DiscovererWithPriority dwp : discoverers) {
			if (dwp.discoverer.accepts(proj)) {
				LOG.debug("Discoverer '{}' accepts project '{}'", dwp.discoverer.getId(), proj.getName());

				// Check if discoverer supports cache validation
				List<java.io.File> sourceFiles = dwp.discoverer.getSourceFilesForCacheValidation(proj);
				if (sourceFiles != null && cache != null) {
					// Try to load from cache if valid
					if (cache.isCacheValid(proj, sourceFiles)) {
						ArrayList<IJavacClasspathEntry> cached = cache.loadCache(proj.getName());
						if (cached != null) {
							LOG.info("Using cached classpath for project '{}'", proj.getName());
							return cached;
						}
					}
				}

				// Cache miss or invalid - perform discovery
				LOG.info("Discovering classpath for project '{}' using '{}'",
					proj.getName(), dwp.discoverer.getId());
				ArrayList<IJavacClasspathEntry> entries = dwp.discoverer.discoverClasspath(proj);

				// Save to cache
				if (cache != null && entries != null) {
					cache.saveCache(proj.getName(), entries);
				}

				return entries;
			}
		}

		LOG.warn("No discoverer accepts project '{}'", proj.getName());
		return new ArrayList<>();
	}

	/**
	 * Gets the list of registered discoverers in priority order.
	 * @return list of discoverers
	 */
	public List<IProjectClasspathDiscoverer> getDiscoverers() {
		List<IProjectClasspathDiscoverer> result = new ArrayList<>();
		for (DiscovererWithPriority dwp : discoverers) {
			result.add(dwp.discoverer);
		}
		return result;
	}

	/**
	 * Get the classpath cache.
	 * @return the cache
	 */
	public ClasspathCache getCache() {
		return cache;
	}
}
