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

package org.apache.royale.compiler.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.royale.compiler.internal.config.annotations.DefaultArgumentValue;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

/**
 * Meta information for each configuration options. It is created by
 * {@link ConfigurationBuffer#loadCache} from either annotations or
 * {@code public static ConfigurationInfo getFooInfo();} methods in
 * {@link Configuration} class.
 */
public class ConfigurationInfo
{
    public static final int NOT_SET = -2;
    public static final int INFINITE_ARGS = -1;

    /**
     * This ctor is used when everything can be introspected off the setter
     * method, or else when the names/types are provided by method overrides
     * rather than ctor arguments
     */
    public ConfigurationInfo()
    {
        this.argcount = NOT_SET;
        this.argnames = null;
    }

    /**
     * Simple ctor for restricting the number of arguments.
     * 
     * @param argcount number of args, -1 for an infinite list
     */
    public ConfigurationInfo(int argcount)
    {
        this.argcount = argcount;
        this.argnames = null;
    }

    /**
     * Simple ctor for naming the arguments.
     * 
     * @param argnames list of argnames, argcount will default to # of elements
     */
    public ConfigurationInfo(String argnames[])
    {
        this.argcount = argnames.length;
        this.argnames = argnames;
    }

    /**
     * Use this ctor when you want to set a single list of some number of
     * identically named args
     * 
     * @param argcount number of arguments (-1 for infinite)
     * @param argname name of each argument
     */
    public ConfigurationInfo(int argcount, String argname)
    {
        this.argcount = argcount;
        this.argnames = new String[] {argname};
    }

    /**
     * More unusual ctor, this would let you have the first few args named one
     * thing, the rest named something else. It is far more likely that you want
     * a constrained list of names or else an arbitrary list of identical names.
     * 
     * @param argcount number of arguments
     * @param argnames array of argument names
     */
    public ConfigurationInfo(int argcount, String argnames[])
    {
        this.argcount = argcount;
        this.argnames = argnames;
    }

    public final int getArgCount()
    {
        return argcount;
    }

    protected int argcount = NOT_SET;

    protected String[] defaultArgValues = null;

    /**
     * Get any default values for an argument
     * 
     * @return an array of default argument values.  May be null
     */
    public final String[] getDefaultArgValues()
    {
        return defaultArgValues;
    }

    private static String classToArgName(Class<?> c)
    {
        // we only support builtin classnames!

        String className = c.getName();
        if (className.startsWith("java.lang."))
            className = className.substring("java.lang.".length());

        return className.toLowerCase();
    }

    /**
     * Return the name of each parameter. The default implementation is usually
     * sufficient for simple cases, but one could do wacky things here like
     * support an infinite list of alternating arg names.
     * 
     * @param argnum The argument number.
     * @return name of argument
     */
    public String getArgName(int argnum)
    {
        if (argNameGeneratorClass != null)
        {
            Method getArgNameMethod;
            try
            {
                getArgNameMethod = argNameGeneratorClass.getMethod("getArgumentName", int.class);
                return (String)getArgNameMethod.invoke(null, argnum);
            }
            catch (Exception e)
            {
                // TODO: connect these exception to our problem logging subsystem.
                e.printStackTrace();
            }

            return "";
        }

        if ((argnames == null) || (argnames.length == 0))
        {
            return classToArgName(getArgType(argnum));
        }
        else if (argnum >= argnames.length)
        {
            return argnames[argnames.length - 1];
        }
        else
        {
            return argnames[argnum];
        }
    }

    /**
     * Return the type of each parameter. This is computed based on your setter,
     * and cannot be overridden
     * 
     * @param argnum The argument number.
     */
    public final Class<?> getArgType(int argnum)
    {
        if (argnum >= argtypes.length)
        {
            return argtypes[argtypes.length - 1];
        }
        else
        {
            return argtypes[argnum];
        }
    }

    protected Class<?> argNameGeneratorClass;
    protected String[] argnames;
    protected Class<?>[] argtypes;

    protected String[] prerequisites = null;

    /**
     * Return variable names that should be set before this one. The buffer is
     * always set such that it tries to set all variables at a given level
     * before setting child values, but you could override by using this. Its
     * probably a bad idea to depend on children, though. It is unnecessary to
     * set parent vars as prerequisites, since they are implicitly set first
     */
    public String[] getPrerequisites()
    {
        return prerequisites;
    }

    protected String[] softPrerequisites = null;

    /**
     * Prerequisites which should be set before this one if they exist
     */
    public String[] getSoftPrerequisites()
    {
        return softPrerequisites;
    }

    protected boolean allowMultiple = false;

    /**
     * Variables are generally only allowed to be set once in a given
     * file/cmdline. It is sometimes useful to allow the same set multiple times
     * in order to aggregate values.
     * 
     * @return true if the setter can be called multiple times
     */
    public boolean allowMultiple()
    {
        return allowMultiple;
    }

    protected String[] aliases = null;

    /**
     * Return an array of other names for this variable.
     */
    public String[] getAliases()
    {
        return aliases;
    }

    protected boolean isAdvanced = false;

    /**
     * Override to make a variable hidden by default (i.e. you need -advanced on
     * the cmdline)
     */
    public boolean isAdvanced()
    {
        return isAdvanced;
    }

    protected boolean isHidden = false;

    /**
     * Override to make a variable completely hidden
     */
    public boolean isHidden()
    {
        return isHidden;
    }

    protected boolean isDisplayed = true;

    /**
     * Override to prevent printing when dumping configuration
     */
    public boolean isDisplayed()
    {
        return isDisplayed;
    }

    /**
     * If a variable -must- be set, override this
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    protected boolean isRequired = false;

    /**
     * Magic used by the command line configurator only at the moment to decide
     * whether this variable should eat all subsequent arguments. Useful for
     * -help...
     */
    public boolean isGreedy()
    {
        return isGreedy;
    }

    protected boolean isGreedy = false;

    public boolean isPath()
    {
        return isPath;
    }

    protected boolean isPath = false;

    public boolean doChecksum()
    {
        return true;
    }

    public String getDeprecatedMessage()
    {
        return deprecatedMessage;
    }

    protected String deprecatedMessage = null;

    public boolean isDeprecated()
    {
        return isDeprecated;
    }

    protected boolean isDeprecated = false;

    public String getDeprecatedReplacement()
    {
        return deprecatedReplacement;
    }

    protected String deprecatedReplacement;

    public String getDeprecatedSince()
    {
        return deprecatedSince;
    }

    protected String deprecatedSince;
    
    /**
     * @return True indicates that the option is no longer 
     * supported and will not have any affect.
     */
    public boolean isRemoved() 
    {
        return isRemoved;
    }

    protected boolean isRemoved = false;
    
    /**
     * @return True the option requires Flex in order to be useful. 
     */
    public boolean isRoyaleOnly() 
    {
        return isRoyaleOnly;
    }

    protected boolean isRoyaleOnly = false;
    
    protected final void setSetterMethod(Method setter)
    {
        Class<?>[] pt = setter.getParameterTypes();

        assert (pt.length >= 2) : ("coding error: config setter must take at least 2 args!");

        this.setter = setter;

        if (pt.length == 2)
        {
            Class<?> c = pt[1];

            if (ConfigurationBuffer.isSupportedListType(c))
            {
                if (argcount == NOT_SET)
                    argcount = -1; // infinite list

                argtypes = new Class[] {String.class};
                return;
            }
            else if (ConfigurationBuffer.isSupportedValueType(c))
            {
                assert (argcount == NOT_SET) : ("coding error: value object setter cannot override argcount");
                assert (argnames == null) : ("coding error: value object setter cannot override argnames");

                Field[] fields = c.getFields();

                argcount = fields.length;

                assert (argcount > 0) : ("coding error: " + setter + " value object " + c.getName() + " must contain at least one public field");

                argnames = new String[fields.length];
                argtypes = new Class[fields.length];

                for (int f = 0; f < fields.length; ++f)
                {
                    argnames[f] = ConfigurationBuffer.c2h(fields[f].getName());
                    argtypes[f] = fields[f].getType();
                }
                return;
            }
        }

        assert ((argcount == NOT_SET) || (argcount == pt.length - 1)) : ("coding error: the argument count must match the number of setter arguments");
        // We've taken care of lists and value objects, from here on out, it must match the parameter list.

        argcount = pt.length - 1;

        DefaultArgumentValue defaultArgValuesAnno = setter.getAnnotation(DefaultArgumentValue.class);
        if (defaultArgValuesAnno != null)
            defaultArgValues = defaultArgValuesAnno.value();

        argtypes = new Class[pt.length - 1];
        for (int i = 1; i < pt.length; ++i)
        {
            assert (ConfigurationBuffer.isSupportedSimpleType(pt[i])) : ("coding error: " + setter.getClass().getName() + "." + setter.getName() + " parameter " + i + " is not a supported type!");
            argtypes[i - 1] = pt[i];
        }
    }

    protected final Method getSetterMethod()
    {
        return setter;
    }

    private Method setter;
    private Method getter;

    protected final void setGetterMethod(Method getter)
    {
        this.getter = getter;
    }

    protected final Method getGetterMethod()
    {
        return getter;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper("")
                .add("alias", arrayAsString(getAliases()))
                .add("argcount", getArgCount())
                .add("argnames", arrayAsString(argnames))
                .add("argtypes", arrayAsString(argtypes))
                .add("deprecated", isDeprecated())
                .add("deprecatedMessage", getDeprecatedMessage())
                .add("deprecatedReplacement", getDeprecatedReplacement())
                .add("deprecatedSince", getDeprecatedSince())
                .add("getter", getGetterMethod() == null ? "null" : getGetterMethod().getName())
                .add("setter", getSetterMethod() == null ? "null" : getSetterMethod().getName())
                .add("required", isRequired())
                .add("Prerequisites", arrayAsString(getPrerequisites()))
                .add("softPrerequisites", arrayAsString(getSoftPrerequisites()))
                .add("advanced", isAdvanced())
                .add("allow multiple", allowMultiple())
                //.add("doChecksum", doChecksum())
                .add("displayed", isDisplayed())
                .add("greedy", isGreedy())
                .add("hidden", isHidden())
                .add("removed", isRemoved())
                .add("path", isPath())
                .toString();
    }

    private String arrayAsString(Object[] array)
    {
        if (array == null)
            return "";
        else
            return "[" + Joiner.on(",").join(array) + "]";
    }

    /**
     * True if only {@code compc} client can use this option.
     */
    public boolean isCompcOnly = false;
}
