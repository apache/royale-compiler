/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;

/**
 * Represents identifiers that are built into the AS3 language.
 */
public class LanguageIdentifierNode extends IdentifierNode implements ILanguageIdentifierNode
{

    /**
     * Builds a {@link LanguageIdentifierNode} that represents
     * <code>super</code>.
     * 
     * @param superToken the token to build from
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildSuper(IASToken superToken)
    {
        return new LanguageIdentifierNode(IASKeywordConstants.SUPER, superToken, LanguageIdentifierKind.SUPER);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents
     * <code>super</code>.
     * 
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildSuper()
    {
        return new LanguageIdentifierNode(IASKeywordConstants.SUPER, null, LanguageIdentifierKind.SUPER);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>this</code>.
     * 
     * @param thisToken the token to build from
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildThis(IASToken thisToken)
    {
        return new LanguageIdentifierNode(IASKeywordConstants.THIS, thisToken, LanguageIdentifierKind.THIS);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>this</code>.
     * 
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildThis()
    {
        return new LanguageIdentifierNode(IASKeywordConstants.THIS, null, LanguageIdentifierKind.THIS);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>this</code>.
     * 
     * @param voidToken the token to build from
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildVoid(IASToken voidToken)
    {
        return new LanguageIdentifierNode(IASKeywordConstants.VOID, voidToken, LanguageIdentifierKind.VOID);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>this</code>.
     * 
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildVoid()
    {
        return new LanguageIdentifierNode(IASKeywordConstants.VOID, null, LanguageIdentifierKind.VOID);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>*</code>.
     * 
     * @param anyTypeToken the token to build from
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildAnyType(IASToken anyTypeToken)
    {
        return new LanguageIdentifierNode(IASLanguageConstants.ANY_TYPE, anyTypeToken, LanguageIdentifierKind.ANY_TYPE);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>*</code>.
     * 
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildAnyType()
    {
        return new LanguageIdentifierNode(IASLanguageConstants.ANY_TYPE, null, LanguageIdentifierKind.ANY_TYPE);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>...</code>.
     * 
     * @param restToken the token to build from
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildRest(IASToken restToken)
    {
        return new LanguageIdentifierNode(IASLanguageConstants.REST, restToken, LanguageIdentifierKind.REST);
    }

    /**
     * Builds a {@link LanguageIdentifierNode} that represents <code>...</code>.
     * 
     * @return a {@link LanguageIdentifierNode}
     */
    public static LanguageIdentifierNode buildRest()
    {
        return new LanguageIdentifierNode(IASLanguageConstants.REST, null, LanguageIdentifierKind.REST);
    }

    /**
     * Constructor.
     */
    private LanguageIdentifierNode(String text, IASToken token, LanguageIdentifierKind kind)
    {
        super(text, token);
        
        this.kind = kind;
    }

    /**
     * Copy constructor.
     *
     * @param other the node to copy
     */
    protected LanguageIdentifierNode (LanguageIdentifierNode other)
    {
        super(other);
        
        this.kind = other.kind;
    }

    private LanguageIdentifierKind kind;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        switch (kind)
        {
            case SUPER:
                return ASTNodeID.SuperID;
                
            case VOID:
                return ASTNodeID.VoidID;
                
            default:
                return super.getNodeID();
        }
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        IDefinition def = null;
        switch (kind)
        {
            case THIS:
            case SUPER:
            {
                // There is no definition that introduced these
                def = null;
                break;
            }
            
            case VOID:
            {
                def = project.getBuiltinType(IASLanguageConstants.BuiltinType.VOID);
                break;
            }
            
            case REST:
            {
                def = project.getBuiltinType(IASLanguageConstants.BuiltinType.ARRAY);
                break;
            }
            
            case ANY_TYPE:
            {
                // a.*, or ns::* is not a reference to the ANY_TYPE
                if (isMemberRef() || isQualifiedExpr(this))
                    def = null;
                else
                    def = project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE);
                break;
            }
            
            default:
            {
                def = super.resolve(project);
                break;
            }
        }
        
        return def;
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        ITypeDefinition type = null;
        
        switch (kind)
        {
            case THIS:
            {
                type = resolveThisType(project);
                break;
            }
            
            case SUPER:
            {
                type = resolveSuperType(project);
                break;
            }
            
            case ANY_TYPE:
            {
                IDefinition def = resolve(project);
                // Didn't resolve to anything, just return '*'
                if (def == null)
                    type = project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE);
                else
                    type = def.resolveType(project);
                break;
            }
                
            case REST:
            {
                type = project.getBuiltinType(IASLanguageConstants.BuiltinType.ARRAY);
                break;
            }
            
            case VOID:
            {
                type = project.getBuiltinType(IASLanguageConstants.BuiltinType.Undefined);
                break;
            }
        }
        
        return type;
    }

    @Override
    protected LanguageIdentifierNode copy()
    {
        return new LanguageIdentifierNode(this);
    }

    @Override
    public Name getMName(ICompilerProject project)
    {
        switch (kind)
        {
            case ANY_TYPE:
            {
                if( isQualifiedExpr(this) || isMemberRef() )
                {
                    Name superName = super.getMName(project);
                    return new Name(superName.getKind(), superName.getQualifiers(), null);
                }
                else if (isAttributeIdentifier())
                {
                    return new Name(ABCConstants.CONSTANT_QnameA,
                            new Nsset(new Namespace(ABCConstants.CONSTANT_PackageNs)), "*");
                }
                else if (isTypeRef())
                {
                    //  null means ANY_TYPE
                    return null;
                }
                else
                {
                    //  E4X wildcard operator.
                    return new Name("*");
            }
            }
            case REST:
            {
                // This is a bit hokey, but this ILanguageIdentifierNode is saying
                // that the type of the parameter definition is type "rest" or "...".
                // Since those aren't spelled "Array", we translate to "Array" here.
                return new Name(IASLanguageConstants.Array);
            }
            default:
            {
                return super.getMName(project);
            }
        }
    }
    
    //
    // IdentifierNode overrrides
    //
    
    /**
     * @return a string description of this pseudo-identifier.
     */
    @Override
    public String getName()
    {
        switch (kind)
        {

            case THIS:
                return "this";
                
            case SUPER:
                return "super";
                
            case ANY_TYPE:
                return "*";
                
            case REST:
                return "...";
                
            case VOID:
                return "void";
                
            default:
                return super.getName();
        }
    }

    //
    // ILanguageIdentifierNode implementations
    //

    @Override
    public LanguageIdentifierKind getKind()
    {
        return kind;
    }
    
    //
    // Other methods
    //
    
    /**
     * calculate the type of a this expression
     * 
     * @param cache
     * @return
     */
    private ITypeDefinition resolveThisType(ICompilerProject project)
    {
        ITypeDefinition thisType = null;

        Context ctx = getContext();

        switch (ctx)
        {
            case INSTANCE_CONTEXT:
            {
                thisType = getContainingClassDef();
                break;
            }
            
            case STATIC_CONTEXT:
            case PACKAGE_CONTEXT:
            {
                // Semantics should validate where this is used, and report an error for these
                // cases.
                thisType = null;
                break;
            }
            
            case GLOBAL_CONTEXT:
            {
                thisType = project.getBuiltinType(IASLanguageConstants.BuiltinType.OBJECT);
                break;
            }
        }

        return thisType;
    }

    /**
     * Calculate the type of a super expression
     */
    private ITypeDefinition resolveSuperType(ICompilerProject project)
    {
        ITypeDefinition superType = null;
        Context ctx = getContext();

        // 'super' is invalid in any other context
        if (ctx == Context.INSTANCE_CONTEXT)
        {
            ClassDefinitionBase clazz = getContainingClassDef();
            if (clazz != null)
                superType = clazz.resolveBaseClass(project);
        }

        return superType;
    }

    /**
     * Helper function to get the class definition that contains this Node
     */
    private ClassDefinitionBase getContainingClassDef()
    {
        ASScope scope = getASScope();
        if (scope != null)
            return scope.getContainingClass();

        return null;
    }

    /**
     * Helper method to determine in what context this expression is being used
     * in.
     * 
     * @return The Context that this expression occurs in
     */
    @SuppressWarnings("incomplete-switch")
	public Context getContext()
    {
        IASNode p = getParent();

        Context c = null;

        while (p != null && c == null)
        {
            if (p instanceof VariableNode)
            {
                VariableNode vn = (VariableNode)p;
                IVariableDefinition varDef = (IVariableDefinition)vn.getDefinition();
                switch (varDef.getVariableClassification())
                {
                    case CLASS_MEMBER:
                    {
                        if (varDef.hasModifier(ASModifier.STATIC))
                            c = Context.STATIC_CONTEXT;
                        else
                            c = Context.INSTANCE_CONTEXT;
                        break;
                    }

                    case PACKAGE_MEMBER:
                    {
                        c = Context.PACKAGE_CONTEXT;
                        break;
                    }

                    case FILE_MEMBER:
                    {
                        c = Context.GLOBAL_CONTEXT;
                        break;
                    }

                    // Otherwise, Keep looking, context will be based on the enclosing definition of the
                    // Variable Decl
                    //case LOCAL:
                    //case PARAMETER:
                    //break;
                }
            }
            else if (p instanceof FunctionNode)
            {
                FunctionNode fn = (FunctionNode)p;
                FunctionDefinition funcDef = fn.getDefinition();
                switch (funcDef.getFunctionClassification())
                {
                    case CLASS_MEMBER:
                    case INTERFACE_MEMBER:
                    {
                        if (funcDef.hasModifier(ASModifier.STATIC))
                            c = Context.STATIC_CONTEXT;
                        else
                            c = Context.INSTANCE_CONTEXT;
                        break;
                    }
                    
                    case PACKAGE_MEMBER:
                    case FILE_MEMBER:
                    {
                        c = Context.GLOBAL_CONTEXT;
                        break;
                    }
                }
            }
            
            if (p instanceof FunctionObjectNode)
            {
                // 'this' could be anything in a function expression - callers can use whatever 'this' they like
                c = Context.GLOBAL_CONTEXT;
            }
            else if (p instanceof ClassNode)
            {
                // If we get here then this expression must be loose code in a class def,
                // so we are running in a static context
                c = Context.STATIC_CONTEXT;
            }
            else if (p instanceof PackageNode)
            {
                // If we get here, then this expression must be loose code in a package def,
                // so we're running in a package context
                c = Context.PACKAGE_CONTEXT;
            }
            else if (p instanceof IMXMLScriptNode)
            {
                // If we get here then this expression must be loose code in a <fx:Script> tag,
                // which is compiled as if it was loose code in a class, so we are a static context
                c = Context.STATIC_CONTEXT;
            }
            else if (p instanceof IMXMLDocumentNode)
            {
                // If we get here, the this expression must be loose code in a MXML document, such
                // as in a databinding expression, so we're in Instance context
                c = Context.INSTANCE_CONTEXT;
            }

            p = p.getParent();
        }

        // If we run out of parents, then we must be in a global context
        if (c == null)
            c = Context.GLOBAL_CONTEXT;

        return c;
    }
    
    //
    // Inner types
    //

    public enum Context
    {
        // Code running in an instance of a class - prop initializers, member methods, etc
        INSTANCE_CONTEXT,
        
        // Code running in a static initialier - static initializers, static members, loose code in a class def
        STATIC_CONTEXT,
        
        // Code running at the package level - code inside a package def, package property initializers
        PACKAGE_CONTEXT,
        
        // Code running at the global level outside of a package def
        GLOBAL_CONTEXT
    }
}
