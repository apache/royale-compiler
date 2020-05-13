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

package org.apache.royale.compiler.internal.targets;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.config.RSLSettings.RSLAndPolicyFileURLPair;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.abc.ClassGeneratorHelper;
import org.apache.royale.compiler.internal.as.codegen.LexicalScope;
import org.apache.royale.compiler.internal.config.FrameInfo;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.targets.Target.DirectDependencies;
import org.apache.royale.compiler.mxml.IMXMLTypeConstants;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InvalidBackgroundColorProblem;
import org.apache.royale.compiler.problems.MissingSignedDigestProblem;
import org.apache.royale.compiler.problems.MissingUnsignedDigestProblem;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCDigest;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swc.ISWCManager;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.tags.ProductInfoTag;
import org.apache.royale.swf.tags.ProductInfoTag.Edition;
import org.apache.royale.swf.tags.ProductInfoTag.Product;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Delegate class used by Royale specific targets to generate Royale specific code.
 * If we were writing this compiler in C++ this would be a mix-in class.
 */
public abstract class RoyaleTarget
{
    public RoyaleTarget(ITargetSettings targetSettings, RoyaleProject project)
    {
        royaleProject = project;
        this.targetSettings = targetSettings;
        
        accessibleClassNames = new HashSet<String>();
    }
    
    protected final RoyaleProject royaleProject;
    private final ITargetSettings targetSettings;
    
    /**
     * {@link Set} of classes referenced from accessibility meta-data.
     * <p>
     * For Example:
     * <p>
     * {@code [AccessibilityClass(implementation="mx.accessibility.ButtonAccImpl")]}
     * <p>
     * This set is accumulated as we discover direct dependencies, which is not as
     * maintainable as I would like.
     */
    protected final HashSet<String> accessibleClassNames;
    
    /**
     * Codegen IFlexModuleFactory.callInContext();
     * 
     *  public final override function callInContext(fn:Function, thisArg:Object, argArray:Array, returns:Boolean=true) : *
     *  {
     *     var ret : * = fn.apply(thisArg, argArray);
     *     if (returns) return ret;
     *     return;
     *  }
     *  
     * @param classGen
     * @param isOverride true if the generated method overrides a base class
     * method, false otherwise.
     */
    protected final void codegenCallInContextMethod(ClassGeneratorHelper classGen, boolean isOverride)
    {
        IResolvedQualifiersReference applyReference = ReferenceFactory.resolvedQualifierQualifiedReference(royaleProject.getWorkspace(), 
                NamespaceDefinition.getAS3NamespaceDefinition(), "apply");

        InstructionList callInContext = new InstructionList();
        callInContext.addInstruction(ABCConstants.OP_getlocal1);
        callInContext.addInstruction(ABCConstants.OP_getlocal2);
        callInContext.addInstruction(ABCConstants.OP_getlocal3);
        callInContext.addInstruction(ABCConstants.OP_callproperty, new Object[] {applyReference.getMName(), 2});
        callInContext.addInstruction(ABCConstants.OP_getlocal, 4);
        Label callInContextReturnVoid = new Label();
        callInContext.addInstruction(ABCConstants.OP_iffalse, callInContextReturnVoid);
        callInContext.addInstruction(ABCConstants.OP_returnvalue);
        callInContext.labelNext(callInContextReturnVoid);
        // TODO This should be OP_returnvoid, but the Boolean default value
        // for the 'returns' parameter isn't defaulting to true.
        // Fix this after CMP-936 is fixed.
        callInContext.addInstruction(ABCConstants.OP_returnvalue);

        ImmutableList<Name> callInContextParams = new ImmutableList.Builder<Name>()
                .add(new Name(IASLanguageConstants.Function))
                .add(new Name(IASLanguageConstants.Object))
                .add(new Name(IASLanguageConstants.Array))
                .add(new Name(IASLanguageConstants.Boolean))
                .build();

        classGen.addITraitsMethod(new Name("callInContext"), callInContextParams, null, 
                Collections.<Object> singletonList(Boolean.TRUE), false, true, isOverride, callInContext);
        
    }
    
    /**
     * Codegen IFlexModuleFactory.create() override public function create(...
     * params):Object { var mainClass : Class; if (params.length <= 0) {
     * mainClass = getlex MainApplicationClass } else if (params[0] is String) {
     * mainClass = getDefinitionByName(params[0]) } else return
     * super.create.apply(this, params); if (!mainClass) return null; var
     * instance:Object = new mainClass(); if (instance is IFlexModule)
     * (IFlexModule(instance)).moduleFactory = this; return instance; }
     * 
     * @param classGen
     * @param mainApplicationName {@link Name} that will refer to the main
     * application class at runtme. May not be null but a library.swf for a SWC
     * may pass in a {@link Name} that resolves to "Object" at runtime.
     */
    protected final void codegenCreateMethod(ClassGeneratorHelper classGen, Name mainApplicationName, boolean isFlexSDKInfo)
    {
        IResolvedQualifiersReference applyReference = ReferenceFactory.resolvedQualifierQualifiedReference(royaleProject.getWorkspace(), 
                NamespaceDefinition.getAS3NamespaceDefinition(), "apply");
        IResolvedQualifiersReference getDefinitionByNameReference =
                ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(), IASLanguageConstants.getDefinitionByName);
        IResolvedQualifiersReference iFlexModule =
                ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(), IMXMLTypeConstants.IFlexModule);
        boolean codegenIFlexModule = iFlexModule.resolve(royaleProject) != null && isFlexSDKInfo;
        Name getDefinitionByName = getDefinitionByNameReference.getMName();
        InstructionList create = new InstructionList();
        create.addInstruction(ABCConstants.OP_getlocal1);
        create.addInstruction(ABCConstants.OP_getproperty, new Name("length"));
        create.addInstruction(ABCConstants.OP_pushbyte, 0);
        Label createL1 = new Label();
        create.addInstruction(ABCConstants.OP_ifgt, createL1);
        create.addInstruction(ABCConstants.OP_findproperty, mainApplicationName);
        create.addInstruction(ABCConstants.OP_getproperty, mainApplicationName);
        Label createL3 = new Label();
        create.addInstruction(ABCConstants.OP_jump, createL3);
        create.labelNext(createL1);
        create.addInstruction(ABCConstants.OP_getlocal1);
        create.addInstruction(ABCConstants.OP_getproperty, new Name("0"));
        create.addInstruction(ABCConstants.OP_istype, new Name("String"));
        Label createL2 = new Label();
        create.addInstruction(ABCConstants.OP_iffalse, createL2);
        create.addInstruction(ABCConstants.OP_finddef, getDefinitionByName);
        create.addInstruction(ABCConstants.OP_getlocal1);
        create.addInstruction(ABCConstants.OP_getproperty, new Name("0"));
        create.addInstruction(ABCConstants.OP_callproperty, new Object[] {getDefinitionByName, 1});
        create.addInstruction(ABCConstants.OP_jump, createL3);
        create.labelNext(createL2);
        create.addInstruction(ABCConstants.OP_getlocal0);
        create.addInstruction(ABCConstants.OP_getsuper, new Name("create"));
        create.addInstruction(ABCConstants.OP_getlocal0);
        create.addInstruction(ABCConstants.OP_getlocal1);
        create.addInstruction(ABCConstants.OP_callproperty, new Object[] {applyReference.getMName(), 2});
        create.addInstruction(ABCConstants.OP_returnvalue);
        create.labelNext(createL3);
        create.addInstruction(ABCConstants.OP_astype, new Name("Class"));
        create.addInstruction(ABCConstants.OP_dup);
        Label createL5 = new Label();
        create.addInstruction(ABCConstants.OP_iffalse, createL5);
        create.addInstruction(ABCConstants.OP_construct, 0);
        if (codegenIFlexModule)
        {
            create.addInstruction(ABCConstants.OP_dup);
            create.addInstruction(ABCConstants.OP_istype, iFlexModule.getMName());
            Label createL4 = new Label();
            create.addInstruction(ABCConstants.OP_iffalse, createL4);
            create.addInstruction(ABCConstants.OP_dup);
            create.addInstruction(ABCConstants.OP_getlocal0);
            create.addInstruction(ABCConstants.OP_setproperty, new Name("moduleFactory"));
            create.labelNext(createL4);
            create.addInstruction(ABCConstants.OP_returnvalue);
        }
        create.labelNext(createL5);
        create.addInstruction(ABCConstants.OP_returnvalue);
        classGen.addITraitsMethod(new Name("create"), Collections.<Name> emptyList(), 
                new Name("Object"), Collections.emptyList(), true, true, true, create);
        
    }
    
    /**
     * Codegen the IFlexModuleFactory.info() method.
     *
     *    public override final function info() : Object
     *    {
     *        if (!_info)
     *        {
     *            _info = {
     *              currentDomain : ApplicationDomain.currentDomain
     *              .
     *              .
     *              .
     *            }
     *        }
     *    }
     * 
     * @param classGen used to create the function
     * @param compatibilityVersion compatibility version set in the compiler option.
     * May be null if not configured.
     * @param mainClassQName qName of the main class of the application. May be null
     * of library swfs.
     * @param preloaderReference reference to the configured preloader class. 
     * May be null for library swfs.
     * @param runtimeDPIProviderReference configured runtimeDPIProvider. May be null
     * for library swfs.
     * @param splashScreen reference to the configured splash screen.
     * @param rootNode root node of the application. May be null for library swfs.
     * @param compiledLocales the locales supported by this application. May be null for
     * library swfs.
     * @param rsls legacy RSLs. May be null for library swfs.
     * @param problemCollection problems found when generating the info method
     * are added to the collection.
     * @param remoteClassAliasMap 
     * @throws InterruptedException 
     */
    
    protected final void codegenInfoMethod(ClassGeneratorHelper classGen,
            Integer compatibilityVersion,
            String mainClassQName,
            IResolvedQualifiersReference preloaderReference,
            IResolvedQualifiersReference runtimeDPIProviderReference,
            FlexSplashScreenImage splashScreen,
            IASNode rootNode,
            ITargetAttributes targetAttributes,
            Collection<String> compiledLocales,
            RoyaleFrame1Info frame1Info,
            Set<String> accessibilityClassNames,
            String royaleInitClassName,
            String stylesClassName,
            List<String> rsls,
            FlexRSLInfo rslInfo,
            Collection<ICompilerProblem> problemCollection,
            boolean isAppFlexInfo,
            boolean isFlexSDKInfo, 
            Map<ClassDefinition, String> remoteClassAliasMap) 
            throws InterruptedException
    {
        IResolvedQualifiersReference applicationDomainRef = ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(),
                IASLanguageConstants.ApplicationDomain);
        IResolvedQualifiersReference infoSlotReference;
        if (isAppFlexInfo)
        {
            NamespaceDefinition.IStaticProtectedNamespaceDefinition staticNSDef = NamespaceDefinition.createStaticProtectedNamespaceDefinition("");
            infoSlotReference = ReferenceFactory.resolvedQualifierQualifiedReference(royaleProject.getWorkspace(),
                    staticNSDef, "_info");            
        }
        else
        {
            NamespaceDefinition.IPrivateNamespaceDefinition privateNSDef = NamespaceDefinition.createPrivateNamespaceDefinition("");
            infoSlotReference = ReferenceFactory.resolvedQualifierQualifiedReference(royaleProject.getWorkspace(),
                    privateNSDef, "info");
        }
        Name infoSlotName = infoSlotReference.getMName();

        InstructionList info = new InstructionList();
        info.addInstruction(ABCConstants.OP_getlocal0);
        info.addInstruction(ABCConstants.OP_getproperty, infoSlotName);
        info.addInstruction(ABCConstants.OP_dup);
        Label infoL1 = new Label();
        info.addInstruction(ABCConstants.OP_iftrue, infoL1);

        int infoEntries = 0;
        
        // currentDomain:
        info.addInstruction(ABCConstants.OP_pop);
        info.addInstruction(ABCConstants.OP_pushstring, "currentDomain");
        info.addInstruction(ABCConstants.OP_getlex, applicationDomainRef.getMName());
        info.addInstruction(ABCConstants.OP_getproperty, new Name("currentDomain"));
        infoEntries++;

        // frames:
        if (targetSettings.getFrameLabels() != null && !targetSettings.getFrameLabels().isEmpty())
        {
            Collection<FrameInfo> frames = targetSettings.getFrameLabels();
            info.addInstruction(ABCConstants.OP_pushstring, "frames");
            for (FrameInfo frame : frames)
            {
                info.addInstruction(ABCConstants.OP_pushstring, frame.getLabel());
                info.addInstruction(ABCConstants.OP_convert_s);
                info.addInstruction(ABCConstants.OP_pushstring, frame.getFrameClasses().get(0));
            }
            info.addInstruction(ABCConstants.OP_newobject, frames.size());
            infoEntries++;
        }

        // royaleVersion:
        if (compatibilityVersion != null)
        {
            info.addInstruction(ABCConstants.OP_pushstring, "royaleVersion");
            info.addInstruction(ABCConstants.OP_pushstring, compatibilityVersion);
            infoEntries++;
        }
        
        // mark this SWF as being built with Royale
        info.addInstruction(ABCConstants.OP_pushstring, "isMXMLC");
        info.addInstruction(ABCConstants.OP_pushfalse);
        infoEntries++;
        
        // mainClassName:
        if (mainClassQName != null)
        {
            info.addInstruction(ABCConstants.OP_pushstring, "mainClassName");
            info.addInstruction(ABCConstants.OP_pushstring, mainClassQName);
            infoEntries++;
        }

        if (!isAppFlexInfo && isFlexSDKInfo)
        {
            // preloader:
            if (preloaderReference != null && preloaderReference.resolve(royaleProject) != null)
            {
                info.addInstruction(ABCConstants.OP_pushstring, ATTRIBUTE_PRELOADER);
                info.addInstruction(ABCConstants.OP_getlex, preloaderReference.getMName());
                infoEntries++;            
            }
            
            // runtimeDPIProvider:
            if (runtimeDPIProviderReference != null && runtimeDPIProviderReference.resolve(royaleProject) != null)
            {
                info.addInstruction(ABCConstants.OP_pushstring, ATTRIBUTE_RUNTIME_DPI_PROVIDER);
                info.addInstruction(ABCConstants.OP_getlex, runtimeDPIProviderReference.getMName());
                infoEntries++;            
            }
            
            // splashScreenImage:
            if (splashScreen.generatedEmbedClassReference != null)
            {
                info.addInstruction(ABCConstants.OP_pushstring, ATTRIBUTE_SPLASH_SCREEN_IMAGE);
                info.addInstruction(ABCConstants.OP_getlex, splashScreen.generatedEmbedClassReference.getMName());
                infoEntries++;            
            }
            
            // Add various root node attributes:
            infoEntries += codegenRootNodeAttributes(targetAttributes, info, rootNode, problemCollection);
            
            // compiledLocales:
            if (compiledLocales != null)
            {
                info.addInstruction(ABCConstants.OP_pushstring, "compiledLocales");
                for(String locale : compiledLocales)
                    info.addInstruction(ABCConstants.OP_pushstring, locale);
                info.addInstruction(ABCConstants.OP_newarray, compiledLocales.size());
                infoEntries++;            
            }
    
            // compiledResourceBundleNames:
            if (!frame1Info.compiledResourceBundleNames.isEmpty())
            {
                info.addInstruction(ABCConstants.OP_pushstring, "compiledResourceBundleNames");
                for(String bundleName : frame1Info.compiledResourceBundleNames)
                    info.addInstruction(ABCConstants.OP_pushstring, bundleName);
                info.addInstruction(ABCConstants.OP_newarray, frame1Info.compiledResourceBundleNames.size());
                infoEntries++;            
            }
        }
        
        // styleDataClassName
        if (stylesClassName != null)
        {
            info.addInstruction(ABCConstants.OP_pushstring, "styleDataClassName");
            info.addInstruction(ABCConstants.OP_pushstring, stylesClassName);            
            infoEntries++;            
        }
        
        // mixins:
        if (royaleInitClassName != null)
        {
            info.addInstruction(ABCConstants.OP_pushstring, "mixins");
            info.addInstruction(ABCConstants.OP_pushstring, royaleInitClassName);
            int mixinEntries = 1;
            final Set<String> mixinClassNames = frame1Info.getMixins();
            for (String className : frame1Info.getMixins())
                info.addInstruction(ABCConstants.OP_pushstring, className);
                
            mixinEntries += mixinClassNames.size();
            
            info.addInstruction(ABCConstants.OP_newarray, mixinEntries);
            infoEntries++;            
        }

        if (remoteClassAliasMap != null && !remoteClassAliasMap.isEmpty())
        {
            info.addInstruction(ABCConstants.OP_pushstring, "remoteClassAliases");
            for (Map.Entry<ClassDefinition, String> classAliasEntry : remoteClassAliasMap.entrySet())
            {
                info.addInstruction(ABCConstants.OP_pushstring, classAliasEntry.getKey().getQualifiedName());
                info.addInstruction(ABCConstants.OP_convert_s);
                String value = classAliasEntry.getValue();
                info.addInstruction(ABCConstants.OP_pushstring, value);
            }
            info.addInstruction(ABCConstants.OP_newobject, remoteClassAliasMap.size());
            infoEntries++;
        }
        
        // fonts:
        if (!frame1Info.embeddedFonts.isEmpty())
        {
            info.addInstruction(ABCConstants.OP_pushstring, "fonts");
            for (Entry<String, RoyaleFontInfo> entry : frame1Info.embeddedFonts.entrySet())
            {
                info.addInstruction(ABCConstants.OP_pushstring, entry.getKey());
                info.addInstruction(ABCConstants.OP_convert_s);
                RoyaleFontInfo fontInfo = entry.getValue();
                info.addInstruction(ABCConstants.OP_pushstring, "regular");
                info.addInstruction(fontInfo.regularOp);
                info.addInstruction(ABCConstants.OP_pushstring, "bold");
                info.addInstruction(fontInfo.boldOp);
                info.addInstruction(ABCConstants.OP_pushstring, "italic");
                info.addInstruction(fontInfo.italicOp);
                info.addInstruction(ABCConstants.OP_pushstring, "boldItalic");
                info.addInstruction(fontInfo.boldItalicOp);
                info.addInstruction(ABCConstants.OP_newobject, 4);
            }
            info.addInstruction(ABCConstants.OP_newobject, frame1Info.embeddedFonts.size());
            infoEntries++;
        }

        // accessibility classes
        if (accessibilityClassNames != null && accessibilityClassNames.size() > 0)
        {
            info.addInstruction(ABCConstants.OP_pushstring, "accessibilityClassNames");
            for (String className : accessibilityClassNames)
                info.addInstruction(ABCConstants.OP_pushstring, className);

            info.addInstruction(ABCConstants.OP_newarray, accessibilityClassNames.size());
            infoEntries++;
        }
        
        // cdRSLs and placeholderRSLs:
        if (!rslInfo.requiredRSLs.isEmpty())
        {
            // Note: The Flex framework spells this info property as Rsl, not RSL.
            if (codegenRSLsEntry(info, problemCollection, "cdRsls", rslInfo.requiredRSLs))
                infoEntries++;
            
            // Note: The Flex framework spells this info property as Rsl, not RSL.
            if (codegenRSLsEntry(info, problemCollection, "placeholderRsls", rslInfo.placeholderRSLs))
                infoEntries++;
        }

        // rsls:
        if (codegenLegacyRSLs(info, rsls))
            infoEntries++;

        // Create a new info object from all of the entries.
        info.addInstruction(ABCConstants.OP_newobject, infoEntries);
        info.addInstruction(ABCConstants.OP_dup);
        info.addInstruction(ABCConstants.OP_getlocal0);
        info.addInstruction(ABCConstants.OP_swap);
        info.addInstruction(ABCConstants.OP_setproperty, infoSlotName);
        info.labelNext(infoL1);
        info.addInstruction(ABCConstants.OP_returnvalue);

        ITraitsVisitor itraitsVisitor;
        ITraitVisitor infoSlotVisitor;
        if (isAppFlexInfo)
        {
            classGen.addCTraitsMethod(new Name("info"), Collections.<Name> emptyList(), 
                new Name("Object"), Collections.emptyList(), false, info);
            itraitsVisitor = classGen.getCTraitsVisitor();
            infoSlotVisitor = itraitsVisitor.visitSlotTrait(ABCConstants.TRAIT_Var, infoSlotName,
                    ITraitsVisitor.RUNTIME_SLOT, new Name(IASLanguageConstants.Object), LexicalScope.noInitializer);
        }
        else
        {
            classGen.addITraitsMethod(new Name("info"), Collections.<Name> emptyList(), 
                    new Name("Object"), Collections.emptyList(), false, true, true, info);
            itraitsVisitor = classGen.getITraitsVisitor();
            infoSlotVisitor = itraitsVisitor.visitSlotTrait(ABCConstants.TRAIT_Var, infoSlotName,
                    ITraitsVisitor.RUNTIME_SLOT, new Name(IASLanguageConstants.Object), LexicalScope.noInitializer);
        }
        
        infoSlotVisitor.visitStart();
        infoSlotVisitor.visitEnd();
 
    }
    
    /**
     * Generate code to add root node attributes to the info object that haven't
     * already been handled.
     * 
     * @param info
     * @return number of entries added to the info object
     * @throws InterruptedException
     */
    private int codegenRootNodeAttributes(ITargetAttributes targetAttributes, InstructionList info,
            IASNode rootNode,
            Collection<ICompilerProblem> problemCollection) throws InterruptedException
    {
        // Number of attributes added to the "info" object.
        int entries = 0;

        // Emit "info" attributes that don't require special processing.
        final Map<String, String> attributes = targetAttributes.getRootInfoAttributes();
        for (final Map.Entry<String, String> e : attributes.entrySet())
        {
            info.addInstruction(ABCConstants.OP_pushstring, e.getKey());
            info.addInstruction(ABCConstants.OP_pushstring, e.getValue());
            entries++;
        }

        // Emit "info.usePreloader" as a boolean value.
        final Boolean usePreloader = targetAttributes.getUsePreloader();
        if (usePreloader != null)
        {
            info.addInstruction(ABCConstants.OP_pushstring, ATTRIBUTE_USE_PRELOADER);
            info.addInstruction(usePreloader ? ABCConstants.OP_pushtrue : ABCConstants.OP_pushfalse);
            entries++;
        }

        // Emit "info.backgroundColor" as a hex string value.
        final String backgroundColorString = targetAttributes.getBackgroundColor();
        if (backgroundColorString != null)
        {
            try
            {
                final int backgroundColor = royaleProject.getColorAsInt(backgroundColorString);
                final String hexString = "0x" + Integer.toHexString(backgroundColor).toUpperCase();
                info.addInstruction(ABCConstants.OP_pushstring, ATTRIBUTE_BACKGROUND_COLOR);
                info.addInstruction(ABCConstants.OP_pushstring, hexString);
                entries++;
            }
            catch (NumberFormatException numberFormatExpression)
            {
                problemCollection.add(new InvalidBackgroundColorProblem(
                        rootNode.getFileSpecification().getPath(),
                        backgroundColorString));
            }
        }

        return entries;
    }
    
    /**
     * Generate either the "cdRsls" or "placeholderRsls" entry in the 
     * IFlexModuleFactory info function.
     * 
     * @param info
     * @param problemCollection
     * @param entryLabel the "info" entry label
     * @param rslSettingsList the rsls for the entry 
     * 
     * @return true if instructions were added for the RSLs, false otherwise.
     */
    private boolean codegenRSLsEntry(InstructionList info, Collection<ICompilerProblem> problemCollection,
            String entryLabel,
            List<RSLSettings> rslSettingsList)
    {
        ISWCManager swcManager = royaleProject.getWorkspace().getSWCManager();
        
        info.addInstruction(ABCConstants.OP_pushstring, entryLabel);
        
        IResolvedQualifiersReference mxCoreRSLDataReference = 
            ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(), IMXMLTypeConstants.RSLData);
        Name mxCoreRSLDataSlotName = mxCoreRSLDataReference.getMName();
        Object[] mxCoreRSLDataCtor = new Object[] { mxCoreRSLDataSlotName, 7 };
        
        // Add an RSLData instance to an array for the primary RSL in RSLSettings
        // plus one for every failover RSL.
        for (RSLSettings rslSettings : rslSettingsList)
        {
            int rslCount = 0;
            for (RSLAndPolicyFileURLPair urls : rslSettings.getRSLURLs())
            {
                info.addInstruction(ABCConstants.OP_findpropstrict, mxCoreRSLDataSlotName);                
                info.addInstruction(ABCConstants.OP_pushstring, urls.getRSLURL());
                info.addInstruction(ABCConstants.OP_pushstring, urls.getPolicyFileURL());
                
                ISWC swc = swcManager.get(new File(rslSettings.getLibraryFile().getPath()));
                
                boolean isSignedRSL = RSLSettings.isSignedRSL(urls.getRSLURL()); 
                ISWCDigest swcDigest = getSWCDigest(Iterables.getFirst(swc.getLibraries(), null), 
                        isSignedRSL);

                if (swcDigest == null)
                {
                    if (isSignedRSL)
                        problemCollection.add(new MissingSignedDigestProblem(swc.getSWCFile().getAbsolutePath()));
                    else
                        problemCollection.add(new MissingUnsignedDigestProblem(swc.getSWCFile().getAbsolutePath()));
                    
                    continue;
                }
                
                info.addInstruction(ABCConstants.OP_pushstring, swcDigest.getValue());
                info.addInstruction(ABCConstants.OP_pushstring, swcDigest.getType());
                info.addInstruction(isSignedRSL ? ABCConstants.OP_pushtrue : ABCConstants.OP_pushfalse);
                info.addInstruction(rslSettings.getVerifyDigest() ? ABCConstants.OP_pushtrue : ABCConstants.OP_pushfalse);
                info.addInstruction(ABCConstants.OP_pushstring, 
                        rslSettings.getApplicationDomain().getApplicationDomainValue());
                
                info.addInstruction(ABCConstants.OP_constructprop, mxCoreRSLDataCtor);
                rslCount++;
            }

            info.addInstruction(ABCConstants.OP_newarray, rslCount);
        }
        
        info.addInstruction(ABCConstants.OP_newarray, rslSettingsList.size());
        
        return true;
    }
    
    /**
     * Get either a signed or unsigned digest from the library.
     * 
     * @param swcLibrary
     * @param signedDigest if true return a signed digest, otherwise an unsigned digest.
     * @return A signed or unsigned digest. Null if the requested digest is not
     * available.
     */
    private ISWCDigest getSWCDigest(ISWCLibrary swcLibrary, boolean signedDigest)
    {
        for (ISWCDigest swcDigest : swcLibrary.getDigests())
        {
            if (swcDigest.isSigned() == signedDigest)
                return swcDigest;
        }
        
        return null;
    }
    
    /**
     * rsls: [{url: "rsl1.swf", size: -1}, {url: "rsl2.swf", size: -1}, {url: "rsl3.swf", size: -1}]
     * 
     * @param info
     * @return
     */
    private boolean codegenLegacyRSLs(InstructionList info, List<String> rsls)
    {
        if (rsls != null && rsls.size() > 0)
        {
            info.addInstruction(ABCConstants.OP_pushstring, "rsls");
            for (String rsl : rsls)
            {
                info.addInstruction(ABCConstants.OP_pushstring, "url");
                info.addInstruction(ABCConstants.OP_pushstring, rsl);
                info.addInstruction(ABCConstants.OP_pushstring, "size");
                info.addInstruction(ABCConstants.OP_pushbyte, -1);
                info.addInstruction(ABCConstants.OP_newobject, 2);
            }

            info.addInstruction(ABCConstants.OP_newarray, rsls.size());
        }
        
        return rsls != null && rsls.size() > 0;
    }
    
    
    
    static final Target.DirectDependencies NO_DEPENDENCIES =
        new Target.DirectDependencies(Collections.<ICompilationUnit>emptyList(), Collections.<ICompilerProblem>emptyList());
    
    /**
     * Find the accessible compilation units on a set of compilation units. The
     * accessible compilation units are gathered from the
     * {@code [AccessiblityClass]} meta-data on a class.
     * <p>
     * Unfortunately this method has a side effect, it updates the set of
     * accessibility classes.
     * 
     * @param compilationUnit A compilation unit.
     * @return A {@link DirectDependencies} object with the set of compilation
     * units representing the new compilation units needed for accessibility. If
     * no accessible compilation units are found or accessibility is not enabled
     * on the target, then the returned {@link DirectDependencies} object will
     * contain an empty set.
     */
    public Target.DirectDependencies getAccessibilityDependencies(ICompilationUnit compilationUnit) throws InterruptedException
    {
        assert targetSettings.isAccessible() : "This method should only be called if accessibility is enabled!";
        Set<ICompilationUnit> accessibleCompilationUnits = new HashSet<ICompilationUnit>();
        IFileScopeRequestResult result = compilationUnit.getFileScopeRequest().get();

        for(IDefinition def : result.getExternallyVisibleDefinitions()) 
        {
            IMetaTag md = def.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ACCESSIBIlITY_CLASS);
            if (md == null)
                continue;
            
            String accessibilityClass = md.getAttributeValue(IMetaAttributeConstants.NAME_ACCESSIBILITY_IMPLEMENTATION);
            if (accessibilityClass == null)
                continue;
            
            IResolvedQualifiersReference ref = ReferenceFactory.packageQualifiedReference(royaleProject.getWorkspace(),
                    accessibilityClass);
            assert ref != null;

            // Collect a list of classes to add to the info structure.
            accessibleClassNames.add(accessibilityClass);
            
            IDefinition accessibilityClassDefinition = ref.resolve(royaleProject);
            if ((accessibilityClassDefinition != null) && (!accessibilityClassDefinition.isImplicit()))
            {
                ICompilationUnit cu = royaleProject.getScope().getCompilationUnitForDefinition(accessibilityClassDefinition);
                assert cu != null : "Unable to find compilation unit for definition!";
                accessibleCompilationUnits.add(cu);
            }
        }
        
        return new Target.DirectDependencies(accessibleCompilationUnits, Collections.<ICompilerProblem>emptyList());
    }
    
    /**
     * Update the SWF model by adding a ProductInfoTag.
     * 
     * @param swf the swf model to update.
     */
    public void addProductInfoToSWF(ISWF swf)
    {
    	long compileDate = new Date().getTime();
    	String rdfDate = targetSettings.getSWFMetadataDate();
    	String rdfDateFormat = targetSettings.getSWFMetadataDateFormat();
    	if (rdfDate != null && rdfDateFormat != null)
    	{
    		try {
    			SimpleDateFormat sdf = new SimpleDateFormat(rdfDateFormat);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                compileDate = sdf.parse(rdfDate).getTime();
    		} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			}
    	}
        // Add product info to the swf.
        ProductInfoTag productInfo = new ProductInfoTag(Product.ROYALE,
                Edition.NONE,
                (byte)Integer.parseInt(VersionInfo.FLEX_MAJOR_VERSION),
                (byte)Integer.parseInt(VersionInfo.FLEX_MINOR_VERSION),
                VersionInfo.getBuildLong(),
                compileDate);
        swf.setProductInfo(productInfo);
    }
}
