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

package org.apache.royale.compiler.internal.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.internal.tree.as.GetterNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.ParameterNode;
import org.apache.royale.compiler.internal.tree.as.SetterNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;

/**
 * Class keeps information about the vector node, used to construct it
 */
class VectorInformation
{
    // Enum to describe what the type of a return or parameter to one of the
    // Vector methods should be.
    // DEFAULT_TYPE - whatever is defined in Vector$object is correct
    // VECTOR_TYPE - the type is "Vector$object", but should be replaced with an instantiated vector type
    //               such as, Vector.<Foo>
    // VECTOR_COLLECTION_TYPE - the type should be replaced with the type parameter of the instantiated vector
    //               i.e. - for Vector.<String> this type should be 'String'
    static enum Type
    {
        DEFAULT_TYPE,
        VECTOR_TYPE,
        VECTOR_COLLECTION_TYPE

    }

    public static class FunctionInfo
    {
        private String name;

        private List<ArgumentInfo> arguments = new ArrayList<ArgumentInfo>();

        Enum<Type> t;

        public FunctionInfo(Type retType)
        {
            t = retType;
        }

        public FunctionInfo(String name, Type retType)
        {
            this.name = name;
            t = retType;
        }

        public void addArgumentType(ArgumentInfo info)
        {
            arguments.add(info);
        }

        public String getName()
        {
            return name;
        }

        public boolean returnIsVector()
        {
            return t == Type.VECTOR_TYPE;
        }

        public boolean returnIsTypeOfCollection()
        {
            return t == Type.VECTOR_COLLECTION_TYPE;
        }

        public ArgumentInfo[] getArgumentInfo()
        {
            return arguments.toArray(new ArgumentInfo[0]);
        }

        public boolean needsAdjustments()
        {

            if (returnIsVector() || returnIsTypeOfCollection())
                return true;

            for (ArgumentInfo arg : getArgumentInfo())
            {
                if (arg.returnIsVector() || arg.returnIsTypeOfCollection())
                    return true;
            }

            return false;
        }

    }

    public static class ArgumentInfo
    {
        private String name;
        private Enum<Type> type;
        private String defaultValue;

        public ArgumentInfo(String name, Type type)
        {
            this.name = name;
            this.type = type;
        }

        public ArgumentInfo(String name, Type type, String defaultValue)
        {
            this(name, type);
            this.defaultValue = defaultValue;
        }

        public String getName()
        {
            return name;
        }

        public String getDefaultValue()
        {
            return defaultValue;
        }

        public boolean returnIsVector()
        {
            return type == Type.VECTOR_TYPE;
        }

        public boolean returnIsTypeOfCollection()
        {
            return type == Type.VECTOR_COLLECTION_TYPE;
        }
    }

    private static VectorInformation instance;

    private HashSet<String> excludedMethods = new HashSet<String>();

    private HashMap<String, FunctionInfo> info = new HashMap<String, FunctionInfo>();

    private ArrayList<FunctionInfo> additionalInfo = new ArrayList<FunctionInfo>();

    public static VectorInformation getInformation()
    {
        if (instance == null)
            instance = new VectorInformation();
        
        return instance;
    }

    public FunctionInfo getFunctionInfo(String funcName)
    {
        return info.get(funcName);
    }

    public FunctionInfo[] getAdditionalInfo()
    {
        return additionalInfo.toArray(new FunctionInfo[0]);
    }

    public IDefinitionNode[] getDefinitions()
    {
        IDefinitionNode[] retVal = new IDefinitionNode[4];
        VariableNode thisNode = new VariableNode(new IdentifierNode(IASKeywordConstants.THIS));
        thisNode.setType(null, new IdentifierNode(IASLanguageConstants.Vector));
        retVal[0] = thisNode;

        //and super
        VariableNode superNode = new VariableNode(new IdentifierNode(IASKeywordConstants.SUPER));
        superNode.setType(null, new IdentifierNode(IASLanguageConstants.Object));
        retVal[1] = superNode;

        //add the fixed getter/setter pair
        GetterNode fixGetter = new GetterNode(null, null, new IdentifierNode("fixed"));
        NamespaceIdentifierNode pub = new NamespaceIdentifierNode(INamespaceConstants.public_);
        pub.span(-1, -1, -1, -1, -1, -1);
        fixGetter.setNamespace(pub);
        fixGetter.setType(null, new IdentifierNode(IASLanguageConstants.Boolean));
        retVal[2] = fixGetter;

        SetterNode fixSetter = new SetterNode(null, null, new IdentifierNode("fixed"));
        pub = new NamespaceIdentifierNode(INamespaceConstants.public_);
        pub.span(-1, -1, -1, -1, -1, -1);
        fixSetter.setNamespace(pub);
        fixSetter.setType(null, new IdentifierNode(IASLanguageConstants.void_));
        ParameterNode value = new ParameterNode(new IdentifierNode("value"));
        value.setType(null, new IdentifierNode(IASLanguageConstants.Boolean));
        fixSetter.getParametersContainerNode().addChild(value);
        retVal[3] = fixSetter;

        return retVal;
    }

    public boolean isExcluded(String name)
    {
        return excludedMethods.contains(name);
    }

    private VectorInformation()
    {
        excludedMethods.add("sortOn");
        excludedMethods.add(IASLanguageConstants.Array);
        excludedMethods.add(IASLanguageConstants.Object);
        //populate hard-coded function into
        //concat
        FunctionInfo concat = new FunctionInfo("concat", VectorInformation.Type.VECTOR_TYPE);
        info.put("concat", concat);
        concat.addArgumentType(new ArgumentInfo("parameters", VectorInformation.Type.DEFAULT_TYPE));
        //every
        FunctionInfo everyInfo = new FunctionInfo("every", VectorInformation.Type.VECTOR_TYPE);
        info.put("every", everyInfo);
        everyInfo.addArgumentType(new ArgumentInfo("callback", VectorInformation.Type.DEFAULT_TYPE));
        everyInfo.addArgumentType(new ArgumentInfo("thisObject", VectorInformation.Type.DEFAULT_TYPE, IASKeywordConstants.NULL));
        //filter
        FunctionInfo filter = new FunctionInfo("filter", VectorInformation.Type.VECTOR_TYPE);
        info.put("filter", filter);
        filter.addArgumentType(new ArgumentInfo("callback", VectorInformation.Type.DEFAULT_TYPE));
        filter.addArgumentType(new ArgumentInfo("thisObject", VectorInformation.Type.DEFAULT_TYPE, IASKeywordConstants.NULL));
        //forEach
        FunctionInfo forEach = new FunctionInfo("forEach", VectorInformation.Type.DEFAULT_TYPE);
        info.put("forEach", forEach);
        forEach.addArgumentType(new ArgumentInfo("callback", VectorInformation.Type.DEFAULT_TYPE));
        forEach.addArgumentType(new ArgumentInfo("thisObject", VectorInformation.Type.DEFAULT_TYPE, IASKeywordConstants.NULL));
        //indexOf
        FunctionInfo indexOf = new FunctionInfo("indexOf", VectorInformation.Type.DEFAULT_TYPE);
        info.put("indexOf", indexOf);
        indexOf.addArgumentType(new ArgumentInfo("searchElement", VectorInformation.Type.VECTOR_COLLECTION_TYPE));
        indexOf.addArgumentType(new ArgumentInfo("fromIndex", VectorInformation.Type.DEFAULT_TYPE, "0"));
        //join
        FunctionInfo joinInfo = new FunctionInfo("join", VectorInformation.Type.DEFAULT_TYPE);
        joinInfo.addArgumentType(new ArgumentInfo("sep", VectorInformation.Type.DEFAULT_TYPE, "\",\""));
        info.put("join", joinInfo);
        //indexOf
        FunctionInfo lastIndexOf = new FunctionInfo("lastIndexOf", VectorInformation.Type.DEFAULT_TYPE);
        info.put("lastIndexOf", lastIndexOf);
        lastIndexOf.addArgumentType(new ArgumentInfo("searchElement", VectorInformation.Type.VECTOR_COLLECTION_TYPE));
        lastIndexOf.addArgumentType(new ArgumentInfo("fromIndex", VectorInformation.Type.DEFAULT_TYPE, "0x7fffffff"));
        //map
        FunctionInfo map = new FunctionInfo("map", VectorInformation.Type.VECTOR_TYPE);
        info.put("map", map);
        map.addArgumentType(new ArgumentInfo("callback", VectorInformation.Type.DEFAULT_TYPE));
        map.addArgumentType(new ArgumentInfo("thisObject", VectorInformation.Type.DEFAULT_TYPE, IASKeywordConstants.NULL));
        //pop
        info.put("pop", new FunctionInfo("pop", VectorInformation.Type.VECTOR_COLLECTION_TYPE));
        //push
        FunctionInfo push = new FunctionInfo("push", VectorInformation.Type.DEFAULT_TYPE);
        push.addArgumentType(new ArgumentInfo("parameters", Type.DEFAULT_TYPE));
        info.put("push", push);

        //reverse
        info.put("reverse", new FunctionInfo("reverse", VectorInformation.Type.VECTOR_TYPE));
        //shift
        info.put("shift", new FunctionInfo("shift", VectorInformation.Type.VECTOR_COLLECTION_TYPE));
        //map
        FunctionInfo slice = new FunctionInfo("slice", VectorInformation.Type.VECTOR_TYPE);
        info.put("slice", slice);
        slice.addArgumentType(new ArgumentInfo("startIndex", VectorInformation.Type.DEFAULT_TYPE, "0"));
        slice.addArgumentType(new ArgumentInfo("endIndex", VectorInformation.Type.DEFAULT_TYPE, "16777215"));
        //some
        FunctionInfo some = new FunctionInfo("some", VectorInformation.Type.DEFAULT_TYPE);
        info.put("some", some);
        some.addArgumentType(new ArgumentInfo("callback", VectorInformation.Type.DEFAULT_TYPE));
        some.addArgumentType(new ArgumentInfo("thisObject", VectorInformation.Type.DEFAULT_TYPE, IASKeywordConstants.NULL));
        //sort
        FunctionInfo sort = new FunctionInfo("sort", VectorInformation.Type.VECTOR_TYPE);
        info.put("sort", sort);
        sort.addArgumentType(new ArgumentInfo("compareFunction", VectorInformation.Type.DEFAULT_TYPE));
        //splice
        FunctionInfo splice = new FunctionInfo("splice", VectorInformation.Type.VECTOR_TYPE);
        info.put("splice", splice);
        splice.addArgumentType(new ArgumentInfo("startIndex", VectorInformation.Type.DEFAULT_TYPE));
        splice.addArgumentType(new ArgumentInfo("deleteCount", VectorInformation.Type.DEFAULT_TYPE));
        splice.addArgumentType(new ArgumentInfo("items", VectorInformation.Type.DEFAULT_TYPE));

        FunctionInfo unshift = new FunctionInfo("unshift", VectorInformation.Type.DEFAULT_TYPE);
        info.put("unshift", unshift);
        unshift.addArgumentType(new ArgumentInfo("parameters", Type.DEFAULT_TYPE));

        additionalInfo.add(new FunctionInfo("toLocaleString", VectorInformation.Type.DEFAULT_TYPE));
    }

    Set<Entry<String, FunctionInfo>> getFunctionInfos()
    {
        return info.entrySet();
    }
}
