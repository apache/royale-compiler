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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import java.io.FilterWriter;

import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.jx.AccessorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.AsIsEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BinaryOperatorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ClassEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.DefinePropertyFunctionEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FieldEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ForEachEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FunctionCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.IdentifierEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.InterfaceEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.LiteralEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MemberAccessEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MethodEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ObjectDefinePropertyEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageHeaderEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SelfReferenceEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SuperCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.VarDeclarationEmitter;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

/**
 * Concrete implementation of the 'FlexJS' JavaScript production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSFlexJSEmitter extends JSGoogEmitter implements IJSFlexJSEmitter
{

    private JSFlexJSDocEmitter docEmitter = null;

    private PackageHeaderEmitter packageHeaderEmitter;
    private PackageFooterEmitter packageFooterEmitter;

    private BindableEmitter bindableEmitter;

    private ClassEmitter classEmitter;
    private InterfaceEmitter interfaceEmitter;

    private FieldEmitter fieldEmitter;
    private VarDeclarationEmitter varDeclarationEmitter;
    private AccessorEmitter accessorEmitter;
    private MethodEmitter methodEmitter;

    private FunctionCallEmitter functionCallEmitter;
    private SuperCallEmitter superCallEmitter;
    private ForEachEmitter forEachEmitter;
    private MemberAccessEmitter memberAccessEmitter;
    private BinaryOperatorEmitter binaryOperatorEmitter;
    private IdentifierEmitter identifierEmitter;
    private LiteralEmitter literalEmitter;

    private AsIsEmitter asIsEmitter;
    private SelfReferenceEmitter selfReferenceEmitter;
    private ObjectDefinePropertyEmitter objectDefinePropertyEmitter;
    private DefinePropertyFunctionEmitter definePropertyFunctionEmitter;

    public BindableEmitter getBindableEmitter()
    {
        return bindableEmitter;
    }

    public ClassEmitter getClassEmiter()
    {
        return classEmitter;
    }

    public AccessorEmitter getAccessorEmitter()
    {
        return accessorEmitter;
    }

    // TODO (mschmalle) Fix; this is not using the backend doc strategy for replacement
    @Override
    public IJSGoogDocEmitter getDocEmitter()
    {
        if (docEmitter == null)
            docEmitter = new JSFlexJSDocEmitter(this);
        return docEmitter;
    }

    public JSFlexJSEmitter(FilterWriter out)
    {
        super(out);

        packageHeaderEmitter = new PackageHeaderEmitter(this);
        packageFooterEmitter = new PackageFooterEmitter(this);

        bindableEmitter = new BindableEmitter(this);

        classEmitter = new ClassEmitter(this);
        interfaceEmitter = new InterfaceEmitter(this);

        fieldEmitter = new FieldEmitter(this);
        varDeclarationEmitter = new VarDeclarationEmitter(this);
        accessorEmitter = new AccessorEmitter(this);
        methodEmitter = new MethodEmitter(this);

        functionCallEmitter = new FunctionCallEmitter(this);
        superCallEmitter = new SuperCallEmitter(this);
        forEachEmitter = new ForEachEmitter(this);
        memberAccessEmitter = new MemberAccessEmitter(this);
        binaryOperatorEmitter = new BinaryOperatorEmitter(this);
        identifierEmitter = new IdentifierEmitter(this);
        literalEmitter = new LiteralEmitter(this);

        asIsEmitter = new AsIsEmitter(this);
        selfReferenceEmitter = new SelfReferenceEmitter(this);
        objectDefinePropertyEmitter = new ObjectDefinePropertyEmitter(this);
        definePropertyFunctionEmitter = new DefinePropertyFunctionEmitter(this);
    }

    @Override
    protected void writeIndent()
    {
        write(JSFlexJSEmitterTokens.INDENT);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    @Override
    public void emitMemberKeyword(IDefinitionNode node)
    {
        if (node instanceof IFunctionNode)
        {
            writeToken(ASEmitterTokens.FUNCTION);
        }
        else if (node instanceof IVariableNode)
        {
            writeToken(ASEmitterTokens.VAR);
        }
    }

    @Override
    public void emitMemberName(IDefinitionNode node)
    {
        write(node.getName());
    }

    @Override
    public String formatQualifiedName(String name)
    {
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        return name;
    }

    //--------------------------------------------------------------------------
    // Package Level
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        packageHeaderEmitter.emit(definition);
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        packageHeaderEmitter.emitContents(definition);
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        packageFooterEmitter.emit(definition);
    }

    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        classEmitter.emit(node);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        interfaceEmitter.emit(node);
    }

    @Override
    public void emitField(IVariableNode node)
    {
        fieldEmitter.emit(node);
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
        varDeclarationEmitter.emit(node);
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
        accessorEmitter.emit(node);
    }

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        accessorEmitter.emitGet(node);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        accessorEmitter.emitSet(node);
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        methodEmitter.emit(node);
    }

    //--------------------------------------------------------------------------
    // Statements
    //--------------------------------------------------------------------------

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        functionCallEmitter.emit(node);
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        forEachEmitter.emit(node);
    }

    //--------------------------------------------------------------------------
    // Expressions
    //--------------------------------------------------------------------------

    @Override
    public void emitSuperCall(IASNode node, String type)
    {
        superCallEmitter.emit(node, type);
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        memberAccessEmitter.emit(node);
    }

    @Override
    public void emitE4XFilter(IMemberAccessExpressionNode node)
    {
        // ToDo (erikdebruin): implement E4X replacement !?!
        write(ASEmitterTokens.SINGLE_QUOTE);
        write("E4XFilter");
        write(ASEmitterTokens.SINGLE_QUOTE);
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        binaryOperatorEmitter.emit(node);
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSGoogEmitterTokens.ARRAY);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        identifierEmitter.emit(node);
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        literalEmitter.emit(node);
    }

    //--------------------------------------------------------------------------
    // Specific
    //--------------------------------------------------------------------------

    public void emitIsAs(IExpressionNode left, IExpressionNode right,
            ASTNodeID id, boolean coercion)
    {
        asIsEmitter.emitIsAs(left, right, id, coercion);
    }

    @Override
    protected void emitSelfReference(IFunctionNode node)
    {
        selfReferenceEmitter.emit(node);
    }

    @Override
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        objectDefinePropertyEmitter.emit(node);
    }

    @Override
    public void emitDefinePropertyFunction(IAccessorNode node)
    {
        definePropertyFunctionEmitter.emit(node);
    }
}
