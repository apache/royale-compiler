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

package org.apache.royale.compiler.internal.codegen.wast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.codegen.wast.IWASTPublisher;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.internal.projects.RoyaleWASTProject;
import org.apache.royale.utils.EnvProperties;

public class WASTPublisher implements IWASTPublisher {

	public WASTPublisher(RoyaleWASTProject project, Configuration config) {
		this.configuration = config;
	}

	
	
	private Configuration configuration;

	private File outputFolder;
	private File outputParentFolder;

	private EnvProperties env;
	
	
	@Override
	public File getOutputFolder() {
		outputParentFolder = new File(configuration.getTargetFileDirectory()).getParentFile();

		outputFolder = new File(outputParentFolder, "bin");

		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		return outputFolder;
	}

	@Override
	public boolean publish(ProblemQuery problems) throws IOException {
		try {
			String targetPath = getOutputFolder().getPath();
			
			String targetWatFilePath = targetPath + "/HelloWorld.wat";
			
			env = EnvProperties.initiate();
			
			String[] cmd = { env.WAT2WASM + "/wat2wasm", targetWatFilePath, "-o", targetPath + "/HelloWorld.wasm" };

			Process p = new ProcessBuilder(cmd).redirectError(Redirect.INHERIT)
                    .redirectOutput(Redirect.INHERIT)
                    .start();

			p.waitFor();
			
			copyGlueJS();
			
			writeIndexHTML();
			
			new File(targetWatFilePath).delete();
			
			System.out.println("The project has been successfully compiled and optimized.");

			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	
	
    private void copyFile(File source, File target) throws IOException
    {
        if (source.isDirectory()) {
            FileUtils.copyDirectory(source, target);
        } else {
            FileUtils.copyFile(source, target);
        }
    }

    private void copyGlueJS() throws IOException {
	    	File flexHome = new File(WASTPublisher.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	    	
	    	String flexHomePath = flexHome.getPath();
	
	    	if (!flexHomePath.substring(flexHomePath.length() - 4).equals(".jar")) {
	    		flexHomePath = new File(flexHome, "../../../../royale-asjs/wast/resources").getPath();
	    	} else {
	    		flexHomePath = new File(flexHome.getParentFile().getPath(), "../resources").getPath();
	    	}
	    	
	    	System.out.println(flexHomePath);
	    	
	    	copyFile(new File(flexHomePath, "glue.js"), new File(getOutputFolder().getPath(), "glue.js"));
    }

    private void writeFile(File target, String content) throws IOException {
        if (!target.exists()) {
            target.createNewFile();
        }

        FileWriter fw = new FileWriter(target, false);
        fw.write(content);
        fw.close();
    }

	private void writeIndexHTML() throws IOException {
		StringBuilder htmlFile = new StringBuilder();
		
		htmlFile.append("<!DOCTYPE html>\n");
		htmlFile.append("\n");
		htmlFile.append("<html>\n");
		htmlFile.append("<head>\n");
		htmlFile.append("  <meta charset=\"utf-8\">\n");
		htmlFile.append("  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
		htmlFile.append("\n");
		htmlFile.append("  <title>Royale WASM</title>\n");
		htmlFile.append("\n");
		htmlFile.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		htmlFile.append("</head>\n");
		htmlFile.append("<body>\n");
		htmlFile.append("\n");
		htmlFile.append("<div id=\"wasm-output\"></div>\n");
		htmlFile.append("\n");
		htmlFile.append("<script src=\"glue.js\"></script>\n");
		htmlFile.append("\n");
		htmlFile.append("<script>\n");
		htmlFile.append("\n");
		htmlFile.append("  fetchAndInstantiate('HelloWorld.wasm')\n");
		htmlFile.append("  .then(function(instance) {\n");
		htmlFile.append("    document.getElementById('wasm-output').innerHTML = 'The result from the call to the WASM method is: ' + instance.exports.add(3, 7); // 10\n");
		htmlFile.append("  });\n");
		htmlFile.append("\n");
		htmlFile.append("</script>\n");
		htmlFile.append("\n");
		htmlFile.append("</body>\n");
		htmlFile.append("</html>\n");

		writeFile(new File(getOutputFolder().getPath(), "index.html"), htmlFile.toString());
	}

}
