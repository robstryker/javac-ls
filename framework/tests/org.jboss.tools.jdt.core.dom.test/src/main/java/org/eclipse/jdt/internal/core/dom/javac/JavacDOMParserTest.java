/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.javac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

/**
 * Tests for JavacDOMParser.
 */
public class JavacDOMParserTest {

	@Test
	public void testBasicParsing() {
		String source = """
			package com.example;

			public class HelloWorld {
				public static void main(String[] args) {
					System.out.println("Hello, World!");
				}
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "HelloWorld.java", null, AST.JLS21, null, false);

		assertNotNull("CompilationUnit should not be null", cu);
		assertEquals("Should have package declaration", "com.example", cu.getPackage().getName().getFullyQualifiedName());
		assertEquals("Should have 1 type", 1, cu.types().size());

		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
		assertEquals("Type name should be HelloWorld", "HelloWorld", type.getName().getIdentifier());
		assertTrue("Type should be public", org.eclipse.jdt.core.dom.Modifier.isPublic(type.getModifiers()));
	}

	@Test
	public void testMethodParsing() {
		String source = """
			public class Test {
				private int value;

				public int getValue() {
					return value;
				}

				public void setValue(int value) {
					this.value = value;
				}
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "Test.java", null, AST.JLS21, null, false);

		assertNotNull(cu);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);

		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Should have 2 methods", 2, methods.length);

		assertEquals("First method should be getValue", "getValue", methods[0].getName().getIdentifier());
		assertEquals("Second method should be setValue", "setValue", methods[1].getName().getIdentifier());
	}

	@Test
	public void testJavadocParsing() {
		String source = """
			/**
			 * A simple test class.
			 *
			 * @author Test Author
			 */
			public class DocTest {
				/**
				 * Gets a value.
				 *
				 * @return the value
				 */
				public int getValue() {
					return 42;
				}
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "DocTest.java", null, AST.JLS21, null, false);

		assertNotNull(cu);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);

		assertNotNull("Type should have javadoc", type.getJavadoc());

		MethodDeclaration method = type.getMethods()[0];
		assertNotNull("Method should have javadoc", method.getJavadoc());
	}

	@Test
	public void testCommentsParsing() {
		String source = """
			// Line comment
			public class CommentTest {
				/* Block comment */
				int field;

				/**
				 * Javadoc comment
				 */
				void method() {
					// Another line comment
				}
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "CommentTest.java", null, AST.JLS21, null, false);

		assertNotNull(cu);

		List<?> comments = cu.getCommentList();
		assertNotNull("Should have comments", comments);
		assertTrue("Should have multiple comments", comments.size() >= 3);
	}

	@Test
	public void testSourcePositions() {
		String source = """
			public class PositionTest {
				int field = 42;
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "PositionTest.java", null, AST.JLS21, null, false);

		assertNotNull(cu);
		assertTrue("CompilationUnit should have valid position", cu.getStartPosition() >= 0);
		assertTrue("CompilationUnit should have valid length", cu.getLength() > 0);

		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
		assertTrue("Type should have valid position", type.getStartPosition() >= 0);
		assertTrue("Type should have valid length", type.getLength() > 0);
	}

	@Test
	public void testNoPackageDeclaration() {
		String source = """
			public class NoPackage {
				void test() {}
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "NoPackage.java", null, AST.JLS21, null, false);

		assertNotNull(cu);
		assertEquals("Should have no package", null, cu.getPackage());
		assertEquals("Should have 1 type", 1, cu.types().size());
	}

	@Test
	public void testMultipleTypes() {
		String source = """
			class First {}
			class Second {}
			class Third {}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "Multiple.java", null, AST.JLS21, null, false);

		assertNotNull(cu);
		assertEquals("Should have 3 types", 3, cu.types().size());
	}

	@Test
	public void testGenericsParsing() {
		String source = """
			import java.util.List;

			public class GenericTest<T extends Number> {
				private List<T> items;

				public <E> E getFirst(List<E> list) {
					return list.get(0);
				}
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "GenericTest.java", null, AST.JLS21, null, false);

		assertNotNull(cu);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);

		assertNotNull("Type should have type parameters", type.typeParameters());
		assertEquals("Should have 1 type parameter", 1, type.typeParameters().size());
	}

	@Test
	public void testAnnotationsParsing() {
		String source = """
			@Deprecated
			public class AnnotatedClass {
				@Override
				public String toString() {
					return "test";
				}
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "AnnotatedClass.java", null, AST.JLS21, null, false);

		assertNotNull(cu);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);

		assertNotNull("Type should have modifiers", type.modifiers());
		assertTrue("Should have at least 2 modifiers (public + @Deprecated)", type.modifiers().size() >= 2);
	}

	@Test
	public void testProblemsReporting() {
		// Code with obvious syntax error
		String source = """
			public class ErrorTest {
				this is not valid java syntax at all!
			}
			""";

		JavacDOMParser parser = new JavacDOMParser();
		CompilationUnit cu = parser.parse(source, "ErrorTest.java", null, AST.JLS21, null, false);

		assertNotNull("CompilationUnit should not be null", cu);

		IProblem[] problems = cu.getProblems();
		assertNotNull("Problems array should not be null", problems);
		assertTrue("Should have at least one problem, got: " + problems.length, problems.length > 0);

		// Verify problem details
		for (IProblem problem : problems) {
			assertNotNull("Problem should not be null", problem);
			assertTrue("Problem should have a message", problem.getMessage() != null && !problem.getMessage().isEmpty());
			assertTrue("Problem should be an error or warning", problem.isError() || problem.isWarning());
		}
	}
}
