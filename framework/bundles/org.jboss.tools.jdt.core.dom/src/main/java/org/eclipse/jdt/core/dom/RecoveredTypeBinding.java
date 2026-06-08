/*******************************************************************************
 * Copyright (c) 2007, 2025 IBM Corporation and others.
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
 *								Bug 429813 - [1.8][dom ast] IMethodBinding#getJavaElement() should return IMethod for lambda
 *     Red Hat, Inc. - adapted for javac-ls (removed Eclipse internal compiler dependencies)
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * This class represents the recovered binding for a type
 */
@SuppressWarnings("rawtypes")
class RecoveredTypeBinding implements ITypeBinding {

	private static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
	private static final IVariableBinding[] NO_VARIABLE_BINDINGS = new IVariableBinding[0];
	private static final IMethodBinding[] NO_METHOD_BINDINGS = new IMethodBinding[0];
	private static final IAnnotationBinding[] NO_ANNOTATIONS = new IAnnotationBinding[0];

	private VariableDeclaration variableDeclaration;
	private Type currentType;
	protected final BindingResolver resolver;
	private int dimensions;
	private RecoveredTypeBinding innerTypeBinding;
	private ITypeBinding[] typeArguments;

	RecoveredTypeBinding(BindingResolver resolver, VariableDeclaration variableDeclaration) {
		this.variableDeclaration = variableDeclaration;
		this.resolver = resolver;
		this.currentType = getType();
		this.dimensions = variableDeclaration.getExtraDimensions();
		if (this.currentType != null && this.currentType.isArrayType()) {
			this.dimensions += ((ArrayType) this.currentType).getDimensions();
		}
	}

	RecoveredTypeBinding(BindingResolver resolver, Type type) {
		this.currentType = type;
		this.resolver = resolver;
		this.dimensions = 0;
		if (type != null && type.isArrayType()) {
			this.dimensions += ((ArrayType) type).getDimensions();
		}
	}

	RecoveredTypeBinding(BindingResolver resolver, RecoveredTypeBinding typeBinding, int dimensions) {
		this.innerTypeBinding = typeBinding;
		this.dimensions = typeBinding.getDimensions() + dimensions;
		this.resolver = resolver;
	}

	@Override
	public ITypeBinding createArrayType(int dims) {
		return null; // Stub - resolver method removed from BindingResolver
	}

	@Override
	public String getBinaryName() {
		return null;
	}

	@Override
	public ITypeBinding getBound() {
		return null;
	}

	@Override
	public ITypeBinding getGenericTypeOfWildcardType() {
		return null;
	}

	@Override
	public int getRank() {
		return -1;
	}

	@Override
	public ITypeBinding getComponentType() {
		if (this.dimensions == 0) return null;
		return null; // Stub - resolver method removed from BindingResolver
	}

	@Override
	public IVariableBinding[] getDeclaredFields() {
		return NO_VARIABLE_BINDINGS;
	}

	@Override
	public IMethodBinding[] getDeclaredMethods() {
		return NO_METHOD_BINDINGS;
	}

	@Override
	public int getDeclaredModifiers() {
		return 0;
	}

	@Override
	public ITypeBinding[] getDeclaredTypes() {
		return NO_TYPE_BINDINGS;
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		return null;
	}

	@Override
	public IMethodBinding getDeclaringMethod() {
		return null;
	}

	@Override
	public IBinding getDeclaringMember() {
		return null;
	}

	@Override
	public int getDimensions() {
		return this.dimensions;
	}

	@Override
	public ITypeBinding getElementType() {
		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.getElementType();
		}
		if (this.currentType != null && this.currentType.isArrayType()) {
			return this.resolver.resolveType(((ArrayType) this.currentType).getElementType());
		}
		if (this.variableDeclaration != null && this.variableDeclaration.getExtraDimensions() != 0) {
			return this.resolver.resolveType(getType());
		}
		return null;
	}

	@Override
	public ITypeBinding getErasure() {
		return this;
	}

	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		return null;
	}

	@Override
	public ITypeBinding[] getInterfaces() {
		return NO_TYPE_BINDINGS;
	}

	@Override
	public int getModifiers() {
		return Modifier.NONE;
	}

	@Override
	public String getName() {
		char[] brackets = new char[this.dimensions * 2];
		for (int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
			brackets[i] = ']';
			brackets[i - 1] = '[';
		}
		StringBuilder buffer = new StringBuilder(getInternalName());
		buffer.append(brackets);
		return String.valueOf(buffer);
	}

	private String getInternalName() {
		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.getInternalName();
		}
		return getTypeNameFrom(getType());
	}

	@Override
	public IModuleBinding getModule() {
		return null; // Stub - no access to scope/package bindings
	}

	@Override
	public IPackageBinding getPackage() {
		return null; // Stub - no access to scope/package bindings
	}

	@Override
	public String getQualifiedName() {
		return getName();
	}

	@Override
	public ITypeBinding getSuperclass() {
		if (getQualifiedName().equals("java.lang.Object")) {	//$NON-NLS-1$
			return null;
		}
		return this.resolver.resolveWellKnownType("java.lang.Object"); //$NON-NLS-1$
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		if (this.typeArguments != null) {
			return this.typeArguments;
		}

		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.getTypeArguments();
		}

		if (this.currentType != null && this.currentType.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) this.currentType;
			List typeArgumentsList = parameterizedType.typeArguments();
			int size = typeArgumentsList.size();
			ITypeBinding[] temp = new ITypeBinding[size];
			for (int i = 0; i < size; i++) {
				ITypeBinding currentTypeBinding = ((Type) typeArgumentsList.get(i)).resolveBinding();
				if (currentTypeBinding == null) {
					return this.typeArguments = NO_TYPE_BINDINGS;
				}
				temp[i] = currentTypeBinding;
			}
			return this.typeArguments = temp;
		}
		return this.typeArguments = NO_TYPE_BINDINGS;
	}

	@Override
	public ITypeBinding[] getTypeBounds() {
		return NO_TYPE_BINDINGS;
	}

	@Override
	public ITypeBinding getTypeDeclaration() {
		return this;
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		return NO_TYPE_BINDINGS;
	}

	@Override
	public ITypeBinding getWildcard() {
		return null;
	}

	@Override
	public boolean isAnnotation() {
		return false;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isAssignmentCompatible(ITypeBinding typeBinding) {
		if ("java.lang.Object".equals(typeBinding.getQualifiedName())) { //$NON-NLS-1$
			return true;
		}
		// since recovered binding are not unique isEqualTo is required
		return isEqualTo(typeBinding);
	}

	@Override
	public boolean isCapture() {
		return false;
	}

	@Override
	public boolean isCastCompatible(ITypeBinding typeBinding) {
		if ("java.lang.Object".equals(typeBinding.getQualifiedName())) { //$NON-NLS-1$
			return true;
		}
		// since recovered binding are not unique isEqualTo is required
		return isEqualTo(typeBinding);
	}

	@Override
	public boolean isClass() {
		return true;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isRecord() {
		return false;
	}

	@Override
	public boolean isFromSource() {
		return false;
	}

	@Override
	public boolean isGenericType() {
		return false;
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public boolean isIntersectionType() {
		return false;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public boolean isMember() {
		return false;
	}

	@Override
	public boolean isNested() {
		return false;
	}

	@Override
	public boolean isNullType() {
		return false;
	}

	@Override
	public boolean isParameterizedType() {
		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.isParameterizedType();
		}
		if (this.currentType != null) {
			return this.currentType.isParameterizedType();
		}
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isRawType() {
		return false;
	}

	@Override
	public boolean isSubTypeCompatible(ITypeBinding typeBinding) {
		if ("java.lang.Object".equals(typeBinding.getQualifiedName())) { //$NON-NLS-1$
			return true;
		}
		// since recovered binding are not unique isEqualTo is required
		return isEqualTo(typeBinding);
	}

	@Override
	public boolean isTopLevel() {
		return true;
	}

	@Override
	public boolean isTypeVariable() {
		return false;
	}

	@Override
	public boolean isUpperbound() {
		return false;
	}

	@Override
	public boolean isWildcardType() {
		return false;
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return NO_ANNOTATIONS;
	}

	@Override
	public String getKey() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Recovered#"); //$NON-NLS-1$
		if (this.innerTypeBinding != null) {
			buffer.append("innerTypeBinding") //$NON-NLS-1$
			      .append(this.innerTypeBinding.getKey());
		} else if (this.currentType != null) {
			buffer.append("currentType") //$NON-NLS-1$
			      .append(this.currentType.toString());
		} else if (this.variableDeclaration != null) {
			buffer
				.append("variableDeclaration") //$NON-NLS-1$
				.append(this.variableDeclaration.getClass())
				.append(this.variableDeclaration.getName().getIdentifier())
				.append(this.variableDeclaration.getExtraDimensions());
		}
		buffer.append(getDimensions());
		if (this.typeArguments != null) {
			buffer.append('<');
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				if (i != 0) {
					buffer.append(',');
				}
				buffer.append(this.typeArguments[i].getKey());
			}
			buffer.append('>');
		}
		return String.valueOf(buffer);
	}

	@Override
	public int getKind() {
		return IBinding.TYPE;
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean isEqualTo(IBinding other) {
		if (!other.isRecovered() || other.getKind() != IBinding.TYPE) return false;
		return getKey().equals(other.getKey());
	}

	@Override
	public boolean isRecovered() {
		return true;
	}

	@Override
	public boolean isSynthetic() {
		return false;
	}

	private String getTypeNameFrom(Type type) {
		if (type == null) return "";
		switch(type.getNodeType0()) {
			case ASTNode.ARRAY_TYPE :
				ArrayType arrayType = (ArrayType) type;
				type = arrayType.getElementType();
				return getTypeNameFrom(type);
			case ASTNode.PARAMETERIZED_TYPE :
				ParameterizedType parameterizedType = (ParameterizedType) type;
				StringBuilder buffer = new StringBuilder(getTypeNameFrom(parameterizedType.getType()));
				ITypeBinding[] tArguments = getTypeArguments();
				final int typeArgumentsLength = tArguments.length;
				if (typeArgumentsLength != 0) {
					buffer.append('<');
					for (int i = 0; i < typeArgumentsLength; i++) {
						if (i > 0) {
							buffer.append(',');
						}
						buffer.append(tArguments[i].getName());
					}
					buffer.append('>');
				}
				return String.valueOf(buffer);
			case ASTNode.PRIMITIVE_TYPE :
				PrimitiveType primitiveType = (PrimitiveType) type;
				return primitiveType.getPrimitiveTypeCode().toString();
			case ASTNode.QUALIFIED_TYPE :
				QualifiedType qualifiedType = (QualifiedType) type;
				return qualifiedType.getName().getIdentifier();
			case ASTNode.NAME_QUALIFIED_TYPE :
				NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
				return nameQualifiedType.getName().getIdentifier();
			case ASTNode.SIMPLE_TYPE :
				SimpleType simpleType = (SimpleType) type;
				Name name = simpleType.getName();
				if (name.isQualifiedName()) {
					QualifiedName qualifiedName = (QualifiedName) name;
					return qualifiedName.getName().getIdentifier();
				}
				return ((SimpleName) name).getIdentifier();
		}
		return "";
	}

	private Type getType() {
		if (this.currentType != null) {
			return this.currentType;
		}
		if (this.variableDeclaration == null) return null;
		switch(this.variableDeclaration.getNodeType()) {
			case ASTNode.SINGLE_VARIABLE_DECLARATION :
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) this.variableDeclaration;
				return singleVariableDeclaration.getType();
			default :
				// this is a variable declaration fragment
				ASTNode parent = this.variableDeclaration.getParent();
				switch(parent.getNodeType()) {
					case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
						VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) parent;
						return variableDeclarationExpression.getType();
					case ASTNode.VARIABLE_DECLARATION_STATEMENT :
						VariableDeclarationStatement statement = (VariableDeclarationStatement) parent;
						return statement.getType();
					case ASTNode.FIELD_DECLARATION :
						FieldDeclaration fieldDeclaration  = (FieldDeclaration) parent;
						return fieldDeclaration.getType();
				}
		}
		return null; // should not happen
	}

	@Override
	public IAnnotationBinding[] getTypeAnnotations() {
		return NO_ANNOTATIONS;
	}
}
