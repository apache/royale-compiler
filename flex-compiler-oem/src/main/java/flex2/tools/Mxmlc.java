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

package flex2.tools;

import flash.localization.LocalizationManager;
import flex2.compiler.common.Configuration;
import flex2.compiler.common.ConfigurationPathResolver;
import flex2.compiler.common.PathResolver;
import flex2.compiler.config.*;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.util.CompilerMessage;
import flex2.compiler.util.ThreadLocalToolkit;
import org.apache.royale.compiler.clients.MXMLC;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

/**
 * A command line tool for compiling Flex applications.  Despite the
 * name, in addition to .mxml files, this tool can be used to compile
 * other file formats, like .as and .css.
 */
public final class Mxmlc extends Tool {
    static private String l10nConfigPrefix = "flex2.configuration";

    public static final String FILE_SPECS = "file-specs";

    /**
     * The entry-point for Mxmlc.
     * Note that if you change anything in this method, make sure to check Compc, Shell, and
     * the server's CompileFilter to see if the same change needs to be made there.  You
     * should also inform the Zorn team of the change.
     *
     * @param args
     */
    public static void main(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        System.exit(mxmlcNoExit(args));
    }

    public static int mxmlcNoExit(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        JS_COMPILER = MxmlJSC.class;

        return compile(args);
    }

    public static void mxmlc(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        JS_COMPILER = MxmlJSC.class;

        compile(args);
    }

    public static Configuration processConfiguration(LocalizationManager lmgr, String program, String[] args,
                                                     ConfigurationBuffer cfgbuf, Class<? extends Configuration> cls, String defaultVar)
            throws ConfigurationException, IOException {
        return processConfiguration(lmgr, program, args, cfgbuf, cls, defaultVar, true);
    }

    public static Configuration processConfiguration(LocalizationManager lmgr, String program, String[] args,
                                                     ConfigurationBuffer cfgbuf, Class<? extends Configuration> cls, String defaultVar,
                                                     boolean ignoreUnknownItems)
            throws IOException {
        ToolsConfiguration toolsConfiguration = null;
        try {
            SystemPropertyConfigurator.load(cfgbuf, "flex");

            // Parse the command line a first time, to peak at stuff like
            // "flexlib" and "load-config".  The first parse is thrown
            // away after that and we intentionally parse a second time
            // below.  See note below.
            CommandLineConfigurator.parse(cfgbuf, defaultVar, args);

            String flexlib = cfgbuf.getToken("flexlib");
            if (flexlib == null) {
                String appHome = System.getProperty("application.home");

                if (appHome == null) {
                    appHome = ".";
                } else {
                    appHome += File.separator + "frameworks";       // FIXME - need to eliminate this from the compiler
                }
                cfgbuf.setToken("flexlib", appHome);
            }

            // Framework Type
            // halo, gumbo, interop...
            String framework = cfgbuf.getToken("framework");
            if (framework == null) {
                cfgbuf.setToken("framework", "halo");
            }

            String configname = cfgbuf.getToken("configname");
            if (configname == null) {
                cfgbuf.setToken("configname", "flex");
            }

            String buildNumber = cfgbuf.getToken("build.number");
            if (buildNumber == null) {
                if ("".equals(VersionInfo.getBuild())) {
                    buildNumber = "workspace";
                } else {
                    buildNumber = VersionInfo.getBuild();
                }
                cfgbuf.setToken("build.number", buildNumber);
            }


            // We need to intercept "help" options because we want to try to correctly
            // interpret them even when the rest of the configuration is totally screwed up.

            if (cfgbuf.getVar("version") != null) {
                System.out.println(VersionInfo.buildMessage());
                System.exit(0);
            }

            processHelp(cfgbuf, program, defaultVar, lmgr, args);

            // at this point, we should have enough to know both
            // flexlib and the config file.

            ConfigurationPathResolver configResolver = new ConfigurationPathResolver();

            List<ConfigurationValue> configs = cfgbuf.peekConfigurationVar("load-config");

            if (configs != null) {
                for (ConfigurationValue cv : configs) {
                    for (String path : cv.getArgs()) {
                        VirtualFile configFile = ConfigurationPathResolver.getVirtualFile(path, configResolver, cv);
                        cfgbuf.calculateChecksum(configFile);
                        InputStream in = configFile.getInputStream();
                        if (in != null) {
                            FileConfigurator.load(cfgbuf, new BufferedInputStream(in), configFile.getName(),
                                    configFile.getParent(), "royale-config", ignoreUnknownItems);
                        } else {
                            throw new ConfigurationException.ConfigurationIOError(path, cv.getVar(), cv.getSource(), cv.getLine());
                        }
                    }
                }
            }

            PathResolver resolver = ThreadLocalToolkit.getPathResolver();
            // Load project file, if any...
            List fileValues = cfgbuf.getVar(FILE_SPECS);
            if ((fileValues != null) && (fileValues.size() > 0)) {
                ConfigurationValue cv = (ConfigurationValue) fileValues.get(fileValues.size() - 1);
                if (cv.getArgs().size() > 0) {
                    String val = cv.getArgs().get(cv.getArgs().size() - 1);
                    int index = val.lastIndexOf('.');
                    if (index != -1) {
                        String project = val.substring(0, index) + "-config.xml";
                        VirtualFile projectFile = resolver.resolve(configResolver, project);
                        if (projectFile != null) {
                            cfgbuf.calculateChecksum(projectFile);
                            InputStream in = projectFile.getInputStream();
                            if (in != null) {
                                FileConfigurator.load(cfgbuf, new BufferedInputStream(in),
                                        projectFile.getName(), projectFile.getParent(), "royale-config",
                                        ignoreUnknownItems);
                            }
                        }
                    }
                }
            }

            // The command line needs to take precedence over all defaults and config files.
            // This is a bit gross, but by simply re-merging the command line back on top,
            // we will get the behavior we want.
            cfgbuf.clearSourceVars(CommandLineConfigurator.source);
            CommandLineConfigurator.parse(cfgbuf, defaultVar, args);

            toolsConfiguration = null;
            try {
                toolsConfiguration = (ToolsConfiguration) cls.newInstance();
                toolsConfiguration.setConfigPathResolver(configResolver);
            } catch (Exception e) {
                LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
                throw new ConfigurationException(l10n.getLocalizedTextString(new CouldNotInstantiate(toolsConfiguration)));
            }
            cfgbuf.commit(toolsConfiguration);

            // enterprise service config file has other config file dependencies. add them here...
            calculateServicesChecksum(toolsConfiguration, cfgbuf);

            toolsConfiguration.validate(cfgbuf);

            // consolidate license keys...
            VirtualFile licenseFile = toolsConfiguration.getLicenseFile();
            if (licenseFile != null) {
                Map<String, String> fileLicenses = Tool.getLicenseMapFromFile(licenseFile.getName());
                Map<String, String> cmdLicenses = toolsConfiguration.getLicensesConfiguration().getLicenseMap();
                if (cmdLicenses == null) {
                    toolsConfiguration.getLicensesConfiguration().setLicenseMap(fileLicenses);
                } else if (fileLicenses != null) {
                    fileLicenses.putAll(cmdLicenses);
                    toolsConfiguration.getLicensesConfiguration().setLicenseMap(fileLicenses);
                }
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        return toolsConfiguration;
    }

    static void processHelp(ConfigurationBuffer cfgbuf, String program, String defaultVar, LocalizationManager lmgr, String[] args) {
        if (cfgbuf.getVar("help") != null) {
            Set<String> keywords = new HashSet<String>();
            List vals = cfgbuf.getVar("help");
            for (Iterator it = vals.iterator(); it.hasNext(); ) {
                ConfigurationValue val = (ConfigurationValue) it.next();
                for (Object element : val.getArgs()) {
                    String keyword = (String) element;
                    while (keyword.startsWith("-"))
                        keyword = keyword.substring(1);
                    keywords.add(keyword);
                }
            }
            if (keywords.size() == 0) {
                keywords.add("help");
            }

            ThreadLocalToolkit.logInfo(getStartMessage(program));
            System.out.println();
            System.out.println(CommandLineConfigurator.usage(program, defaultVar, cfgbuf, keywords, lmgr, l10nConfigPrefix));
            System.exit(0);
        }

        if (args.length == 0 && ("mxmlc".equals(program) || "compc".equals(program))) {
            ThreadLocalToolkit.logInfo(getStartMessage(program));
            System.err.println(CommandLineConfigurator.brief(program, defaultVar, lmgr, l10nConfigPrefix));
            System.exit(1);
        }
    }

    private static void calculateServicesChecksum(Configuration config, ConfigurationBuffer cfgbuf) {
        Map<String, Long> services = null;
        if (config.getCompilerConfiguration().getServicesDependencies() != null) {
            services = config.getCompilerConfiguration().getServicesDependencies().getConfigPaths();
        }

        if (services != null) {
            for (Entry<String, Long> entry : services.entrySet()) {
                cfgbuf.calculateChecksum(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void processConfigurationException(ConfigurationException ex, String program) {
        ThreadLocalToolkit.log(ex);

        if (ex.source == null || ex.source.equals("command line")) {
            Map<String, String> p = new HashMap<String, String>();
            p.put("program", program);
            String help = ThreadLocalToolkit.getLocalizationManager().getLocalizedTextString("flex2.compiler.CommandLineHelp", p);
            if (help != null) {
                // "Use '" + program + " -help' for information about using the command line.");
                System.err.println(help);
            }
        }
    }

    private static String getStartMessage(String program) {
        LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

        return l10n.getLocalizedTextString(new StartMessage(program, VersionInfo.buildMessage()));
    }

    public static class CouldNotInstantiate extends CompilerMessage.CompilerInfo {
        private static final long serialVersionUID = -8970190710117830662L;

        public CouldNotInstantiate(Configuration config) {
            super();
            this.config = config;
        }

        public final Configuration config;
    }

    public static class StartMessage extends CompilerMessage.CompilerInfo {
        private static final long serialVersionUID = 4807822711658875257L;

        public StartMessage(String program, String buildMessage) {
            super();
            this.program = program;
            this.buildMessage = buildMessage;
        }

        public final String program, buildMessage;
    }

    public static class OutputMessage extends CompilerMessage.CompilerInfo {
        private static final long serialVersionUID = -4859993585489031839L;

        public String name;
        public String length;

        public OutputMessage(String name, String length) {
            this.name = name;
            this.length = length;
        }
    }
}

