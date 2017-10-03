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

package org.apache.royale.compiler.internal.codegen.js.amd;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.codegen.js.amd.IJSAMDDocEmitter;
import org.apache.royale.compiler.codegen.js.amd.IJSAMDEmitter;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitter;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.ClassTraitsDefinition;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.utils.NativeUtils;

/**
 * Concrete implementation of the 'AMD' JavaScript production.
 * 
 * @author Michael Schmalle
 */
public class JSAMDEmitter extends JSEmitter implements IJSAMDEmitter
{

    private Map<String, IDefinitionNode> foundAccessors = new HashMap<String, IDefinitionNode>();

    private int inheritenceLevel = -1;

    private ExportWriter exportWriter;

    private boolean initializingFieldsInConstructor;

    private List<IDefinition> baseClassCalls = new ArrayList<IDefinition>();

    StringBuilder builder()
    {
        return getBuilder();
    }

    IJSAMDDocEmitter getDoc()
    {
        return (IJSAMDDocEmitter) getDocEmitter();
    }

    public JSAMDEmitter(FilterWriter out)
    {
        super(out);

        exportWriter = new ExportWriter(this);
    }

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        // TODO (mschmalle|AMD) this is a hack but I know no other way to do replacements in a Writer
        setBufferWrite(true);

        write(JSAMDEmitterTokens.DEFINE);
        write(ASEmitterTokens.PAREN_OPEN);

        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        exportWriter.addFrameworkDependencies();
        exportWriter.addImports(type);

        exportWriter.queueExports(type, true);

        writeToken(ASEmitterTokens.COMMA);
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        // nothing
    }

    @Override
    public void emitPackageContents(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        write("function($exports");

        exportWriter.queueExports(type, false);

        write(") {");
        indentPush();
        writeNewline();
        write("\"use strict\"; ");
        writeNewline();

        ITypeNode tnode = findTypeNode(definition.getNode());
        if (tnode != null)
        {
            getWalker().walk(tnode); // IClassNode | IInterfaceNode
        }

        indentPop();
        writeNewline();
        write("}"); // end returned function
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        exportWriter.writeExports(type, true);
        exportWriter.writeExports(type, false);

        write(");"); // end define()

        // flush the buffer, writes the builder to out
        flushBuilder();
    }

    private void emitConstructor(IFunctionNode node)
    {
        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        //IFunctionDefinition definition = node.getDefinition();

        write("function ");
        write(node.getName());
        emitParameters(node.getParametersContainerNode());
        if (!isImplicit((IContainerNode) node.getScopedNode()))
        {
            emitMethodScope(node.getScopedNode());
        }
        else
        {
            // we have a synthesized constructor, implict
        }
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        final IInterfaceDefinition definition = node.getDefinition();
        final String interfaceName = definition.getBaseName();

        write("AS3.interface_($exports, {");
        indentPush();
        writeNewline();

        write("package_: \"");
        write(definition.getPackageName());
        write("\",");
        writeNewline();

        write("interface_: \"");
        write(interfaceName);
        write("\"");

        IReference[] references = definition.getExtendedInterfaceReferences();
        final int len = references.length;
        if (len > 0)
        {
            writeNewline();
            write("extends_: [");
            indentPush();
            writeNewline();
            int i = 0;
            for (IReference reference : references)
            {
                write(reference.getName());
                if (i < len - 1)
                {
                    write(",");
                    writeNewline();
                }
                i++;
            }
            indentPop();
            writeNewline();
            write("]");
        }

        indentPop();
        writeNewline();
        write("});"); // end compilation unit
    }

    @Override
    public void emitClass(IClassNode node)
    {
        //ICompilerProject project = getWalker().getProject();

        IClassDefinition definition = node.getDefinition();
        getModel().setCurrentClass(definition);

        final String className = definition.getBaseName();

        write("AS3.compilationUnit($exports, function($primaryDeclaration){");
        indentPush();
        writeNewline();

        // write constructor
        emitConstructor((IFunctionNode) definition.getConstructor().getNode());
        writeNewline();

        // base class
        IReference baseClassReference = definition.getBaseClassReference();
        boolean hasSuper = baseClassReference != null
                && !baseClassReference.getName().equals("Object");
        if (hasSuper)
        {
            String baseName = baseClassReference.getName();
            write("var Super = (" + baseName + "._ || " + baseName
                    + "._$get());");
            writeNewline();
            write("var super$ = Super.prototype;");
            writeNewline();
        }

        write("$primaryDeclaration(AS3.class_({");
        indentPush();
        writeNewline();

        // write out package
        write("package_: \"" + definition.getPackageName() + "\",");
        writeNewline();
        // write class
        write("class_: \"" + definition.getBaseName() + "\",");
        writeNewline();
        if (hasSuper)
        {
            write("extends_: Super,");
            writeNewline();
        }

        IReference[] references = definition
                .getImplementedInterfaceReferences();
        int len = references.length;

        // write implements
        write("implements_:");
        write(" [");

        if (len > 0)
        {
            indentPush();
            writeNewline();
        }

        int i = 0;
        for (IReference reference : references)
        {
            write(reference.getName());
            exportWriter.addDependency(reference.getName(),
                    reference.getDisplayString(), false, false);
            if (i < len - 1)
            {
                write(",");
                writeNewline();
            }
            i++;
        }

        if (len > 0)
        {
            indentPop();
            writeNewline();
        }

        write("],");
        writeNewline();

        // write members
        final IDefinitionNode[] members = node.getAllMemberNodes();

        write("members: {");

        indentPush();
        writeNewline();

        // constructor
        write("constructor: " + className);
        if (members.length > 0)
        {
            write(",");
            writeNewline();
        }

        List<IDefinitionNode> instanceMembers = new ArrayList<IDefinitionNode>();
        List<IDefinitionNode> staticMembers = new ArrayList<IDefinitionNode>();
        List<IASNode> staticStatements = new ArrayList<IASNode>();

        TempTools.fillInstanceMembers(members, instanceMembers);
        TempTools.fillStaticMembers(members, staticMembers, true, false);
        TempTools.fillStaticStatements(node, staticStatements, false);

        len = instanceMembers.size();
        i = 0;
        for (IDefinitionNode mnode : instanceMembers)
        {
            if (mnode instanceof IAccessorNode)
            {
                if (foundAccessors.containsKey(mnode.getName()))
                {
                    len--;
                    continue;
                }

                getWalker().walk(mnode);
            }
            else if (mnode instanceof IFunctionNode)
            {
                getWalker().walk(mnode);
            }
            else if (mnode instanceof IVariableNode)
            {
                getWalker().walk(mnode);
            }
            else
            {
                write(mnode.getName());
            }

            if (i < len - 1)
            {
                write(",");
                writeNewline();
            }
            i++;
        }

        // base class super calls
        len = baseClassCalls.size();
        i = 0;
        if (len > 0)
        {
            write(",");
            writeNewline();
        }

        for (IDefinition baseCall : baseClassCalls)
        {
            write(baseCall.getBaseName() + "$" + inheritenceLevel + ": super$."
                    + baseCall.getBaseName());

            if (i < len - 1)
            {
                write(",");
                writeNewline();
            }
        }

        // end members
        indentPop();
        writeNewline();
        write("},");
        writeNewline();

        len = staticMembers.size();

        write("staticMembers: {");

        indentPush();
        writeNewline();

        i = 0;
        for (IDefinitionNode mnode : staticMembers)
        {
            if (mnode instanceof IAccessorNode)
            {
                // TODO (mschmalle|AMD) havn't taken care of static accessors
                if (foundAccessors.containsKey(mnode.getName()))
                    continue;

                foundAccessors.put(mnode.getName(), mnode);

                getWalker().walk(mnode);
            }
            else if (mnode instanceof IFunctionNode)
            {
                getWalker().walk(mnode);
            }
            else if (mnode instanceof IVariableNode)
            {
                getWalker().walk(mnode);
            }

            if (i < len - 1)
            {
                write(",");
                writeNewline();
            }
            i++;
        }
        indentPop();
        if (len > 0)
            writeNewline();
        write("}");

        indentPop();
        writeNewline();
        write("}));");

        // static statements
        len = staticStatements.size();
        if (len > 0)
            writeNewline();

        i = 0;
        for (IASNode statement : staticStatements)
        {
            getWalker().walk(statement);
            if (!(statement instanceof IBlockNode))
                write(";");

            if (i < len - 1)
                writeNewline();

            i++;
        }

        indentPop();
        writeNewline();
        write("});"); // end compilation unit

    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitField(IVariableNode node)
    {
        IVariableDefinition definition = (IVariableDefinition) node
                .getDefinition();

        if (definition.isStatic())
        {
            IClassDefinition parent = (IClassDefinition) definition.getParent();
            write(parent.getBaseName());
            write(".");
            write(definition.getBaseName());
            write(" = ");
            emitFieldInitialValue(node);
            return;
        }

        String name = toPrivateName(definition);
        write(name);
        write(": ");
        write("{");
        indentPush();
        writeNewline();
        // field value
        write("value:");
        emitFieldInitialValue(node);
        write(",");
        writeNewline();
        // writable
        write("writable:");
        write(!(definition instanceof IConstantDefinition) ? "true" : "false");
        indentPop();
        writeNewline();
        write("}");
    }

    private void emitFieldInitialValue(IVariableNode node)
    {
        ICompilerProject project = getWalker().getProject();
        IVariableDefinition definition = (IVariableDefinition) node
                .getDefinition();

        IExpressionNode valueNode = node.getAssignedValueNode();
        if (valueNode != null)
            getWalker().walk(valueNode);
        else
            write(TempTools.toInitialValue(definition, project));
    }

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        if (foundAccessors.containsKey(node.getName()))
            return;

        foundAccessors.put(node.getName(), node);

        ICompilerProject project = getWalker().getProject();
        IAccessorDefinition getter = (IAccessorDefinition) node.getDefinition();
        IAccessorDefinition setter = getter
                .resolveCorrespondingAccessor(project);

        emitGetterSetterPair(getter, setter);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        if (foundAccessors.containsKey(node.getName()))
            return;

        foundAccessors.put(node.getName(), node);

        ICompilerProject project = getWalker().getProject();
        IAccessorDefinition setter = (IAccessorDefinition) node.getDefinition();
        IAccessorDefinition getter = setter
                .resolveCorrespondingAccessor(project);

        emitGetterSetterPair(getter, setter);
    }

    private void emitGetterSetterPair(IAccessorDefinition getter,
            IAccessorDefinition setter)
    {
        write(getter.getBaseName());
        write(": {");
        indentPush();
        writeNewline();

        if (getter != null)
        {
            emitAccessor("get", getter);
        }
        if (setter != null)
        {
            write(",");
            writeNewline();
            emitAccessor("set", setter);
        }

        indentPop();
        writeNewline();
        write("}");

    }

    protected void emitAccessor(String kind, IAccessorDefinition definition)
    {
        IFunctionNode fnode = definition.getFunctionNode();

        FunctionNode fn = (FunctionNode) fnode;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        write(kind + ": function ");
        write(definition.getBaseName() + "$" + kind);
        emitParameters(fnode.getParametersContainerNode());
        emitMethodScope(fnode.getScopedNode());
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        if (node.isConstructor())
        {
            emitConstructor(node);
            return;
        }

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());
        IFunctionDefinition definition = node.getDefinition();

        String name = toPrivateName(definition);
        write(name);
        write(":");
        write(" function ");
        write(node.getName());
        emitParameters(node.getParametersContainerNode());
        emitMethodScope(node.getScopedNode());
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        IFunctionDefinition definition = node.getDefinition();

        if (node.isConstructor())
        {
            initializingFieldsInConstructor = true;
            IClassDefinition type = (IClassDefinition) definition
                    .getAncestorOfType(IClassDefinition.class);
            // emit public fields init values
            List<IVariableDefinition> fields = TempTools.getFields(type, true);
            for (IVariableDefinition field : fields)
            {
                if (TempTools.isVariableAParameter(field,
                        definition.getParameters()))
                    continue;
                write("this.");
                write(field.getBaseName());
                write(" = ");
                emitFieldInitialValue((IVariableNode) field.getNode());
                write(";");
                writeNewline();
            }
            initializingFieldsInConstructor = false;
        }

        emitDefaultParameterCodeBlock(node);
    }

    private void emitDefaultParameterCodeBlock(IFunctionNode node)
    {
        // TODO (mschmalle|AMD) test for ... rest 
        // if default parameters exist, produce the init code
        IParameterNode[] pnodes = node.getParameterNodes();
        Map<Integer, IParameterNode> defaults = TempTools.getDefaults(pnodes);
        if (pnodes.length == 0)
            return;

        if (defaults != null)
        {
            boolean hasBody = node.getScopedNode().getChildCount() > 0;

            if (!hasBody)
            {
                indentPush();
                write(ASEmitterTokens.INDENT);
            }

            final StringBuilder code = new StringBuilder();

            List<IParameterNode> parameters = new ArrayList<IParameterNode>(
                    defaults.values());
            Collections.reverse(parameters);

            int len = defaults.size();
            // make the header in reverse order
            for (IParameterNode pnode : parameters)
            {
                if (pnode != null)
                {
                    code.setLength(0);

                    code.append(ASEmitterTokens.IF.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.PAREN_OPEN.getToken());
                    code.append(JSEmitterTokens.ARGUMENTS.getToken());
                    code.append(ASEmitterTokens.MEMBER_ACCESS.getToken());
                    code.append(JSAMDEmitterTokens.LENGTH.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.LESS_THAN.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(len);
                    code.append(ASEmitterTokens.PAREN_CLOSE.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.BLOCK_OPEN.getToken());

                    write(code.toString());

                    indentPush();
                    writeNewline();
                }
                len--;
            }

            Collections.reverse(parameters);
            for (int i = 0, n = parameters.size(); i < n; i++)
            {
                IParameterNode pnode = parameters.get(i);

                if (pnode != null)
                {
                    code.setLength(0);

                    code.append(pnode.getName());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(ASEmitterTokens.EQUAL.getToken());
                    code.append(ASEmitterTokens.SPACE.getToken());
                    code.append(pnode.getDefaultValue());
                    code.append(ASEmitterTokens.SEMICOLON.getToken());
                    write(code.toString());

                    indentPop();
                    writeNewline();

                    write(ASEmitterTokens.BLOCK_CLOSE);

                    if (i == n - 1 && !hasBody)
                        indentPop();

                    writeNewline();
                }
            }
        }
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        if (!(node.getLeftOperandNode() instanceof ILanguageIdentifierNode))
            write(node.getOperator().getOperatorText());
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        if (node.isNewExpression())
        {
            write(ASEmitterTokens.NEW);
            write(ASEmitterTokens.SPACE);
        }
        //        IDefinition resolve = node.resolveType(project);
        //        if (NativeUtils.isNative(resolve.getBaseName()))
        //        {
        //
        //        }

        getWalker().walk(node.getNameNode());

        emitArguments(node.getArgumentsNode());
    }

    @Override
    public void emitArguments(IContainerNode node)
    {
        IContainerNode newNode = node;
        FunctionCallNode fnode = (FunctionCallNode) node.getParent();
        if (TempTools.injectThisArgument(fnode, false))
        {
            IdentifierNode thisNode = new IdentifierNode("this");
            newNode = EmitterUtils.insertArgumentsBefore(node, thisNode);
        }

        int len = newNode.getChildCount();
        write(ASEmitterTokens.PAREN_OPEN);
        for (int i = 0; i < len; i++)
        {
            IExpressionNode inode = (IExpressionNode) newNode.getChild(i);
            if (inode.getNodeID() == ASTNodeID.IdentifierID)
            {
                emitArgumentIdentifier((IIdentifierNode) inode);
            }
            else
            {
                getWalker().walk(inode);
            }

            if (i < len - 1)
            {
                writeToken(ASEmitterTokens.COMMA);
            }
        }
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    private void emitArgumentIdentifier(IIdentifierNode node)
    {
        ITypeDefinition type = node.resolveType(getWalker().getProject());
        if (type instanceof ClassTraitsDefinition)
        {
            String qualifiedName = type.getQualifiedName();
            write(qualifiedName);
        }
        else
        {
            // XXX A problem?
            getWalker().walk(node);
        }
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        ICompilerProject project = getWalker().getProject();

        IDefinition resolve = node.resolve(project);
        if (TempTools.isBinding(node, project))
        {
            // AS3.bind( this,"secret$1");
            // this will happen on the right side of the = sign to bind a methof/function
            // to a variable

            write("AS3.bind(this, \"" + toPrivateName(resolve) + "\")");
        }
        else
        {
            IExpressionNode leftBase = TempTools.getNode(node, false, project);
            if (leftBase == node)
            {
                if (TempTools.isValidThis(node, project))
                    write("this.");
                // in constructor and a type
                if (initializingFieldsInConstructor
                        && resolve instanceof IClassDefinition)
                {
                    String name = resolve.getBaseName();
                    write("(" + name + "._ || " + name + "._$get())");
                    return;
                }
            }

            if (resolve != null)
            {
                // TODO (mschmalle|AMD) optimize
                String name = toPrivateName(resolve);
                if (NativeUtils.isNative(name))
                    exportWriter.addDependency(name, name, true, false);

                if (node.getParent() instanceof IMemberAccessExpressionNode)
                {
                    IMemberAccessExpressionNode mnode = (IMemberAccessExpressionNode) node
                            .getParent();
                    if (mnode.getLeftOperandNode().getNodeID() == ASTNodeID.SuperID)
                    {
                        IIdentifierNode lnode = (IIdentifierNode) mnode
                                .getRightOperandNode();

                        IClassNode cnode = (IClassNode) node
                                .getAncestorOfType(IClassNode.class);

                        initializeInheritenceLevel(cnode.getDefinition());

                        // super.foo();
                        write("this.");

                        write(lnode.getName() + "$" + inheritenceLevel);

                        baseClassCalls.add(resolve);

                        return;
                    }
                }
                write(name);
            }
            else
            {
                // no definition, just plain ole identifer
                write(node.getName());
            }
        }
    }

    @Override
    protected void emitType(IExpressionNode node)
    {
    }

    @Override
    public void emitLanguageIdentifier(ILanguageIdentifierNode node)
    {
        if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.ANY_TYPE)
        {
            write("");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.REST)
        {
            write("");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.SUPER)
        {
            IIdentifierNode inode = (IIdentifierNode) node;
            if (inode.getParent() instanceof IMemberAccessExpressionNode)
            {

            }
            else
            {
                write("Super.call");
            }
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS)
        {
            write("");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.VOID)
        {
            write("");
        }
    }

    private String toPrivateName(IDefinition definition)
    {
        if (definition instanceof ITypeDefinition)
            return definition.getBaseName();
        if (!definition.isPrivate())
            return definition.getBaseName();

        initializeInheritenceLevel(definition);

        return definition.getBaseName() + "$" + inheritenceLevel;
    }

    void initializeInheritenceLevel(IDefinition definition)
    {
        if (inheritenceLevel != -1)
            return;

        IClassDefinition cdefinition = null;
        if (definition instanceof IClassDefinition)
            cdefinition = (IClassDefinition) definition;
        else
            cdefinition = (IClassDefinition) definition
                    .getAncestorOfType(IClassDefinition.class);

        ICompilerProject project = getWalker().getProject();
        IClassDefinition[] ancestry = cdefinition.resolveAncestry(project);
        inheritenceLevel = ancestry.length - 1;
    }
}
