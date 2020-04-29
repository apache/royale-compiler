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

package org.apache.royale.compiler.internal.parsing.mxml;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.ATTRIBUTE_NAME;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.PackageDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.IncludeHandler;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLNodeBase;
import org.apache.royale.compiler.internal.units.MXMLCompilationUnit;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLNamespaceAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLTextData.TextType;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLLibraryTagNotTheFirstChildProblem;
import org.apache.royale.compiler.problems.MXMLUnresolvedTagProblem;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * This class analyzes the tags and attributes of an MXML file, as represented
 * in an MXMLData object, and builds an ASFileScope which is the
 * externally-visible API of the MXML file.
 * <p>
 * This class is conceptually similar to ABCScopeBuilder, which builds one or
 * more ASFileScopes from an ABC block.
 */
public class MXMLScopeBuilder
{
    // RegEx for splitting implements="a.b.c , d.e.f"
    private static final String IMPLEMENTS_SPLITTER = ",";

    public MXMLScopeBuilder(MXMLCompilationUnit compilationUnit, IFileSpecificationGetter fileSpecGetter, IMXMLData mxmlData, String qname, String fileName)
    {
        this.project = compilationUnit.getProject();

        this.compilationUnit = compilationUnit;

        this.mxmlData = mxmlData;
        
        this.qname = qname;

        fileScope = new MXMLFileScope(compilationUnit, fileName, mxmlData);
        String packageName = Multiname.getPackageNameForQName(qname);
        packageScope = new PackageScope(fileScope, packageName);
        packageScope.setContainingScope(fileScope);
        PackageDefinition packageDefinition = new PackageDefinition(packageName);
        // CMP-742: packages created for MXML scopes should be marked implicit
        packageDefinition.setImplicit();
        packageDefinition.setContainedScope(packageScope);
        fileScope.addDefinition(packageDefinition);
        
        problems = new LinkedList<ICompilerProblem>();

        includeHandler = new IncludeHandler(fileSpecGetter);
        includeHandler.setProjectAndCompilationUnit(project, compilationUnit);

        this.fileSpecGetter = fileSpecGetter;
        assert fileSpecGetter != null;
    }

    // The MXML for which we're building a file scope.
    private IMXMLData mxmlData;

    // Each project knows how to resolve MXML tags to ActionScript classes.
    // Different projects can resolve the same tag to different classes.
    private RoyaleProject project;
    
    // The MXMLCompilationUnit for which we're building a file scope.
    private MXMLCompilationUnit compilationUnit;

    // The fully-qualified name of the MXML class.
    private String qname;

    // The file scope that we're building.
    private MXMLFileScope fileScope;
    
    // The package scope inside the file scope that contains the main class definition.
    private PackageScope packageScope;

    // Initially, this is the class definition for the main MXML class, inside the package scope.
    // As we process an <fx:Component> or <fx:Definition> tag, this changes to be the class
    // definition for the component or definition class.
    private ClassDefinition currentClassDefinition;

    // Initially, this is the class scope for the main MXML class, inside the package scope.
    // As we process an <fx:Component> or <fx:Definition> tag, this changes to be the class
    // scope for the component or definition class.
    private TypeScope currentClassScope;

    // Problems encountered while building the file scope.
    private Collection<ICompilerProblem> problems;
        
    private final IncludeHandler includeHandler;
    
    private final IFileSpecificationGetter fileSpecGetter;
    
    /**
     * Builds the file scope for the MXML file.
     * 
     * @return An {@code ASFileScope} representing the externally-visible
     * API of the MXML file.
     */
    public MXMLFileScope build()
    {
        includeHandler.enterFile(mxmlData.getPath());
        
        IMXMLTagData rootTag = mxmlData.getRootTag();
        if (rootTag == null)
            return fileScope;

        processRootTag(rootTag);

        ImmutableSet<String> includedFiles = includeHandler.getIncludedFiles();
        for (String includedFile : includedFiles)
            fileScope.addSourceDependency(includedFile);

        includeHandler.leaveFile();

        return fileScope;
    }

    private void processRootTag(IMXMLTagData rootTag)
    {
        IReference baseClass = fileScope.resolveTagToReference(rootTag);

        String implementsAttrValue = rootTag.getRawAttributeValue("implements");
        IReference[] implementedInterfaces = null;
        if (implementsAttrValue != null) //TODO this should use a parser method that collects qnames or identifiers
        {
            String interfaces[] = rootTag.getMXMLDialect().trim(implementsAttrValue).split(IMPLEMENTS_SPLITTER);
            implementedInterfaces = new IReference[interfaces.length];
            for( int i = 0; i < interfaces.length; ++i )
            {
                implementedInterfaces[i] = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), interfaces[i].trim());
            }
        }

        Multiname mname = Multiname.crackDottedQName(this.project, qname);
        INamespaceDefinition packageNS = Iterables.getOnlyElement(mname.getNamespaceSet());
        assert packageNS instanceof INamespaceReference;
        currentClassDefinition = new ClassDefinition(mname.getBaseName(), (INamespaceReference)packageNS);
        currentClassDefinition.setLocation(rootTag);
        currentClassDefinition.setBaseClassReference(baseClass);
        currentClassDefinition.setImplementedInterfaceReferences(implementedInterfaces);
        currentClassDefinition.setMetaTags(new IMetaTag[0]);
        // Grr... CM clients expect the name start of the root class definition to be at 0.
        currentClassDefinition.setNameLocation(0, 0);
        packageScope.addDefinition(currentClassDefinition);
        fileScope.setMainClassDefinition(currentClassDefinition);

        currentClassScope = new TypeScope(packageScope, currentClassDefinition);
        currentClassScope.setContainingDefinition(currentClassDefinition);

        if (baseClass instanceof IResolvedQualifiersReference)
        {
            IDefinition baseDef = ((IResolvedQualifiersReference)baseClass).resolve(project);
            if (baseDef == null)
                problems.add(new MXMLUnresolvedTagProblem(rootTag));
            else
                currentClassScope.addImport(baseDef.getQualifiedName());           
        }

        currentClassDefinition.setContainedScope(currentClassScope);
        currentClassDefinition.setupThisAndSuper();
        
        // An <fx:Library> tag can only be the first child tag of the root tag.
        IMXMLTagData child = rootTag.getFirstChild(true);
        if (child != null && fileScope.isLibraryTag(child))
        {
            processLibraryTag(child);
            child = child.getNextSibling(true);    
        }
        
        // Process the subsequent child tags after the first.
        for (;
             child != null;
             child = child.getNextSibling(true))
        {
            processTag(child);
        }

        currentClassDefinition.buildContingentDefinitions();
    }

    /**
     * Builds {@link ClassDefinition}'s for &lt;fx:Definition&gt; tags
     * found in the specified &lt:fx:Library&gt; tag.
     * @param libraryTagData
     */
    private void processLibraryTag(IMXMLTagData libraryTag)
    {
        assert fileScope.isLibraryTag(libraryTag);
        
        for (IMXMLTagData child = libraryTag.getFirstChild(true);
             child != null;
             child = child.getNextSibling(true))
        {
            if (fileScope.isDefinitionTag(child))
                processDefinitionTag(child);
            
            // TODO create problems when library has children that are not
            // definition tags.
        }       
    }
    
    /**
     * Builds a {@link ClassDefinition} for the specified &lt;fx:Definition&gt; tag.
     * @param definitionTag
     */
    private void processDefinitionTag(IMXMLTagData definitionTag)
    {
        assert fileScope.isDefinitionTag(definitionTag);
        
        // An <fx:Definition> tag will create a new class.
        // Save off the current class definition and scope
        // because we're going to change them before recursing down.
        ClassDefinition oldClassDefinition = currentClassDefinition;
        TypeScope oldClassScope = currentClassScope;
        
        // Walk the attributes looking for 'name'.
        IMXMLTagAttributeData[] definitionTagAttrs = definitionTag.getAttributeDatas();
        String definitionName = null;
        int nameStart = -1;
        int nameEnd = -1;
        for (IMXMLTagAttributeData attr : definitionTagAttrs)
        {
            if ((!(attr instanceof IMXMLNamespaceAttributeData)) && (attr.hasValue()))
            {
               if ((attr.getURI() == null) && (ATTRIBUTE_NAME.equals(attr.getName())))
               {
                   if (definitionName == null)
                   {
                       definitionName = attr.getRawValue();
                       nameStart = attr.getValueStart() + 1;
                       nameEnd = attr.getValueEnd() - 1;
                   }
                   // TODO create problem if definition name has already been set.
               }
            }
        }
        
        // We expect one child tag inside <fx:Definition>.
        IMXMLTagData firstChild = definitionTag.getFirstChild(true);
        if (firstChild != null)
        {
            // TODO create problem if there is more than one child tag
            // in a definition.
            IMXMLTagData secondChild = firstChild.getNextSibling(true);
            if (secondChild != null)
                return;
            
            // This child tag specifies the base class of the definition class.
            String baseClassQName = fileScope.resolveTagToQualifiedName(firstChild);
            // TODO create problem if we can't resolve the base class name.
            if (baseClassQName == null)
                return;
            
            // Add a class definition for the definition class to the file scope.
            // The file scope will handle resolving a tag like <fx:MyDefinition>
            // to the class for <fx:Definition name="MyDefinition">.
            ClassDefinition fxDefinitionClassDefinition =
                fileScope.addFXDefinition(qname, definitionTag, definitionName, baseClassQName);
            fxDefinitionClassDefinition.setLocation(firstChild);
            fxDefinitionClassDefinition.setNameLocation(nameStart, nameEnd);
            
            // Set up the "context" for building definitions inside the definition class.
            currentClassDefinition = fxDefinitionClassDefinition;
            currentClassScope = (TypeScope)fxDefinitionClassDefinition.getContainedScope();
            
            // add the definitions inside the definition tag to the definition class scope.
            processTag(firstChild);
        }
        
        // Restore the previous class definition and scope.
        currentClassDefinition = oldClassDefinition;
        currentClassScope = oldClassScope;
    }
    
    private void processTag(IMXMLTagData tag)
    {
        includeHandler.onNextMXMLUnitData((IMXMLUnitData)tag);
        
        boolean recurse = true;
                
        if (fileScope.isScriptTag(tag))
        {
            processScriptTag(tag);
        }
        else if (fileScope.isStyleTag(tag))
        {
            processStyleTag(tag);
        }
        else if (fileScope.isMetadataTag(tag))
        {
            processMetadataTag(tag);
        }
        else if (fileScope.isComponentTag(tag))
        {
            processComponentTag(tag);
            // we've already processed the tags
            recurse = false;
        }
        else if (fileScope.isLibraryTag(tag))
        {
            // log a problem but continue processing the tag
            // so that a valid tree is formed
            MXMLLibraryTagNotTheFirstChildProblem problem = new MXMLLibraryTagNotTheFirstChildProblem(tag);
            problems.add(problem);
            processLibraryTag(tag);
            recurse = false;
        }
        else if (fileScope.isXMLTag(tag))
        {
            processXMLTag(tag);
            
            // Skip over this tag. Nothing inside it contributes to the externally-visible API.
            recurse = false;
        }
        else if (fileScope.isPrivateTag(tag) ||
                 fileScope.isXMLListTag(tag))
        {
           // Skip over these tags. Nothing inside them contributes to the externally-visible API.
           recurse = false;
        }
        else if (fileScope.isModelTag(tag))
        {
            processModelTag(tag);
            
            // Skip over this tag. Nothing inside it contributes to the externally-visible API.
            recurse = false;
        }
        else if (fileScope.isStringTag(tag))
        {
            processStringTag(tag);
        }
        
        // If the tag can be resolved to a qualified name,
        // import that name into the MXML class being defined.
        // This is how the old InterfaceCompiler worked,
        // but not how the old ImplementationCompiler worked.
        // There may be pathological cases in which the tag
        // actually turns out to be a property/style/event tag
        // once the MXML AST has been built, but in these cases
        // the spurious import will simply cause an ambiguous
        // identifier problem in Royale whereas the old compiler
        // would have compiled the code without error.
        String qname = fileScope.resolveTagToQualifiedName(tag);
        if (qname != null)
        {
            currentClassScope.addImport(qname);
            
            // CodeModel needs nodes for such implicit imports.
            // Keep track of the the implicit imports
            // on the ClassDefinition we're creating,
            // where MXMLClassDefinitionNode can later create nodes for them.
            currentClassDefinition.addImplicitImport(qname);

            // if the tag couldn't be resolved to a qname, assume it it's not
            // a state, as we can't check it it resolved to mx.states.State.
            if (IMXMLLanguageConstants.STATE.equals(tag.getShortName()))
                processState(tag, qname);
        }

        IMXMLTagAttributeData idAttribute = tag.getTagAttributeData("id");
        String id = tag.getRawAttributeValue("id");
        if (id != null)
            processID(tag, idAttribute);
        else
        {
            idAttribute = tag.getTagAttributeData("localId");
            id = tag.getRawAttributeValue("localId");
            if (id != null)
                processID(tag, idAttribute);        	
        }

        if (recurse)
        {
            for (IMXMLTagData child = tag.getFirstChild(true);
                 child != null;
                 child = child.getNextSibling(true))
            {
                processTag(child);
            }
        }
    }

    /**
     * Build scopes and definitions from a script tag.
     * 
     * @param scriptTag script tag
     * @see {@link MXMLScriptNode} for AST building.
     */
    private void processScriptTag(IMXMLTagData scriptTag)
    {
        assert fileScope.isScriptTag(scriptTag);
        
        IMXMLTagAttributeData sourceAttribute = scriptTag.getTagAttributeData(IMXMLLanguageConstants.ATTRIBUTE_SOURCE);
        if (sourceAttribute != null)
        {
            // script tag with source attribute
            String sourcePath = MXMLNodeBase.resolveSourceAttributePath(null, sourceAttribute, null);
            Reader sourceFileReader = null;
            try
            {
                Workspace workspace = project.getWorkspace();
                
                IFileSpecification sourceFileSpec = fileSpecGetter.getFileSpecification(sourcePath);
                sourceFileReader = sourceFileSpec.createReader();
                //TODO what do we do about errors that are encountered in the attached file?
                //is this even the correct approach?
                String scriptText = IOUtils.toString(sourceFileReader);
                
                // no need to pass in start offset because include handler will take care of it
                ASParser.parseFragment2(
                        scriptText,
                        sourcePath,
                        0,  
                        0,
                        0,
                        problems,
                        workspace,
                        null,
                        currentClassScope,
                        project.getProjectConfigVariables(),
                        EnumSet.of(PostProcessStep.CALCULATE_OFFSETS, PostProcessStep.POPULATE_SCOPE),
                        true,
                        includeHandler);

                fileScope.addSourceDependency(sourcePath);
            }
            catch (FileNotFoundException e)
            {
                // The error reporting will be handled during tree-building time, but the incremental
                // flow needs to have a dependency on the compilation unit during scope building time,
                // as builder may not necessarily ask for the syntax tree and the dependency get's
                // missed. CMP-2040
                project.addUnfoundReferencedSourceFileDependency(sourcePath, compilationUnit);
            }
            catch (IOException e)
            {
                // File-not-found will be reported by MXMLScriptNode during tree-building.
            }
            finally
            {
                if (sourceFileReader != null)
                    try { sourceFileReader.close(); }
                    catch (IOException e) { }
            }
        }
        else
        {
            final List<ScopedBlockNode> nodes = new ArrayList<ScopedBlockNode>(2);
            for (IMXMLUnitData unit = scriptTag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
            {
                if (unit instanceof IMXMLTextData)
                {
                    final IMXMLTextData mxmlTextData = (IMXMLTextData)unit;
                    if (mxmlTextData.getTextType() != TextType.WHITESPACE)
                    {
                        final EnumSet<PostProcessStep> postProcess = EnumSet.of(
                                PostProcessStep.CALCULATE_OFFSETS, 
                                PostProcessStep.POPULATE_SCOPE);
                        final ScopedBlockNode node = ASParser.parseInlineScript(
                                null,
                                mxmlTextData,
                                problems,
                                currentClassScope,
                                project.getProjectConfigVariables(),
                                includeHandler,
                                postProcess);
                        assert node != null : "Expected node from ASParser.getScopesFromInlineScript().";
                        nodes.add(node);
                    }
                }
            }
        }
    }
    
    private void processStyleTag(IMXMLTagData styleTag)
    {
        assert fileScope.isStyleTag(styleTag);
        
        processSourceAttribute(styleTag);
    }
    
    private void processMetadataTag(IMXMLTagData metadataTag)
    {
        assert fileScope.isMetadataTag(metadataTag);
        
        for (IMXMLUnitData unit = metadataTag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit instanceof IMXMLTextData)
            {
                final IMXMLTextData mxmlTextData = (IMXMLTextData)unit;
                if (mxmlTextData.getTextType() != TextType.WHITESPACE)
                {
                    MetaTagsNode metaTagNodes = ASParser.parseMetadata(project.getWorkspace(), mxmlTextData.getCompilableText(),
                            mxmlTextData.getSourcePath(),
                            mxmlTextData.getCompilableTextStart(),
                            mxmlTextData.getCompilableTextLine(),
                            mxmlTextData.getCompilableTextColumn(), problems);
                    if (metaTagNodes != null)
                    {
                    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
                    		System.out.println("MXMLScopeBuilder waiting for lock in processMetadataTag");
                        IFileSpecification containingFileSpec = fileScope.getWorkspace().getFileSpecification(fileScope.getContainingPath());
                    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
                    		System.out.println("MXMLScopeBuilder done with lock in processMetadataTag");
                        IMetaTag[] newMetaTags = metaTagNodes.buildMetaTags(containingFileSpec, currentClassDefinition);
                        if( newMetaTags != null )
                        {
                            IMetaTag[] oldMetaTags = currentClassDefinition.getAllMetaTags();

                            IMetaTag[] mergedTags = newMetaTags;
                            if( oldMetaTags != null )
                            {
                                // Merge the new metadata and the already existent metadata - there can be multiple
                                // <fx:Metadata> tags in the source
                                mergedTags = new IMetaTag [oldMetaTags.length + newMetaTags.length];
                                System.arraycopy(oldMetaTags, 0, mergedTags, 0, oldMetaTags.length);
                                System.arraycopy(newMetaTags, 0, mergedTags, oldMetaTags.length, newMetaTags.length);
                            }
                            currentClassDefinition.setMetaTags(mergedTags);
                        }
                        else
                        {
                            // nothing to do, no additional metadata was specified
                        }
                    }
                }
            }
        }
    }

    private void processComponentTag(IMXMLTagData componentTag)
    {
        assert fileScope.isComponentTag(componentTag);
        
        // An <fx:Definition> tag will create a new class.
        // Save off the current class definition and scope
        // because we're going to change them before recursing down.
        ClassDefinition oldClassDefinition = currentClassDefinition;
        TypeScope oldClassScope = currentClassScope;
        
        // Walk the attributes looking for 'className'.
        IMXMLTagAttributeData[] definitionTagAttrs = componentTag.getAttributeDatas();
        String className = null;
        int nameStart = -1;
        int nameEnd = -1;
        for (IMXMLTagAttributeData attr : definitionTagAttrs)
        {
            if ((!(attr instanceof IMXMLNamespaceAttributeData)) && (attr.hasValue()))
            {
               if ((attr.getURI() == null) && (attr.getName().equals("className")))
               {
                   if (className == null)
                   {
                       className = attr.getRawValue();
                       nameStart = attr.getValueStart() + 1;
                       nameEnd = attr.getValueEnd() - 1;
                   }
                   // TODO create problem if className has already been set.
               }
            }
        }
                
        // We expect one child tag inside <fx:Component>.
        IMXMLTagData firstChild = componentTag.getFirstChild(true);
        if (firstChild != null)
        {
            // TODO create problem if there is more than one child tag
            // in a definition.
            IMXMLTagData secondChild = firstChild.getNextSibling(true);
            if (secondChild != null)
                return;
            
            // This child tag specifies the base class of the component class.
            String baseClassQName = fileScope.resolveTagToQualifiedName(firstChild);
            // TODO create problem if we can't resolve the base class name.
            if (baseClassQName == null)
                return;
            
            // Add a class definition for the component class to the file scope.
            ClassDefinition fxComponentClassDefinition =
                fileScope.addFXComponent(qname, componentTag.getAbsoluteStart(), className, baseClassQName);
            fxComponentClassDefinition.setNameLocation(nameStart, nameEnd);
            
            if (className == null)
                fxComponentClassDefinition.setExcludedClass();
            
            // Set up the "context" for building definitions inside the definition class.
            currentClassDefinition = fxComponentClassDefinition;
            currentClassScope = (TypeScope)fxComponentClassDefinition.getContainedScope();

            currentClassDefinition.buildOuterDocumentMember(ReferenceFactory.resolvedReference(oldClassDefinition));

            // add the definitions inside the component tag to the component class scope.
            processTag(firstChild);
        } 
        
        // Restore the previous class definition and scope.
        currentClassDefinition = oldClassDefinition;
        currentClassScope = oldClassScope;
    }

    private void processState(IMXMLTagData tag, String qname)
    {
        if (!qname.equals(project.getStateClass()) || tag.getMXMLDialect() == MXMLDialect.MXML_2006)
            return;

        // if there is no name attribute, ignore it as a state, as name is
        // a required attribute
        IMXMLTagAttributeData nameAttribute = tag.getTagAttributeData(IMXMLLanguageConstants.ATTRIBUTE_NAME);
        if (nameAttribute == null)
            return;

        // TODO: need to handle evaluations of entities
        String stateName = nameAttribute.getRawValue();
        currentClassDefinition.addStateName(stateName);
    }

    private void processXMLTag(IMXMLTagData xmlTag)
    {
        assert fileScope.isXMLTag(xmlTag);
        
        processSourceAttribute(xmlTag);
    }
    
    private void processModelTag(IMXMLTagData modelTag)
    {
        assert fileScope.isModelTag(modelTag);
        
        processSourceAttribute(modelTag);
    }
    
    private void processStringTag(IMXMLTagData stringTag)
    {
        assert fileScope.isStringTag(stringTag);
        
        processSourceAttribute(stringTag);
    }
    
    /**
     * If the tag has a <code>source</code> attribute,
     * determine whether the source file exists or not.
     * If so, call <code>addSourceDependency()</code>
     * on the <code>MXMLFileScope</code>.
     * If not, call <code>addUnfoundReferenceSourceFileDependency()</code>
     * on the <code>RoyaleProject</code>.
     */
    private void processSourceAttribute(IMXMLTagData tag)
    {
        IMXMLTagAttributeData sourceAttribute = tag.getTagAttributeData(IMXMLLanguageConstants.ATTRIBUTE_SOURCE);
        if (sourceAttribute != null)
        {
            String sourcePath = MXMLNodeBase.resolveSourceAttributePath(null, sourceAttribute, null);
            Reader sourceFileReader = null;
            try
            {
                IFileSpecification sourceFileSpec = fileSpecGetter.getFileSpecification(sourcePath);
                sourceFileReader = sourceFileSpec.createReader();

                fileScope.addSourceDependency(sourcePath);
            }
            catch (FileNotFoundException e)
            {
                // The error reporting will be handled during tree-building time, but the incremental
                // flow needs to have a dependency on the compilation unit during scope building time,
                // as builder may not necessarily ask for the syntax tree and the dependency get's
                // missed. CMP-2040
                project.addUnfoundReferencedSourceFileDependency(sourcePath, compilationUnit);
            }
            finally
            {
                if (sourceFileReader != null)
                {
                    try
                    {
                        sourceFileReader.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
    }

    private void processID(IMXMLTagData tag, IMXMLTagAttributeData idAttribute)
    {
        String id = idAttribute.getRawValue();
        IReference typeRef = fileScope.resolveTagToReference(tag);
        
        // TODO: put in the code below so that instances of the model tag
        // would have a resolvable class (ObjectProxy). We might want to do this a different way.
        // TODO: Also, we need some treatment of the component tag. That case is different,
        // however, in that is already has a class - we just need to add ClassFactory as a base class
        // (I think).
        if (typeRef == null)
        {
            if (fileScope.isModelTag(tag))
            {
                typeRef = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), "mx.utils.ObjectProxy");
            }
        }
        

        // If we see a tag like <s:Button id="b">, add a VariableDefinition
        // to the class scope, corresponding to the ActionScript code
        //    [Bindable]
        //    public var b:spark.components:Button;
        VariableDefinition variableDefinition = new VariableDefinition(id);
        variableDefinition.setPublic();
        variableDefinition.setTypeReference(typeRef);
        variableDefinition.setNameLocation(idAttribute.getValueStart(), idAttribute.getValueEnd());
        variableDefinition.setBindable();
        variableDefinition.setMxmlDeclared();
        currentClassScope.addDefinition(variableDefinition);
    }
    
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }
    
    public TypeScope getClassScope()
    {
        return currentClassScope;
    }
    
    /**
     * @return the includeHandler
     */
    public IncludeHandler getIncludeHandler()
    {
        return includeHandler;
    }
}
