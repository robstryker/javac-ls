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
package org.jboss.tools.javac.ls.index.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.jboss.tools.javac.ls.index.model.FieldDeclarationEntry;
import org.jboss.tools.javac.ls.index.model.Location;
import org.jboss.tools.javac.ls.index.model.MethodDeclarationEntry;
import org.jboss.tools.javac.ls.index.model.ReferenceEntry;
import org.jboss.tools.javac.ls.index.model.TypeDeclarationEntry;
import org.jboss.tools.javac.ls.index.model.TypeDeclarationEntry.TypeKind;
import org.jboss.tools.javac.ls.index.store.JavaIndex;
import org.junit.Before;
import org.junit.Test;

public class JavaIndexTest {

	private JavaIndex index;
	private Path testFile;

	@Before
	public void setUp() {
		index = new JavaIndex();
		testFile = Paths.get("/test/Example.java");
	}

	@Test
	public void testAddAndGetType() {
		TypeDeclarationEntry entry = new TypeDeclarationEntry();
		entry.setQualifiedName("com.example.MyClass");
		entry.setSimpleName("MyClass");
		entry.setPackageName("com.example");
		entry.setKind(TypeKind.CLASS);
		entry.setSuperclass("java.lang.Object");
		entry.setInterfaces(Arrays.asList("java.io.Serializable"));

		index.addType(entry);

		TypeDeclarationEntry retrieved = index.getType("com.example.MyClass");
		assertNotNull("Type should be retrievable", retrieved);
		assertEquals("Qualified name should match", "com.example.MyClass", retrieved.getQualifiedName());
		assertEquals("Simple name should match", "MyClass", retrieved.getSimpleName());
		assertEquals("Kind should be CLASS", TypeKind.CLASS, retrieved.getKind());
		assertEquals("Superclass should match", "java.lang.Object", retrieved.getSuperclass());
		assertEquals("Should have 1 interface", 1, retrieved.getInterfaces().size());
		assertTrue("Should implement Serializable", retrieved.getInterfaces().contains("java.io.Serializable"));
	}

	@Test
	public void testTypeHierarchy() {
		// Create hierarchy: Object -> Animal -> Dog
		TypeDeclarationEntry animal = new TypeDeclarationEntry();
		animal.setQualifiedName("com.example.Animal");
		animal.setSimpleName("Animal");
		animal.setSuperclass("java.lang.Object");
		index.addType(animal);

		TypeDeclarationEntry dog = new TypeDeclarationEntry();
		dog.setQualifiedName("com.example.Dog");
		dog.setSimpleName("Dog");
		dog.setSuperclass("com.example.Animal");
		index.addType(dog);

		TypeDeclarationEntry cat = new TypeDeclarationEntry();
		cat.setQualifiedName("com.example.Cat");
		cat.setSimpleName("Cat");
		cat.setSuperclass("com.example.Animal");
		index.addType(cat);

		// Test direct subtypes
		Collection<String> animalSubtypes = index.findDirectSubtypes("com.example.Animal");
		assertEquals("Animal should have 2 direct subtypes", 2, animalSubtypes.size());
		assertTrue("Should contain Dog", animalSubtypes.contains("com.example.Dog"));
		assertTrue("Should contain Cat", animalSubtypes.contains("com.example.Cat"));

		// Test transitive subtypes
		Set<String> objectSubtypes = index.findAllSubtypes("java.lang.Object");
		assertTrue("Should contain Animal transitively", objectSubtypes.contains("com.example.Animal"));
		assertTrue("Should contain Dog transitively", objectSubtypes.contains("com.example.Dog"));
		assertTrue("Should contain Cat transitively", objectSubtypes.contains("com.example.Cat"));
	}

	@Test
	public void testInterfaceImplementors() {
		TypeDeclarationEntry serializable = new TypeDeclarationEntry();
		serializable.setQualifiedName("java.io.Serializable");
		serializable.setKind(TypeKind.INTERFACE);
		index.addType(serializable);

		TypeDeclarationEntry myClass = new TypeDeclarationEntry();
		myClass.setQualifiedName("com.example.MyClass");
		myClass.setInterfaces(Arrays.asList("java.io.Serializable"));
		index.addType(myClass);

		Collection<String> implementors = index.findDirectImplementors("java.io.Serializable");
		assertEquals("Should have 1 implementor", 1, implementors.size());
		assertTrue("Should contain MyClass", implementors.contains("com.example.MyClass"));
	}

	@Test
	public void testAddAndGetMethod() {
		MethodDeclarationEntry entry = new MethodDeclarationEntry();
		entry.setDeclaringType("com.example.MyClass");
		entry.setMethodName("doSomething");
		entry.setParameterTypes(Arrays.asList("java.lang.String", "int"));
		entry.setReturnType("void");
		entry.setConstructor(false);

		index.addMethod(entry);

		String signatureKey = entry.getSignatureKey();
		MethodDeclarationEntry retrieved = index.getMethod(signatureKey);
		assertNotNull("Method should be retrievable", retrieved);
		assertEquals("Method name should match", "doSomething", retrieved.getMethodName());
		assertEquals("Should have 2 parameters", 2, retrieved.getParameterTypes().size());
		assertEquals("Return type should match", "void", retrieved.getReturnType());
	}

	@Test
	public void testFindMethodsInType() {
		MethodDeclarationEntry method1 = new MethodDeclarationEntry();
		method1.setDeclaringType("com.example.MyClass");
		method1.setMethodName("method1");
		index.addMethod(method1);

		MethodDeclarationEntry method2 = new MethodDeclarationEntry();
		method2.setDeclaringType("com.example.MyClass");
		method2.setMethodName("method2");
		index.addMethod(method2);

		MethodDeclarationEntry otherMethod = new MethodDeclarationEntry();
		otherMethod.setDeclaringType("com.example.OtherClass");
		otherMethod.setMethodName("otherMethod");
		index.addMethod(otherMethod);

		Collection<MethodDeclarationEntry> methods = index.findMethodsInType("com.example.MyClass");
		assertEquals("Should have 2 methods", 2, methods.size());
	}

	@Test
	public void testAddAndGetField() {
		FieldDeclarationEntry entry = new FieldDeclarationEntry();
		entry.setDeclaringType("com.example.MyClass");
		entry.setFieldName("myField");
		entry.setFieldType("java.lang.String");

		index.addField(entry);

		String fieldKey = entry.getFieldKey();
		FieldDeclarationEntry retrieved = index.getField(fieldKey);
		assertNotNull("Field should be retrievable", retrieved);
		assertEquals("Field name should match", "myField", retrieved.getFieldName());
		assertEquals("Field type should match", "java.lang.String", retrieved.getFieldType());
	}

	@Test
	public void testTypeReferences() {
		Location loc1 = new Location(testFile, 10, 20, 1, 5);
		Location loc2 = new Location(testFile, 30, 40, 2, 10);

		index.addTypeReference("com.example.MyClass",
				new ReferenceEntry(loc1, ReferenceEntry.ReferenceKind.TYPE_REFERENCE));
		index.addTypeReference("com.example.MyClass",
				new ReferenceEntry(loc2, ReferenceEntry.ReferenceKind.CONSTRUCTOR_INVOCATION));

		Collection<ReferenceEntry> usages = index.findTypeUsages("com.example.MyClass");
		assertEquals("Should have 2 usages", 2, usages.size());
	}

	@Test
	public void testNameReferences() {
		Location loc1 = new Location(testFile, 10, 20, 1, 5);
		Location loc2 = new Location(testFile, 30, 40, 2, 10);

		index.addNameReference("myVariable",
				new ReferenceEntry(loc1, ReferenceEntry.ReferenceKind.NAME_REFERENCE));
		index.addNameReference("myVariable",
				new ReferenceEntry(loc2, ReferenceEntry.ReferenceKind.NAME_REFERENCE));

		Collection<ReferenceEntry> usages = index.findNameUsages("myVariable");
		assertEquals("Should have 2 usages", 2, usages.size());
	}

	@Test
	public void testStatistics() {
		TypeDeclarationEntry type = new TypeDeclarationEntry();
		type.setQualifiedName("com.example.MyClass");
		index.addType(type);

		MethodDeclarationEntry method = new MethodDeclarationEntry();
		method.setDeclaringType("com.example.MyClass");
		method.setMethodName("method");
		index.addMethod(method);

		FieldDeclarationEntry field = new FieldDeclarationEntry();
		field.setDeclaringType("com.example.MyClass");
		field.setFieldName("field");
		index.addField(field);

		assertEquals("Type count should be 1", 1, index.getTypeCount());
		assertEquals("Method count should be 1", 1, index.getMethodCount());
		assertEquals("Field count should be 1", 1, index.getFieldCount());
	}
}
