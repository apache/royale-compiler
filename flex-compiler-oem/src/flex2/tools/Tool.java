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
import flex2.compiler.Logger;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.util.CompilerMessage;
import flex2.compiler.util.CompilerMessage.CompilerError;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.oem.Message;
import org.apache.flex.compiler.clients.problems.ProblemQueryProvider;
import org.apache.flex.compiler.clients.MXMLC;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.problems.CompilerProblemSeverity;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.annotations.DefaultSeverity;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Common base class for most flex tools.
 */
public class Tool
{
    protected static Class<? extends MXMLC> COMPILER;
    protected static Class<? extends MxmlJSC> JS_COMPILER;

    protected static int compile(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        int exitCode;

        if (hasJsOutputType(args)) {
            MxmlJSC mxmlJSC = JS_COMPILER.newInstance();
            exitCode = mxmlJSC.execute(args);
            processProblemReport(mxmlJSC);
        } else {
            MXMLC mxmlc = COMPILER.newInstance();
            exitCode = mxmlc.execute(args);
            processProblemReport(new CompilerRequestableProblems(mxmlc));
        }

        return exitCode;
    }

    protected static boolean hasJsOutputType(String[] args) {
        boolean found = false;

        for (String s : args) {
            String[] kvp = s.split("=");

            if (s.contains("-js-output-type")) {
                found = true;
                break;
            }
        }

        return found;
    }

    public static Map<String, String> getLicenseMapFromFile(String fileName) throws ConfigurationException
    {
        Map<String, String> result = null;
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(fileName);
            Properties properties = new Properties();
            properties.load(in);
            Enumeration enumeration = properties.propertyNames();

            if ( enumeration.hasMoreElements() )
            {
                result = new HashMap<String, String>();

                while ( enumeration.hasMoreElements() )
                {
                    String propertyName = (String) enumeration.nextElement();
                    result.put(propertyName, properties.getProperty(propertyName));
                }
            }
        }
        catch (IOException ioException)
        {
        	LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
            throw new ConfigurationException(l10n.getLocalizedTextString(new FailedToLoadLicenseFile(fileName)));
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                }
            }
        }

        return result;
    }

	public static class FailedToLoadLicenseFile extends CompilerError
	{
		private static final long serialVersionUID = -2980033917773108328L;
        
        public String fileName;

		public FailedToLoadLicenseFile(String fileName)
		{
			super();
			this.fileName = fileName;
		}
	}

    public static void processProblemReport(ProblemQueryProvider requestableProblems)
    {
        for (ICompilerProblem problem : requestableProblems.getProblemQuery().getProblems())
        {
            Class aClass = problem.getClass();
            Annotation[] annotations = aClass.getAnnotations();

            for(Annotation annotation : annotations){
                if(annotation instanceof DefaultSeverity){
                    DefaultSeverity myAnnotation = (DefaultSeverity) annotation;
                    CompilerProblemSeverity cps = myAnnotation.value();
                    String level;
                    if (cps.equals(CompilerProblemSeverity.ERROR))
                        level = Message.ERROR;
                    else if (cps.equals(CompilerProblemSeverity.WARNING))
                        level = Message.WARNING;
                    else
                        break; // skip if IGNORE?
                    CompilerMessage msg = new CompilerMessage(level,
                            problem.getSourcePath(),
                            problem.getLine() + 1,
                            problem.getColumn());
                    try
                    {
                        String errText = (String) aClass.getField("DESCRIPTION").get(aClass);
                        while (errText.contains("${"))
                        {
                            int start = errText.indexOf("${");
                            int end = errText.indexOf("}", start);
                            String token = errText.substring(start + 2, end);
                            String value = (String) aClass.getField(token).get(problem);
                            token = "${" + token + "}";
                            errText = errText.replace(token, value);
                        }

                        msg.setMessage(errText);
                        logMessage(msg);
                    }
                    catch (IllegalArgumentException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (SecurityException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (NoSuchFieldException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private static void logMessage(CompilerMessage msg) {

        final Logger logger = ThreadLocalToolkit.getLogger();

        if (logger != null) {
            if (msg.getLevel().equals(Message.INFO)) {
                logger.logInfo(msg.getPath(), msg.getLine(), msg.getColumn(), msg.getMessage());
            } else if (msg.getLevel().equals(Message.WARNING)) {
                logger.logWarning(msg.getPath(), msg.getLine(), msg.getColumn(), msg.getMessage());
            } else if (msg.getLevel().equals(Message.ERROR)) {
                logger.logError(msg.getPath(), msg.getLine(), msg.getColumn(), msg.getMessage());
            }
        }
    }

    public static class NoUpdateMessage extends CompilerMessage.CompilerInfo {
        private static final long serialVersionUID = 6943388392279226490L;
        public String name;

        public NoUpdateMessage(String name) {
            this.name = name;
        }
    }

    static class CompilerRequestableProblems implements ProblemQueryProvider {
        private MXMLC mxmlc;

        public CompilerRequestableProblems(MXMLC mxmlc) {
            this.mxmlc = mxmlc;
        }

        @Override
        public ProblemQuery getProblemQuery() {
            return mxmlc.getProblems();
        }
    }
}
