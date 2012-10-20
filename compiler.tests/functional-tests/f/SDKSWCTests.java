package f;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.clients.COMPC;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests to compile the SWCs of the Flex SDK.
 * <p>
 * The projects to compile are in the <code>frameworks/projects</code> directory
 * referenced by the <code>FLEX_HOME</code> environment variable.
 * Each project has a config file which the <code>COMPC</code> uses to compile the SWC.
 * 
 * @author Gordon Smith
 */
public class SDKSWCTests
{
	private void compileSWC(String projectName)
	{
		// Construct a command line which simply loads the project's config file.
		String configFilePath = System.getenv("FLEX_HOME") +
		    "/frameworks/projects/" + projectName + "/" + projectName + "-config.xml";
		String[] args = new String[] { "-load-config=" + configFilePath };
		
		// Run the COMPC client with the specified command line.
		COMPC compc = new COMPC();
		compc.mainNoExit(args);
		
		// Check that the SWC compiled cleanly.
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		for (ICompilerProblem problem : compc.getProblems().getFilteredProblems())
		{
			problems.add(problem);
		}
		assertThat(problems.size(), is(0));
	}
	
	@Ignore
	@Test
	public void frameworkSWC()
	{
		compileSWC("framework");
	}
	
	@Ignore
	@Test
	public void rpcSWC()
	{
		compileSWC("rpc");
	}
	
	@Ignore
	@Test
	public void textLayoutSWC()
	{
		compileSWC("textLayout");
	}
	
	@Ignore
	@Test
	public void mxSWC()
	{
		compileSWC("mx");
	}
	
	@Ignore
	@Test
	public void sparkSWC()
	{
		compileSWC("spark");
	}
	
	// others...
}
