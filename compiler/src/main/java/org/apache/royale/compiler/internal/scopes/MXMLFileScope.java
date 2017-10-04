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

package org.apache.royale.compiler.internal.scopes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.units.MXMLCompilationUnit;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.mxml.IXMLNameResolver;

import com.google.common.collect.ImmutableSet;

/**
 * Subclass of {@link ASFileScope} for MXML file scopes.
 * <p>
 * It keeps track of the main MXML class definition in an MXML file scope.
 * <p>
 * It supports creating additional classes defined by
 * <code>&lt;fx:Component&gt;</code> and <code>&lt;fx:Definition&gt;</code> tags
 * in an MXML file.
 * <p>
 * It keeps track of the mapping from an MXML tag such as
 * <code>&lt;fx:MyDefinition&gt;</code> to the the class defined by
 * <code>&lt;fx:Definition name="MyDefinition"&gt;</code>
 * <p>
 * It has APIs such as {@code isScriptTag()} for determining whether an MXML tag
 * is a particular language tag, as determined by the version of MXML being used
 * in the MXML file that created this file scope.
 */
public class MXMLFileScope extends ASFileScope implements IXMLNameResolver
{
    /**
     * Constructor.
     * 
     * @param compilationUnit The {@link MXMLCompilationUnit} in which the new file scope
     * resides.
     * @param filePath The path of the MXML file for which this file scope is
     * being constructed.
     * @param mxmlData The {@code IMXMLData} that built this file scope. This is
     * used to determine which version of MXML is being used.
     */
    public MXMLFileScope(MXMLCompilationUnit compilationUnit, String filePath, IMXMLData mxmlData)
    {
        super(compilationUnit.getProject().getWorkspace(), filePath);

        this.project = compilationUnit.getProject();
        this.mxmlDialect = mxmlData.getMXMLDialect();
        this.sourceDependencies = new HashSet<String>();

        addImplicitImportsForMXML();
    }

    /**
     * The {@code RoyaleProject} for this file scope.
     */
    private final RoyaleProject project;
    
    /**
     * The {@code MXMLDialect} for this file scope.
     */
    private final MXMLDialect mxmlDialect;

    /**
     * Any source files which this file scope includes
     */
    private final Set<String> sourceDependencies;

    /**
     * The {@code ClassDefinition} of the main class for the MXML file.
     */
    private ClassDefinition mainClassDefinition;

    /**
     * A map from XMLNames that refer to <fx:Definition>s to the
     * ClassDefinitions for those <fx:Definition>s.
     */
    private Map<XMLName, ClassDefinition> fxDefinitionsMap;
    
    /**
     * A map from the starting offset of an <fx:Definition> tag to the
     * ClassDefinition produced by that <fx:Definition> This is built during MXML
     * scope-building, and used later by MXML tree-building to find the
     * already-built definition to connect to the node.
     */
    private Map<Integer, ClassDefinition> fxDefinitionsOffsetMap;

    /**
     * A map from XMLNames that refer to <fx:Components>s to the
     * ClassDefinitions for those <fx:Components>s.
     */
    private Map<XMLName, ClassDefinition> fxComponentsMap;
    
    /**
     * A map from the starting offset of an <fx:Component> tag to the
     * ClassDefinition produced by that <fx:Component> This is built during MXML
     * scope-building, and used later by MXML tree-building to find the
     * already-built definition to connect to the node.
     */
    private Map<Integer, ClassDefinition> fxComponentsOffsetMap;
    
    /**
     * Adds the appropriate implicit imports for ActionScript.
     */
    private void addImplicitImportsForMXML()
    {
        // Add the implicit imports for MXML.
        for (String implicitImport : project.getImplicitImportsForMXML(mxmlDialect))
        {
            addImport(implicitImport);
        }
    }

    /**
     * Returns the main class definition in this file scope.
     * 
     * @return The main class definition in this file scope.
     */
    public ClassDefinition getMainClassDefinition()
    {
        assert mainClassDefinition != null : "Main class definition should be set before it is retrieved";
        return mainClassDefinition;
    }

    /**
     * Called by the MXML scope-building code to set the main class definition
     * in this file scope.
     * 
     * @param mainClassDefinition The main class definition in this file scope.
     */
    public void setMainClassDefinition(ClassDefinition mainClassDefinition)
    {
        assert this.mainClassDefinition == null : "Main class definition should only be set once.";
        assert mainClassDefinition != null;
        this.mainClassDefinition = mainClassDefinition;
    }

    /**
     * @return a Collection of source files included in this file scope
     */
    public ImmutableSet<String> getSourceDependencies()
    {
        return ImmutableSet.copyOf(sourceDependencies);
    }

    /**
     * Add a source file dependency to this file scope
     * 
     * @param filename Source dependency filename
     */
    public void addSourceDependency(String filename)
    {
        sourceDependencies.add(filename);
    }

    /**
     * Creates a new class definition for an &lt;fx:Component&gt; tag and adds
     * it to this scope.
     * 
     * @param mainClassQName The fully-qualified class name of the main class
     * for the entire MXML document (e.g., <code>"MyApp"</code>).
     * @param componentTagStart The starting offset of the &lt;fx:Component&gt;
     * tag.
     * @param componentClassName The class name for the component, as specified
     * by the <code>className</code> attribute on the &lt;fx:Component&gt; tag,
     * or <code>null</code> if there was no such attribute.
     * @param componentBaseClassQName The fully-qualified class name of the base
     * class for the component class.
     * @return The newly-added {@code ClassDefinition} for the component class.
     */
    public ClassDefinition addFXComponent(String mainClassQName,
                                          int componentTagStart,
                                          String componentClassName,
                                          String componentBaseClassQName)
    {
        // Use the class name specified by the <code>className</code> attribute,
        // or generate a unique class name for the new component class,
        // such as "com_whatever_Whatever_component2"
        // for the 3rd anonymous <fx:Component> inside com.whatever.Whatever.
        String className = componentClassName != null ?
                           componentClassName :
                           generateComponentClassName(mainClassQName);
        
        String packageName = Multiname.getPackageNameForQName(className);
        String baseName = Multiname.getBaseNameForQName(className);
        String namespace = packageName.isEmpty() ? "*" : packageName + ".*";
        XMLName xmlName = new XMLName(namespace, baseName);

        // Create a ClassDefinition for the component class,
        // and add it to this file scope.
        ClassDefinition fxComponentClassDefinition =
                new ClassDefinition(className, getFilePrivateNamespaceReference());
        fxComponentClassDefinition.setBaseClassReference(
                ReferenceFactory.packageQualifiedReference(getWorkspace(), componentBaseClassQName));
        fxComponentClassDefinition.setMetaTags(new IMetaTag[0]);
        addDefinition(fxComponentClassDefinition);

        // Create a class scope for the component class.
        TypeScope classScope = new TypeScope(this, fxComponentClassDefinition);
        classScope.setContainingDefinition(fxComponentClassDefinition);
        fxComponentClassDefinition.setContainedScope(classScope);
        fxComponentClassDefinition.setupThisAndSuper();

        // Keep track of the tag-name-to-class-definition mapping so that we can
        // resolve a tag like <MyComponent>.
        if (fxComponentsMap == null)
            fxComponentsMap = new HashMap<XMLName, ClassDefinition>();
        fxComponentsMap.put(xmlName, fxComponentClassDefinition);
        
        // Keep track of the starting-offset-of-component-tag-to-component-class-definition
        // mapping so that we can find the class defined by an <fx:Component> tag
        // later when we build the MXML tree.
        if (fxComponentsOffsetMap == null)
            fxComponentsOffsetMap = new HashMap<Integer, ClassDefinition>();
        fxComponentsOffsetMap.put(componentTagStart, fxComponentClassDefinition);

        return fxComponentClassDefinition;
    }

    /**
     * Generates a class name for a class defined by an anonymous
     * <code>&lt;fx:Component&gt;</code> tag.
     * <p>
     * If the main class of the MXML document is, for example,
     * <code>com.whatever.Whatever</code>, then the 3rd anonymous component
     * class will be named <code>com_whatever_Whatever_component2</code>.
     * 
     * @return The generated class name.
     */
    private String generateComponentClassName(String mainClassQName)
    {
        int currentComponentCount = fxComponentsOffsetMap != null ? fxComponentsOffsetMap.size() : 0;
        return mainClassQName.replace('.', '_') + "_component" +
               String.valueOf(currentComponentCount);
    }

    /**
     * Gets the {@code ClassDefinition} for a class defined by a
     * <code>&lt;fx:Component&gt;</code> tag.
     * 
     * @param componentTag The {@code MXMLTagData} for the
     * <code>&lt;fx:Component&gt;</code> tag.
     * @return The {@code ClassDefinition} associated with the
     * <code>&lt;fx:Component&gt;</code> tag.
     */
    public ClassDefinition getClassDefinitionForComponentTag(IMXMLTagData componentTag)
    {
        return fxComponentsOffsetMap != null ?
                fxComponentsOffsetMap.get(componentTag.getAbsoluteStart()) :
                null;
    }

    /**
     * Creates a new class definition for an &lt;fx:Definition&gt; tag and adds
     * it to this scope.
     * 
     * @param mainClassQName The fully-qualified class name of the main class
     * for the entire MXML document (e.g., <code>"MyApp"</code>).
     * @param definitionTag the MXMLTagData representing the 
     * &lt;fx:Definition&gt; tag 
     * @param definitionName The definition name as specified by the 
     * <code>name</code> attribute on the &lt;fx:Definition&gt; tag.
     * @param definitionBaseClassQName The fully-qualified class name of the base
     * class for the definition class.
     * @return The newly-added {@code ClassDefinition} for the definition class.
     */
    public ClassDefinition addFXDefinition(String mainClassQName,
                                           IMXMLTagData definitionTag,
                                           String definitionName,
                                           String definitionBaseClassQName)
    {
        // Generate a unique class name for the new <fx:Definition> class,
        // such as "com_whatever_Whatever_definition2"
        // for the 3rd <fx:Definition> inside com.whatever.Whatever.
        String className = generateDefinitionClassName(mainClassQName);
        
        XMLName definitionXMLName = new XMLName(definitionTag.getURI(), definitionName);

        // Create a ClassDefinition for the definition class,
        // and add it to this file scope.
        ClassDefinition fxDefinitionClassDefinition =
                new ClassDefinition(className, getFilePrivateNamespaceReference());
        fxDefinitionClassDefinition.setBaseClassReference(
                ReferenceFactory.packageQualifiedReference(getWorkspace(), definitionBaseClassQName));
        fxDefinitionClassDefinition.setMetaTags(new IMetaTag[0]);
        addDefinition(fxDefinitionClassDefinition);

        // Create a class scope for the definition class.
        TypeScope classScope = new TypeScope(this, fxDefinitionClassDefinition);
        classScope.setContainingDefinition(fxDefinitionClassDefinition);
        fxDefinitionClassDefinition.setContainedScope(classScope);
        fxDefinitionClassDefinition.setupThisAndSuper();

        // Keep track of the tag-name-to-class-definition mapping so that we can
        // resolve a tag like <fx:MyDefinition>.
        if (fxDefinitionsMap == null)
            fxDefinitionsMap = new HashMap<XMLName, ClassDefinition>();
        fxDefinitionsMap.put(definitionXMLName, fxDefinitionClassDefinition);
        
        // Keep track of the starting-offset-of-definition-tag-to-definition-class-definition
        // mapping so that we can find the class defined by an <fx:Definition> tag
        // later when we build the MXML tree.
        if (fxDefinitionsOffsetMap == null)
            fxDefinitionsOffsetMap = new HashMap<Integer, ClassDefinition>();
        fxDefinitionsOffsetMap.put(definitionTag.getAbsoluteStart(), fxDefinitionClassDefinition);

        return fxDefinitionClassDefinition;
    }

    /**
     * Generates a class name for a class defined by an
     * <code>&lt;fx:Definition&gt;</code> tag.
     * <p>
     * If the main class of the MXML document is, for example,
     * <code>com.whatever.Whatever</code>, then the 3rd definition class will be
     * named <code>com_whatever_Whatever_definition2</code>.
     * 
     * @return The generated class name.
     */
    private String generateDefinitionClassName(String mainClassQName)
    {
        // TODO when http://bugs.adobe.com/jira/browse/CMP-403 is fixed,
        // make the name of the definition classes, just the name specified on
        // the tag and the namespace reference a private implementation namespace.
        int currentDefinitionCount = fxDefinitionsMap != null ? fxDefinitionsMap.size() : 0;
        return mainClassQName.replace('.', '_') + "_definition" +
               String.valueOf(currentDefinitionCount);
    }

    /**
     * Resolves an MXMLTagData to the fully qualified AS3 class name the tag
     * refers to.
     * <p>
     * TODO This method should return a name object instead of a string.
     * 
     * @param tag An MXMLTagData whose name potentially refers to a AS3 class
     * name via manifest or &lt;fx:Definition&gt; tags.
     * @return Fully qualified AS3 class name the specified tag refers to.
     */
    public String resolveTagToQualifiedName(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        return resolveXMLNameToQualifiedName(tagName, tag.getParent().getMXMLDialect());
    }

    @Override
    public String resolveXMLNameToQualifiedName(XMLName tagName, MXMLDialect mxmlDialect)
    {
        ClassDefinition classDef = getClassDefinitionForDefinitionTagName(tagName);
        if (classDef != null)
            return classDef.getQualifiedName();

        return project.resolveXMLNameToQualifiedName(tagName, mxmlDialect);
    }

    /**
     * Resolves an MXMLTagData to the IReference class the tag refers to.
     * 
     * @param tag An MXMLTagData whose name potentially refers to a AS3 class
     * name via manifest or &lt;fx:Definition&gt; tags.
     * @return IReference to the specified tag refers to.
     */
    public IReference resolveTagToReference(IMXMLTagData tag)
    {
        String qname = resolveTagToQualifiedName(tag);
        if (qname != null)
            return ReferenceFactory.packageQualifiedReference(getWorkspace(), qname);

        return null;
    }

    /**
     * Resolves an MXML tag such as <s:Button> to a class definition that the
     * manifest information has associated with the tag.
     * <p>
     * This method handles both manifest namespaces (such as in the above
     * example) and package namespaces such as <d:Sprite
     * xmlns:d="flash.display.*">.
     * 
     * @param tag An MXML tag.
     * @return The definition of the ActionScript class, or <code>null</code> if
     * the tag has a manifest namespace and isn't found in the
     * MXMLManifestManager.
     */
    public IDefinition resolveTagToDefinition(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        return resolveXMLNameToDefinition(tagName, tag.getParent().getMXMLDialect());
    }

    /**
     * Resolves an {@link XMLName} such as <s:Button> to a class definition that
     * the manifest information has associated with the tag.
     * <p>
     * This method handles both manifest namespaces (such as in the above
     * example) and package namespaces such as <d:Sprite
     * xmlns:d="flash.display.*">.
     * 
     * @param tagXMLName {@link XMLName} of a tag.
     * @param mxmlDialect Knowledge about dialect-specific resolution
     * strategies.
     * @return The definition of the ActionScript class, or <code>null</code> if
     * the tag has a manifest namespace and isn't found in the
     * MXMLManifestManager.
     */
    @Override
    public IDefinition resolveXMLNameToDefinition(XMLName tagXMLName, MXMLDialect mxmlDialect)
    {
        // See if there is a class defined by a <Component> tag.
        ClassDefinition componentTagClassDef = getClassDefinitionForComponentTagName(tagXMLName);
        if (componentTagClassDef != null)
            return componentTagClassDef;

        // See if there is a class defined by a <Definition> tag.
        ClassDefinition definitionTagClassDef = getClassDefinitionForDefinitionTagName(tagXMLName);
        if (definitionTagClassDef != null)
            return definitionTagClassDef;
        
        return project.resolveXMLNameToDefinition(tagXMLName, mxmlDialect);
    }
    
    /**
     * Gets the {@link ClassDefinition} for a class defined by a
     * &lt;fx:Component&gt; tag.
     * 
     * @param componentTagName {@link XMLName} that refers to the
     * &lt;fx:Component&gt;. The name of the tag is determined by the className
     * attribute of the &lt;fx:Component&gt; tag.
     */
    public ClassDefinition getClassDefinitionForComponentTagName(XMLName componentTagName)
    {
        return fxComponentsMap != null ?
                fxComponentsMap.get(componentTagName) :
                null;
    }

    /**
     * Gets the {@code ClassDefinition} for a class defined by a
     * <code>&lt;fx:Definition&gt;</code> tag.
     * 
     * @param definitionTag The {@code MXMLTagData} for the
     * <code>&lt;fx:Definition&gt;</code> tag.
     * @return The {@code ClassDefinition} associated with the
     * <code>&lt;fx:Definition&gt;</code> tag.
     */
    public ClassDefinition getClassDefinitionForDefinitionTag(IMXMLTagData definitionTag)
    {
        return fxDefinitionsOffsetMap != null ?
                fxDefinitionsOffsetMap.get(definitionTag.getAbsoluteStart()) :
                null;
    }

    /**
     * Gets the {@link ClassDefinition} for a class defined by a
     * &lt;fx:Definition&gt; tag.
     * 
     * @param definitionTagName {@link XMLName} that refers to the
     * &lt;fx:Definition&gt;. The name of the tag is determined by the name
     * attribute of the &lt;fx:Definition&gt; tag.
     */
    public ClassDefinition getClassDefinitionForDefinitionTagName(XMLName definitionTagName)
    {
        return fxDefinitionsMap != null ?
                fxDefinitionsMap.get(definitionTagName) :
                null;
    }

    /**
     * Gets the class definitions for all the &lt;fx:Definition&gt; tags in this
     * scope.
     * 
     * @return The class definitions for all the &lt;fx:Definition&gt; tags in
     * this scope.
     */
    public IClassDefinition[] getLibraryDefinitions()
    {
        return fxDefinitionsMap != null ?
                fxDefinitionsMap.values().toArray(new IClassDefinition[0]) :
                new IClassDefinition[0];
    }

    /**
     * Returns all the {@link XMLName}'s that refer to &lt;fx:Definition&gt;'s
     * in this file scope.
     * 
     * @return All the {@link XMLName}'s that refer to &lt;fx:Definition&gt;'s
     * in this file scope.
     */
    public XMLName[] getLibraryDefinitionTagNames()
    {
        return fxDefinitionsMap != null ?
                fxDefinitionsMap.keySet().toArray(new XMLName[0]) :
                new XMLName[0];
    }

    public boolean isBindingTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName bindingTagName = mxmlDialect.resolveBinding();
        return tagName.equals(bindingTagName);
    }

    public boolean isComponentTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName componentTagName = mxmlDialect.resolveComponent();
        return tagName.equals(componentTagName);
    }

    public boolean isDeclarationsTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName declarationsTagName = mxmlDialect.resolveDeclarations();
        return tagName.equals(declarationsTagName);
    }

    public boolean isDefinitionTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName definitionTagName = mxmlDialect.resolveDefinition();
        return tagName.equals(definitionTagName);
    }

    public boolean isLibraryTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName libraryTagName = mxmlDialect.resolveLibrary();
        return tagName.equals(libraryTagName);
    }

    public boolean isMetadataTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName metadataTagName = mxmlDialect.resolveMetadata();
        return tagName.equals(metadataTagName);
    }

    public boolean isModelTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName modelTagName = mxmlDialect.resolveModel();
        return tagName.equals(modelTagName);
    }

    public boolean isPrivateTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName privateTagName = mxmlDialect.resolvePrivate();
        return tagName.equals(privateTagName);
    }

    public boolean isReparentTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName reparentTagName = mxmlDialect.resolveReparent();
        return tagName.equals(reparentTagName);
    }

    public boolean isScriptTag(IMXMLUnitData unitData)
    {
        if (unitData instanceof IMXMLTagData)
            return isScriptTag((IMXMLTagData)unitData);
        return false;
    }

    public boolean isScriptTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName scriptTagName = mxmlDialect.resolveScript();
        return tagName.equals(scriptTagName);
    }
    
    public boolean isStringTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName stringTagName = mxmlDialect.resolveString();
        return tagName.equals(stringTagName);
    }

    public boolean isStyleTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName styleTagName = mxmlDialect.resolveStyle();
        return tagName.equals(styleTagName);
    }

    public boolean isXMLTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName xmlTagName = mxmlDialect.resolveXML();
        return tagName.equals(xmlTagName);
    }

    public boolean isXMLListTag(IMXMLTagData tag)
    {
        XMLName tagName = tag.getXMLName();
        XMLName xmlListTagName = mxmlDialect.resolveXMLList();
        return tagName.equals(xmlListTagName);
    }
}
