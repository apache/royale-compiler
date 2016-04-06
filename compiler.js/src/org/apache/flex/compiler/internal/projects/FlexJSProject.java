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
    	// if the from unit is already in the requires list for the to compilation unit
    	// then don't add as requires otherwise we get circularities
    	// that the closure compiler can't handle
    	boolean circular = false;
    	if (requires.containsKey(to))
    	{
    		try {
				List<String> qnames = from.getQualifiedNames();
	    	    ArrayList<String> targetReqs = requires.get(to);
	    		if (targetReqs.contains(qnames.get(0)))
	    			circular = true;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if (!circular)
    		reqs.add(qname);
        super.addDependency(from, to, dt, qname);
    }
    
    public ArrayList<String> getRequires(ICompilationUnit from)
    {
    	if (requires.containsKey(from))
    		return requires.get(from);
    	return null;
    }
}
