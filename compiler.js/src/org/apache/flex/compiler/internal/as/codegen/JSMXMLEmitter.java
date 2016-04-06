package org.apache.flex.compiler.internal.as.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.flex.abc.semantics.InstanceInfo;
import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.internal.as.codegen.JSEmitter.EmitterClassVisitor;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.ICompilationUnit.Operation;

class JSMXMLEmitter extends JSEmitter
{

	public JSMXMLEmitter(JSSharedData sharedData, Operation buildPhase,
			ICompilerProject project, JSGenerator generator) {
		super(sharedData, buildPhase, project, generator);
		// TODO Auto-generated constructor stub
	}
	
	private JSMXMLClassDirectiveProcessor cdp;
    private String NEWLINE = "\n";
	
	public void register(JSMXMLClassDirectiveProcessor cdp)
	{
		this.cdp = cdp;
	}
	
	@Override
	public byte[] emit()
	{
		try {
			generateClass();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return w.toByteArray();
	}
	
	private void generateClass() throws Exception
	{
		ClassDefinition definition = cdp.classDefinition;
        IClassDefinition superClass = (ClassDefinition)definition.resolveBaseClass(cdp.getProject());
        String classQName = definition.getQualifiedName();
		writeString("goog.provide('" + classQName + "');" + NEWLINE);
        writeString(NEWLINE);
        /*
		String[] list = definition.getImplicitImports();
		ArrayList<String> imps = new ArrayList<String>(Arrays.asList(list));
		imps.add(superClass.getQualifiedName());
		Collections.sort(imps);
        for (String imp : imps)
        {
            if (imp.indexOf("__AS3__") != -1)
                continue;
            writeString("goog.require('" + imp + "');");
            writeString(NEWLINE);
        }
        writeString(NEWLINE);
        */
        FlexJSProject project = (FlexJSProject)m_project;
        ASProjectScope projectScope = (ASProjectScope) m_project.getScope();
        ICompilationUnit cu = projectScope.getCompilationUnitForDefinition(definition);
        ArrayList<String> deps = project.getRequires(cu);
        emitRequires(deps, classQName);

        writeString("/**" + NEWLINE);
        writeString(" * @constructor" + NEWLINE);
        if (superClass != null)
        	writeString(" * @extends {" + superClass.getQualifiedName() + "}" + NEWLINE);
        writeString(" */" + NEWLINE);
        writeString(definition.getQualifiedName() + " = function() {" + NEWLINE);
        if (superClass != null)
        	writeString("    " + superClass.getQualifiedName() + ".call(this);" + NEWLINE);
        
        for (int i = 0; i < cdp.variableTraits.size(); i += 2)
        {
            writeString(NEWLINE);
        	Name name = cdp.variableTraits.get(i);
        	Name type = cdp.variableTraits.get(i + 1);
        	writeString("    /**" + NEWLINE);
        	writeString("     * @private" + NEWLINE);
        	writeString("     * @type {");
        		String ns = type.getSingleQualifier().getName();
        		if (ns.length() > 0)
        		{
        			writeString(ns);
        			writeString(".");
        		}
        		writeString(type.getBaseName());
        		writeString("}" + NEWLINE);
        	writeString("     */" + NEWLINE);
        	writeString("    this." + name.getBaseName() + ";");
        	writeString(NEWLINE);
        }
        writeString("};" + NEWLINE);
        
        if (superClass != null)
        {
            writeString("goog.inherits(" + definition.getQualifiedName() + ", "
                    + superClass.getQualifiedName() + ");" + NEWLINE);
            writeString(NEWLINE);
        }
        
        // write out instance traits for script block
        for (EmitterClassVisitor clz : this.definedClasses)
        {
            InstanceInfo ii = clz.instanceInfo;

            // Skipping classes that are "marked" as IExtern.
            final Boolean isExtern = isExtern(ii);
            if (isExtern)
                return;

            final Boolean isInterface = ii.isInterface();
            final Boolean isPackageFunction = ii.name == null;

            String packageName;
            String className;
            if (ii.name != null)
            {
                final IDefinition def = getDefinition(ii.name);
                packageName = def.getPackageName();
                className = JSGeneratingReducer.getBasenameFromName(ii.name);
            }
            else
            {
                packageName = m_packageName;
                className = "";
            }
            // register class with super class
            final IDefinition superClassDef = getDefinition(ii.superName);
            final String superClassName = superClassDef == null ? "Object" : superClassDef.getQualifiedName();
        	emitTraits(clz.instanceTraits, true, isExtern, isInterface, isPackageFunction, (MethodInfo)null, packageName, className, superClassName, "this.", "", ",", "\t");
        }
        writeString(NEWLINE);
        writeString(NEWLINE);
        
		for (String s : cdp.fragments)
		{
			writeString(s);
            writeString(NEWLINE);
            writeString(NEWLINE);
		}
	}
}