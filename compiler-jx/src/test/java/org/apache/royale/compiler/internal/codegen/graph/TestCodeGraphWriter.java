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

package org.apache.royale.compiler.internal.codegen.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Test;

public class TestCodeGraphWriter
{
    @Test
    public void testDeterministicDocument() throws IOException
    {
        CodeGraphModel model = new CodeGraphModel("js", null);
        CodeGraphSymbol second = new CodeGraphSymbol("as3://example/Zed", "example.Zed", "Zed", "example", "class");
        second.setSource("example\\Zed.as");
        CodeGraphSymbol first = new CodeGraphSymbol("as3://example/Alpha", "example.Alpha", "Alpha", "example", "interface");
        model.addSymbol(second);
        model.addSymbol(first);

        StringWriter output = new StringWriter();
        new CodeGraphWriter().write(model, output);

        assertConformsToSchema(output.toString());

        assertEquals("{\n"
                + "  \"schemaVersion\": \"1.0\",\n"
                + "  \"target\": \"js\",\n"
                + "  \"module\": null,\n"
                + "  \"symbols\": [\n"
                + "    {\n"
                + "      \"id\": \"as3://example/Alpha\",\n"
                + "      \"qualifiedName\": \"example.Alpha\",\n"
                + "      \"baseName\": \"Alpha\",\n"
                + "      \"package\": \"example\",\n"
                + "      \"kind\": \"interface\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"id\": \"as3://example/Zed\",\n"
                + "      \"qualifiedName\": \"example.Zed\",\n"
                + "      \"baseName\": \"Zed\",\n"
                + "      \"package\": \"example\",\n"
                + "      \"kind\": \"class\",\n"
                + "      \"source\": \"example/Zed.as\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"externalSymbols\": []\n"
                + "}\n", output.toString());
    }

    @Test
    public void testSemanticDetails() throws IOException
    {
        CodeGraphModel model = new CodeGraphModel("swf", "example-module");
        CodeGraphSymbol owner = new CodeGraphSymbol("as3://example/Widget", "example.Widget", "Widget", "example", "class");
        owner.setBaseType(new CodeGraphReference("as3://Object", "Object", true, false));
        CodeGraphMetadata metadata = new CodeGraphMetadata("DefaultProperty");
        metadata.addAttribute(new CodeGraphMetadataAttribute(null, "content"));
        owner.addMetadata(metadata);
        CodeGraphASDoc asDoc = new CodeGraphASDoc("A widget.");
        asDoc.addTag(new CodeGraphASDocTag("copy", "example.Base#label"));
        asDoc.addTag(new CodeGraphASDocTag("see", "example.Other"));
        owner.setASDoc(asDoc);
        CodeGraphSymbol method = new CodeGraphSymbol("as3://example/Widget#work(String)",
                "example.Widget.work", "work", "example", "method");
        method.setDeclaringType(new CodeGraphReference(owner.getId(), owner.getQualifiedName(), false, false));
        method.setReturnType(new CodeGraphReference("as3://Boolean", "Boolean", true, false));
        method.addParameter(new CodeGraphParameter("value",
                new CodeGraphReference("as3://String", "String", true, false), false, false, null));
        owner.addMember(method);
        model.addSymbol(owner);

        StringWriter firstOutput = new StringWriter();
        StringWriter secondOutput = new StringWriter();
        CodeGraphWriter writer = new CodeGraphWriter();
        writer.write(model, firstOutput);
        writer.write(model, secondOutput);

        assertEquals(firstOutput.toString(), secondOutput.toString());
        assertConformsToSchema(firstOutput.toString());
        assertTrue(firstOutput.toString().contains("\"baseType\": {"));
        assertTrue(firstOutput.toString().contains("\"members\": ["));
        assertTrue(firstOutput.toString().contains("\"parameters\": ["));
        assertTrue(firstOutput.toString().contains("\"metadata\": ["));
        assertTrue(firstOutput.toString().contains("\"asdoc\": {"));
        assertTrue(firstOutput.toString().contains("\"description\": \"A widget.\""));
        assertTrue(firstOutput.toString().contains("\"name\": \"copy\""));
        assertTrue(firstOutput.toString().contains("\"key\": null"));
        assertTrue(firstOutput.toString().contains("\"external\": true"));
    }

    @Test
    public void testGoldenDocumentConformsToSchema() throws IOException
    {
        InputStream documentInput = getClass().getResourceAsStream("/codegraph/golden/GraphRoot.json");
        assertTrue("Golden code graph resource is missing", documentInput != null);
        try
        {
            assertConformsToSchema(new ObjectMapper().readTree(documentInput));
        }
        finally
        {
            documentInput.close();
        }
    }

    @Test
    public void testNonFiniteValuesAreValidJSONStrings() throws IOException
    {
        CodeGraphModel model = new CodeGraphModel("js", "non-finite-values");
        CodeGraphSymbol symbol = new CodeGraphSymbol("as3://example/Values", "example.Values", "Values",
                "example", "class");
        CodeGraphSymbol nan = new CodeGraphSymbol("as3://example/Values#nan", "example.Values.nan", "nan",
                "example", "constant");
        nan.setInitialValue(Double.NaN);
        CodeGraphSymbol positiveInfinity = new CodeGraphSymbol("as3://example/Values#positiveInfinity",
                "example.Values.positiveInfinity", "positiveInfinity", "example", "constant");
        positiveInfinity.setInitialValue(Double.POSITIVE_INFINITY);
        CodeGraphSymbol negativeInfinity = new CodeGraphSymbol("as3://example/Values#negativeInfinity",
                "example.Values.negativeInfinity", "negativeInfinity", "example", "constant");
        negativeInfinity.setInitialValue(Double.NEGATIVE_INFINITY);
        symbol.addMember(nan);
        symbol.addMember(positiveInfinity);
        symbol.addMember(negativeInfinity);
        model.addSymbol(symbol);

        StringWriter output = new StringWriter();
        new CodeGraphWriter().write(model, output);

        assertConformsToSchema(output.toString());
        assertTrue(output.toString().contains("\"initialValue\": \"NaN\""));
        assertTrue(output.toString().contains("\"initialValue\": \"Infinity\""));
        assertTrue(output.toString().contains("\"initialValue\": \"-Infinity\""));
    }

    private void assertConformsToSchema(String document) throws IOException
    {
        assertConformsToSchema(new ObjectMapper().readTree(document));
    }

    private void assertConformsToSchema(JsonNode document) throws IOException
    {
        InputStream schemaInput = getClass().getResourceAsStream("codegraph-1.0.schema.json");
        assertTrue("Code graph schema resource is missing", schemaInput != null);
        try
        {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = factory.getSchema(schemaInput);
            Set<ValidationMessage> errors = schema.validate(document);
            assertTrue(errors.toString(), errors.isEmpty());
        }
        finally
        {
            schemaInput.close();
        }
    }
}