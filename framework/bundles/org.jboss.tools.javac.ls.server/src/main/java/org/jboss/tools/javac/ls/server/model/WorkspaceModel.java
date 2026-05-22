/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.javac.ls.server.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.javac.ls.server.model.classpath.ClasspathCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Manages the workspace model - mapping project names to filesystem paths.
 * Persisted as JSON in the workspace directory.
 */
public class WorkspaceModel {
	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceModel.class);
	private static final String WORKSPACE_FILE = "workspace.json";

	private final File workspaceDir;
	private final File workspaceFile;
	private final Map<String, WorkspaceProject> projects;
	private final Gson gson;
	private final ClasspathCache classpathCache;

	public WorkspaceModel(File workspaceDir) {
		this.workspaceDir = workspaceDir;
		this.workspaceFile = new File(workspaceDir, WORKSPACE_FILE);
		this.projects = new HashMap<>();
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		this.classpathCache = new ClasspathCache(workspaceDir);
		load();
	}

	/**
	 * Add a project to the workspace.
	 *
	 * @param name the project name (must be unique)
	 * @param path the filesystem path to the project
	 * @return true if added, false if a project with this name already exists
	 */
	public synchronized boolean addProject(String name, String path) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Project name cannot be null or empty");
		}
		if (path == null || path.trim().isEmpty()) {
			throw new IllegalArgumentException("Project path cannot be null or empty");
		}

		if (projects.containsKey(name)) {
			LOG.warn("Project '{}' already exists in workspace", name);
			return false;
		}

		WorkspaceProject project = new WorkspaceProject(name, path);
		projects.put(name, project);
		save();
		LOG.info("Added project '{}' at path: {}", name, path);
		return true;
	}

	/**
	 * Remove a project from the workspace.
	 * Note: This only removes it from the workspace model, not from the filesystem.
	 *
	 * @param name the project name
	 * @return true if removed, false if not found
	 */
	public synchronized boolean removeProject(String name) {
		WorkspaceProject removed = projects.remove(name);
		if (removed != null) {
			save();
			LOG.info("Removed project '{}' from workspace", name);
			return true;
		}
		LOG.warn("Project '{}' not found in workspace", name);
		return false;
	}

	/**
	 * Get a project by name.
	 *
	 * @param name the project name
	 * @return the project, or null if not found
	 */
	public synchronized WorkspaceProject getProject(String name) {
		return projects.get(name);
	}

	/**
	 * Get the filesystem path for a project.
	 *
	 * @param name the project name
	 * @return the path, or null if project not found
	 */
	public synchronized String getProjectPath(String name) {
		WorkspaceProject project = projects.get(name);
		return project != null ? project.getPath() : null;
	}

	/**
	 * Check if a project exists in the workspace.
	 *
	 * @param name the project name
	 * @return true if exists
	 */
	public synchronized boolean hasProject(String name) {
		return projects.containsKey(name);
	}

	/**
	 * Get all projects in the workspace.
	 *
	 * @return unmodifiable list of projects
	 */
	public synchronized List<WorkspaceProject> getProjects() {
		return Collections.unmodifiableList(new ArrayList<>(projects.values()));
	}

	/**
	 * Get all project names in the workspace.
	 *
	 * @return unmodifiable list of project names
	 */
	public synchronized List<String> getProjectNames() {
		List<String> names = new ArrayList<>(projects.keySet());
		Collections.sort(names);
		return Collections.unmodifiableList(names);
	}

	/**
	 * Get the number of projects in the workspace.
	 *
	 * @return project count
	 */
	public synchronized int getProjectCount() {
		return projects.size();
	}

	/**
	 * Load the workspace from disk.
	 */
	private void load() {
		if (!workspaceFile.exists()) {
			LOG.info("Workspace file does not exist, starting with empty workspace: {}", workspaceFile.getAbsolutePath());
			return;
		}

		try (FileReader reader = new FileReader(workspaceFile)) {
			List<WorkspaceProject> loadedProjects = gson.fromJson(reader,
					new TypeToken<List<WorkspaceProject>>(){}.getType());

			if (loadedProjects != null) {
				projects.clear();
				for (WorkspaceProject project : loadedProjects) {
					projects.put(project.getName(), project);
				}
				LOG.info("Loaded {} projects from workspace file", projects.size());
			}
		} catch (IOException e) {
			LOG.error("Error loading workspace from {}", workspaceFile.getAbsolutePath(), e);
		} catch (Exception e) {
			LOG.error("Error parsing workspace file {}", workspaceFile.getAbsolutePath(), e);
		}
	}

	/**
	 * Save the workspace to disk.
	 */
	private void save() {
		// Ensure workspace directory exists
		if (!workspaceDir.exists()) {
			if (!workspaceDir.mkdirs()) {
				LOG.error("Failed to create workspace directory: {}", workspaceDir.getAbsolutePath());
				return;
			}
		}

		try (FileWriter writer = new FileWriter(workspaceFile)) {
			List<WorkspaceProject> projectList = new ArrayList<>(projects.values());
			// Sort by name for consistent output
			projectList.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
			gson.toJson(projectList, writer);
			LOG.debug("Saved {} projects to workspace file", projects.size());
		} catch (IOException e) {
			LOG.error("Error saving workspace to {}", workspaceFile.getAbsolutePath(), e);
		}
	}

	/**
	 * Get the workspace directory.
	 *
	 * @return the workspace directory
	 */
	public File getWorkspaceDirectory() {
		return workspaceDir;
	}

	/**
	 * Get the workspace file path.
	 *
	 * @return the workspace file
	 */
	public File getWorkspaceFile() {
		return workspaceFile;
	}

	/**
	 * Get the classpath cache.
	 *
	 * @return the classpath cache
	 */
	public ClasspathCache getClasspathCache() {
		return classpathCache;
	}
}
