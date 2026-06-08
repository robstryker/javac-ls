/*******************************************************************************
 * Copyright (c) 2004, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 458577 - IClassFile.getWorkingCopy() may lead to NPE in BecomeWorkingCopyOperation
 *******************************************************************************/
package shaded.org.eclipse.jdt.core.dom;


/**
 * A Java language parser for creating abstract syntax trees (ASTs).
 * <p>
 * STUB: This class implementation has been commented out.
 * Use the new entry point when it becomes available.
 * </p>
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ASTParser {

	/**
	 * Kind constant used to request that the source be parsed
     * as a single expression.
	 */
	public static final int K_EXPRESSION = 0x01;

	/**
	 * Kind constant used to request that the source be parsed
     * as a sequence of statements.
	 */
	public static final int K_STATEMENTS = 0x02;

	/**
	 * Kind constant used to request that the source be parsed
	 * as a sequence of class body declarations.
	 */
	public static final int K_CLASS_BODY_DECLARATIONS = 0x04;

	/**
	 * Kind constant used to request that the source be parsed
	 * as a compilation unit.
	 */
	public static final int K_COMPILATION_UNIT = 0x08;

	/**
	 * Creates a new object for creating a Java abstract syntax tree
     * (AST) following the specified set of API rules.
     *
 	 * @param level the API level; one of the <code>.JLS*</code> level constants declared on {@link AST} or {@link AST#getJLSLatest}
	 * @return new ASTParser instance
	 */
	public static ASTParser newParser(int level) {
		throw new UnsupportedOperationException("ASTParser implementation has been stubbed out. Use the new entry point when available.");
	}

	private ASTParser(int level) {
		throw new UnsupportedOperationException("ASTParser implementation has been stubbed out.");
	}
}

/*
 * ORIGINAL IMPLEMENTATION COMMENTED OUT
 *
 * The full implementation has been commented out because it depends on many
 * internal JDT classes that are not available in this standalone project:
 * - shaded.org.eclipse.jdt.internal.core.dom.ICompilationUnitResolver
 * - org.eclipse.jdt.internal.compiler.* classes
 * - org.eclipse.core.runtime.IProgressMonitor
 * - And many more
 *
 * A new entry point will be created to replace this functionality.
 *
 * Original file was 1655 lines with the following public methods:
 * - newParser(int level)
 * - setKind(int kind)
 * - setSource(char[] source)
 * - setSource(ICompilationUnit source)
 * - setSource(IClassFile source)
 * - setSourceRange(int offset, int length)
 * - setProject(IJavaProject project)
 * - setUnitName(String unitName)
 * - setResolveBindings(boolean enabled)
 * - setStatementsRecovery(boolean enabled)
 * - setBindingsRecovery(boolean enabled)
 * - setCompilerOptions(Map options)
 * - setFocalPosition(int position)
 * - setEnvironment(String[] classpathEntries, String[] sourcepathEntries, String[] encodings, boolean includeRunningVMBootclasspath)
 * - setIgnoreMethodBodies(boolean enabled)
 * - setWorkingCopyOwner(WorkingCopyOwner owner)
 * - createAST(IProgressMonitor monitor)
 * - createASTs(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor, IProgressMonitor monitor)
 * - createASTs(IJavaProject project, String[] bindingKeys, ASTRequestor requestor, IProgressMonitor monitor)
 *
 * All of these will need to be reimplemented using a different architecture
 * that doesn't depend on the Eclipse JDT internal compiler infrastructure.
 */
