/**
 * 
 */
package org.apache.flex.compiler.internal.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.flex.compiler.asdoc.IASDocBundleDelegate;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.units.ICompilationUnit;

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
    public FlexJSProject(Workspace workspace)
    {
        super(workspace);
    }

    private HashMap<ICompilationUnit, ArrayList<String>> requires = new HashMap<ICompilationUnit, ArrayList<String>>();
    private HashMap<String, String> already = new HashMap<String, String>();
    
    @Override
    public void addDependency(ICompilationUnit from, ICompilationUnit to, DependencyType dt, String qname)
    {
    	ArrayList<String> reqs;
    	if (requires.containsKey(from))
    		reqs = requires.get(from);
    	else
    	{
    		reqs = new ArrayList<String>();
    		requires.put(from, reqs);
    	}
    	// if the class is already required by some other class
    	// don't add it.  Otherwise we can get circular
    	// dependencies.
    	boolean circular = false;
    	if (requires.containsKey(to))
    	{
    		if (already.containsKey(qname))
    			circular = true;
    	}
    	if (!circular || dt == DependencyType.INHERITANCE)
    	{
    		reqs.add(qname);
    		already.put(qname, qname);
    	}
        super.addDependency(from, to, dt, qname);
    }
    
    public ArrayList<String> getRequires(ICompilationUnit from)
    {
    	if (requires.containsKey(from))
    		return requires.get(from);
    	return null;
    }
}
