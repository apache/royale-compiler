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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Writes code graph models as deterministic JSON documents.
 */
public final class CodeGraphWriter
{
    private static final Comparator<CodeGraphSymbol> SYMBOL_COMPARATOR = new Comparator<CodeGraphSymbol>()
    {
        @Override
        public int compare(CodeGraphSymbol first, CodeGraphSymbol second)
        {
            return first.getId().compareTo(second.getId());
        }
    };

    public void write(CodeGraphModel model, Writer writer) throws IOException
    {
        writer.write("{\n");
        writeProperty(writer, 1, "schemaVersion", CodeGraphModel.SCHEMA_VERSION, true);
        writeProperty(writer, 1, "target", model.getTarget(), true);
        writeProperty(writer, 1, "module", model.getModule(), true);
        writeSymbols(writer, "symbols", model.getSymbols(), 1, true);
        writeSymbols(writer, "externalSymbols", model.getExternalSymbols(), 1, false);
        writer.write("}\n");
    }

    private void writeSymbols(Writer writer, String name, List<CodeGraphSymbol> symbols, int level, boolean comma) throws IOException
    {
        indent(writer, level);
        writeString(writer, name);
        writer.write(": [");
        List<CodeGraphSymbol> sortedSymbols = new ArrayList<CodeGraphSymbol>(symbols);
        Collections.sort(sortedSymbols, SYMBOL_COMPARATOR);
        if (!sortedSymbols.isEmpty())
            writer.write('\n');
        for (int i = 0; i < sortedSymbols.size(); i++)
        {
            writeSymbol(writer, sortedSymbols.get(i), level + 1);
            if (i + 1 < sortedSymbols.size())
                writer.write(',');
            writer.write('\n');
        }
        if (!sortedSymbols.isEmpty())
            indent(writer, level);
        writer.write(']');
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeSymbol(Writer writer, CodeGraphSymbol symbol, int level) throws IOException
    {
        indent(writer, level);
        writer.write("{\n");
        writeProperty(writer, level + 1, "id", symbol.getId(), true);
        writeProperty(writer, level + 1, "qualifiedName", symbol.getQualifiedName(), true);
        writeProperty(writer, level + 1, "baseName", symbol.getBaseName(), true);
        writeProperty(writer, level + 1, "package", symbol.getPackageName(), true);
        int optionalPropertyCount = getOptionalPropertyCount(symbol);
        writeProperty(writer, level + 1, "kind", symbol.getKind(), optionalPropertyCount > 0);
        if (symbol.isExternal())
            writeBooleanProperty(writer, level + 1, "external", true, --optionalPropertyCount > 0);
        if (symbol.getSource() != null)
            writeProperty(writer, level + 1, "source", symbol.getSource().replace('\\', '/'), --optionalPropertyCount > 0);
        if (symbol.getOrigin() != null)
            writeProperty(writer, level + 1, "origin", symbol.getOrigin().replace('\\', '/'), --optionalPropertyCount > 0);
        if (symbol.getVisibility() != null)
            writeProperty(writer, level + 1, "visibility", symbol.getVisibility(), --optionalPropertyCount > 0);
        if (hasModifiers(symbol))
            writeModifiers(writer, symbol, level + 1, --optionalPropertyCount > 0);
        if (symbol.hasInitialValue())
            writeValueProperty(writer, level + 1, "initialValue", symbol.getInitialValue(), --optionalPropertyCount > 0);
        if (symbol.getDeclaringType() != null)
            writeReferenceProperty(writer, level + 1, "declaringType", symbol.getDeclaringType(), --optionalPropertyCount > 0);
        if (symbol.getType() != null)
            writeReferenceProperty(writer, level + 1, "type", symbol.getType(), --optionalPropertyCount > 0);
        if (symbol.getReturnType() != null)
            writeReferenceProperty(writer, level + 1, "returnType", symbol.getReturnType(), --optionalPropertyCount > 0);
        if (symbol.getBaseType() != null)
            writeReferenceProperty(writer, level + 1, "baseType", symbol.getBaseType(), --optionalPropertyCount > 0);
        if (symbol.getOverriddenMember() != null)
            writeReferenceProperty(writer, level + 1, "overrides", symbol.getOverriddenMember(), --optionalPropertyCount > 0);
        if (symbol.getImplementedMember() != null)
            writeReferenceProperty(writer, level + 1, "implements", symbol.getImplementedMember(), --optionalPropertyCount > 0);
        if (symbol.getASDoc() != null)
            writeASDoc(writer, symbol.getASDoc(), level + 1, --optionalPropertyCount > 0);
        if (!symbol.getInterfaces().isEmpty())
        {
            writeReferences(writer, "interfaces", symbol.getInterfaces(), level + 1, --optionalPropertyCount > 0);
        }
        if (!symbol.getParameters().isEmpty())
        {
            writeParameters(writer, symbol.getParameters(), level + 1, --optionalPropertyCount > 0);
        }
        if (!symbol.getMetadata().isEmpty())
        {
            writeMetadata(writer, symbol.getMetadata(), level + 1, --optionalPropertyCount > 0);
        }
        if (!symbol.getMembers().isEmpty())
            writeSymbols(writer, "members", symbol.getMembers(), level + 1, false);
        indent(writer, level);
        writer.write('}');
    }

    private int getOptionalPropertyCount(CodeGraphSymbol symbol)
    {
        int result = 0;
        if (symbol.isExternal())
            result++;
        if (symbol.getSource() != null)
            result++;
        if (symbol.getOrigin() != null)
            result++;
        if (symbol.getVisibility() != null)
            result++;
        if (hasModifiers(symbol))
            result++;
        if (symbol.hasInitialValue())
            result++;
        if (symbol.getDeclaringType() != null)
            result++;
        if (symbol.getType() != null)
            result++;
        if (symbol.getReturnType() != null)
            result++;
        if (symbol.getBaseType() != null)
            result++;
        if (symbol.getOverriddenMember() != null)
            result++;
        if (symbol.getImplementedMember() != null)
            result++;
        if (symbol.getASDoc() != null)
            result++;
        if (!symbol.getInterfaces().isEmpty())
            result++;
        if (!symbol.getParameters().isEmpty())
            result++;
        if (!symbol.getMetadata().isEmpty())
            result++;
        if (!symbol.getMembers().isEmpty())
            result++;
        return result;
    }

    private boolean hasModifiers(CodeGraphSymbol symbol)
    {
        return symbol.isStatic() || symbol.isFinal() || symbol.isDynamic() || symbol.isOverride()
                || symbol.isAbstract() || symbol.isNative();
    }

    private void writeModifiers(Writer writer, CodeGraphSymbol symbol, int level, boolean comma) throws IOException
    {
        indent(writer, level);
        writeString(writer, "modifiers");
        writer.write(": {\n");
        writeBooleanProperty(writer, level + 1, "static", symbol.isStatic(), true);
        writeBooleanProperty(writer, level + 1, "final", symbol.isFinal(), true);
        writeBooleanProperty(writer, level + 1, "dynamic", symbol.isDynamic(), true);
        writeBooleanProperty(writer, level + 1, "override", symbol.isOverride(), true);
        writeBooleanProperty(writer, level + 1, "abstract", symbol.isAbstract(), true);
        writeBooleanProperty(writer, level + 1, "native", symbol.isNative(), false);
        indent(writer, level);
        writer.write('}');
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeASDoc(Writer writer, CodeGraphASDoc asDoc, int level, boolean comma) throws IOException
    {
        indent(writer, level);
        writeString(writer, "asdoc");
        writer.write(": {\n");
        writeProperty(writer, level + 1, "description", asDoc.getDescription(), true);
        indent(writer, level + 1);
        writeString(writer, "tags");
        writer.write(": [");
        if (!asDoc.getTags().isEmpty())
            writer.write('\n');
        for (int i = 0; i < asDoc.getTags().size(); i++)
        {
            CodeGraphASDocTag tag = asDoc.getTags().get(i);
            indent(writer, level + 2);
            writer.write("{\n");
            writeProperty(writer, level + 3, "name", tag.getName(), true);
            writeProperty(writer, level + 3, "description", tag.getDescription(), false);
            indent(writer, level + 2);
            writer.write('}');
            if (i + 1 < asDoc.getTags().size())
                writer.write(',');
            writer.write('\n');
        }
        if (!asDoc.getTags().isEmpty())
            indent(writer, level + 1);
        writer.write("]\n");
        indent(writer, level);
        writer.write('}');
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeReferenceProperty(Writer writer, int level, String name, CodeGraphReference reference,
            boolean comma) throws IOException
    {
        indent(writer, level);
        writeString(writer, name);
        writer.write(": ");
        writeReference(writer, reference, level);
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeReference(Writer writer, CodeGraphReference reference, int level) throws IOException
    {
        writer.write("{\n");
        writeProperty(writer, level + 1, "id", reference.getId(), true);
        writeProperty(writer, level + 1, "qualifiedName", reference.getQualifiedName(), true);
        writeBooleanProperty(writer, level + 1, "external", reference.isExternal(), true);
        writeBooleanProperty(writer, level + 1, "unresolved", reference.isUnresolved(), false);
        indent(writer, level);
        writer.write('}');
    }

    private void writeReferences(Writer writer, String name, List<CodeGraphReference> references, int level,
            boolean comma) throws IOException
    {
        indent(writer, level);
        writeString(writer, name);
        writer.write(": [\n");
        for (int i = 0; i < references.size(); i++)
        {
            indent(writer, level + 1);
            writeReference(writer, references.get(i), level + 1);
            if (i + 1 < references.size())
                writer.write(',');
            writer.write('\n');
        }
        indent(writer, level);
        writer.write(']');
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeParameters(Writer writer, List<CodeGraphParameter> parameters, int level, boolean comma)
            throws IOException
    {
        indent(writer, level);
        writeString(writer, "parameters");
        writer.write(": [\n");
        for (int i = 0; i < parameters.size(); i++)
        {
            CodeGraphParameter parameter = parameters.get(i);
            indent(writer, level + 1);
            writer.write("{\n");
            writeProperty(writer, level + 2, "name", parameter.getName(), true);
            if (parameter.getType() == null)
                writeProperty(writer, level + 2, "type", null, true);
            else
                writeReferenceProperty(writer, level + 2, "type", parameter.getType(), true);
            writeBooleanProperty(writer, level + 2, "optional", parameter.isOptional(), true);
            writeBooleanProperty(writer, level + 2, "rest", parameter.isRest(), true);
            writeValueProperty(writer, level + 2, "defaultValue", parameter.getDefaultValue(), false);
            indent(writer, level + 1);
            writer.write('}');
            if (i + 1 < parameters.size())
                writer.write(',');
            writer.write('\n');
        }
        indent(writer, level);
        writer.write(']');
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeMetadata(Writer writer, List<CodeGraphMetadata> metadata, int level, boolean comma)
            throws IOException
    {
        indent(writer, level);
        writeString(writer, "metadata");
        writer.write(": [\n");
        for (int i = 0; i < metadata.size(); i++)
        {
            CodeGraphMetadata metadataTag = metadata.get(i);
            indent(writer, level + 1);
            writer.write("{\n");
            writeProperty(writer, level + 2, "name", metadataTag.getName(), true);
            indent(writer, level + 2);
            writeString(writer, "attributes");
            writer.write(": [");
            if (!metadataTag.getAttributes().isEmpty())
                writer.write('\n');
            for (int j = 0; j < metadataTag.getAttributes().size(); j++)
            {
                CodeGraphMetadataAttribute attribute = metadataTag.getAttributes().get(j);
                indent(writer, level + 3);
                writer.write("{\n");
                writeProperty(writer, level + 4, "key", attribute.getKey(), true);
                writeProperty(writer, level + 4, "value", attribute.getValue(), false);
                indent(writer, level + 3);
                writer.write('}');
                if (j + 1 < metadataTag.getAttributes().size())
                    writer.write(',');
                writer.write('\n');
            }
            if (!metadataTag.getAttributes().isEmpty())
                indent(writer, level + 2);
            writer.write(']');
            if (!metadataTag.getReferences().isEmpty() || metadataTag.getASDoc() != null)
                writer.write(',');
            writer.write('\n');
            if (!metadataTag.getReferences().isEmpty())
                writeReferences(writer, "references", metadataTag.getReferences(), level + 2,
                        metadataTag.getASDoc() != null);
            if (metadataTag.getASDoc() != null)
                writeASDoc(writer, metadataTag.getASDoc(), level + 2, false);
            indent(writer, level + 1);
            writer.write('}');
            if (i + 1 < metadata.size())
                writer.write(',');
            writer.write('\n');
        }
        indent(writer, level);
        writer.write(']');
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeBooleanProperty(Writer writer, int level, String name, boolean value, boolean comma)
            throws IOException
    {
        indent(writer, level);
        writeString(writer, name);
        writer.write(value ? ": true" : ": false");
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeValueProperty(Writer writer, int level, String name, Object value, boolean comma)
            throws IOException
    {
        indent(writer, level);
        writeString(writer, name);
        writer.write(": ");
        if (value == null)
            writer.write("null");
        else if ((value instanceof Double && !Double.isFinite((Double)value))
                || (value instanceof Float && !Float.isFinite((Float)value)))
            writeString(writer, value.toString());
        else if (value instanceof Number || value instanceof Boolean)
            writer.write(value.toString());
        else
            writeString(writer, value.toString());
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeProperty(Writer writer, int level, String name, String value, boolean comma) throws IOException
    {
        indent(writer, level);
        writeString(writer, name);
        writer.write(": ");
        if (value == null)
            writer.write("null");
        else
            writeString(writer, value);
        if (comma)
            writer.write(',');
        writer.write('\n');
    }

    private void writeString(Writer writer, String value) throws IOException
    {
        writer.write('"');
        for (int i = 0; i < value.length(); i++)
        {
            char character = value.charAt(i);
            switch (character)
            {
                case '"':
                    writer.write("\\\"");
                    break;
                case '\\':
                    writer.write("\\\\");
                    break;
                case '\b':
                    writer.write("\\b");
                    break;
                case '\f':
                    writer.write("\\f");
                    break;
                case '\n':
                    writer.write("\\n");
                    break;
                case '\r':
                    writer.write("\\r");
                    break;
                case '\t':
                    writer.write("\\t");
                    break;
                default:
                    if (character < 0x20)
                        writer.write(String.format("\\u%04x", (int)character));
                    else
                        writer.write(character);
            }
        }
        writer.write('"');
    }

    private void indent(Writer writer, int level) throws IOException
    {
        for (int i = 0; i < level; i++)
            writer.write("  ");
    }
}