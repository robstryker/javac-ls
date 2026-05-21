/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     Red Hat, Inc. - extracted from BindingResolver for javac-ls
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * Abstract binding resolver for resolving bindings in AST nodes.
 * Provides default implementations that return null or false.
 * <p>
 * This class serves as a simplified version of the internal BindingResolver
 * that does not depend on JDT compiler internals.
 * </p>
 */
abstract class AbstractBindingResolver {

	/**
	 * Creates a binding resolver.
	 */
	AbstractBindingResolver() {
		// default implementation: do nothing
	}

	/**
	 * Finds the corresponding AST node from which the given binding originated.
	 */
	ASTNode findDeclaringNode(IBinding binding) {
		return null;
	}

	/**
	 * Finds the corresponding AST node from which the given binding key originated.
	 */
	ASTNode findDeclaringNode(String bindingKey) {
		return null;
	}

	/**
	 * Finds the corresponding AST node from which the given annotation instance originated.
	 */
	ASTNode findDeclaringNode(IAnnotationBinding instance) {
		return null;
	}

	boolean isResolvedTypeInferredFromExpectedType(MethodInvocation methodInvocation) {
		return false;
	}

	/**
	 * Returns whether this expression node is the site of a boxing conversion (JLS3 5.1.7).
	 */
	boolean resolveBoxing(Expression expression) {
		return false;
	}

	/**
	 * Returns whether this expression node is the site of an unboxing conversion (JLS3 5.1.8).
	 */
	boolean resolveUnboxing(Expression expression) {
		return false;
	}

	/**
	 * Resolves and returns the compile-time constant expression value.
	 */
	Object resolveConstantExpressionValue(Expression expression) {
		return null;
	}

	/**
	 * Resolves and returns the binding for the constructor being invoked.
	 */
	IMethodBinding resolveConstructor(ClassInstanceCreation expression) {
		return null;
	}

	/**
	 * Resolves and returns the binding for the constructor being invoked.
	 */
	IMethodBinding resolveConstructor(ConstructorInvocation expression) {
		return null;
	}

	/**
	 * Resolves and returns the binding for the constructor being invoked.
	 */
	IMethodBinding resolveConstructor(EnumConstantDeclaration enumConstantDeclaration) {
		return null;
	}

	/**
	 * Resolves and returns the binding for the constructor being invoked.
	 */
	IMethodBinding resolveConstructor(SuperConstructorInvocation expression) {
		return null;
	}

	/**
	 * Resolves the type of the given expression and returns the type binding for it.
	 */
	ITypeBinding resolveExpressionType(Expression expression) {
		return null;
	}

	/**
	 * Resolves the given field access and returns the binding for it.
	 */
	IVariableBinding resolveField(FieldAccess fieldAccess) {
		return null;
	}

	/**
	 * Resolves the given super field access and returns the binding for it.
	 */
	IVariableBinding resolveField(SuperFieldAccess fieldAccess) {
		return null;
	}

	/**
	 * Resolves the given import declaration and returns the binding for it.
	 */
	IBinding resolveImport(ImportDeclaration importDeclaration) {
		return null;
	}

	/**
	 * Resolves the given annotation type member declaration and returns the binding for it.
	 */
	IMethodBinding resolveMember(AnnotationTypeMemberDeclaration member) {
		return null;
	}

	/**
	 * Resolves the given method declaration and returns the binding for it.
	 */
	IMethodBinding resolveMethod(MethodDeclaration method) {
		return null;
	}

	/**
	 * Resolves the given method reference and returns the binding for it.
	 */
	IMethodBinding resolveMethod(MethodReference methodReference) {
		return null;
	}

	/**
	 * Resolves the given Lambda Expression and returns the binding for it.
	 */
	IMethodBinding resolveMethod(LambdaExpression lambda) {
		return null;
	}

	/**
	 * Resolves the given method invocation and returns the binding for it.
	 */
	IMethodBinding resolveMethod(MethodInvocation method) {
		return null;
	}

	/**
	 * Resolves the given module declaration and returns the binding for it.
	 */
	IModuleBinding resolveModule(ModuleDeclaration module) {
		return null;
	}

	/**
	 * Resolves the given name and returns the type binding for it.
	 */
	IBinding resolveName(Name name) {
		return null;
	}

	/**
	 * Resolves the given package declaration and returns the binding for it.
	 */
	IPackageBinding resolvePackage(PackageDeclaration pkg) {
		return null;
	}

	/**
	 * Resolves the given member reference and returns the binding for it.
	 */
	IBinding resolveReference(MemberRef ref) {
		return null;
	}

	/**
	 * Resolves the given method reference and returns the binding for it.
	 */
	IBinding resolveReference(MethodRef ref) {
		return null;
	}

	/**
	 * Resolves the given member value pair and returns the binding for it.
	 */
	IMemberValuePairBinding resolveMemberValuePair(MemberValuePair memberValuePair) {
		return null;
	}

	/**
	 * Resolves the given annotation type declaration and returns the binding for it.
	 */
	ITypeBinding resolveType(AnnotationTypeDeclaration type) {
		return null;
	}

	/**
	 * Resolves the given anonymous class declaration and returns the binding for it.
	 */
	ITypeBinding resolveType(AnonymousClassDeclaration type) {
		return null;
	}

	/**
	 * Resolves the given enum declaration and returns the binding for it.
	 */
	ITypeBinding resolveType(EnumDeclaration type) {
		return null;
	}

	/**
	 * Resolves the given record declaration and returns the binding for it.
	 */
	ITypeBinding resolveType(RecordDeclaration type) {
		return null;
	}

	/**
	 * Resolves the given type and returns the type binding for it.
	 */
	ITypeBinding resolveType(Type type) {
		return null;
	}

	/**
	 * Resolves the given class or interface declaration and returns the binding for it.
	 */
	ITypeBinding resolveType(TypeDeclaration type) {
		return null;
	}

	/**
	 * Resolves the given implicit type declaration and returns the binding for it.
	 */
	ITypeBinding resolveType(ImplicitTypeDeclaration type) {
		return null;
	}

	/**
	 * Resolves the given type parameter and returns the type binding for the type parameter.
	 */
	ITypeBinding resolveTypeParameter(TypeParameter typeParameter) {
		return null;
	}

	/**
	 * Resolves the given enum constant declaration and returns the binding for the field.
	 */
	IVariableBinding resolveVariable(EnumConstantDeclaration enumConstant) {
		return null;
	}

	/**
	 * Resolves the given variable declaration and returns the binding for it.
	 */
	IVariableBinding resolveVariable(VariableDeclaration variable) {
		return null;
	}

	/**
	 * Resolves the given well known type by name and returns the type binding for it.
	 */
	ITypeBinding resolveWellKnownType(String name) {
		return null;
	}

	/**
	 * Resolves the given annotation instance and returns the DOM representation for it.
	 */
	IAnnotationBinding resolveAnnotation(Annotation annotation) {
		return null;
	}

	public boolean isResolvedTypeInferredFromExpectedType(ClassInstanceCreation classInstanceCreation) {
		// TODO Auto-generated method stub
		return false;
	}

	public IMethodBinding resolveMethod(SuperMethodInvocation superMethodInvocation) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isResolvedTypeInferredFromExpectedType(SuperMethodInvocation superMethodInvocation) {
		// TODO Auto-generated method stub
		return false;
	}
}
