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

package org.apache.flex.compiler.internal.js.codegen.amd;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.internal.js.codegen.JSEmitter;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.js.codegen.amd.IJSAMDDocEmitter;
import org.apache.flex.compiler.js.codegen.amd.IJSAMDEmitter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.ITypeNode;

/**
 * Concrete implementation of the 'AMD' JavaScript production.
 * 
 * @author Michael Schmalle
 */
public class JSAMDEmitter extends JSEmitter implements IJSAMDEmitter
{
    private List<String> runtime = new ArrayList<String>();

    private List<String> types = new ArrayList<String>();

    /*
    
    define(["exports", "..."], function($exports, Type, ...) {
        "use strict"; AS3.class_($exports, 
        function() {
    
            var Super=Object._;
            var super$=Super.prototype;
            
            return {
                class_: "A",
                extends_: Super,
                members: {
                    constructor: A,
                    // public methods
                    foo: {}
                }
            };
        });
    });
    */

    IJSAMDDocEmitter getDoc()
    {
        return (IJSAMDDocEmitter) getDocEmitter();
    }

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        write("define(");

        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = findType(containedScope.getAllLocalDefinitions());
        if (type == null)
            return;

        addFrameworkDependencies(runtime);
        addImports(type, types);

        // runtime
        writeExports(type, true);
        write(", ");

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
        writeExports(type, false);
        write(") {");
        indentPush();
        writeNewline();
        write("\"use strict\"; ");

        //-----------------------------------------------------
        ITypeNode tnode = findTypeNode(definition.getNode());
        if (tnode != null)
        {
            getWalker().walk(tnode); // IClassNode | IInterfaceNode
        }
        //-----------------------------------------------------

        indentPop();
        writeNewline();
        write("}"); // end returned function
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        write(");"); // end define()
    }

    @Override
    public void emitClass(IClassNode node)
    {
        IClassDefinition definition = node.getDefinition();
        final String className = definition.getBaseName();

        write("AS3.class_($exports,");
        writeNewline();
        write("function() {");

        // walk IClassNode | IInterfaceNode

        indentPush();
        writeNewline();
        //var Super=Object._;
        //var super$=Super.prototype;
        String baseName = toSuperBaseName(definition);
        if (baseName != null)
        {
            write("var Super=" + baseName + "._;");
            writeNewline();
            write("var super$=Super.prototype;");
        }

        writeNewline();
        write("return {");
        indentPush();

        // class
        writeNewline();
        write("class_: \"" + className + "\",");
        writeNewline();
        write("extends_: Super");

        final IDefinitionNode[] members = node.getAllMemberNodes();

        if (members.length > 0)
        {
            write(",");
            writeNewline();
            write("members: {");
            indentPush();
            writeNewline();

            // constructor
            write("constructor: " + className + ",");
            writeNewline();

            // end members
            indentPop();
            writeNewline();
            write("};");
        }

        // end return
        indentPop();
        writeNewline();
        write("};");

        indentPop();

        writeNewline();
        write("});");
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitMethod(IFunctionNode node)
    {
        if (node.isConstructor())
        {
            IClassDefinition definition = getClassDefinition(node);

            FunctionNode fn = (FunctionNode) node;
            fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

            String qname = definition.getQualifiedName();
            write(qname);
            write(SPACE);
            write(EQUALS);
            write(SPACE);
            write(FUNCTION);
            emitParamters(node.getParameterNodes());
            emitMethodScope(node.getScopedNode());

            return;
        }

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        String qname = getTypeDefinition(node).getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(qname);
            write(PERIOD);
            if (!fn.hasModifier(ASModifier.STATIC))
            {
                write(PROTOTYPE);
                write(PERIOD);
            }
        }

        emitMemberName(node);
        write(SPACE);
        write(EQUALS);
        write(SPACE);
        write(FUNCTION);
        emitParamters(node.getParameterNodes());
        emitMethodScope(node.getScopedNode());
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        emitDefaultParameterCodeBlock(node);
    }

    private void emitDefaultParameterCodeBlock(IFunctionNode node)
    {
        // TODO (mschmalle) test for ... rest 
        // if default parameters exist, produce the init code
        IParameterNode[] pnodes = node.getParameterNodes();
        Map<Integer, IParameterNode> defaults = getDefaults(pnodes);
        if (pnodes.length == 0)
            return;

        if (defaults != null)
        {
            boolean hasBody = node.getScopedNode().getChildCount() > 0;

            if (!hasBody)
            {
                indentPush();
                write(INDENT);
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

                    code.append(IASKeywordConstants.IF);
                    code.append(SPACE);
                    code.append(PARENTHESES_OPEN);
                    code.append(IASLanguageConstants.arguments);
                    code.append(PERIOD);
                    code.append(LENGTH);
                    code.append(SPACE);
                    code.append(LESS_THEN);
                    code.append(SPACE);
                    code.append(len);
                    code.append(PARENTHESES_CLOSE);
                    code.append(SPACE);
                    code.append(CURLYBRACE_OPEN);

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
                    code.append(SPACE);
                    code.append(EQUALS);
                    code.append(SPACE);
                    code.append(pnode.getDefaultValue());
                    code.append(SEMICOLON);
                    write(code.toString());

                    indentPop();
                    writeNewline();

                    write(CURLYBRACE_CLOSE);

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

    public JSAMDEmitter(FilterWriter out)
    {
        super(out);
    }

    private Map<Integer, IParameterNode> getDefaults(IParameterNode[] nodes)
    {
        Map<Integer, IParameterNode> result = new HashMap<Integer, IParameterNode>();
        int i = 0;
        boolean hasDefaults = false;
        for (IParameterNode node : nodes)
        {
            if (node.hasDefaultValue())
            {
                hasDefaults = true;
                result.put(i, node);
            }
            else
            {
                result.put(i, null);
            }
            i++;
        }

        if (!hasDefaults)
            return null;

        return result;
    }

    private static ITypeDefinition getTypeDefinition(IDefinitionNode node)
    {
        ITypeNode tnode = (ITypeNode) node.getAncestorOfType(ITypeNode.class);
        return (ITypeDefinition) tnode.getDefinition();
    }

    private static IClassDefinition getClassDefinition(IDefinitionNode node)
    {
        IClassNode tnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        return tnode.getDefinition();
    }

    private void writeExports(ITypeDefinition type, boolean outputString)
    {
        if (outputString)
        {
            write("[");
            write("\"exports\"");
        }

        write(", ");

        int i = 0;
        int len = runtime.size();
        for (String dependency : runtime)
        {
            if (outputString)
            {
                write("\"" + dependency.replaceAll("\\.", "/") + "\"");
            }
            else
            {
                write(dependency);
            }

            if (i < len - 1)
                write(", ");
            i++;
        }

        i = 0;
        len = types.size();
        if (len > 0)
            write(", ");

        for (String dependency : types)
        {
            if (outputString)
            {
                write("\"" + dependency.replaceAll("\\.", "/") + "\"");
            }
            else
            {
                write(dependency);
            }
            if (i < len - 1)
                write(", ");
            i++;
        }

        if (outputString)
        {
            write("]");
        }
    }

    protected void addImports(ITypeDefinition type, List<String> dependencies)
    {
        Collection<String> imports = new ArrayList<String>();
        type.getContainedScope().getScopeNode().getAllImports(imports);
        for (String imp : imports)
        {
            if (!isExcludedImport(imp))
                dependencies.add(imp);
        }
    }

    protected void addFrameworkDependencies(List<String> dependencies)
    {
        dependencies.add("AS3");
    }

    protected boolean isExcludedImport(String imp)
    {
        return imp.startsWith(AS3);
    }

    private String toSuperBaseName(ITypeDefinition type)
    {
        if (type instanceof IClassDefinition)
        {
            IClassDefinition cdefintion = (IClassDefinition) type;
            IReference reference = cdefintion.getBaseClassReference();
            if (reference != null)
                return reference.getName();
        }
        return null;
    }
}
