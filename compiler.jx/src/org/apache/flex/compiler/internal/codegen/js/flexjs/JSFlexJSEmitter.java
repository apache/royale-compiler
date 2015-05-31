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

import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.IMetaInfo;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.jx.AccessorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.AsIsEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BinaryOperatorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ClassEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FieldEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ForEachEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FunctionCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.IdentifierEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MemberAccessEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageHeaderEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SuperCallEmitter;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.flex.compiler.internal.tree.as.SetterNode;
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
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
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
    private FieldEmitter fieldEmitter;
    private AccessorEmitter accessorEmitter;
    private FunctionCallEmitter functionCallEmitter;
    private SuperCallEmitter superCallEmitter;
    private ForEachEmitter forEachEmitter;
    private MemberAccessEmitter memberAccessEmitter;
    private AsIsEmitter asIsEmitter;
    private BinaryOperatorEmitter binaryOperatorEmitter;
    private IdentifierEmitter identifierEmitter;

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

    @Override
    public IDocEmitter getDocEmitter()
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
        fieldEmitter = new FieldEmitter(this);
        accessorEmitter = new AccessorEmitter(this);
        functionCallEmitter = new FunctionCallEmitter(this);
        superCallEmitter = new SuperCallEmitter(this);
        forEachEmitter = new ForEachEmitter(this);
        memberAccessEmitter = new MemberAccessEmitter(this);
        asIsEmitter = new AsIsEmitter(this);
        binaryOperatorEmitter = new BinaryOperatorEmitter(this);
        identifierEmitter = new IdentifierEmitter(this);
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
    protected void emitMemberName(IDefinitionNode node)
    {
        write(node.getName());
    }

    @Override
    public void emitClass(IClassNode node)
    {
        classEmitter.emit(node);
    }

    @Override
    public void emitField(IVariableNode node)
    {
        fieldEmitter.emit(node);
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
        accessorEmitter.emit(node);
    }

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        functionCallEmitter.emit(node);
    }

    //--------------------------------------------------------------------------

    @Override
    protected void emitSelfReference(IFunctionNode node)
    {
        // we don't want 'var self = this;' in FlexJS
        // unless there are anonymous functions
        if (node.containsAnonymousFunctions())
            super.emitSelfReference(node);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        // TODO (mschmalle) remove when project field is removed
        identifierEmitter.emit(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitSuperCall(IASNode node, String type)
    {
        superCallEmitter.emit(node, type);
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        binaryOperatorEmitter.emit(node);
    }

    public void emitIsAs(IExpressionNode left, IExpressionNode right,
            ASTNodeID id, boolean coercion)
    {
        asIsEmitter.emitIsAs(left, right, id, coercion);
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        // TODO (mschmalle) remove when project field is removed
        memberAccessEmitter.emit(node);
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
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        //TODO: ajh  is this method needed anymore?

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();

        // ToDo (erikdebruin): add VF2JS conditional -> only use check during full SDK compilation
        if (type == null)
            return;

        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
            IMetaInfo[] metaInfos = null;
            metaInfos = node.getMetaInfos();
            for (IMetaInfo metaInfo : metaInfos)
            {
                String name = metaInfo.getTagName();
                if (name.equals("Bindable")
                        && metaInfo.getAllAttributes().length == 0)
                {
                    isBindableSetter = true;
                    break;
                }
            }
        }
        if (isBindableSetter)
        {
            getDoc().emitMethodDoc(fn, getWalker().getProject());
            write(formatQualifiedName(type.getQualifiedName()));
            if (!node.hasModifier(ASModifier.STATIC))
            {
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
            }

            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__");
            writeToken(node.getName());
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            emitParameters(node.getParameterNodes());
            //writeNewline();
            emitMethodScope(node.getScopedNode());
        }
        super.emitObjectDefineProperty(node);
    }

    @Override
    public void emitDefinePropertyFunction(IAccessorNode node)
    {
        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
            IMetaInfo[] metaInfos = null;
            metaInfos = node.getMetaInfos();
            for (IMetaInfo metaInfo : metaInfos)
            {
                String name = metaInfo.getTagName();
                if (name.equals("Bindable")
                        && metaInfo.getAllAttributes().length == 0)
                {
                    isBindableSetter = true;
                    break;
                }
            }
        }
        if (isBindableSetter)
        {
            //write(ASEmitterTokens.FUNCTION);
            //emitParameters(node.getParameterNodes());
            write(ASEmitterTokens.SPACE);
            writeNewline(ASEmitterTokens.BLOCK_OPEN);

            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write("oldValue");
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(node.getName());
            //write(ASEmitterTokens.PAREN_OPEN);
            //write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);

            // add change check
            write(ASEmitterTokens.IF);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.PAREN_OPEN);
            write("oldValue");
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.STRICT_EQUAL);
            write(ASEmitterTokens.SPACE);
            IParameterNode[] params = node.getParameterNodes();
            write(params[0].getName());
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.RETURN);
            writeNewline(ASEmitterTokens.SEMICOLON);

            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__" + node.getName());
            write(ASEmitterTokens.PAREN_OPEN);
            write(params[0].getName());
            write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);

            // add dispatch of change event
            writeNewline("    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(");
            writeNewline("         this, \"" + node.getName()
                    + "\", oldValue, " + params[0].getName() + "));");
            write(ASEmitterTokens.BLOCK_CLOSE);
            //writeNewline(ASEmitterTokens.SEMICOLON);
            writeNewline();
            writeNewline();
        }
        else
            super.emitDefinePropertyFunction(node);
    }

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

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        forEachEmitter.emit(node);
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSGoogEmitterTokens.ARRAY);
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        boolean isWritten = false;

        String s = node.getValue(true);
        if (!(node instanceof RegExpLiteralNode))
        {
            if (node.getLiteralType() == LiteralType.XML)
            {
                // ToDo (erikdebruin): VF2JS -> handle XML output properly...

                write("'" + s + "'");

                isWritten = true;
            }
            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
            //s = "\'" + s.replaceAll("\'", "\\\\\'") + "\'";
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", "\\\\n");
        }

        if (!isWritten)
        {
            write(s);
        }
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
    public String formatQualifiedName(String name)
    {
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        return name;
    }
}
