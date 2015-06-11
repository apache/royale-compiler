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

package org.apache.flex.compiler.internal.codegen.externals.reference;

import java.io.File;

import org.apache.flex.compiler.clients.ExternCConfiguration.ExcludedMemeber;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Marker;
import com.google.javascript.rhino.JSDocInfo.StringPosition;
import com.google.javascript.rhino.JSDocInfo.TypePosition;
import com.google.javascript.rhino.Node;

public abstract class BaseReference
{
    private String qualfiedName;

    protected JSDocInfo comment;

    private File currentFile;

    private Node node;

    private ReferenceModel model;

    public File getCurrentFile()
    {
        return currentFile;
    }

    public void setCurrentFile(File currentFile)
    {
        this.currentFile = currentFile;
    }

    public String getCurrentFileBaseName()
    {
        return "";
        // return FilenameUtils.getBaseName(currentFile.getAbsolutePath());
    }

    public String getBaseName()
    {
        return qualfiedName.substring(qualfiedName.lastIndexOf('.') + 1);
    }

    public String getPackageName()
    {
        int end = qualfiedName.lastIndexOf('.');
        if (end == -1)
            return "";
        return qualfiedName.substring(0, end);
    }

    public String getQualifiedName()
    {
        return qualfiedName;
    }

    public final boolean isQualifiedName()
    {
        return qualfiedName.indexOf('.') != -1;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    public void setComment(JSDocInfo comment)
    {
        this.comment = comment;
    }

    public JSDocInfo getComment()
    {
        return comment;
    }

    public Compiler getCompiler()
    {
        return model.getCompiler();
    }

    public ReferenceModel getModel()
    {
        return model;
    }

    public BaseReference(ReferenceModel model, Node node, String qualfiedName,
            JSDocInfo comment)
    {
        this.model = model;
        this.node = node;
        this.qualfiedName = qualfiedName;
        this.comment = comment;
    }

    public void printComment(StringBuilder sb)
    {
        sb.append("    /**\n");

        String blockDescription = getComment().getBlockDescription();
        if (blockDescription != null)
        {
            sb.append("     * ");
            sb.append(blockDescription.replaceAll("\\n", "\n     * "));
            sb.append("\n     *\n");
        }

        for (Marker marker : getComment().getMarkers())
        {
            StringPosition name = marker.getAnnotation();
            TypePosition typePosition = marker.getType();
            StringPosition descriptionPosition = marker.getDescription();
            StringBuilder desc = new StringBuilder();

            // XXX Figure out how to toString() a TypePosition Node for markers
            // XXX Figure out how to get a @param name form the Marker
            if (!name.getItem().equals("see"))
                continue;

            if (name != null)
            {
                desc.append(name.getItem());
                desc.append(" ");
            }

            if (typePosition != null)
            {
                //desc.append(typePosition.getItem().getString());
                //desc.append(" ");
            }

            if (descriptionPosition != null)
            {
                desc.append(descriptionPosition.getItem());
                desc.append(" ");
            }

            sb.append("     * @" + desc.toString() + "\n");
        }

        sb.append("     * @see " + getNode().getSourceFileName() + "\n");
        sb.append("     */\n");
    }

    public ExcludedMemeber isExcluded()
    {
        return null;
    }

    public abstract void emit(StringBuilder sb);

    //    public DocletTag findDocTagByName(String tagName)
    //    {
    //        for (DocletTag tag : getComment().getTags())
    //        {
    //            if (tag.getName().equals(tagName))
    //            {
    //                return tag;
    //            }
    //        }
    //        return null;
    //    }
    //
    //    public boolean hasTag(String tagName)
    //    {
    //        for (DocletTag tag : getComment().getTags())
    //        {
    //            if (tag.getName().equals(tagName))
    //                return true;
    //        }
    //        return false;
    //    }
}
