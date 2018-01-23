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

package org.apache.royale.compiler.internal.units;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.problems.UnresolvedClassReferenceProblem;
import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.embedding.IEmbedData;
import org.apache.royale.compiler.internal.abc.ClassGeneratorHelper;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.PackageDefinition;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.resourcebundles.PropertiesFileParser;
import org.apache.royale.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.as.ClassReferenceNode;
import org.apache.royale.compiler.internal.tree.as.EmbedNode;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.properties.ResourceBundleEntryNode;
import org.apache.royale.compiler.internal.tree.properties.ResourceBundleFileNode;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.internal.units.requests.FileScopeRequestResultBase;
import org.apache.royale.compiler.internal.units.requests.SWFTagsRequestResult;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.problems.CodegenInternalProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * This is a compilation unit that handles .properties file compilation.
 */
public class ResourceBundleCompilationUnit extends CompilationUnitBase
{
    /**
     * Parent folder name for properties files in a swc. We read properties
     * files off of this folder while reading a swc and copy them into this
     * folder while writing a swc.
     */
    public static final String LOCALE = "locale";

    /**
     * Qualified bundle name for the properties file processed 
     * by this compilation unit.
     */
    private final String bundleNameInColonSyntax;

    /**
     * Locale of the properties file processed by this compilation unit or
     * <code>null</code> if the file is not locale dependent.
     */
    private final String locale;

    /**
     * SWC entry for the properties file processed by this compilation unit if
     * it comes from a swc, <code>null</code> if it doesn't come from a SWC.
     */
    private final ISWCFileEntry fileEntry;

    /**
     * Constructor.
     * 
     * @param project project this compilation unit is associated with
     * @param path path of the properties file
     * @param basePriority base priority
     * @param qname qualified name for the properties file that will be
     * processed by this comp unit
     * @param locale the locale this compilation unit depends on or <code>null</code>
     * if the compilation unit is not locale dependent
     */
    public ResourceBundleCompilationUnit(final CompilerProject project,
                                         final String path,
                                         final DefinitionPriority.BasePriority basePriority,
                                         final String qname, 
                                         final String locale)
    {
        super(project, path, basePriority,
                getQnames(project, qname, locale));
        this.locale = locale;
        this.bundleNameInColonSyntax = ResourceBundleUtils.convertBundleNameToColonSyntax(qname);
        this.fileEntry = null;
    }

    /**
     * Constructor. This constructor should be used for properties files that
     * come from a swc.
     * 
     * @param project project this compilation unit is associated with
     * @param fileEntry swc entry for the properties file that will be processed
     * by this compilation unit
     * @param qname qualified name for the properties file that will be
     * processed by this comp unit
     * @param locale the locale this compilation unit depends on or <code>null</code>
     * if the compilation unit is not locale dependent
     */
    public ResourceBundleCompilationUnit(final CompilerProject project,
                                         final ISWCFileEntry fileEntry,
                                         final String qname, 
                                         final String locale)
    {
        super(project, fileEntry.getContainingSWCPath(), DefinitionPriority.BasePriority.LIBRARY_PATH,
                getQnames(project, qname, locale));
        this.bundleNameInColonSyntax =  ResourceBundleUtils.convertBundleNameToColonSyntax(qname);
        this.fileEntry = fileEntry;
        this.locale = locale;
    }
    
    /**
     * utility to get the qnames promised by this compilation unit

     */
    private static Collection<String> getQnames(CompilerProject project, String qname, String locale)
    {
        //if this comp unit is not locale dependent, then create qnames  for each locale project targets
        Collection<String> locales = (locale == null) ? ((RoyaleProject)project).getLocales() : Collections.<String>singleton(locale);
        
        
        // For each local we are using, add the qnames promised for that locale
        ArrayList<String> qnames = new ArrayList<String>();
        for(String loc : locales)
        {
            String qualifiedName = ResourceBundleUtils.getQualifiedName(loc, qname); //determine qualified name
            qnames.add(qualifiedName);
        }
        return qnames;
    }

    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.RESOURCE_UNIT;
    }
   
    /**
     * Returns the name of the bundle processed by this compilation unit. Bundle
     * name is the qualified name that is used when referencing this bundle in
     * action script. 
     * 
     * For qualified names, "colon syntax" is used such as foo.bar:xyz for 
     * "../foo/bar/xyz.properties" file.
     * 
     * @return the name of the bundle processed by this compilation unit
     */
    public String getBundleNameInColonSyntax()
    {
        return bundleNameInColonSyntax;
    }

    /**
     * Returns the locale of the properties file associated with this
     * compilation unit depends on or <code>null</code> if the file is not
     * locale dependent.
     * 
     * @return the locale this compilation unit depends on or <code>null</code>
     * if the compilation unit is not locale dependent.
     */
    public String getLocale()
    {
        return locale;
    }

    /**
     * Returns the flex project that contains this compilation unit.
     * 
     * @return the flex project.
     */
    private RoyaleProject getRoyaleProject()
    {
        return (RoyaleProject)getProject();
    }

    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_SYNTAX_TREE);
        try
        {
            getProject().clearScopeCacheForCompilationUnit(this);
            
            final Collection<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
            
            PropertiesFileParser parser = new PropertiesFileParser(getProject().getWorkspace());
            final ResourceBundleFileNode fileNode = parser.parse(getFileName(), this.locale, getFileReader(problems), problems);

            ASFileScope fileScope = createFileScope(fileNode);
            addScopeToProjectScope(new ASFileScope[] { fileScope });

            return new SyntaxTreeRequestResult(fileNode, ImmutableSet.<String>of(), getRootFileSpecification().getLastModified(), problems);
        }
        finally
        {
            stopProfile(Operation.GET_SYNTAX_TREE);
        }
    }

    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_FILESCOPE);
        try
        {
            ISyntaxTreeRequestResult syntaxTreeResult = getSyntaxTreeRequest().get();
            final ResourceBundleFileNode rootNode = (ResourceBundleFileNode)syntaxTreeResult.getAST();

            IASScope fileScope = rootNode.getScope();
            assert fileScope instanceof ASFileScope : "Expect ASFileScope as the top-level scope, but found " + fileScope.getClass();

            return new FileScopeRequestResultBase(Collections.<ICompilerProblem> emptyList(), Collections.singleton(fileScope)) {
                
                @Override
                public IDefinition getMainDefinition(String qname)
                {
                    assert qname != null : "Excpect QName.";
                    for (final IDefinition def : definitions)
                    {
                        if (qname.equals(def.getQualifiedName()))
                        {
                            return def;
                        }
                    }
                    return null;
                }
            };
        }
        finally
        {
            stopProfile(Operation.GET_FILESCOPE);
        }
    }

    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        ISyntaxTreeRequestResult syntaxTreeResult = getSyntaxTreeRequest().get();
        final IASNode rootNode = syntaxTreeResult.getAST();

        startProfile(Operation.GET_ABC_BYTES);
        try
        {
            final Collection<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
            final ABCEmitter emitter = new ABCEmitter();

            // TODO: hook this up to something in the settings - how do we access settings from here?
            emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);

            byte[] generatedBytes = null;
            try
            {
                for (IDefinition def : getDefinitionPromises())
                {
                    String qualifiedClassName = def.getQualifiedName();
                    String locale = ResourceBundleUtils.getLocale(qualifiedClassName);

                    generateABCForBundle(emitter, (ResourceBundleFileNode)rootNode, qualifiedClassName, bundleNameInColonSyntax, locale, problems);
                }

                generatedBytes = emitter.emit();
            }
            catch (Exception ex)
            {
                problems.add(new CodegenInternalProblem(rootNode, ex));
            }

            Set<EmbedData> embeds = new HashSet<EmbedData>();
            EmbedCompilationUnitFactory.collectEmbedDatas(getProject(), (IFileNodeAccumulator)rootNode, embeds, problems);

            Set<IEmbedData> iembeds = new HashSet<IEmbedData>();
            for (EmbedData embed : embeds)
                iembeds.add(embed);
            return new ABCBytesRequestResult(generatedBytes, problems.toArray(new ICompilerProblem[0]), iembeds);
        }
        finally
        {
            stopProfile(Operation.GET_ABC_BYTES);
        }
    }

    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        // Fulfill other requests before profiling this request.
        final IABCBytesRequestResult abc = getABCBytesRequest().get();

        startProfile(Operation.GET_SWF_TAGS);
        try
        {
            String tagName = getDefinitionPromises().get(0).getQualifiedName();
            return new SWFTagsRequestResult(abc.getABCBytes(), tagName, abc.getEmbeds());
        }
        finally
        {
            stopProfile(Operation.GET_SWF_TAGS);
        }
    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        final ResourceBundleFileNode fileNode = (ResourceBundleFileNode)getSyntaxTreeRequest().get().getAST();

        startProfile(Operation.GET_SEMANTIC_PROBLEMS);
        try
        {
            Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

            updateEmbedCompilationUnitDependencies(fileNode.getEmbedNodes(), problems);
            
            // Kick off code generation to add all the dependencies found by code generation.
            getABCBytesRequest().get();

            //Add dependency to 'mx.resources.ResourceBundle' since we want it to be picked up.
            RoyaleProject project = getRoyaleProject();
            IResolvedQualifiersReference resourceBundleClassRef = ReferenceFactory.packageQualifiedReference(
                    getProject().getWorkspace(), project.getResourceBundleClass());
            resourceBundleClassRef.resolve(project, this, DependencyType.INHERITANCE);

            return new IOutgoingDependenciesRequestResult()
            {
                @Override
                public ICompilerProblem[] getProblems()
                {
                    return IOutgoingDependenciesRequestResult.NO_PROBLEMS;
                }
            };
        }
        finally
        {
            stopProfile(Operation.GET_SEMANTIC_PROBLEMS);
        }
    }

    @Override
    public void waitForBuildFinish(final Collection<ICompilerProblem> problems, TargetType targetType) throws InterruptedException
    {
        assert problems != null : "Expected 'problems'. Do not ignore problems.";
        Collections.addAll(problems, getSyntaxTreeRequest().get().getProblems());
        Collections.addAll(problems, getFileScopeRequest().get().getProblems());
        Collections.addAll(problems, getOutgoingDependenciesRequest().get().getProblems());

        //Properties files doesn't get compiled down to abc while generating a swc. 
        //Therefore, do the following steps for only swf case.
        if (TargetType.SWF.equals(targetType))
        {
            Collections.addAll(problems, getABCBytesRequest().get().getProblems());
            Collections.addAll(problems, getSWFTagsRequest().get().getProblems());
        }
    }

    @Override
    public void startBuildAsync(TargetType targetType)
    {
        getSyntaxTreeRequest();
        getFileScopeRequest();
        getOutgoingDependenciesRequest();

        //Properties files doesn't get compiled down to abc while generating a swc. 
        //Therefore, do the following steps for only swf case.
        if (TargetType.SWF.equals(targetType))
        {
            getABCBytesRequest();
            getSWFTagsRequest();
        }
    }

    /**
     * Get the time-stamp of the properties file processed by this compilation
     * unit.
     * 
     * @return time stamp in milliseconds from epoch time.
     */
    public long getFileLastModified()
    {
        if (fileEntry != null)
            return fileEntry.getLastModified();

        return getRootFileSpecification().getLastModified();
    }

    /**
     * Returns the content of the file processed by this compilation unit.
     * 
     * @return byte array that represents the content or <code>null</code> if
     * any problem occurs.
     */
    public byte[] getFileContent(final Collection<ICompilerProblem> problems)
    {
        Reader reader = null;
        try
        {
            reader = getFileReader(problems);

            return IOUtils.toByteArray(reader);
        }
        catch (IOException ex)
        {
            problems.add(new InternalCompilerProblem(ex));
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ex)
                {
                    //ignore
                }
            }
        }

        return null;
    }

    /**
     * Returns the file name that should be dispayed with errors message generated from errors in the
     * properties file.
     * 
     * @return File name as a string.
     */
    private String getFileName()
    {
        if (fileEntry != null)
            return FilenameNormalization.normalize(getAbsoluteFilename() + ":" + fileEntry.getPath());
        return getAbsoluteFilename();
    }
    
    /**
     * Returns the {@link Reader} for the contents of file that is processed by
     * this compilation unit.
     * 
     * @return file reader or <code>null</code> if any problem occurs.
     */
    private Reader getFileReader(final Collection<ICompilerProblem> problems)
    {
        try
        {
            if (fileEntry != null)
            {
                InputStream in = fileEntry.createInputStream();
                if (in != null)
                {
                    return new InputStreamReader(in, "UTF-8");
                }
            }
            else
            {
                return getRootFileSpecification().createReader();
            }
        }
        catch (IOException ex)
        {
            problems.add(new InternalCompilerProblem(ex));
        }

        return null;
    }

    /**
     * Create the file scope for this compilation unit.
     * 
     * @param fileNode file node of this compilation unit
     * @return root file scope for this compilation unit
     */
    private ASFileScope createFileScope(final ResourceBundleFileNode fileNode)
    {
        List<IDefinition> definitions = getDefinitionPromises(); 
        ASFileScope fileScope = new ASFileScope(fileNode);

        String packageName = Multiname.getPackageNameForQName(definitions.get(0).getQualifiedName());
        PackageScope packageScope = new PackageScope(fileScope, packageName);
        packageScope.setContainingScope(fileScope);

        PackageDefinition packageDefinition = new PackageDefinition(packageName);
        packageDefinition.setContainedScope(packageScope);

        fileScope.addDefinition(packageDefinition);
        
        for(IDefinition def : definitions)
        {
            Multiname mname = Multiname.crackDottedQName(getProject(), def.getQualifiedName());
            INamespaceDefinition packageNS = Iterables.getOnlyElement(mname.getNamespaceSet());

            ClassDefinition classDefinition = new ClassDefinition(mname.getBaseName(), (INamespaceReference)packageNS);
            IReference baseClass = ReferenceFactory.packageQualifiedReference(getProject().getWorkspace(), getRoyaleProject().getResourceBundleClass());
            classDefinition.setBaseClassReference(baseClass);
            classDefinition.setExcludedClass();

            TypeScope classScope = new TypeScope(packageScope, classDefinition);
            classScope.setContainingDefinition(classDefinition);
            classDefinition.setContainedScope(classScope);
            classDefinition.setupThisAndSuper();

            packageScope.addDefinition(classDefinition);
        }

        return fileScope;
    }

    /**
     * Generates abc for this compilation unit and the specified locale. AS
     * equivalent of a generated class looks like this: 
     * 
     * ----------------
     * package mypackage 
     * { 
     *      import mx.resources.ResourceBundle; 
     *      
     *      public class en_US$myfile_properties extends ResourceBundle 
     *      { 
     *          public function en_US$myfile_properties() { 
     *              super("en_US", "myfile"); 
     *          } 
     *          
     *          override protected function getContent():Object { 
     *              return {
     *                      "name": "Royale",
     *                      "version": "1.0", 
     *                      "motto": "Awesome '{0}' ever.", 
     *                      "classref": org.apache.royale.foo 
     *                      "embededAsset" : embed_properties_awesome_jpg_1808423157
     *              }; 
     *          }
     *      }
     * }
     * --------------
     * 
     * @param emitter emitter object to use to generate abc code
     * @param fileNode file node of this compilation unit
     * @param qualifiedClassName qualified name of the class to generate abc for
     * @param bundleName name of the properties file processed by this compilation unit
     * @param locale locale of the properties file
     * @param problems problems collection that is used to collect problems
     */
    private void generateABCForBundle(final ABCEmitter emitter, final ResourceBundleFileNode fileNode,
            final String qualifiedClassName, final String bundleName, final String locale,
            final Collection<ICompilerProblem> problems)
    {

        RoyaleProject project = getRoyaleProject();

        //this class extends "mx.resources.ResourceBundle"
        IResolvedQualifiersReference resourceBundleReference = ReferenceFactory.packageQualifiedReference(
                project.getWorkspace(), project.getResourceBundleClass());

        //Create constructor instruction list
        InstructionList constructorInstructionList = new InstructionList();
        constructorInstructionList.addInstruction(ABCConstants.OP_getlocal0);
        constructorInstructionList.addInstruction(ABCConstants.OP_pushstring, locale);
        constructorInstructionList.addInstruction(ABCConstants.OP_pushstring, bundleName);
        constructorInstructionList.addInstruction(ABCConstants.OP_constructsuper, 2);
        constructorInstructionList.addInstruction(ABCConstants.OP_returnvoid);

        
        IResolvedQualifiersReference mainClassRef = ReferenceFactory.packageQualifiedReference(
                project.getWorkspace(), qualifiedClassName);
        
        ClassGeneratorHelper classGen = new ClassGeneratorHelper(project, emitter,
                mainClassRef.getMName(),
                (ClassDefinition)resourceBundleReference.resolve(project),
                Collections.<Name> emptyList(), Collections.<Name> emptyList(),
                constructorInstructionList, true);

        //Create method body for getContents
        InstructionList bodyInstructionList = new InstructionList();
        bodyInstructionList.addInstruction(ABCConstants.OP_getlocal0);
        bodyInstructionList.addInstruction(ABCConstants.OP_pushscope);

        //Create key value pair entries "key":"value"
        int entryCount = 0;
        for (int i = 0; i < fileNode.getChildCount(); i++)
        {
            IASNode node = fileNode.getChild(i);
            if (node instanceof ResourceBundleEntryNode)
            {
                entryCount++;
                ResourceBundleEntryNode entryNode = (ResourceBundleEntryNode)node;

                //push key
                bodyInstructionList.addInstruction(ABCConstants.OP_pushstring, entryNode.getKeyNode().getValue());

                //push value
                ExpressionNodeBase valueNode = entryNode.getValueNode();
                switch (valueNode.getNodeID())
                {

                    case LiteralStringID:
                        bodyInstructionList.addInstruction(ABCConstants.OP_pushstring,
                                ((LiteralNode)valueNode).getValue());
                        break;

                    case ClassReferenceID:
                        ClassReferenceNode crn = (ClassReferenceNode)valueNode;
                        if (crn.getName() != null)
                        {

                            IResolvedQualifiersReference refClass = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), crn.getName());

                            if (refClass.resolve(project, crn.getASScope(), DependencyType.EXPRESSION, true) == null)
                            {
                                ICompilerProblem problem = new UnresolvedClassReferenceProblem(crn, crn.getName());
                                problems.add(problem);
                            }
                        }
                        String className = crn.getName();
                        if(className == null) 
                        {
                            bodyInstructionList.addInstruction(ABCConstants.OP_pushnull);
                        } 
                        else 
                        {
                            IResolvedQualifiersReference classRef = ReferenceFactory.packageQualifiedReference(
                                    project.getWorkspace(), className);

                            bodyInstructionList.addInstruction(ABCConstants.OP_getlex, classRef.getMName());
                        }
                        
                        break;

                    case EmbedID:
                        EmbedNode embedNode = (EmbedNode)valueNode;
                        try
                        {
                            String name = embedNode.getName(project, problems);
                            IResolvedQualifiersReference embedClassRef = ReferenceFactory.packageQualifiedReference(
                                    project.getWorkspace(), name);
                            bodyInstructionList.addInstruction(ABCConstants.OP_getlex, embedClassRef.getMName());
                        }
                        catch (InterruptedException ex)
                        {
                            problems.add(new CodegenInternalProblem(embedNode, ex));
                        }
                        break;

                    default:
                        //This shouldn't happen. Should we handle this case by collecting a problem? 
                }
            }
        }

        bodyInstructionList.addInstruction(ABCConstants.OP_newobject, entryCount);
        bodyInstructionList.addInstruction(ABCConstants.OP_returnvalue);

        Name getContentsMethodName = new Name(ABCConstants.CONSTANT_Qname,
                new Nsset(classGen.getProtectedNamespace()), "getContent");

        //Create getContents method
        classGen.addITraitsMethod(getContentsMethodName, Collections.<Name> emptyList(),
                new Name(IASLanguageConstants.Object), Collections.<Object> emptyList(),
                false, false, true, bodyInstructionList);

        classGen.finishScript();
    }

}
