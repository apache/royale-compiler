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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.mxml.js.IMXMLJSEmitter;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.mxml.MXMLSubEmitter;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLDescriptorSpecifier;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLEventSpecifier;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;

public class JSRoyaleBasicMXMLDescriptorEmitter extends MXMLSubEmitter implements ISubEmitter<MXMLDescriptorSpecifier>
{
    private boolean useGoogReflectObjectProperty = false;

    public JSRoyaleBasicMXMLDescriptorEmitter(IMXMLJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(MXMLDescriptorSpecifier root)
    {
        RoyaleJSProject project = (RoyaleJSProject) getMXMLWalker().getProject();
        useGoogReflectObjectProperty = project.config != null && project.config.getMxmlReflectObjectProperty();
		outputDescriptorSpecifier(root, true);
	}

    //---------------------------------
    //    writeSimpleDescriptor
    //---------------------------------

    protected void writeSimpleDescriptor(String name, String type, String value,
            boolean writeNewline)
    {
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(name);
        write(ASEmitterTokens.SINGLE_QUOTE);
        writeDelimiter(writeNewline);

        if (type != null)
        {
            write(type);
            writeDelimiter(writeNewline);
        }

        write(value);
    }

    //---------------------------------
    //    writeDelimiter
    //---------------------------------

    protected void writeDelimiter(boolean writeNewline)
    {
        if (writeNewline)
            writeNewline(ASEmitterTokens.COMMA);
        else
            writeToken(ASEmitterTokens.COMMA);
	}

    //---------------------------------
    //    outputDescriptorSpecifier
    //---------------------------------

	private void outputDescriptorSpecifier(MXMLDescriptorSpecifier root, boolean writeNewline)
    {
        if (root.isTopNode)
        {
            int count = 0;
            for (MXMLDescriptorSpecifier md : root.propertySpecifiers)
            {
                if (md.name != null)
                    count++;
            }

            write(count + "");
            writeNewline(ASEmitterTokens.COMMA);
        }

		outputPropertySpecifiers(root, writeNewline);

        if (!root.isProperty)
        {
            outputStyleSpecifiers(root, writeNewline);

            // TODO (erikdebruin) not yet implemented in Royale
            //outputEffectSpecifiers(writeNewline);

            outputEventSpecifiers(root, writeNewline);

            if (!root.isTopNode)
            {
                writeDelimiter(writeNewline);

                if (root.childrenSpecifier == null)
                    write(ASEmitterTokens.NULL);
                else
                    outputChildren(root.childrenSpecifier, writeNewline);
            }

            boolean isLastChild = root.parent != null
                    && root.parent.propertySpecifiers.indexOf(root) == root.parent.propertySpecifiers
                            .size() - 1;

            if (!isLastChild && !root.isTopNode)
                writeDelimiter(writeNewline);
        }
    }

    //---------------------------------
    //    outputPropertySpecifiers
    //---------------------------------
	
	private void outputPropertySpecifiers(MXMLDescriptorSpecifier root, boolean writeNewline)
	{
        MXMLDescriptorSpecifier model = null; // model goes first
        MXMLDescriptorSpecifier beads = null; // beads go last

        for (MXMLDescriptorSpecifier md : root.propertySpecifiers)
        {
            if (md.name != null && md.name.equals("model"))
            {
                model = md;
                break;
            }
        }

        if (model != null)
        {
            outputPropertySpecifier(model, writeNewline);
        }

        for (MXMLDescriptorSpecifier md : root.propertySpecifiers)
        {
            if (md.name != null)
            {
                if (!md.name.equals("model") && !md.name.equals("beads"))
                    outputPropertySpecifier(md, writeNewline);
                else if (md.name.equals("beads"))
                    beads = md;
            }
        }

        if (beads != null)
        {
            outputPropertySpecifier(beads, writeNewline);
        }
	}

    //---------------------------------
    //    outputEventSpecifiers
    //---------------------------------

    private void outputEventSpecifiers(MXMLDescriptorSpecifier root, boolean writeNewline)
    {
        // number of events
        int count = 0;
        for (MXMLEventSpecifier me : root.eventSpecifiers)
        {
            if (me.name != null)
                count++;
        }
        write(count + "");

        for (MXMLEventSpecifier me : root.eventSpecifiers)
        {
			writeDelimiter(writeNewline);
			outputEventSpecifier(me, writeNewline);
        }
    }

    //---------------------------------
    //    outputStyleSpecifiers
    //---------------------------------

    private void outputStyleSpecifiers(MXMLDescriptorSpecifier root, boolean writeNewline)
    {
        // TODO (erikdebruin) not yet implemented in Royale

        write("0");
        writeDelimiter(writeNewline);
    }

    //---------------------------------
    //    outputPropertySpecifier
    //---------------------------------

    private void outputPropertySpecifier(MXMLDescriptorSpecifier specifier, boolean writeNewline)
    {
        if(specifier.isProperty)
        {
            if(useGoogReflectObjectProperty)
            {
                write(JSGoogEmitterTokens.GOOG_REFLECT_OBJECTPROPERTY);
                write(ASEmitterTokens.PAREN_OPEN);
            }
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(specifier.name);
            write(ASEmitterTokens.SINGLE_QUOTE);
            if(useGoogReflectObjectProperty)
            {
                MXMLDescriptorSpecifier parentSpecifier = specifier.parent;
                String id = (parentSpecifier.id != null) ? parentSpecifier.id : parentSpecifier.effectiveId;
                write(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.SPACE);
                write(ASEmitterTokens.THIS);
                if (id != null)
                {
                    write(ASEmitterTokens.MEMBER_ACCESS);
                    write(id);
                }
                write(ASEmitterTokens.PAREN_CLOSE);
            }
        }
        else
        {
            write(specifier.name);
        }
        writeDelimiter(writeNewline);

        if (specifier.isProperty)
        {
            if (specifier.value != null)
            {
                write(ASEmitterTokens.TRUE);
                writeDelimiter(writeNewline);
                write(specifier.value);
            }
            else
            {
                write((specifier.hasArray) ? ASEmitterTokens.NULL : ASEmitterTokens.FALSE);
                writeDelimiter(writeNewline);
                write(ASEmitterTokens.SQUARE_OPEN);
                indentPush();
                writeNewline();
                outputDescriptorSpecifier(specifier, writeNewline);
                indentPop();
                writeNewline();
                write(ASEmitterTokens.SQUARE_CLOSE);
            }

            if (specifier.parent != null)
                writeDelimiter(writeNewline);
        }
        else
        {
            for (MXMLDescriptorSpecifier md : specifier.propertySpecifiers)
            {
                if (md.name != null && md.name.equals("mxmlContent"))
                {
                    specifier.childrenSpecifier = md;
                    specifier.propertySpecifiers.remove(md);
                    break;
                }
            }

            if (specifier.id != null || specifier.effectiveId != null)
            {
                write(specifier.propertySpecifiers.size() + 1 + "");
                writeDelimiter(writeNewline);
                String idPropName = (specifier.effectiveId != null) ? "_id" : "id";
                String id = (specifier.id != null) ? specifier.id : specifier.effectiveId;
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(idPropName);
                write(ASEmitterTokens.SINGLE_QUOTE);
                writeDelimiter(writeNewline);
                write(ASEmitterTokens.TRUE);
                writeDelimiter(writeNewline);
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(id);
                write(ASEmitterTokens.SINGLE_QUOTE);
                writeDelimiter(writeNewline);
            }
            else
            {
                write(specifier.propertySpecifiers.size() + "");
                writeDelimiter(writeNewline);
            }

            outputDescriptorSpecifier(specifier, writeNewline);
        }
    }

    //---------------------------------
    //    outputEventSpecifier
    //---------------------------------

    public void outputEventSpecifier(MXMLEventSpecifier specifier, boolean writeNewline)
    {
        String handler = ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + specifier.eventHandler;
        if (MXMLEventSpecifier.nameMap.contains(specifier.name))
			specifier.name = specifier.name.toLowerCase();
		else if (specifier.name.equals("doubleClick"))
			specifier.name = "dblclick";
		else if (specifier.name.equals("mouseWheel"))
			specifier.name = "wheel";
        writeSimpleDescriptor(specifier.name, null, handler, writeNewline);
    }

    //---------------------------------
    //    outputChildren
    //---------------------------------

    private void outputChildren(MXMLDescriptorSpecifier children, boolean writeNewline)
    {
        write(ASEmitterTokens.SQUARE_OPEN.getToken());
        if(writeNewline)
        {
            indentPush();
            writeNewline();
		}
		outputDescriptorSpecifier(children, writeNewline);
        if(writeNewline)
        {
            indentPop();
            writeNewline();
        }
        write(ASEmitterTokens.SQUARE_CLOSE.getToken());
	}
	
}
