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
package org.apache.flex.compiler.internal.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.flex.compiler.clients.JSConfiguration;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.css.ICSSMediaQueryCondition;
import org.apache.flex.compiler.css.ICSSRule;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.driver.js.flexjs.JSCSSCompilationSession;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.flex.compiler.internal.targets.ITargetAttributes;
import org.apache.flex.compiler.internal.targets.LinkageChecker;
import org.apache.flex.compiler.internal.tree.mxml.MXMLDocumentNode;
import org.apache.flex.compiler.internal.tree.mxml.MXMLFileNode;
import org.apache.flex.compiler.internal.units.SWCCompilationUnit;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.units.ICompilationUnit;

import com.google.common.collect.ImmutableList;

/**
 * @author aharui
 *
 */
public class FlexJSProject extends FlexProject
{

    /**
     * Constructor
     *
     * @param workspace The {@code Workspace} containing this project.
     */
    public FlexJSProject(Workspace workspace, IBackend backend)
    {
        super(workspace);
        this.backend = backend;
    }

    private HashMap<ICompilationUnit, HashMap<String, String>> interfaces = new HashMap<ICompilationUnit, HashMap<String, String>>();
    private HashMap<ICompilationUnit, HashMap<String, DependencyType>> requires = new HashMap<ICompilationUnit, HashMap<String, DependencyType>>();
    private HashMap<ICompilationUnit, HashMap<String, DependencyType>> jsModules = new HashMap<ICompilationUnit, HashMap<String, DependencyType>>();
    public TreeSet<String> mixinClassNames;

    public JSGoogConfiguration config;
    public Configurator configurator;

    private IBackend backend;

    public ICompilationUnit mainCU;

    @Override
    public synchronized void addDependency(ICompilationUnit from, ICompilationUnit to,
                              DependencyType dt, String qname)
    {
        List<IDefinition> dp = to.getDefinitionPromises();

        if (dp.size() == 0)
            return;

        IDefinition def = dp.get(0);
        // IDefinition def = to.getDefinitionPromises().get(0);
        IDefinition actualDef = ((DefinitionPromise) def).getActualDefinition();
        boolean isInterface = (actualDef instanceof InterfaceDefinition) && (dt == DependencyType.INHERITANCE);
        if (!isInterface)
        {
            if (from != to)
            {
                HashMap<String, DependencyType> reqs;
                if (requires.containsKey(from))
                    reqs = requires.get(from);
                else
                {
                    reqs = new HashMap<String, DependencyType>();
                    requires.put(from, reqs);
                }
                if (reqs.containsKey(qname))
                {
                    // inheritance is important so remember it
                    if (reqs.get(qname) != DependencyType.INHERITANCE)
                    {
                        if (!isExternalLinkage(to))
                            reqs.put(qname, dt);
                    }
                }
                else if (!isExternalLinkage(to) || qname.equals("Namespace"))
                {
                    if (qname.equals("XML"))
                        needXML = true;
                    reqs.put(qname, dt);
                }
                if (jsModules.containsKey(from))
                {
                    reqs = jsModules.get(from);
                }
                else
                {
                    reqs = new HashMap<String, DependencyType>();
                    jsModules.put(from, reqs);
                }
                IMetaTag tag = getJSModuleMetadata(to);
                if (tag != null)
                {
                    IMetaTagAttribute nameAttribute = tag.getAttribute("name");
                    if (nameAttribute != null)
                    {
                        reqs.put(nameAttribute.getValue(), dt);
                    }
                    else
                    {
                        reqs.put(qname, dt);
                    }
                }
            }
        }
        else
        {
            if (from != to)
            {
                HashMap<String, String> interfacesArr;

                if (interfaces.containsKey(from))
                {
                    interfacesArr = interfaces.get(from);
                }
                else
                {
                    interfacesArr = new HashMap<String, String>();
                    interfaces.put(from, interfacesArr);
                }

                if (!interfacesArr.containsKey(qname))
                {
                    interfacesArr.put(qname, qname);
                }
            }
        }

        super.addDependency(from, to, dt, qname);
    }

    public boolean needLanguage;
    public boolean needCSS;
    public boolean needXML;

    private LinkageChecker linkageChecker;
    private ITargetSettings ts;

    // definitions that should be considered external linkage
    public Collection<String> unitTestExterns;

    private IMetaTag getJSModuleMetadata(ICompilationUnit cu)
    {
        try
        {
            Iterator<IDefinition> iterator = cu.getFileScopeRequest().get().getExternallyVisibleDefinitions().iterator();
            while(iterator.hasNext())
            {
                IDefinition def = iterator.next();
                if (def.hasMetaTagByName("JSModule"))
                {
                    return def.getMetaTagByName("JSModule");
                }
            }
        }
        catch (Exception ex)
        {
            //it's safe to ignore an exception here
        }
        return null;
    }

    public boolean isExternalLinkage(ICompilationUnit cu)
    {
        if (linkageChecker == null)
        {
            ts = getTargetSettings();
            linkageChecker = new LinkageChecker(this, ts);
        }
        // in unit tests, ts may be null and LinkageChecker NPEs
        if (ts == null)
        {
            if (unitTestExterns != null)
            {
                try {
                    if (!(cu instanceof SWCCompilationUnit))
                        if (unitTestExterns.contains(cu.getQualifiedNames().get(0)))
                            return true;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;
        }

        List<String> qnames;
        try {
            qnames = cu.getQualifiedNames();
            String qname = qnames.get(0);
            if (qname.equals("QName") || qname.equals("XML") || qname.equals("XMLList"))
                return false;
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try
        {
            return linkageChecker.isExternal(cu);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> getInterfaces(ICompilationUnit from)
    {
        if (interfaces.containsKey(from))
        {
            HashMap<String, String> map = interfaces.get(from);
            ArrayList<String> arr = new ArrayList<String>();
            Set<String> cus = map.keySet();
            for (String s : cus)
                arr.add(s);
            return arr;
        }
        return null;
    }

    public ArrayList<String> getRequires(ICompilationUnit from)
    {
        if (requires.containsKey(from))
        {
            HashMap<String, DependencyType> map = requires.get(from);
            ArrayList<String> arr = new ArrayList<String>();
            Set<String> cus = map.keySet();
            for (String s : cus)
                arr.add(s);
            return arr;
        }
        return null;
    }

    public ArrayList<String> getExternalRequires(ICompilationUnit from)
    {
        if (jsModules.containsKey(from))
        {
            HashMap<String, DependencyType> map = jsModules.get(from);
            ArrayList<String> arr = new ArrayList<String>();
            Set<String> cus = map.keySet();
            for (String s : cus)
                arr.add(s);
            return arr;
        }
        return null;
    }

    JSCSSCompilationSession cssSession = new JSCSSCompilationSession();

    @Override
    public CSSCompilationSession getCSSCompilationSession()
    {
        // When building SWFs, each MXML document may have its own styles
        // specified by fx:Style blocks.  The CSS is separately compiled and
        // stored in the class definition for the MXML document.  That helps
        // with deferred loading of classes.  The styles and thus the
        // classes for an MXML document are not initialized until the MXML
        // class is initialized.
        // For JS compilation, the CSS for non-standard CSS could be done the
        // same way, but AFAICT, standard CSS properties are best loaded by
        // specifying a .CSS file in the HTML.  The CSS is probably less text
        // than its codegen'd representation, and the browser can probably
        // load a .CSS file faster than us trying to run code to update the
        // styles.
        // So, for FlexJS, all style blocks from all MXML files are gathered into
        // one .css file and a corresponding codegen block that is output as
        // part of the main .JS file.
        return cssSession;
    }

    private HashMap<IASNode, String> astCache = new HashMap<IASNode, String>();

    @Override
    public void addToASTCache(IASNode ast)
    {
        astCache.put(ast, "");
    }

    @Override
    public void setTargetSettings(ITargetSettings value)
    {
        super.setTargetSettings(value);
        ts = value;
        linkageChecker = new LinkageChecker(this, value);
        try {
            linkageChecker.initExterns();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public String getGeneratedIDBase()
    {
        return MXMLFlexJSEmitterTokens.ID_PREFIX.getToken();
    }

    public ITargetAttributes computeTargetAttributes()
    {
    	List<String> names;
		try {
			names = mainCU.getQualifiedNames();
	    	IDefinition def = this.resolveQNameToDefinition(names.get(0));
	    	IDefinitionNode node = def.getNode();
	    	if (node instanceof MXMLDocumentNode)
	    	{
	    		MXMLDocumentNode mxmlDoc = (MXMLDocumentNode)node;
	    		MXMLFileNode mxmlFile = (MXMLFileNode)mxmlDoc.getParent();
	    		return mxmlFile.getTargetAttributes(this);
	    	}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }

    public IBackend getBackend() {
        return backend;
    }

    @Override
    protected void overrideDefines(Map<String, String> defines)
    {
    	if (defines.containsKey("COMPILE::SWF"))
    	{
    		if (defines.get("COMPILE::SWF").equals("AUTO"))
    			defines.put("COMPILE::SWF", "false");
    	}
    	if (defines.containsKey("COMPILE::JS"))
    	{
    		if (defines.get("COMPILE::JS").equals("AUTO"))
    			defines.put("COMPILE::JS", "true");
    	}
    }

    /**
     * List of external libraries so it can be overridden
     */
    public List<String> getCompilerExternalLibraryPath(Configuration config)
    {
    	List<String> list = ((JSConfiguration)config).getCompilerJsExternalLibraryPath();
        list.addAll(config.getCompilerExternalLibraryPath());
    	return list;
    }

    /**
     * List of libraries so it can be overridden
     */
    public List<String> getCompilerLibraryPath(Configuration config)
    {
    	List<String> list = ((JSConfiguration)config).getCompilerJsLibraryPath();
        list.addAll(config.getCompilerLibraryPath());
    	return list;
    }
    
    /**
     * List of libraries so it can be overridden
     */
    public List<MXMLNamespaceMapping> getCompilerNamespacesManifestMappings(Configuration config)
    {
    	List<MXMLNamespaceMapping> list = ((JSConfiguration)config).getCompilerJsNamespacesManifestMappings();
    	if (list != null && list.size() > 0)
    		return list;
    	return config.getCompilerNamespacesManifestMappings();
    }
    
	@Override
	public boolean isPlatformRule(ICSSRule rule) {
        ImmutableList<ICSSMediaQueryCondition> mqlist = rule.getMediaQueryConditions();
        int n = mqlist.size();
        if (n > 0)
        {
            if (mqlist.get(0).toString().equals("-flex-flash"))
                return false;
        }
		return true;
	}

}
