/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

/**
 * Severity constants for problems.
 */
public interface ProblemSeverities {
	int Error = 1;
	int Warning = 0;
	int Info = 4;
	int Ignore = -1;
}
