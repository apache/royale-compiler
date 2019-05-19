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

package org.apache.royale.compiler.ant;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Environment.Variable;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.taskdefs.Java;

import org.apache.royale.compiler.ant.config.ConfigVariable;
import org.apache.royale.compiler.ant.config.NestedAttributeElement;
import org.apache.royale.compiler.ant.config.IOptionSource;
import org.apache.royale.compiler.ant.config.OptionSpec;

/**
 * This class contains common data and logic used by all the Flex Ant tasks.
 */
public abstract class FlexTask extends Java
{
    protected static OptionSpec RUNTIME_SHARED_LIBRARY_PATH =
    	new OptionSpec("runtime-shared-library-path", "rslp");

    /**
     * Constructor.
     * 
     * @param taskName The name of the Ant task.
     * @param configVariables An array of ConfigVariables that will be set by attributes of the task.
     * @param toolJARFileName The build tool's jar file.
     * @param toolClassName The build tool's class name.
     * @param toolMethodName The build tool's method name.
     * @param toolFailureMethodName The build tool's method to determine
     * whether an exit code means failure.
     */
    protected FlexTask(String taskName, ConfigVariable[] configVariables,
    		           String toolJARFileName, String toolClassName,
    		           String toolMethodName, String toolFailureMethodName)
    {
    	this.taskName = taskName;
        this.configVariables = configVariables;
        this.toolJARFileName = toolJARFileName;
        this.toolClassName = toolClassName;
        this.toolMethodName = toolMethodName;
        this.toolFailureMethodName = toolFailureMethodName;

        cmdline = new Commandline();
    }

	/**
	 * The name of the Ant task.
	 */
	private final String taskName;
	
    /**
     * An array of ConfigVariabes that are set by setDynamicAttribute().
     */
    protected final ConfigVariable[] configVariables;

    /**
     * The build tool's jar file name.
     */
    private final String toolJARFileName;

    /**
     * The build tool's class name.
     */
    private final String toolClassName;

    /**
     * The build tool's entry point.
     * It must be a static method that takes a String[]
     * and returns an int exit code.
     */
    private final String toolMethodName;
    
    /**
     * The build tools' method that determines whether the
     * exit code should make the Ant task fail.
     * It must be a static method that takes an int
     * and returns a boolean.
     */
    private final String toolFailureMethodName;

    /**
     * The commandline used in execute()
     */
    protected final Commandline cmdline;

    protected List<IOptionSource> nestedAttribs;

    /**
     * fork attribute
     */
    protected boolean fork;
    
    private ClassLoader originalContextClassLoader;

    protected NestedAttributeElement createElem(String attrib, OptionSpec spec)
    {
        NestedAttributeElement e = new NestedAttributeElement(attrib, spec, this);
        nestedAttribs.add(e);
        return e;
    }

    protected NestedAttributeElement createElem(String[] attribs, OptionSpec spec)
    {
        NestedAttributeElement e = new NestedAttributeElement(attribs, spec, this);
        nestedAttribs.add(e);
        return e;
    }
    
    protected NestedAttributeElement createElemAllowAppend(String[] attribs, OptionSpec spec)
    {
        NestedAttributeElement e = new NestedAttributeElement(attribs, spec, this, true);
        nestedAttribs.add(e);
        return e;
    }

    /*=======================================================================*
     * 	Static Attributes                                                    *
     *=======================================================================*/
    /**
     * Sets whether to run the task in a separate VM.
     *
     * @param f if true then run in a separate VM.
     */
    public void setFork(boolean f)
    {
        super.setFork(f);
        this.fork = f;
    }

    /*=======================================================================*
     *  Dynamic Attributes                                                   *
     *=======================================================================*/

    /**
     * Set the named attribute to the given value.
     *
     * @param attributeName The name of the attribute to set
     * @param value The value to set the named attribute to
     */
    public void setDynamicAttribute(String attributeName, String value)
    {
        ConfigVariable var = null;

        for (int i = 0; i < configVariables.length && var == null; i++)
        {
            if (configVariables[i].matches(attributeName))
                var = configVariables[i];
        }

        if (var != null)
        {
            var.set(value);
        }
        else
        {
            throw new BuildException("The <" + taskName + "> type doesn't support the \"" +
                                     attributeName + "\" attribute.", getLocation());
        }
    }

    /*=======================================================================*
     *  Dynamic Elements                                                     *
     *=======================================================================*/

    public Object createDynamicElement(String elementName)
    {
        ConfigVariable var = null;

        for (int i = 0; i < configVariables.length && var == null; i++)
        {
            if (configVariables[i].matches(elementName))
                var = configVariables[i];
        }

        if (var != null)
        {
            return createElem(elementName, var.getSpec());
        }
        else
        {
            throw new BuildException("The <" + taskName + "> type doesn't support the \"" +
                                     elementName + "\" nested element.", getLocation());
        }
    }

    /*=======================================================================*
     *  Execute and Related Functions                                        *
     *=======================================================================*/

    /**
     * Called by execute after the set ConfigVariables in <code>vars</code> has
     * been added to the commandline. This function is responsible for adding
     * all tool-specific options to the commandline as well as setting the
     * default options of a build tool.
     */
    protected abstract void prepareCommandline() throws BuildException;

    /**
     * Execute the task
     *
     * @throws BuildException If running build tool failed
     */
    public final void execute() throws BuildException
    {
        String royaleHomeProperty = getProject().getProperty("ROYALE_HOME");
        if (royaleHomeProperty == null)
            royaleHomeProperty = getProject().getProperty("FLEX_HOME");

        if (royaleHomeProperty == null)
            throw new BuildException("ROYALE_HOME or FLEX_HOME must be set to use the Flex Ant Tasks");
		
        String compilerHomeProperty = getProject().getProperty("ROYALE_COMPILER_HOME");
        if (compilerHomeProperty == null)
            throw new BuildException("ROYALE_COMPILER_HOME must be set to use the Flex Ant Tasks");
				
        System.setProperty("ROYALE_HOME", royaleHomeProperty);
        String royalelibProperty = royaleHomeProperty.concat("/frameworks/");
		System.setProperty("royalelib", royalelibProperty);

        final Variable variable = new Variable();
        variable.setKey("royalelib");
        variable.setValue(royaleHomeProperty);
		addSysproperty(variable);
        
        // This allows the tool to find the default config file.
        cmdline.createArgument().setValue("+royalelib=" + royalelibProperty);
        
        prepareCommandline();

        if (fork)
            executeOutOfProcess();
        else
            executeInProcess();
    }

    /**
     * Executes the task in a separate VM
     */
    private void executeOutOfProcess() throws BuildException
    {
        try
        {
        	// Without this, the tool class won't be found by executeJava().
        	Class<?> toolClass = resolveClass(toolClassName);
        	
            super.setClassname(toolClassName);

            // convert arguments into a string for use by executeJava()
            // also auto-quotes arguments with spaces
            String line = Commandline.toString(cmdline.getArguments());
            super.createArg().setLine(line);

            int exitCode = super.executeJava();

            // Check exit code.
            if (isFatalFailure(toolClass, exitCode))
                throw new BuildException(taskName + " task failed.");
        }
        finally
        {
            if (originalContextClassLoader != null)
                Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }

    /**
     * Executes the task in the same VM
     */
    private void executeInProcess() throws BuildException
    {
        try
        {
            Class<?> toolClass = resolveClass(toolClassName);

            log("FlexTask.execute: " + cmdline, Project.MSG_DEBUG);
            
            int exitCode = -1;

            try
            {
                Method toolMethod = toolClass.getMethod(toolMethodName, String[].class);
                Object result = toolMethod.invoke(null, new Object[] {cmdline.getArguments()});
                exitCode = ((Integer)result);
            }
            catch (Exception e)
            {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                log(stringWriter.toString(), Project.MSG_DEBUG);
                throw new BuildException("Unable to run " + toolMethodName + ": " + e.getMessage(), e);
            }

            if (isFatalFailure(toolClass, exitCode))
                throw new BuildException(taskName + " task failed");//            }
        }
        finally
        {
            if (originalContextClassLoader != null)
                Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }

    private boolean isFatalFailure(Class<?> toolClass, int exitCode)
    {
    	boolean fatal = true;
    	
    	try
    	{
			Method toolFailureMethod = toolClass.getMethod(toolFailureMethodName, int.class);
			Object result = toolFailureMethod.invoke(null, exitCode);
			fatal = ((Boolean)result);
		}
    	catch (Exception e)
    	{
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            log(stringWriter.toString(), Project.MSG_DEBUG);
            throw new BuildException("Unable to run " + toolFailureMethodName + ": " + e.getMessage(), e);
		}
    	
    	return fatal;
    }

    private Class<?> resolveClass(String className)
    {
        Class<?> result = null;

        try
        {
            result = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException ignoredClassNotFoundException)
        {
            String royaleHomeProperty = getProject().getProperty("ROYALE_COMPILER_HOME");

            if (royaleHomeProperty != null)
            {
                File royaleHome = new File(royaleHomeProperty);

                if ( royaleHome.exists() )
                {
                    File jarFile = new File(royaleHome + "/lib", toolJARFileName);

                    if (jarFile.exists())
                    {
                        try
                        {
                        	URL url = jarFile.toURI().toURL();
                            URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {url});
                            result = Class.forName(className, true, urlClassLoader);
                            originalContextClassLoader = Thread.currentThread().getContextClassLoader();
                            Thread.currentThread().setContextClassLoader(urlClassLoader);
                            
							if (fork)
								super.setClasspath(new Path(getProject(), jarFile.getAbsolutePath()));
                        }
                        catch (MalformedURLException malformedURLException)
                        {
                            // We shouldn't really get here, but just in case.
                            malformedURLException.printStackTrace();
                        }
                        catch (ClassNotFoundException classNotFoundException)
                        {
                            throw new BuildException("The class " + className + " was not found in jar file " + toolJARFileName,
                                                     getLocation());
                        }
                    }
                    else
                    {
                        throw new BuildException("File does not exist: " + toolJARFileName, getLocation());
                    }
                }
                else
                {
                    throw new BuildException("ROYALE_COMPILER_HOME does not exist.", getLocation());
                }
            }
            else
            {
                throw new BuildException("The class, " + className +
                                         ", must be in the classpath or the ROYALE_COMPILER_HOME property must be set.",
                                         getLocation());
            }
        }

        return result;
    }
}
