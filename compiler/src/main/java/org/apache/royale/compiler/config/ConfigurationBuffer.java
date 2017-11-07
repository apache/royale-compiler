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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.config.IConfigurationFilter;
import org.apache.royale.compiler.internal.config.annotations.ArgumentNameGenerator;
import org.apache.royale.compiler.internal.config.annotations.Arguments;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.RoyaleOnly;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
import org.apache.royale.compiler.internal.config.annotations.SoftPrerequisites;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.utils.Trace;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * The basic idea here is to let you keep all your configuration knowledge in
 * your configuration object, and to automate as much as possible. Reflection is
 * used to convert public fields and setters on your configuration object into
 * settable vars. There are a few key concepts:
 * <p>
 * - You should be able to configure absolutely any object.<br>
 * - Child configuration variables in your config become a dotted hierarchy of
 * varnames<br>
 * - All sources of configuration data are buffered and merged (as string
 * var/vals) before committing to the final configuration. This class acts as
 * the buffer.<br>
 * - Hyphenated variables (i.e. "some-var") are automatically configured by
 * calling your matching setter (i.e. setSomeVar)<br>
 * - Implementing an getSomeVarInfo() method on your class lets you set up more
 * complicated config objects<br>
 * - You can make variables depend on other variables having been set first.
 * This lets you set a root directory in one var and then use its value in
 * another.<br>
 * - Per-variable validation can be performed in setters. Overall validation
 * should take place as a post-process step.<br>
 * - You can keep ConfigurationBuffers around and merge multiple buffers
 * together before committing. Most recent definitions always win.<br>
 * <p>
 * The contract with your configuration class:
 * <p>
 * - You must provide a method with the signature
 * "void setYourVar(ConfigurationValue val)" to set your config var. Your setter
 * method should accept either a single arg of type List or String[], or else an
 * arglist of simple types. For example
 * "void myvar(int a, boolean b, String c")".<br>
 * - You can implement a function with the signature "int yourvar_argcount()" to
 * require a different number of arguments. This limit will be enforced by
 * configurators (command line, file, etc.)<br>
 * - If you provide a setter and explicit parameters (i.e. not List or String[])
 * the number of arguments will be automatically determined.<br>
 * - Each argument to your configuration variable is assumed to have a
 * (potentially non-unique) name. The default is the simple type of the argument
 * (boolean, int, string). If the var takes an undetermined number of args via
 * List or String[], the argname defaults to string.<br>
 * - You can implement a function with the signature
 * "String yourvar_argnames(int)" to provide names for each of the parameters.
 * The integer passed in is the argument number. Return the same name (i.e.
 * "item") for infinite lists.<br>
 * - You can implement a function with the signature "String[] yourvar_deps()"
 * to provide a list of other prerequisites for this var. You will be guaranteed
 * that the deps are committed before your var, or else a configurationexception
 * will be thrown if a prerequsite was unset. (Note that infinite cycles are not
 * checked, so be careful.)<br>
 */
public final class ConfigurationBuffer
{
    public ConfigurationBuffer(Class<? extends Configuration> configClass)
    {
        this(configClass, new HashMap<String, String>());
    }

    public ConfigurationBuffer(Class<? extends Configuration> configClass, Map<String, String> aliases)
    {
        this(configClass, aliases, null);
    }

    /**
     * Create a configuration buffer with an optional filter. The filter can be
     * used to remove unwanted options from a super class.
     * 
     * @param filter if null there is no filter, otherwise the set of
     * configuration options is filtered.
     */
    public ConfigurationBuffer(Class<? extends Configuration> configClass, Map<String, String> aliases, IConfigurationFilter filter)
    {
        this.configClass = configClass;
        this.varMap = new HashMap<String, List<ConfigurationValue>>();
        this.committed = new HashSet<String>();

        loadCache(configClass, filter);
        assert (varCache.size() > 0) : "coding error: nothing was configurable in the provided object!";
        for (Map.Entry<String, String> e : aliases.entrySet())
        {
            addAlias(e.getKey(), e.getValue());
        }
    }

    public ConfigurationBuffer(ConfigurationBuffer copyFrom, boolean copyCommitted)
    {
        this.configClass = copyFrom.configClass;
        this.varMap = new HashMap<String, List<ConfigurationValue>>(copyFrom.varMap);
        this.committed = copyCommitted ? new HashSet<String>(copyFrom.committed) : new HashSet<String>();
        this.varCache = copyFrom.varCache; // doesn't change after creation
        this.varList = copyFrom.varList; // doesn't change after creation
        this.tokens = new HashMap<String, String>(copyFrom.tokens);
    }

    public final List<String> dump()
    {
        final List<String> dump = new ArrayList<String>(varCache.size());
        for (final Map.Entry<String, ConfigurationInfo> entry : varCache.entrySet())
        {
            dump.add(entry.getKey() + "," + entry.getValue().toString());
        }
        Collections.sort(dump);
        return dump;
    }

    public void setVar(String var, String val, String source, int line) throws ConfigurationException
    {
        List<String> list = new LinkedList<String>();
        list.add(val);
        setVar(var, list, source, line, null, false);
    }

    public void setVar(String var, List<String> vals, String source, int line) throws ConfigurationException
    {
        setVar(var, vals, source, line, null, false);
    }

    public void setVar(String avar, List<String> vals, String source, int line, String contextPath, boolean append) throws ConfigurationException
    {
        String var = unalias(avar);
        if (!isValidVar(var))
            throw new ConfigurationException.UnknownVariable(var, source, line);

        int argCount = getVarArgCount(var);

        // -1 means unspecified length, its up to the receiving setter to validate.
        if (argCount != -1)
        {
            addAnyDefaultArgValues(var, argCount, vals);

            if (vals.size() != argCount)
            {
                throw new ConfigurationException.IncorrectArgumentCount(argCount, // expected
                        vals.size(), //passed
                var, source, line);
            }
        }

        ConfigurationValue val = new ConfigurationValue(this, var,
                                                         vals, //processValues( var, vals, source, line ),
                                                         source, line, contextPath);
        storeValue(var, val, append);
        committed.remove(var);
    }

    public void clearVar(String avar, String source, int line) throws ConfigurationException
    {
        String var = unalias(avar);
        if (!isValidVar(var))
            throw new ConfigurationException.UnknownVariable(var, source, line);
        varMap.remove(var);
        committed.remove(var);
    }

    /**
     * Remove the configuration values came from the given source.
     * 
     * @param source source name
     * @see CommandLineConfigurator#SOURCE_COMMAND_LINE
     */
    public void clearSourceVars(String source)
    {
        List<String> remove = new LinkedList<String>();
        for (Map.Entry<String, List<ConfigurationValue>> e : varMap.entrySet())
        {
            String var = e.getKey();
            List<ConfigurationValue> vals = e.getValue();

            List<ConfigurationValue> newvals = new LinkedList<ConfigurationValue>();
            for (ConfigurationValue val : vals)
            {
                if (!val.getSource().equals(source))
                {
                    newvals.add(val);
                }
            }
            if (newvals.size() > 0)
                varMap.put(var, newvals);
            else
                remove.add(var);
        }
        for (Iterator<String> it = remove.iterator(); it.hasNext();)
        {
            varMap.remove(it.next());
        }
    }

    public List<String> processValues(String var, List<String> args, String source, int line) throws ConfigurationException
    {
        List<String> newArgs = new LinkedList<String>();
        for (Iterator<String> it = args.iterator(); it.hasNext();)
        {
            String arg = it.next();

            int depth = 100;
            while (depth-- > 0)
            {
                int o = arg.indexOf("${");
                if (o == -1)
                    break;

                int c = arg.indexOf("}", o);

                if (c == -1)
                {
                    throw new ConfigurationException.Token(ConfigurationException.Token.MISSING_DELIMITER,
                                                           null, var, source, line);
                }
                String token = arg.substring(o + 2, c);
                String value = getToken(token);

                if (value == null)
                {
                    if (value == null)

                    {
                        throw new ConfigurationException.Token(ConfigurationException.Token.UNKNOWN_TOKEN,
                                                                token, var, source, line);
                    }

                }
                arg = arg.substring(0, o) + value + arg.substring(c + 1);

            }
            if (depth == 0)
            {
                throw new ConfigurationException.Token(ConfigurationException.Token.RECURSION_LIMIT,
                                                        null, var, source, line);
            }

            newArgs.add(arg);
        }
        return newArgs;
    }

    public void setToken(String token, String value)
    {
        tokens.put(token, value);
    }

    public String getToken(String token)
    {
        if (tokens.containsKey(token))
            return tokens.get(token);
        else
        {
            try
            {
                return System.getProperty(token);
            }
            catch (SecurityException se)
            {
                return null;
            }
        }
    }

    private void storeValue(String avar, ConfigurationValue val, boolean append) throws ConfigurationException
    {
        String var = unalias(avar);
        ConfigurationInfo info = getInfo(var);

        List<ConfigurationValue> vals;
        if (varMap.containsKey(var))
        {
            vals = varMap.get(var);
            assert (vals.size() > 0);
            ConfigurationValue first = vals.get(0);
            if (!append && !first.getSource().equals(val.getSource()))
                vals.clear();
            else if (!info.allowMultiple())
                throw new ConfigurationException.IllegalMultipleSet(
                                                  var,
                                                  val.getSource(), val.getLine());
        }
        else
        {
            vals = new LinkedList<ConfigurationValue>();
            varMap.put(var, vals);
        }
        vals.add(val);
    }

    public List<ConfigurationValue> getVar(String avar)
    {
        String var = unalias(avar);
        return varMap.get(var);
    }

    public Set<String> getVars()
    {
        return varCache.keySet();
    }

    public void merge(ConfigurationBuffer other)
    {
        assert (configClass == other.configClass);
        varMap.putAll(other.varMap);
        committed.addAll(other.committed);
    }

    private final Map<String, List<ConfigurationValue>> varMap; // list of vars that have been set
    private final Set<String> committed; // set of vars committed to backing config
    private final Class<? extends Configuration> configClass; // configuration class
    private Map<String, ConfigurationInfo> varCache // info cache
    = new HashMap<String, ConfigurationInfo>();
    private List<String> requiredList = new LinkedList<String>(); // required vars
    private List<String> varList = new LinkedList<String>(); // list of vars in order they should be set
    private Map<String, String> aliases = new HashMap<String, String>(); // variable name aliases
    private Map<String, String> tokens = new HashMap<String, String>(); // tokens for replacement
    private List<Object[]> positions = new ArrayList<Object[]>();

    private static final String SET_PREFIX = "cfg";
    private static final String GET_PREFIX = "get";
    private static final String INFO_SUFFIX = "Info";

    //-----------------------------------------------
    //

    /**
     * WORKAROUND FOR BUG CMP-396
     * 
     * <p>
     * {@link #c2h(String)} generates option names based on cfgXXX names in
     * {@code Configuration}. Since we collapsed all the sub-configurations into
     * one class, there's no longer a "base name" like "compiler.*" or
     * "compiler.fonts.*". In order to preserve the dotted naming convention, we
     * need to know which "-" separated names are actually dotted names. The
     * {@link #CONVERT_FROM} and {@link #CONVERT_TO} is an <b>ordered</b> lookup
     * table for option group base names. It's order makes sure that the longest
     * possible replacement is done.
     */
    private static final ImmutableList<String> CONVERT_FROM =
            ImmutableList.of(
                    "compiler-fonts-languages-",
                    "compiler-fonts-",
                    "compiler-namespaces-",
                    "compiler-mxml-",
                    "compiler-",
                    "metadata-",
                    "licenses-",
                    "frames-",
                    "runtime-shared-library-settings-");

    private static final ImmutableList<String> CONVERT_TO =
            ImmutableList.of(
                    "compiler.fonts.languages.",
                    "compiler.fonts.",
                    "compiler.namespaces.",
                    "compiler.mxml.",
                    "compiler.",
                    "metadata.",
                    "licenses.",
                    "frames.",
                    "runtime-shared-library-settings.");

    /**
     * convert StudlyCaps or camelCase to hyphenated
     * 
     * @param camel someVar or SomeVar
     * @return hyphen some-var
     */
    protected static String c2h(String camel)
    {
        StringBuilder b = new StringBuilder(camel.length() + 5);
        for (int i = 0; i < camel.length(); ++i)
        {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c))
            {
                if (i != 0)
                    b.append('-');
                b.append(Character.toLowerCase(c));
            }
            else
            {
                b.append(camel.charAt(i));
            }
        }
        final String combined = b.toString();

        for (int i = 0; i < CONVERT_FROM.size(); i++)
        {
            if (combined.startsWith(CONVERT_FROM.get(i)))
            {
                return combined.replaceFirst(CONVERT_FROM.get(i), CONVERT_TO.get(i));
            }
        }
        return combined;
    }

    /**
     * convert hyphenated to StudlyCaps or camelCase
     * 
     * @param hyphenated some-var
     * @return result
     */
    protected static String h2c(String hyphenated, boolean studly)
    {
        StringBuilder b = new StringBuilder(hyphenated.length());
        boolean capNext = studly;
        for (int i = 0; i < hyphenated.length(); ++i)
        {
            char c = hyphenated.charAt(i);
            if (c == '-')
                capNext = true;
            else
            {
                b.append(capNext ? Character.toUpperCase(c) : c);
                capNext = false;
            }
        }
        return b.toString();
    }

    public static String varname(String membername, String basename)
    {
        return ((basename == null) ? membername : (basename + "." + membername));
    }

    private static ConfigurationInfo createInfo(Method setterMethod)
    {
        ConfigurationInfo info = null;

        String infoMethodName = GET_PREFIX + setterMethod.getName().substring(SET_PREFIX.length()) + INFO_SUFFIX;
        String getterMethodName = GET_PREFIX + setterMethod.getName().substring(SET_PREFIX.length());
        @SuppressWarnings("unchecked")
        Class<? extends Configuration> cfgClass = (Class<? extends Configuration>)setterMethod.getDeclaringClass();

        Method infoMethod = null, getterMethod = null;
        if (!setterMethod.isAnnotationPresent(Config.class))
        {
            try
            {
                infoMethod = cfgClass.getMethod(infoMethodName);
    
                if (!Modifier.isStatic(infoMethod.getModifiers()))
                {
                    assert false : ("coding error: " + cfgClass.getName() + "." + infoMethodName + " needs to be static!");
                    infoMethod = null;
                }
    
                info = (ConfigurationInfo)infoMethod.invoke(null, (Object[])null);
    
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchMethodException e)
            {
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
        
        if (info == null)
            info = new ConfigurationInfo();

        try
        {
            getterMethod = cfgClass.getMethod(getterMethodName, (Class[])null);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
        }
        info.setSetterMethod(setterMethod);
        info.setGetterMethod(getterMethod);

        return info;
    }

    /**
     * load - prefetch all the interesting names into a dictionary so that we
     * can find them again more easily. At the end of this call, we will have a
     * list of every variable and their associated method.
     * 
     * @param filter if null there is no filter, otherwise the set of
     * configuration options is filtered.
     */
    private boolean loadCache(Class<? extends Configuration> cfg, IConfigurationFilter filter)
    {
        int count = 0;

        // First, find all vars at this level.
        for (final Method method : cfg.getMethods())
        {
            if (method.getName().startsWith(SET_PREFIX) ||
                method.isAnnotationPresent(Config.class))
            {
                String configName = null;

                final Class<?>[] pt = method.getParameterTypes();
                assert pt.length > 1 : "Expected at least one parameters on setter.";

                // Collect configuration info from getXXXInfo() static methods.
                final ConfigurationInfo info = createInfo(method);

                // Collect configuration info from annotations.
                final Config config = method.getAnnotation(Config.class);
                if (config != null)
                {
                    info.isAdvanced = config.advanced();
                    info.isHidden = config.hidden();
                    info.isRemoved = config.removed();
                    info.allowMultiple = config.allowMultiple();
                    info.isPath = config.isPath();
                    info.isDisplayed = config.displayed();
                    info.isCompcOnly = config.compcOnly();
                    info.isRequired = config.isRequired();
                    
                    // Argument name generator class
                    final ArgumentNameGenerator argumentNameGeneratorClass = 
                        method.getAnnotation(ArgumentNameGenerator.class);
                    if (argumentNameGeneratorClass != null)
                    {
                        info.argNameGeneratorClass = argumentNameGeneratorClass.value();
                    }
                    else
                    {
                        // Argument names
                        final Arguments arguments = method.getAnnotation(Arguments.class);
                        if (arguments != null)
                            info.argnames = arguments.value();
                    }
                    
                    // Argument count
                    final InfiniteArguments infinite = method.getAnnotation(InfiniteArguments.class);
                    if (infinite != null)
                        info.argcount = ConfigurationInfo.INFINITE_ARGS;

                    // Soft Prerequisites
                    final SoftPrerequisites softPre = method.getAnnotation(SoftPrerequisites.class);
                    if (softPre != null)
                        info.softPrerequisites = softPre.value();
                    
                    // XML element name for configuration
                    final Mapping mapping = method.getAnnotation(Mapping.class);
                    if (mapping != null)
                        configName = Joiner.on(".").skipNulls().join(mapping.value());
                    
                    // Is this a Flex only option?
                    final RoyaleOnly royaleOnly = method.getAnnotation(RoyaleOnly.class);
                    if (royaleOnly != null)
                        info.isRoyaleOnly = true;
                }

                // Fall back to naming convention for configuration names.
                if (configName == null)
                    configName = c2h(method.getName().substring(SET_PREFIX.length()));

                if( filter == null || filter.select(configName) )
                {
                    varCache.put(configName, info);
                    varList.add(configName);
                    if (info.isRequired())
                    {
                        requiredList.add(configName);
                    }
                    ++count;
                }
            }
        }

        assert (count > 0 || filter != null) : "coding error: config class " + cfg.getName() + " did not define any setters or child configs";
        return (count > 0);
    }

    String classToArgName(Class<?> c)
    {
        // we only support builtin classnames!

        String className = c.getName();
        if (className.startsWith("java.lang."))
            className = className.substring("java.lang.".length());

        return className.toLowerCase();
    }

    public ConfigurationInfo getInfo(String avar)
    {
        String var = unalias(avar);
        return varCache.get(var);
    }

    public String getVarArgName(String avar, int argnum)
    {
        String var = unalias(avar);
        ConfigurationInfo info = getInfo(var);

        if (info == null)
        {
            assert false : ("must call isValid to check vars!");
        }

        return info.getArgName(argnum);
    }

    public boolean isValidVar(String avar)
    {
        String var = unalias(avar);
        ConfigurationInfo info = getInfo(var);
        return (info != null);
    }

    public int getVarArgCount(String avar)
    {
        ConfigurationInfo info = getInfo(avar);
        assert (info != null);
        return info.getArgCount();
    }

    /**
     * Add any default values to an argument, if the user did not specify them
     * on the command line.
     * 
     * @param avar the argument variable
     * @param argCount the number of argument values specified
     * @param vals Values to add any default values to
     */
    private void addAnyDefaultArgValues(String avar, int argCount, List<String> vals)
    {
        ConfigurationInfo info = getInfo(avar);
        final int missingArgsCount = argCount - vals.size();
        if (missingArgsCount == 0 || info.getDefaultArgValues() == null)
            return;

        final String[] defaultArgValues = info.getDefaultArgValues();
        final int defaultArgsCount = defaultArgValues.length;
        final int defaultArgsStart = defaultArgsCount - missingArgsCount;
        for (int i = defaultArgsStart; i < defaultArgsCount; i++)
        {
            vals.add(defaultArgValues[i]);
        }
    }

    /**
     * commit - bake the resolved map to the configuration
     * 
     * @param config The configuration to set the buffer variables into.
     * @param problems A collection where configuration problems are reported.
     * 
     * @return true if successful, false otherwise.
     */
    public boolean commit(Object config, Collection<ICompilerProblem> problems)
    {
        assert (config.getClass() == configClass) : 
            ("coding error: configuration " + config.getClass() + " != template " + configClass);
        Set<String> done = new HashSet<String>();
        boolean success = true;
        
        for (Iterator<String> vars = varList.iterator(); vars.hasNext();)
        {
            String var = vars.next();
            if (varMap.containsKey(var))
            {
                try
                {
                    commitVariable(config, var, done);
                }
                catch (ConfigurationException e)
                {
                    problems.add(new ConfigurationProblem(e));
                    success = false;
                }
            }
        }

        for (Iterator<String> reqs = requiredList.iterator(); reqs.hasNext();)
        {
            String req = reqs.next();

            if (!committed.contains(req))
            {
                ConfigurationException e = new ConfigurationException.MissingRequirement(req, null, null, -1);
                problems.add(new ConfigurationProblem(
                        null,
                        -1,
                        -1,
                        -1,
                        -1,
                        e.getMessage()));
                success = false;
            }
        }
        
        return success;
    }

    /**
     * commitVariable - copy a variable out of a state into the final config.
     * This should only be called on variables that are known to exist in the
     * state!
     * 
     * @param var variable name to lookup
     * @param done set of variable names that have been completed so far (for
     * recursion)
     */
    private void commitVariable(Object config, String var, Set<String> done) throws ConfigurationException
    {
        ConfigurationInfo info = getInfo(var);

        setPrerequisites(info.getPrerequisites(), var, done, config, true);
        setPrerequisites(info.getSoftPrerequisites(), var, done, config, false);

        if (committed.contains(var))
            return;

        committed.add(var);
        done.add(var);

        assert (varMap.containsKey(var));
        List<ConfigurationValue> vals = varMap.get(var);

        if (vals.size() > 1)
        {
            assert (info.allowMultiple()); // assumed to have been previously checked
        }
        for (ConfigurationValue val : vals)
        {
            try
            {
                Object[] args = buildArgList(info, val);

                info.getSetterMethod().invoke(config, args);
            }
            catch (Exception e)
            {
                Throwable t = e;

                if (e instanceof InvocationTargetException)
                {
                    t = ((InvocationTargetException)e).getTargetException();
                }

                if (Trace.error)
                    t.printStackTrace();

                if (t instanceof ConfigurationException)
                {
                    throw (ConfigurationException)t;
                }
                else
                {
                    throw new ConfigurationException.OtherThrowable(t, var, val.getSource(), val.getLine());
                }
            }
        }

    }

    private void setPrerequisites(String[] prerequisites, String var, Set<String> done, Object config, boolean required)
            throws ConfigurationException
    {
        if (prerequisites != null)
        {
            for (int p = 0; p < prerequisites.length; ++p)
            {
                String depvar = prerequisites[p];

                // Dependencies can only go downward.
                int dot = var.lastIndexOf('.');

                if (dot >= 0)
                {
                    String car = var.substring(0, dot);
                    //String cdr = var.substring( dot + 1 );

                    String newDepvar = car + "." + depvar;

                    // Since in royale we have collapsed sub-configurations into one
                    // configuration, some options that were in sub-configurations now
                    // have prerequisites on options in the same configuration. We
                    // need to keep the old configuration mappings so old configurations
                    // options will still work. So a simple thing we can do is if the 
                    // dependency variable is invalid (presumably because the 
                    // dependency is really on a parent configuration option), 
                    // then use the dependency as is depvar) and see if it is 
                    // valid. If depvar ends up not being valid then set depvar
                    // to newDepvar so error reporting isn't changed by the new
                    // fall-back behavior.
                    if (isValidVar(newDepvar) || !isValidVar(depvar))
                        depvar = newDepvar;

                }

                if (!done.contains(depvar))
                {
                    if (!isValidVar(depvar))
                    {
                        assert false : ("invalid " + var + " dependency " + depvar);
                        continue;
                    }
                    if (varMap.containsKey(depvar))
                    {
                        commitVariable(config, depvar, done);
                    }
                    else if (required && !committed.contains(depvar))
                    {
                        // TODO - can we get source/line for this?
                        throw new ConfigurationException.MissingRequirement(depvar, var, null, -1);
                    }
                }
            }
        }
    }

    private String[] constructStringArray(List<String> args)
    {
        String[] sa = new String[args.size()];

        int i = 0;
        for (Iterator<String> it = args.iterator(); it.hasNext();)
            sa[i++] = it.next();

        return sa;
    }

    private Object constructValueObject(ConfigurationInfo info, ConfigurationValue cv) throws ConfigurationException
    {
        try
        {
            Class<?>[] pt = info.getSetterMethod().getParameterTypes();
            assert (pt.length == 2); // assumed to be checked upstream

            Object o = pt[1].newInstance();

            Field[] fields = pt[1].getFields();

            assert (fields.length == cv.getArgs().size()); // assumed to be checked upstream

            Iterator<String> argsit = cv.getArgs().iterator();
            for (int f = 0; f < fields.length; ++f)
            {
                String val = (String)argsit.next();
                Object valobj = null;
                Class<?> fc = fields[f].getType();

                assert (info.getArgType(f) == fc);
                assert (info.getArgName(f).equals(ConfigurationBuffer.c2h(fields[f].getName())));

                if (fc == String.class)
                {
                    valobj = val;
                }
                else if ((fc == Boolean.class) || (fc == boolean.class))
                {
                    // TODO - Boolean.valueOf is pretty lax.  Maybe we should restrict to true/false?
                    valobj = Boolean.valueOf(val);
                }
                else if ((fc == Integer.class) || (fc == int.class))
                {
                    valobj = Integer.decode(val);
                }
                else if ((fc == Long.class) || (fc == long.class))
                {
                    valobj = Long.decode(val);
                }
                else
                {
                    assert false; // should have checked any other condition upstream!
                }
                fields[f].set(o, valobj);
            }

            return o;
        }
        catch (InstantiationException e)
        {
            assert false : ("coding error: unable to instantiate value object when trying to set var " + cv.getVar());
            throw new ConfigurationException.OtherThrowable(e, cv.getVar(), cv.getSource(), cv.getLine());

        }
        catch (IllegalAccessException e)
        {
            assert false : ("coding error: " + e + " when trying to set var " + cv.getVar());
            throw new ConfigurationException.OtherThrowable(e, cv.getVar(), cv.getSource(), cv.getLine());
        }
    }

    protected static boolean isSupportedSimpleType(Class<?> c)
    {
        return ((c == String.class)
                || (c == Integer.class) || (c == int.class)
                || (c == Long.class) || (c == long.class)
                || (c == Boolean.class) || (c == boolean.class));
    }

    protected static boolean isSupportedListType(Class<?> c)
    {
        return ((c == List.class) || (c == String[].class));
    }

    protected static boolean isSupportedValueType(Class<?> c)
    {
        if (isSupportedSimpleType(c))
            return false;

        Field[] fields = c.getFields();

        for (int f = 0; f < fields.length; ++f)
        {
            if (!isSupportedSimpleType(fields[f].getType()))
                return false;
        }
        return true;
    }

    private Object[] buildArgList(ConfigurationInfo info, ConfigurationValue val) throws ConfigurationException
    {
        Method setter = info.getSetterMethod();

        Class<?>[] pt = setter.getParameterTypes();

        List<String> args = processValues(val.getVar(), val.getArgs(), val.getSource(), val.getLine());

        if (info.getArgCount() == -1)
        {
            if (pt.length != 2)
            {
                assert false : ("coding error: unlimited length setter " + val.getVar() + " must take a single argument of type List or String[]");
                return null;
            }
            else if (List.class.isAssignableFrom(pt[1]))
            {
                return new Object[] {val, args};
            }
            else if (String[].class.isAssignableFrom(pt[1]))
            {
                return new Object[] {val, constructStringArray(args)};
            }
            else
            {
                assert false : ("coding error: unlimited length setter " + val.getVar() + " must take a single argument of type List or String[]");
                return null;
            }
        }
        else
        {
            assert (pt.length > 1) : ("coding error: config setter " + val.getVar() + " must accept at least one argument");
            // ok, we first check to see if the signature of their setter accepts a list.

            if (pt.length == 2)
            {
                // a variety of specialty setters here...

                if (List.class.isAssignableFrom(pt[1]))
                {
                    return new Object[] {val, args};
                }
                else if (String[].class == pt[1])
                {
                    return new Object[] {val, constructStringArray(args)};
                }
                else if (isSupportedValueType(pt[1]))
                {
                    return new Object[] {val, constructValueObject(info, val)};
                }
            }

            // otherwise, they must have a matching size parm list as the number of args passed in.

            assert (pt.length == (args.size() + 1)) : ("coding error: config setter " + val.getVar() + " does not have " + args.size() + " parameters!");

            Object[] pa = new Object[pt.length];

            pa[0] = val;

            for (int p = 1; p < pt.length; ++p)
            {
                String arg = args.get(p - 1);
                if (pt[p].isAssignableFrom(String.class))
                {
                    pa[p] = arg;
                }
                else if ((pt[p] == int.class) || (pt[p] == Integer.class))
                {
                    try
                    {
                        pa[p] = Integer.decode(arg);

                    }
                    catch (Exception e)
                    {
                        throw new ConfigurationException.TypeMismatch(ConfigurationException.TypeMismatch.INTEGER,
                                                                       arg, val.getVar(), val.getSource(), val.getLine());
                    }
                }
                else if ((pt[p] == long.class) || (pt[p] == Long.class))
                {
                    try
                    {
                        pa[p] = Long.decode(arg);

                    }
                    catch (Exception e)
                    {
                        throw new ConfigurationException.TypeMismatch(
                                ConfigurationException.TypeMismatch.LONG,
                                arg, val.getVar(), val.getSource(), val.getLine());
                    }
                }
                else if ((pt[p] == boolean.class) || (pt[p] == Boolean.class))
                {
                    try
                    {
                        arg = arg.trim().toLowerCase();
                        if (arg.equals("true") || arg.equals("false"))
                        {
                            pa[p] = Boolean.valueOf(arg);
                        }
                        else
                        {
                            throw new ConfigurationException.TypeMismatch(
                                    ConfigurationException.TypeMismatch.BOOLEAN, arg, val.getVar(), val.getSource(), val.getLine());
                        }
                    }
                    catch (Exception e)
                    {
                        throw new ConfigurationException.TypeMismatch(
                                ConfigurationException.TypeMismatch.BOOLEAN, arg, val.getVar(), val.getSource(), val.getLine());
                    }
                }
                else
                {
                    assert false : ("coding error: " + val.getVar() + " setter argument " + p + " is not a supported type");
                }
            }

            return pa;
        }
    }

    public void addAlias(String alias, String var)
    {
        if (!isValidVar(var))
        {
            assert false : ("coding error: can't bind alias " + alias + " to nonexistent var " + var);
            return;
        }
        if (aliases.containsKey(alias))
        {
            assert false : ("coding error: alias " + alias + " already defined as " + aliases.get(alias));
            return;
        }
        if (varCache.containsKey(alias))
        {
            assert false : ("coding error: can't define alias " + alias + ", it already exists as a var");
            return;
        }

        aliases.put(alias, var);
    }

    public Map<String, String> getAliases()
    {
        return aliases;
    }

    public String unalias(String var)
    {
        String realvar = aliases.get(var);
        return (realvar == null) ? var : realvar;
    }

    public String peekSimpleConfigurationVar(String avar) throws ConfigurationException
    {
        String val = null;
        List<ConfigurationValue> valList = getVar(avar);
        if (valList != null)
        {
            ConfigurationValue cv = (ConfigurationValue)valList.get(0);
            List<String> args = processValues(avar, cv.getArgs(), cv.getSource(), cv.getLine());
            val = args.get(0);
        }
        return val;
    }

    public List<ConfigurationValue> peekConfigurationVar(String avar) throws ConfigurationException
    {
        List<ConfigurationValue> srcList = getVar(avar);
        if (srcList == null)
            return null;

        List<ConfigurationValue> dstList = new LinkedList<ConfigurationValue>();
        for (ConfigurationValue srcVal : srcList)
        {
            List<String> args = processValues(avar, srcVal.getArgs(), srcVal.getSource(), srcVal.getLine());

            ConfigurationValue dstVal = new ConfigurationValue(srcVal.getBuffer(), avar, args, srcVal.getSource(), srcVal.getLine(), srcVal.getContext());
            dstList.add(dstVal);
        }
        return dstList;
    }

    public void addPosition(String var, int iStart, int iEnd)
    {
        positions.add(new Object[] {var, new Integer(iStart), new Integer(iEnd)});
    }

    public List<Object[]> getPositions()
    {
        return positions;
    }

    public static List<String> formatText(String input, int columns)
    {
        ArrayList<String> lines = new ArrayList<String>();

        if ((input == null) || (input.length() == 0))
            return lines;

        int current = 0;
        int lineStart = -1;
        int lineEnd = -1;
        int wordStart = -1;
        int wordEnd = -1;
        boolean start = true;
        boolean preserve = true;

        while (true)
        {
            if (current < input.length())
            {
                boolean newline = input.charAt(current) == '\n';
                boolean printable = (preserve && !newline) || !Character.isWhitespace(input.charAt(current));

                if (start) // find a word
                {
                    if (printable)
                    {
                        if (lineStart == -1)
                        {
                            lineStart = current;
                        }
                        wordStart = current;
                        start = false;
                    }
                    else
                    {
                        if (newline && lineStart != -1)
                        {
                            lines.add(input.substring(lineStart, current));
                            lineStart = -1;
                        }
                        else if (newline)
                        {
                            lines.add("");
                        }
                        ++current;
                    }
                }
                else
                // have a word
                {
                    preserve = false;
                    if (printable)
                    {
                        ++current;
                    }
                    else
                    {
                        wordEnd = current;
                        if (lineEnd == -1)
                        {
                            lineEnd = current;
                        }

                        // two possibilities; if the new word fits in the current line length
                        // without being too many columns, leave on current line.
                        // otherwise, set it as the start of a new line.

                        if (wordEnd - lineStart < columns)
                        {
                            if (newline)
                            {
                                lines.add(input.substring(lineStart, current));
                                lineStart = -1;
                                lineEnd = -1;
                                wordStart = -1;
                                start = true;
                                preserve = true;
                                ++current;
                            }
                            else
                            {
                                // we have room to add the current word to this line, find new word
                                start = true;
                                lineEnd = current;
                            }
                        }
                        else
                        {
                            // current word pushes things beyond the requested column limit,
                            // dump current text
                            lines.add(input.substring(lineStart, lineEnd));
                            lineStart = wordStart;
                            lineEnd = -1;
                            wordStart = -1;
                            start = true;
                            if (newline)
                                preserve = true;
                        }
                    }
                }
            }
            else
            // we're done
            {
                // a) no line yet, so don't do anything
                // b) have line and new word would push over edge, need two lines
                // c) have line and current word fits, need one line
                // d) only one word and its too long anyway, need one line

                if (lineStart != -1) // we have a line in progress
                {
                    wordEnd = current;
                    if (lineEnd == -1)
                        lineEnd = current;

                    if (((wordEnd - lineStart) < columns) // current word fits
                        || (wordEnd == lineEnd)) // or one long word
                    {
                        lineEnd = wordEnd;
                        lines.add(input.substring(lineStart, wordEnd));
                    }
                    else
                    // didn't fit, multiple words
                    {
                        lines.add(input.substring(lineStart, lineEnd));
                        lines.add(input.substring(wordStart, wordEnd));
                    }
                }
                break;
            }
        }
        return lines;
    }

    /**
     * For debugging only.
     * <p>
     * Produces an alphabetized list of this buffer's configuration options and their values.
     * An option such as
     * <pre>
     * -foo=aaa,bbb -foo+=ccc
     * </pre>
     * will appear as
     * <pre>
     * foo=aaa,bbb;ccc
     * </pre>
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        String[] variables = varMap.keySet().toArray(new String[0]);
        Arrays.sort(variables);
        
        for (String var : variables)
        {
            sb.append(var);
            sb.append("=");

            ArrayList<String> commaSeparatedValues = new ArrayList<String>();
            for (ConfigurationValue cv : varMap.get(var))
            {
                List<String> args = cv.getArgs();
                String joinedArgs = Joiner.on(',').join(args);
                commaSeparatedValues.add(joinedArgs);
            }
            String rhs = Joiner.on(';').join(commaSeparatedValues);
            sb.append(rhs);
            
            sb.append('\n');
        }

        return sb.toString();
    }
}
