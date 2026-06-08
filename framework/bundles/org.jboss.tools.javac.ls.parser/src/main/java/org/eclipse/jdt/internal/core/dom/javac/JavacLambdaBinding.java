/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.javac;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import shaded.com.sun.tools.javac.code.Type;
import shaded.com.sun.tools.javac.tree.JCTree.JCLambda;

public class JavacLambdaBinding extends JavacMethodBinding {

	private LambdaExpression declaration;
	private JCLambda jcLambda;

	public JavacLambdaBinding(JavacMethodBinding methodBinding, LambdaExpression declaration, JCLambda lambda) {
		super(methodBinding.methodType, methodBinding.methodSymbol, methodBinding.parentType, methodBinding.resolver);
		this.declaration = declaration;
		this.jcLambda = lambda;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && obj instanceof JavacLambdaBinding other && Objects.equals(other.declaration, this.declaration);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ declaration.hashCode();
	}

	@Override
	public int getModifiers() {
		return super.getModifiers() & ~Modifier.ABSTRACT;
	}

	@Override
	public IBinding getDeclaringMember() {
		if (this.declaration.getParent() instanceof VariableDeclarationFragment fragment &&
				fragment.getParent() instanceof FieldDeclaration) {
			return fragment.resolveBinding();
		}
		ASTNode parent = this.declaration.getParent();
		while (parent != null) {
			if (parent instanceof MethodDeclaration method) {
				return method.resolveBinding();
			}
			if (parent instanceof LambdaExpression lambda) {
				return lambda.resolveMethodBinding();
			}
			parent = parent.getParent();
		};
		return null;
	}

	@Override
	public String[] getParameterNames() {
		return ((List<VariableDeclaration>)this.declaration.parameters()).stream()
			.map(VariableDeclaration::getName)
			.map(SimpleName::getIdentifier)
			.toArray(String[]::new);
	}
	@Override
	public ITypeBinding[] getParameterTypes() {
		ITypeBinding[] res = new ITypeBinding[this.methodType.getParameterTypes().size()];
		boolean allFound = false;
		if( this.jcLambda != null ) {
			allFound = true;
			List<Type> paramTypes = jcLambda.params.stream().map(p -> p.sym.type).toList();
			int count = Math.min(res.length, paramTypes.size());
			for (int i = 0; i < count; i++) {
				Type paramType = paramTypes.get(i);
				ITypeBinding paramBinding = this.resolver.bindings.getTypeBinding(paramType);
				if (paramBinding == null) {
					allFound = false;
				}
				res[i] = paramBinding;
			}
			if (count != res.length) {
			    allFound = false;
			}
		}

		if( !allFound ) {
			for (int i = 0; i < res.length; i++) {
				Type paramType = (Type)methodType.getParameterTypes().get(i);
				ITypeBinding paramBinding = this.resolver.bindings.getTypeBinding(paramType);
				if (paramBinding == null) {
					// workaround javac missing recovery symbols for unresolved parameterized types
					if (this.resolver.findDeclaringNode(this) instanceof MethodDeclaration methodDecl) {
						if (methodDecl.parameters().get(i) instanceof SingleVariableDeclaration paramDeclaration) {
							paramBinding = this.resolver.resolveType(paramDeclaration.getType());
						}
					}
				}
				res[i] = paramBinding;
			}
		}
		return res;
	}

}
