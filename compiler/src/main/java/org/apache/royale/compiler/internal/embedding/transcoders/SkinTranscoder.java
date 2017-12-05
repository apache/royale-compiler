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

package org.apache.royale.compiler.internal.embedding.transcoders;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.abc.ClassGeneratorHelper;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.embedding.EmbedData.SkinClassInfo;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedNoSkinClassProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import com.google.common.collect.ImmutableList;

/**
 * Handle the embedding of Skin assets
 */
public class SkinTranscoder extends TranscoderBase
{
    /**
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public SkinTranscoder(EmbedData data, Workspace workspace, SkinClassInfo skinClassInfo)
    {
        super(data, workspace);
        iBorderReference = ReferenceFactory.packageQualifiedReference(workspace, CORE_PACKAGE + ".IBorder");
        iFlexDisplayObjectReference = ReferenceFactory.packageQualifiedReference(workspace, CORE_PACKAGE + ".IFlexDisplayObject");
        iFlexAssetReference = ReferenceFactory.packageQualifiedReference(workspace, CORE_PACKAGE + ".IFlexAsset");

        this.skinClass = "";
        this.skinClassInfo = skinClassInfo;
    }

    private String skinClass;
    private final SkinClassInfo skinClassInfo;
    private final IResolvedQualifiersReference iBorderReference;
    private final IResolvedQualifiersReference iFlexDisplayObjectReference;
    private final IResolvedQualifiersReference iFlexAssetReference;

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);
        if (!result)
            return false;

        baseClassQName = skinClass;

        return result;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected boolean setAttribute(EmbedAttribute attribute)
    {
        boolean isSupported = true;
        switch (attribute)
        {
            case SKIN_CLASS:
                skinClass = (String)data.getAttribute(EmbedAttribute.SKIN_CLASS);
                break;
            default:
                isSupported = super.setAttribute(attribute);
        }

        return isSupported;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected boolean checkAttributeValues(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        if (skinClass.length() == 0)
        {
            problems.add(new EmbedNoSkinClassProblem(location));
            return false;
        }

        return true;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        // no tags related to skins, as all it's only a generated class
        return Collections.emptyMap();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public FileNode buildAST(Collection<ICompilerProblem> problems, String filename)
    {
        // TODO: remove when all other transcoders updated to generate ABC - should never be called.
        assert false : "buildAST() called on a SkinTranscoder";
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public byte[] buildABC(ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        ABCEmitter emitter = new ABCEmitter();
        final String classQName = data.getQName();
        Name className = new Name(classQName);

        IDefinition baseClass = project.resolveQNameToDefinition(skinClass);
        assert (baseClass instanceof ClassDefinition) : "skinClass does not resolve to a class";
        ClassDefinition baseClassDef = (ClassDefinition)baseClass;

        Collection<Name> implementedInterfaces = new ImmutableList.Builder<Name>()
        .add(iBorderReference.getMName())
        .add(iFlexDisplayObjectReference.getMName())
        .add(iFlexAssetReference.getMName())
        .build();

        //FIXME: move this to ClassGeneratorHelper
        Namespace privateNs = new Namespace(ABCConstants.CONSTANT_PrivateNs, className.getSingleQualifier().getName() + ":" + className.getBaseName());

        // generate the constructor:
        // public function $className() extends $baseClassDef implements IBorder, IFlexDisplayObject, IFlexAsset
        // {
        //    super();
        InstructionList classITraitsInit = new InstructionList();
        classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
        classITraitsInit.addInstruction(ABCConstants.OP_constructsuper, 0);

        // generate:
        // _measuredWidth = width;
        Name _measuredWidth = null;
        if (skinClassInfo.needsMeasuredWidth)
        {
            Name width = new Name("width");
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
            classITraitsInit.addInstruction(ABCConstants.OP_getproperty, width);

            _measuredWidth = new Name(ABCConstants.CONSTANT_Qname, new Nsset(privateNs), "_measuredWidth");
            classITraitsInit.addInstruction(ABCConstants.OP_initproperty, _measuredWidth);
        }

        // generate:
        // _measuredHeight = height;
        Name _measuredHeight = null;
        if (skinClassInfo.needsMeasuredHeight)
        {
            Name height = new Name("height");
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);
            classITraitsInit.addInstruction(ABCConstants.OP_getproperty, height);

            _measuredHeight = new Name(ABCConstants.CONSTANT_Qname, new Nsset(privateNs), "_measuredHeight");
            classITraitsInit.addInstruction(ABCConstants.OP_initproperty, _measuredHeight);
        }

        // generate: TODO: currently we don't wrap this call in a try/catch, as it
        // seems unnecessary.  Change if need be.
        // try
        // {
        //     name = NameUtil.createUniqueName(this);
        // }
        // catch(e:Error)
        // {
        // }
        if (!skinClassInfo.royaleMovieClipOrSprite)
        {
            Name name = new Name("name");
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);

            Name nameUtil = ReferenceFactory.packageQualifiedReference(workspace, UTILS_PACKAGE + ".NameUtil").getMName();
            classITraitsInit.addInstruction(ABCConstants.OP_getlex, nameUtil);
            classITraitsInit.addInstruction(ABCConstants.OP_getlocal0);

            Object[] createUniqueName = new Object[] { new Name("createUniqueName"), 1 };
            classITraitsInit.addInstruction(ABCConstants.OP_callproperty, createUniqueName);
            classITraitsInit.addInstruction(ABCConstants.OP_initproperty, name);
        }

        // finish the constructor
        classITraitsInit.addInstruction(ABCConstants.OP_returnvoid);

        TypeDefinitionBase numberDef = (TypeDefinitionBase)project.getBuiltinType(BuiltinType.NUMBER);
        Name numberName = numberDef.getMName(project);

        // add all the member variables to the class
        ClassGeneratorHelper classGen = new ClassGeneratorHelper(project, emitter, className, baseClassDef, implementedInterfaces, classITraitsInit);

        // generate:
        // public function get borderMetrics():EdgeMetrics
        // {
        //     if (scale9Grid == null)
        //     {
        //         return EdgeMetrics.EMPTY;
        //     }
        //     else
        //     {
        //         return new EdgeMetrics(scale9Grid.left,
        //                                scale9Grid.top,
        //                                Math.ceil(measuredWidth - scale9Grid.right),
        //                                Math.ceil(measuredHeight - scale9Grid.bottom));
        //     }
        // }
        if (skinClassInfo.needsIBorder && skinClassInfo.needsBorderMetrics)
        {
            InstructionList body = new InstructionList();
            Name scale9Grid = new Name("scale9Grid");
            body.addInstruction(ABCConstants.OP_getlocal0);
            body.addInstruction(ABCConstants.OP_getproperty, scale9Grid);

            Label trueLabel = new Label();
            body.addInstruction(ABCConstants.OP_iftrue, trueLabel);

            Name edgeMetrics = ReferenceFactory.packageQualifiedReference(workspace, CORE_PACKAGE + ".EdgeMetrics").getMName();
            body.addInstruction(ABCConstants.OP_getlex, edgeMetrics);
            body.addInstruction(ABCConstants.OP_getproperty, new Name("EMPTY"));
            body.addInstruction(ABCConstants.OP_returnvalue);

            body.labelNext(trueLabel);
            body.addInstruction(ABCConstants.OP_findpropstrict, edgeMetrics);
            
            body.addInstruction(ABCConstants.OP_getlocal0);
            body.addInstruction(ABCConstants.OP_getproperty, scale9Grid);
            body.addInstruction(ABCConstants.OP_getproperty, new Name("left"));

            body.addInstruction(ABCConstants.OP_getlocal0);
            body.addInstruction(ABCConstants.OP_getproperty, scale9Grid);
            body.addInstruction(ABCConstants.OP_getproperty, new Name("top"));

            Name math = new Name("Math");
            body.addInstruction(ABCConstants.OP_getlex, math);
            body.addInstruction(ABCConstants.OP_getlocal0);

            body.addInstruction(ABCConstants.OP_getproperty, new Name("measuredWidth"));
            body.addInstruction(ABCConstants.OP_getlocal0);
            body.addInstruction(ABCConstants.OP_getproperty, scale9Grid);
            body.addInstruction(ABCConstants.OP_getproperty, new Name("right"));
            body.addInstruction(ABCConstants.OP_subtract);

            Object[] ceil = new Object[] {new Name("ceil"), 1};
            body.addInstruction(ABCConstants.OP_callproperty, ceil);
            body.addInstruction(ABCConstants.OP_getlex, math);
            body.addInstruction(ABCConstants.OP_getlocal0);

            body.addInstruction(ABCConstants.OP_getproperty, new Name("measuredHeight"));
            body.addInstruction(ABCConstants.OP_getlocal0);
            body.addInstruction(ABCConstants.OP_getproperty, scale9Grid);
            body.addInstruction(ABCConstants.OP_getproperty, new Name("bottom"));
            body.addInstruction(ABCConstants.OP_subtract);

            body.addInstruction(ABCConstants.OP_callproperty, ceil);
            body.addInstruction(ABCConstants.OP_constructprop, new Object[] {edgeMetrics, 4});
            body.addInstruction(ABCConstants.OP_returnvalue);

            classGen.addITraitsGetter(new Name("borderMetrics"), edgeMetrics, body);
        }

        if (skinClassInfo.needsIFlexDisplayObject)
        {
            // generate:
            // public function get measuredWidth():Number
            // {
            //     return _measuredWidth;
            // }
            if (skinClassInfo.needsMeasuredWidth)
            {
                InstructionList body = new InstructionList();
                body.addInstruction(ABCConstants.OP_getlocal0);
                body.addInstruction(ABCConstants.OP_getproperty, _measuredWidth);
                body.addInstruction(ABCConstants.OP_returnvalue);

                classGen.addMemberVariable(_measuredWidth, numberName);
                classGen.addITraitsGetter(new Name("measuredWidth"), numberName, body);
            }

            // generate:
            // public function get measuredHeight():Number
            // {
            //     return _measuredHeight;
            // }
            if (skinClassInfo.needsMeasuredHeight)
            {
                InstructionList body = new InstructionList();
                body.addInstruction(ABCConstants.OP_getlocal0);
                body.addInstruction(ABCConstants.OP_getproperty, _measuredHeight);
                body.addInstruction(ABCConstants.OP_returnvalue);

                classGen.addMemberVariable(_measuredHeight, numberName);
                classGen.addITraitsGetter(new Name("measuredHeight"), numberName, body);
            }

            // generate:
            // public function move(x:Number, y:Number):void
            // {
            //     this.x = x;
            //     this.y = y;
            // }
            if (skinClassInfo.needsMove)
            {
                InstructionList body = new InstructionList();

                body.addInstruction(ABCConstants.OP_getlocal0);
                body.addInstruction(ABCConstants.OP_getlocal1);
                body.addInstruction(ABCConstants.OP_setproperty, new Name("x"));

                body.addInstruction(ABCConstants.OP_getlocal0);
                body.addInstruction(ABCConstants.OP_getlocal2);
                body.addInstruction(ABCConstants.OP_setproperty, new Name("y"));

                body.addInstruction(ABCConstants.OP_returnvoid);

                Collection<Name> paramTypes = new ImmutableList.Builder<Name>()
                .add(numberName)
                .add(numberName)
                .build();

                classGen.addITraitsMethod(new Name("move"), paramTypes, new Name("void"), Collections.<Object>emptyList(), false, false, false, body);
            }

            // generate:
            // public function setActualSize(newWidth:Number, newHeight:Number):void
            // {
            //     if (width != newWidth)
            //     {
            //         width = newWidth;
            //     }
            //     if (height != newHeight)
            //     {
            //         height = newHeight;
            //     }
            // }
            if (skinClassInfo.needsSetActualSize)
            {
                InstructionList body = new InstructionList();

                Name width = new Name("width");
                body.addInstruction(ABCConstants.OP_getlocal0);
                body.addInstruction(ABCConstants.OP_getproperty, width);
                body.addInstruction(ABCConstants.OP_getlocal1);

                Label trueLabel = new Label();
                body.addInstruction(ABCConstants.OP_ifeq, trueLabel);

                body.addInstruction(ABCConstants.OP_findproperty, width);
                body.addInstruction(ABCConstants.OP_getlocal1);
                body.addInstruction(ABCConstants.OP_setproperty, width);

                body.labelNext(trueLabel);

                Name height = new Name("height");
                body.addInstruction(ABCConstants.OP_getlocal0);
                body.addInstruction(ABCConstants.OP_getproperty, height);
                body.addInstruction(ABCConstants.OP_getlocal2);

                trueLabel = new Label();
                body.addInstruction(ABCConstants.OP_ifeq, trueLabel);

                body.addInstruction(ABCConstants.OP_findproperty, height);
                body.addInstruction(ABCConstants.OP_getlocal2);
                body.addInstruction(ABCConstants.OP_setproperty, height);

                body.labelNext(trueLabel);

                body.addInstruction(ABCConstants.OP_returnvoid);

                Collection<Name> paramTypes = new ImmutableList.Builder<Name>()
                .add(numberName)
                .add(numberName)
                .build();

                classGen.addITraitsMethod(new Name("setActualSize"), paramTypes, new Name("void"), Collections.<Object>emptyList(), false, false, false, body);
            }
        }

        // generate:
        // override public function toString():String
        // {
        //     return NameUtil.displayObjectToString(this);
        // }
        if (!skinClassInfo.royaleMovieClipOrSprite)
        {
            InstructionList body = new InstructionList();
            body.addInstruction(ABCConstants.OP_getlocal0);
            body.addInstruction(ABCConstants.OP_pushscope);

            Name nameUtil = ReferenceFactory.packageQualifiedReference(workspace, UTILS_PACKAGE + ".NameUtil").getMName();
            body.addInstruction(ABCConstants.OP_getlex, nameUtil);
            body.addInstruction(ABCConstants.OP_getlocal0);

            Object[] displayObjectToString = new Object[] { new Name("displayObjectToString"), 1 };
            body.addInstruction(ABCConstants.OP_callproperty, displayObjectToString);
            body.addInstruction(ABCConstants.OP_returnvalue);

            TypeDefinitionBase stringDef = (TypeDefinitionBase)project.getBuiltinType(BuiltinType.STRING);
            Name stringName = stringDef.getMName(project);
            classGen.addITraitsMethod(new Name("toString"), Collections.<Name>emptyList(), stringName, Collections.<Object>emptyList(), false, false, true, body);
        }

        classGen.finishScript();

        try
        {
            return emitter.emit();
        }
        catch (Exception e)
        {
            problems.add(new InternalCompilerProblem(e));
            return null;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
            return false;

        if (!(o instanceof SkinTranscoder))
            return false;

        SkinTranscoder t = (SkinTranscoder)o;
        if (!skinClass.equals(t.skinClass))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode += skinClass.hashCode();

        return hashCode;
    }
}
