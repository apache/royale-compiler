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

package org.apache.royale.playerglobal;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Generates .as source files to build playerglobal.swc and airglobal.swc from
 * the ASDoc DITA XML files.
 * 
 * Usage:
 * 
 * java -jar playerglobal-source-gen.jar flex-sdk/frameworks/projects/playerglobal/bundles/en_US/docs/
 * java -jar playerglobal-source-gen.jar flex-sdk/frameworks/projects/playerglobal/bundles/en_US/docs/ target/generated-sources/
 */
class PlayerglobalSourceGen {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Must specify docs folder path");
			System.exit(1);
		}
		if (args.length > 2) {
			System.err.println("Too many arguments. Required docs folder path. Optional target folder path.");
			System.exit(1);
		}
		File sourceFolder = new File(args[0]);
		File targetFolder = (args.length == 2) ? new File(args[1])
				: new File(System.getProperty("user.dir"), "target/generated-sources/");
		PlayerglobalSourceGen sourceGen = new PlayerglobalSourceGen(sourceFolder, targetFolder);
		try {
			sourceGen.generateSources();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static final List<String> VECTOR_SUFFIXES = Arrays.asList("$double", "$int", "$uint", "$object");

	private File sourceFolder;
	private File targetFolder;
	private File currentFile;

	public PlayerglobalSourceGen(File sourceFolder, File targetFolder) {
		this.sourceFolder = sourceFolder;
		this.targetFolder = targetFolder;
	}

	public void generateSources() throws Exception {
		preclean();

		for (File sourceFile : sourceFolder.listFiles()) {
			if (sourceFile.isDirectory()) {
				continue;
			}
			String sourceFileName = sourceFile.getName();
			if (!sourceFileName.endsWith(".xml")) {
				continue;
			}
			if (sourceFileName.endsWith(".dita.xml")) {
				continue;
			}
			parseFile(sourceFile);
		}
	}

	private void preclean() throws Exception {
		File playerglobalFolder = new File(targetFolder, "playerglobal");
		FileUtils.deleteDirectory(playerglobalFolder);
		File airglobalFolder = new File(targetFolder, "airglobal");
		FileUtils.deleteDirectory(airglobalFolder);
	}

	private void writeFileForDefinition(String fullyQualifiedName, boolean airOnly, String contents)
			throws IOException {
		StringBuilder fileNameBuilder = new StringBuilder();
		if (airOnly) {
			fileNameBuilder.append("airglobal");
		} else {
			fileNameBuilder.append("playerglobal");
		}
		String[] parts = fullyQualifiedName.split("\\.");
		for (String part : parts) {
			fileNameBuilder.append("/");
			fileNameBuilder.append(part);
		}
		fileNameBuilder.append(".as");
		File targetFile = new File(this.targetFolder, fileNameBuilder.toString());
		FileUtils.writeStringToFile(targetFile, contents);
	}

	private boolean isAIROnly(Element prologElement) {
		if (prologElement == null) {
			return false;
		}
		Element asMetadataElement = prologElement.element("asMetadata");
		if (asMetadataElement == null) {
			return false;
		}
		Element apiVersionElement = asMetadataElement.element("apiVersion");
		if (apiVersionElement == null) {
			return false;
		}
		List<Element> apiPlatformElements = apiVersionElement.elements("apiPlatform");
		if (apiPlatformElements == null || apiPlatformElements.size() == 0) {
			return false;
		}
		for (Element apiPlatformElement : apiPlatformElements) {
			if (!"AIR".equals(apiPlatformElement.attributeValue("name"))) {
				return false;
			}
		}
		return true;
	}

	private void parseFile(File ditaFile) throws Exception {
		currentFile = ditaFile;
		String contents = null;
		try {
			contents = FileUtils.readFileToString(ditaFile, Charset.forName("utf8"));
		} catch (Exception e) {
			System.err.println("Failed to read XML file: " + ditaFile.getAbsolutePath());
			return;
		}

		SAXReader xmlReader = new SAXReader();
		Document xmlDoc = xmlReader.read(new StringReader(contents));

		Element apiPackageElement = xmlDoc.getRootElement();
		if (!"apiPackage".equals(apiPackageElement.getName())) {
			throw new Exception("No apiPackage root element: " + ditaFile.getAbsolutePath());
		}

		parsePackage(apiPackageElement);
		currentFile = null;
	}

	private void parsePackage(Element apiPackageElement) throws Exception {
		List<Element> apiOperationElements = apiPackageElement.elements("apiOperation");
		for (Element apiOperationElement : apiOperationElements) {
			parsePackageFunction(apiOperationElement);
		}
		List<Element> apiValueElements = apiPackageElement.elements("apiValue");
		for (Element apiValueElement : apiValueElements) {
			parsePackageVariable(apiValueElement);
		}
		List<Element> apiClassifierElements = apiPackageElement.elements("apiClassifier");
		for (Element apiClassifierElement : apiClassifierElements) {
			Element apiClassifierDetailElement = apiClassifierElement.element("apiClassifierDetail");
			if (apiClassifierDetailElement == null) {
				String fullyQualifiedName = apiClassifierElement.attributeValue("id");
				throw new Exception("Not found: " + fullyQualifiedName);
			}
			Element apiClassifierDefElement = apiClassifierDetailElement.element("apiClassifierDef");
			if (apiClassifierDefElement == null) {
				String fullyQualifiedName = apiClassifierElement.attributeValue("id");
				throw new Exception("Not found: " + fullyQualifiedName);
			}
			Element apiInterfaceElement = apiClassifierDefElement.element("apiInterface");
			if (apiInterfaceElement != null) {
				parseInterface(apiClassifierElement);
				continue;
			}
			parseClass(apiClassifierElement);
		}
	}

	private void parseClass(Element apiClassifierElement) throws Exception {
		String fullyQualifiedName = apiClassifierElement.attributeValue("id");
		if (fullyQualifiedName.startsWith("globalClassifier:")) {
			fullyQualifiedName = fullyQualifiedName.substring(17);
		}
		if (fullyQualifiedName.equals("Vector")) {
			//special case in the compiler that doesn't get exposed in docs
			fullyQualifiedName = "__AS3__.vec:Vector";
			StringBuilder vectorBuilder = new StringBuilder();
			vectorBuilder.append("// generated from: ");
			vectorBuilder.append(currentFile.getName());
			vectorBuilder.append("\n");
			vectorBuilder.append("package __AS3__.vec {\n");
			vectorBuilder.append("\tpublic final dynamic class Vector {\n");
			vectorBuilder.append("\tpublic native function Vector();\n");
			vectorBuilder.append("\t}\n");
			vectorBuilder.append("}\n");
			writeFileForDefinition("__AS3__.vec.Vector", false, vectorBuilder.toString());
			for (String suffix : VECTOR_SUFFIXES) {
				parseClassWithFullyQualifiedName(apiClassifierElement, fullyQualifiedName + suffix);
			}
			return;
		}
		parseClassWithFullyQualifiedName(apiClassifierElement, fullyQualifiedName);
	}

	private void parseClassWithFullyQualifiedName(Element apiClassifierElement, String fullyQualifiedName)
			throws Exception {

		String[] parts = fullyQualifiedName.split(":");
		String packageName = "";
		String className = fullyQualifiedName;
		if (parts.length > 1) {
			packageName = parts[0];
			className = parts[1];
			fullyQualifiedName = packageName + "." + className;
		}

		boolean isAIROnly = isAIROnly(apiClassifierElement.element("prolog"));
		boolean isVector = className.startsWith("Vector$");

		Set<String> importFullyQualifiedNames = new HashSet<String>();
		collectImports(apiClassifierElement, packageName, importFullyQualifiedNames);

		String baseClassFullyQualifiedName = "";
		List<String> interfaceFullyQualifiedNames = new ArrayList<String>();
		String access = null;
		boolean isFinal = false;
		boolean isDynamic = false;

		Element apiClassifierDetailElement = apiClassifierElement.element("apiClassifierDetail");
		if (apiClassifierDetailElement == null) {
			throw new Exception("apiClassifierDetail not found for: " + className);
		}
		Element apiClassifierDefElement = apiClassifierDetailElement.element("apiClassifierDef");
		if (apiClassifierDefElement == null) {
			throw new Exception("apiClassifierDef not found for: " + className);
		}

		Element apiBaseClassifierElement = apiClassifierDefElement.element("apiBaseClassifier");
		if (apiBaseClassifierElement != null) {
			baseClassFullyQualifiedName = apiBaseClassifierElement.getTextTrim();
			baseClassFullyQualifiedName = baseClassFullyQualifiedName.replace(":", ".");
		}

		List<Element> apiBaseInterfaceElements = apiClassifierDefElement.elements("apiBaseInterface");
		for (Element apiBaseInterfaceElement : apiBaseInterfaceElements) {
			String interfaceFullyQualifiedName = apiBaseInterfaceElement.getTextTrim();
			interfaceFullyQualifiedName = interfaceFullyQualifiedName.replace(":", ".");
			interfaceFullyQualifiedNames.add(interfaceFullyQualifiedName);
		}

		Element apiAccessElement = apiClassifierDefElement.element("apiAccess");
		if (apiAccessElement != null) {
			access = apiAccessElement.attributeValue("value");
		}
		if (isVector) {
			access = "internal";
		}

		Element apiFinalElement = apiClassifierDefElement.element("apiFinal");
		if (apiFinalElement != null) {
			isFinal = true;
		}

		Element apiDynamicElement = apiClassifierDefElement.element("apiDynamic");
		if (apiDynamicElement != null) {
			isDynamic = true;
		}

		Element apiConstructorElement = apiClassifierElement.element("apiConstructor");
		List<Element> apiOperationElements = apiClassifierElement.elements("apiOperation");
		List<Element> apiValueElements = apiClassifierElement.elements("apiValue");

		StringBuilder classBuilder = new StringBuilder();
		classBuilder.append("// generated from: ");
		classBuilder.append(currentFile.getName());
		classBuilder.append("\n");
		classBuilder.append("package");
		if (packageName.length() > 0) {
			classBuilder.append(" ");
			classBuilder.append(packageName);
		}
		classBuilder.append(" ");
		classBuilder.append("{");
		classBuilder.append("\n");
		writeImports(importFullyQualifiedNames, classBuilder);
		classBuilder.append("\t");
		if (access != null && access.length() > 0) {
			classBuilder.append(access);
			classBuilder.append(" ");
		}
		if (isFinal) {
			classBuilder.append("final ");
		}
		if (isDynamic) {
			classBuilder.append("dynamic ");
		}
		classBuilder.append("class ");
		classBuilder.append(className);
		if (baseClassFullyQualifiedName != null && baseClassFullyQualifiedName.length() > 0
				&& !"Object".equals(baseClassFullyQualifiedName)) {
			classBuilder.append(" extends ");
			classBuilder.append(baseClassFullyQualifiedName);
		}
		for (int i = 0; i < interfaceFullyQualifiedNames.size(); i++) {
			String interfaceFullyQualifiedName = interfaceFullyQualifiedNames.get(i);
			if (i == 0) {
				classBuilder.append(" implements ");
			} else {
				classBuilder.append(", ");
			}
			classBuilder.append(interfaceFullyQualifiedName);
		}
		classBuilder.append(" ");
		classBuilder.append("{");
		classBuilder.append("\n");
		if (apiConstructorElement != null) {
			parseConstructor(apiConstructorElement, className, classBuilder);
		}
		for (Element apiOperationElement : apiOperationElements) {
			parseFunction(apiOperationElement, className, false, classBuilder);
		}
		for (Element apiValueElement : apiValueElements) {
			parseVariable(apiValueElement, false, classBuilder);
		}
		classBuilder.append("\t");
		classBuilder.append("}");
		classBuilder.append("\n");
		classBuilder.append("}");
		classBuilder.append("\n");

		writeFileForDefinition(fullyQualifiedName, isAIROnly, classBuilder.toString());
	}

	private void parseInterface(Element apiClassifierElement) throws Exception {
		String fullyQualifiedName = apiClassifierElement.attributeValue("id");
		if (fullyQualifiedName.startsWith("globalClassifier:")) {
			fullyQualifiedName = fullyQualifiedName.substring(17);
		}

		String[] parts = fullyQualifiedName.split(":");
		String packageName = "";
		String interfaceName = fullyQualifiedName;
		if (parts.length > 1) {
			packageName = parts[0];
			interfaceName = parts[1];
			fullyQualifiedName = packageName + "." + interfaceName;
		}

		boolean isAIROnly = isAIROnly(apiClassifierElement.element("prolog"));

		Set<String> importFullyQualifiedNames = new HashSet<String>();
		collectImports(apiClassifierElement, packageName, importFullyQualifiedNames);

		List<String> interfaceFullyQualifiedNames = new ArrayList<String>();
		String access = null;

		Element apiClassifierDetailElement = apiClassifierElement.element("apiClassifierDetail");
		if (apiClassifierDetailElement == null) {
			throw new Exception("apiClassifierDetail not found for: " + interfaceName);
		}
		Element apiClassifierDefElement = apiClassifierDetailElement.element("apiClassifierDef");
		if (apiClassifierDefElement == null) {
			throw new Exception("apiClassifierDef not found for: " + interfaceName);
		}

		List<Element> apiBaseInterfaceElements = apiClassifierDefElement.elements("apiBaseInterface");
		for (Element apiBaseInterfaceElement : apiBaseInterfaceElements) {
			String baseInterfaceFullyQualifiedName = apiBaseInterfaceElement.getTextTrim();
			baseInterfaceFullyQualifiedName = baseInterfaceFullyQualifiedName.replace(":", ".");
			interfaceFullyQualifiedNames.add(baseInterfaceFullyQualifiedName);
		}

		Element apiAccessElement = apiClassifierDefElement.element("apiAccess");
		if (apiAccessElement != null) {
			access = apiAccessElement.attributeValue("value");
		}

		List<Element> apiOperationElements = apiClassifierElement.elements("apiOperation");
		List<Element> apiValueElements = apiClassifierElement.elements("apiValue");

		StringBuilder interfaceBuilder = new StringBuilder();
		interfaceBuilder.append("// generated from: ");
		interfaceBuilder.append(currentFile.getName());
		interfaceBuilder.append("\n");
		interfaceBuilder.append("package");
		if (packageName.length() > 0) {
			interfaceBuilder.append(" ");
			interfaceBuilder.append(packageName);
		}
		interfaceBuilder.append(" ");
		interfaceBuilder.append("{");
		interfaceBuilder.append("\n");
		writeImports(importFullyQualifiedNames, interfaceBuilder);
		interfaceBuilder.append("\t");
		if (access != null && access.length() > 0) {
			interfaceBuilder.append(access);
			interfaceBuilder.append(" ");
		}
		interfaceBuilder.append("interface ");
		interfaceBuilder.append(interfaceName);
		for (int i = 0; i < interfaceFullyQualifiedNames.size(); i++) {
			String interfaceFullyQualifiedName = interfaceFullyQualifiedNames.get(i);
			if (i == 0) {
				interfaceBuilder.append(" extends ");
			} else {
				interfaceBuilder.append(", ");
			}
			interfaceBuilder.append(interfaceFullyQualifiedName);
		}
		interfaceBuilder.append(" ");
		interfaceBuilder.append("{");
		interfaceBuilder.append("\n");
		for (Element apiOperationElement : apiOperationElements) {
			parseFunction(apiOperationElement, null, true, interfaceBuilder);
		}
		for (Element apiValueElement : apiValueElements) {
			parseVariable(apiValueElement, true, interfaceBuilder);
		}
		interfaceBuilder.append("\t");
		interfaceBuilder.append("}");
		interfaceBuilder.append("\n");
		interfaceBuilder.append("}");
		interfaceBuilder.append("\n");

		writeFileForDefinition(fullyQualifiedName, isAIROnly, interfaceBuilder.toString());
	}

	private void parsePackageFunction(Element apiOperationElement) throws Exception {
		String fullyQualifiedName = apiOperationElement.attributeValue("id");
		if (fullyQualifiedName.startsWith("globalOperation:")) {
			fullyQualifiedName = fullyQualifiedName.substring(16);
		}
		if (fullyQualifiedName.equals("Vector")) {
			//special case in the compiler that doesn't get exposed in docs
			return;
		}

		String[] parts = fullyQualifiedName.split(":");
		String packageName = "";
		if (parts.length > 1) {
			packageName = parts[0];
			fullyQualifiedName = packageName + "." + parts[1];
		}

		Set<String> importFullyQualifiedNames = new HashSet<String>();
		collectImports(apiOperationElement, packageName, importFullyQualifiedNames);

		boolean isAIROnly = isAIROnly(apiOperationElement.element("prolog"));

		StringBuilder functionBuilder = new StringBuilder();
		functionBuilder.append("// generated from: ");
		functionBuilder.append(currentFile.getName());
		functionBuilder.append("\n");
		functionBuilder.append("package");
		if (packageName != null && packageName.length() > 0) {
			functionBuilder.append(" ");
			functionBuilder.append(packageName);
		}
		functionBuilder.append(" ");
		functionBuilder.append("{");
		functionBuilder.append("\n");
		writeImports(importFullyQualifiedNames, functionBuilder);
		parseFunction(apiOperationElement, null, false, functionBuilder);
		functionBuilder.append("}");
		functionBuilder.append("\n");

		writeFileForDefinition(fullyQualifiedName, isAIROnly, functionBuilder.toString());
	}

	private void parsePackageVariable(Element apiValueElement) throws Exception {
		String fullyQualifiedName = apiValueElement.attributeValue("id");
		if (fullyQualifiedName.startsWith("globalValue:")) {
			fullyQualifiedName = fullyQualifiedName.substring(12);
		}

		String[] parts = fullyQualifiedName.split(":");
		String packageName = "";
		String variableName = fullyQualifiedName;
		if (parts.length > 1) {
			packageName = parts[0];
			variableName = parts[1];
			fullyQualifiedName = packageName + "." + variableName;
		}

		if (variableName.startsWith("-")) {
			//nothing to do here
			//it's just a negative value getting documented (like -Infinity)
			return;
		}

		Set<String> importFullyQualifiedNames = new HashSet<String>();
		collectImports(apiValueElement, packageName, importFullyQualifiedNames);

		boolean isAIROnly = isAIROnly(apiValueElement.element("prolog"));

		StringBuilder variableBuilder = new StringBuilder();
		variableBuilder.append("// generated from: ");
		variableBuilder.append(currentFile.getName());
		variableBuilder.append("\n");
		variableBuilder.append("package");
		if (packageName != null && packageName.length() > 0) {
			variableBuilder.append(" ");
			variableBuilder.append(packageName);
		}
		variableBuilder.append(" ");
		variableBuilder.append("{");
		variableBuilder.append("\n");
		writeImports(importFullyQualifiedNames, variableBuilder);
		parseVariable(apiValueElement, false, variableBuilder);
		variableBuilder.append("}");
		variableBuilder.append("\n");

		writeFileForDefinition(fullyQualifiedName, isAIROnly, variableBuilder.toString());
	}

	private void parseVariable(Element apiValueElement, boolean forInterface, StringBuilder variableBuilder)
			throws Exception {
		String variableName = apiValueElement.element("apiName").getTextTrim();

		boolean isGetter = false;
		boolean isSetter = false;
		boolean isConst = true;
		boolean isStatic = false;
		boolean isOverride = false;
		String variableType = "*";
		String access = null;

		Element apiValueDetailElement = apiValueElement.element("apiValueDetail");
		if (apiValueDetailElement == null) {
			throw new Exception("apiValueDetail not found for: " + variableName);
		}
		Element apiValueDefElement = apiValueDetailElement.element("apiValueDef");
		if (apiValueDefElement == null) {
			throw new Exception("apiValueDef not found for: " + variableName);
		}

		Element apiValueClassifierElement = apiValueDefElement.element("apiValueClassifier");
		if (apiValueClassifierElement != null) {
			variableType = apiValueClassifierElement.getTextTrim();
			variableType = variableType.replace(":", ".");
		}

		Element apiAccessElement = apiValueDefElement.element("apiAccess");
		if (!forInterface && apiAccessElement != null) {
			access = apiAccessElement.attributeValue("value");
		}

		Element apiStaticElement = apiValueDefElement.element("apiStatic");
		if (!forInterface && apiStaticElement != null) {
			isStatic = true;
		}

		Element apiDynamicElement = apiValueDefElement.element("apiDynamic");
		if (apiDynamicElement != null) {
			isConst = false;
		}

		Element apiValueAccessElement = apiValueDefElement.element("apiValueAccess");
		if (apiValueAccessElement != null) {
			String readwrite = apiValueAccessElement.attributeValue("value");
			isGetter = "readwrite".equals(readwrite) || "read".equals(readwrite);
			isSetter = "readwrite".equals(readwrite) || "write".equals(readwrite);
		}

		Element apiIsOverrideElement = apiValueDefElement.element("apiIsOverride");
		if (apiIsOverrideElement != null) {
			isOverride = true;
		}

		if (!forInterface && isGetter && isSetter && !isOverride
				&& apiValueElement.attributeValue("id").endsWith(":set")) {
			//skip the getter because it is already defined on the base class
			//example: flash.text.engine.TextElement.text
			isGetter = false;
		}

		Element apiDataElement = apiValueDefElement.element("apiData");

		if (isGetter) {
			variableBuilder.append("\t");
			if (access != null && access.length() > 0) {
				variableBuilder.append(access);
				variableBuilder.append(" ");
			}
			if (isStatic) {
				variableBuilder.append("static ");
			}
			if (!forInterface) {
				variableBuilder.append("native ");
			}
			if (isOverride) {
				variableBuilder.append("override ");
			}
			variableBuilder.append("function ");
			variableBuilder.append("get ");
			variableBuilder.append(variableName);
			variableBuilder.append("(");
			variableBuilder.append(")");
			variableBuilder.append(":");
			variableBuilder.append(variableType);
			variableBuilder.append(";");
			variableBuilder.append("\n");
		}

		if (isSetter) {
			variableBuilder.append("\t");
			if (access != null && access.length() > 0) {
				variableBuilder.append(access);
				variableBuilder.append(" ");
			}
			if (isStatic) {
				variableBuilder.append("static ");
			}
			if (!forInterface) {
				variableBuilder.append("native ");
			}
			if (isOverride) {
				variableBuilder.append("override ");
			}
			variableBuilder.append("function ");
			variableBuilder.append("set ");
			variableBuilder.append(variableName);
			variableBuilder.append("(");
			variableBuilder.append("value");
			variableBuilder.append(":");
			variableBuilder.append(variableType);
			variableBuilder.append(")");
			variableBuilder.append(":");
			variableBuilder.append("void");
			variableBuilder.append(";");
			variableBuilder.append("\n");
		}

		if (!isGetter && !isSetter) {
			variableBuilder.append("\t");
			if (access != null && access.length() > 0) {
				variableBuilder.append(access);
				variableBuilder.append(" ");
			}
			if (isStatic) {
				variableBuilder.append("static ");
			}
			if (isConst) {
				variableBuilder.append("const ");
			} else {
				variableBuilder.append("var ");
			}
			variableBuilder.append(variableName);
			variableBuilder.append(":");
			variableBuilder.append(variableType);
			if (apiDataElement != null) {
				writeVariableOrParameterValue(apiDataElement, variableType, variableBuilder);
			}
			variableBuilder.append(";");
			variableBuilder.append("\n");
		}
	}

	private void parseFunction(Element apiOperationElement, String contextClassName, boolean forInterface,
			StringBuilder functionBuilder) throws Exception {
		String functionName = apiOperationElement.element("apiName").getTextTrim();

		boolean isStatic = false;
		boolean isOverride = false;
		String returnType = "*";
		String access = null;

		Element apiOperationDetailElement = apiOperationElement.element("apiOperationDetail");
		if (apiOperationDetailElement == null) {
			throw new Exception("apiOperationDetail not found for: " + functionName);
		}
		Element apiOperationDefElement = apiOperationDetailElement.element("apiOperationDef");
		if (apiOperationDefElement == null) {
			throw new Exception("apiOperationDef not found for: " + functionName);
		}

		Element apiIsOverrideElement = apiOperationDefElement.element("apiIsOverride");
		if (apiIsOverrideElement != null) {
			isOverride = true;
		}

		Element apiReturnElement = apiOperationDefElement.element("apiReturn");
		if (apiReturnElement != null) {
			Element apiTypeElement = apiReturnElement.element("apiType");
			if (apiTypeElement != null) {
				returnType = parseReturnOrParamType(apiTypeElement, contextClassName);
			}
			Element apiOperationClassifierElement = apiReturnElement.element("apiOperationClassifier");
			if (apiOperationClassifierElement != null) {
				returnType = apiOperationClassifierElement.getTextTrim();
				returnType = returnType.replace(":", ".");
			}
		}

		Element apiAccessElement = apiOperationDefElement.element("apiAccess");
		if (!forInterface && apiAccessElement != null) {
			access = apiAccessElement.attributeValue("value");
		}

		Element apiStaticElement = apiOperationDefElement.element("apiStatic");
		if (!forInterface && apiStaticElement != null) {
			isStatic = true;
		}

		List<Element> apiParamElements = apiOperationDefElement.elements("apiParam");

		if ("public".equals(access) && ("toString".equals(functionName) || "toLocaleString".equals(functionName))
				|| "valueOf".equals(functionName) || "hasOwnProperty".equals(functionName)
				|| "propertyIsEnumerable".equals(functionName)) {
			return;
		}

		functionBuilder.append("\t");
		if (access != null && access.length() > 0) {
			functionBuilder.append(access);
			functionBuilder.append(" ");
		}
		if (isStatic) {
			functionBuilder.append("static ");
		}
		if (!forInterface) {
			functionBuilder.append("native ");
		}
		if (isOverride) {
			functionBuilder.append("override ");
		}
		functionBuilder.append("function ");
		functionBuilder.append(functionName);
		functionBuilder.append("(");
		parseParameters(apiParamElements, contextClassName, functionBuilder);
		functionBuilder.append(")");
		functionBuilder.append(":");
		functionBuilder.append(returnType);
		functionBuilder.append(";");
		functionBuilder.append("\n");
	}

	private void parseConstructor(Element apiConstructorElement, String contextClassName, StringBuilder functionBuilder)
			throws Exception {
		String constructorName = contextClassName != null ? contextClassName
				: apiConstructorElement.element("apiName").getTextTrim();

		String access = null;

		Element apiConstructorDetailElement = apiConstructorElement.element("apiConstructorDetail");
		if (apiConstructorDetailElement == null) {
			throw new Exception("apiConstructorDetail not found for: " + constructorName);
		}
		Element apiConstructorDefElement = apiConstructorDetailElement.element("apiConstructorDef");
		if (apiConstructorDefElement == null) {
			throw new Exception("apiConstructorDef not found for: " + constructorName);
		}

		Element apiAccessElement = apiConstructorDefElement.element("apiAccess");
		if (apiAccessElement != null) {
			access = apiAccessElement.attributeValue("value");
		}

		List<Element> apiParamElements = apiConstructorDefElement.elements("apiParam");

		functionBuilder.append("\t");
		if (access != null && access.length() > 0) {
			functionBuilder.append(access);
			functionBuilder.append(" ");
		}
		functionBuilder.append("native ");
		functionBuilder.append("function ");
		functionBuilder.append(constructorName);
		functionBuilder.append("(");
		parseParameters(apiParamElements, contextClassName, functionBuilder);
		functionBuilder.append(")");
		functionBuilder.append(";");
		functionBuilder.append("\n");
	}

	private String parseReturnOrParamType(Element apiTypeElement, String contextClassName) throws Exception {
		String apiTypeValue = apiTypeElement.attributeValue("value");
		if ("restParam".equals(apiTypeValue)) {
			return null;
		}
		if ("any".equals(apiTypeValue)) {
			return "*";
		}
		if ("T".equals(apiTypeValue)) {
			return "T";
		}
		if ("void".equals(apiTypeValue)) {
			return "void";
		}
		if (apiTypeValue.startsWith("Vector$")) {
			String[] parts = apiTypeValue.split("\\$");
			String vectorItemType = parts[1];
			vectorItemType = vectorItemType.replace(":", ".");
			if (contextClassName != null && contextClassName.startsWith("Vector$") && vectorItemType.equals("T")) {
				return contextClassName;
			}
			return "Vector.<" + vectorItemType + ">";
		} else {
			throw new Exception("Unknown apiType value: " + apiTypeValue);
		}
	}

	private void parseParameters(List<Element> apiParamElements, String contextClassName, StringBuilder functionBuilder)
			throws Exception {
		for (int i = 0; i < apiParamElements.size(); i++) {
			if (i > 0) {
				functionBuilder.append(", ");
			}
			Element apiParamElement = apiParamElements.get(i);
			Element apiTypeElement = apiParamElement.element("apiType");
			String paramType = null;
			if (apiTypeElement != null) {
				String apiTypeValue = apiTypeElement.attributeValue("value");
				if ("restParam".equals(apiTypeValue)) {
					functionBuilder.append("...");
				}
				paramType = parseReturnOrParamType(apiTypeElement, contextClassName);
			}
			Element apiItemNameElement = apiParamElement.element("apiItemName");
			if (apiItemNameElement == null) {
				throw new Exception("apiItemName not found");
			}
			functionBuilder.append(apiItemNameElement.getTextTrim());
			Element apiOperationClassifierElement = apiParamElement.element("apiOperationClassifier");
			if (apiOperationClassifierElement != null) {
				paramType = apiOperationClassifierElement.getTextTrim();
				paramType = paramType.replace(":", ".");
			}
			if (paramType != null) {
				functionBuilder.append(":");
				functionBuilder.append(paramType);
			}
			Element apiDataElement = apiParamElement.element("apiData");
			if (apiDataElement != null) {
				writeVariableOrParameterValue(apiDataElement, paramType, functionBuilder);
			}
		}
	}

	private void writeVariableOrParameterValue(Element apiDataElement, String varType, StringBuilder builder) {
		builder.append(" = ");
		String paramValue = apiDataElement.getTextTrim();
		if ("unknown".equals(paramValue)) {
			paramValue = "null";
		}
		boolean isString = ("String".equals(varType) || paramValue.matches("[A-Za-z\\*]+"))
				&& !"undefined".equals(paramValue) && !"null".equals(paramValue) && !"NaN".equals(paramValue)
				&& !"true".equals(paramValue) && !"false".equals(paramValue);
		if (isString) {
			builder.append("\"");
		}
		builder.append(paramValue);
		if (isString) {
			builder.append("\"");
		}
	}

	private void collectImport(String fullyQualifiedName, String forPackage, Set<String> result) throws Exception {
		String[] parts = fullyQualifiedName.split(":");
		if (parts.length == 1) {
			//top-level package
			return;
		}
		String packageName = parts[0];
		if (packageName.equals(forPackage)) {
			//same package
			return;
		}
		result.add(packageName + "." + parts[1]);
	}

	private void collectImports(Element element, String forPackage, Set<String> result) throws Exception {
		String elementName = element.getName();
		if ("apiClassifier".equals(elementName)) {
			String className = element.element("apiName").getTextTrim();

			Element apiClassifierDetailElement = element.element("apiClassifierDetail");
			if (apiClassifierDetailElement == null) {
				throw new Exception("apiClassifierDetail not found for: " + className);
			}
			Element apiClassifierDefElement = apiClassifierDetailElement.element("apiClassifierDef");
			if (apiClassifierDefElement == null) {
				throw new Exception("apiClassifierDef not found for: " + className);
			}

			Element apiBaseClassifierElement = apiClassifierDefElement.element("apiBaseClassifier");
			if (apiBaseClassifierElement != null) {
				String baseClassType = apiBaseClassifierElement.getTextTrim();
				collectImport(baseClassType, forPackage, result);
			}

			List<Element> apiBaseInterfaceElements = apiClassifierDefElement.elements("apiBaseInterface");
			for (Element apiBaseInterfaceElement : apiBaseInterfaceElements) {
				String interfaceType = apiBaseInterfaceElement.getTextTrim();
				collectImport(interfaceType, forPackage, result);
			}

			Element apiConstructorElement = element.element("apiConstructor");
			if (apiConstructorElement != null) {
				collectImports(apiConstructorElement, forPackage, result);
			}

			List<Element> apiOperationElements = element.elements("apiOperation");
			for (Element apiOperationElement : apiOperationElements) {
				collectImports(apiOperationElement, forPackage, result);
			}

			List<Element> apiValueElements = element.elements("apiValue");
			for (Element apiValueElement : apiValueElements) {
				collectImports(apiValueElement, forPackage, result);
			}
		}
		if ("apiOperation".equals(elementName)) {
			String functionName = element.element("apiName").getTextTrim();

			Element apiOperationDetailElement = element.element("apiOperationDetail");
			if (apiOperationDetailElement == null) {
				throw new Exception("apiOperationDetail not found for: " + functionName);
			}
			Element apiOperationDefElement = apiOperationDetailElement.element("apiOperationDef");
			if (apiOperationDefElement == null) {
				throw new Exception("apiOperationDef not found for: " + functionName);
			}

			Element apiReturnElement = apiOperationDefElement.element("apiReturn");
			if (apiReturnElement != null) {
				Element apiTypeElement = apiOperationDefElement.element("apiType");
				if (apiTypeElement != null) {
					String apiTypeValue = apiTypeElement.attributeValue("value");
					if (apiTypeValue.startsWith("Vector$")) {
						String[] parts = apiTypeValue.split("\\$");
						String vectorItemType = parts[1];
						collectImport(vectorItemType, forPackage, result);
					}
				}
				Element apiOperationClassifierElement = apiReturnElement.element("apiOperationClassifier");
				if (apiOperationClassifierElement != null) {
					String returnType = apiOperationClassifierElement.getTextTrim();
					collectImport(returnType, forPackage, result);
				}
			}

			List<Element> apiParamElements = apiOperationDefElement.elements("apiParam");
			for (Element apiParamElement : apiParamElements) {
				Element apiTypeElement = apiParamElement.element("apiType");
				if (apiTypeElement != null) {
					String apiTypeValue = apiTypeElement.attributeValue("value");
					if (apiTypeValue.startsWith("Vector$")) {
						String[] parts = apiTypeValue.split("\\$");
						String vectorItemType = parts[1];
						collectImport(vectorItemType, forPackage, result);
					}
				}
				Element apiOperationClassifierElement = apiParamElement.element("apiOperationClassifier");
				if (apiOperationClassifierElement != null) {
					String paramType = apiOperationClassifierElement.getTextTrim();
					collectImport(paramType, forPackage, result);
				}
			}
		}
		if ("apiConstructor".equals(elementName)) {
			String functionName = element.element("apiName").getTextTrim();

			Element aapiConstructorDetailElement = element.element("apiConstructorDetail");
			if (aapiConstructorDetailElement == null) {
				throw new Exception("apiConstructor not found for: " + functionName);
			}
			Element apiConstructorDefElement = aapiConstructorDetailElement.element("apiConstructorDef");
			if (apiConstructorDefElement == null) {
				throw new Exception("apiConstructorDef not found for: " + functionName);
			}

			List<Element> apiParamElements = apiConstructorDefElement.elements("apiParam");
			for (Element apiParamElement : apiParamElements) {
				Element apiTypeElement = apiParamElement.element("apiType");
				if (apiTypeElement != null) {
					String apiTypeValue = apiTypeElement.attributeValue("value");
					if (apiTypeValue.startsWith("Vector$")) {
						String[] parts = apiTypeValue.split("\\$");
						String vectorItemType = parts[1];
						collectImport(vectorItemType, forPackage, result);
					}
				}
				Element apiOperationClassifierElement = apiParamElement.element("apiOperationClassifier");
				if (apiOperationClassifierElement != null) {
					String paramType = apiOperationClassifierElement.getTextTrim();
					collectImport(paramType, forPackage, result);
				}
			}
		}
		if ("apiValue".equals(elementName)) {
			String variableName = element.element("apiName").getTextTrim();

			Element apiValueDetailElement = element.element("apiValueDetail");
			if (apiValueDetailElement == null) {
				throw new Exception("apiValueDetail not found for: " + variableName);
			}
			Element apiValueDefElement = apiValueDetailElement.element("apiValueDef");
			if (apiValueDefElement == null) {
				throw new Exception("apiValueDef not found for: " + variableName);
			}

			Element apiValueClassifierElement = apiValueDefElement.element("apiValueClassifier");
			if (apiValueClassifierElement != null) {
				String variableType = apiValueClassifierElement.getTextTrim();
				collectImport(variableType, forPackage, result);
			}
		}
	}

	private void writeImports(Set<String> imports, StringBuilder builder) {
		for (String importName : imports) {
			builder.append("\t");
			builder.append("import ");
			builder.append(importName);
			builder.append(";");
			builder.append("\n");
		}
		if (imports.size() > 0) {
			builder.append("\n");
		}
	}
}