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

package org.apache.royale.compiler.definitions;

import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.metadata.IEffectTagNode;

/**
 * Represents effect metadata decorating a class definition,
 * such as <code>[Effect(name="rollOverEffect", event="rollOver")]</code>.
 * <p>
 * Use the inherited {@link IDefinition#getBaseName}() method to get
 * the effect's name (e.g., <code>"rollOverEffect"</code>).
 * <p>
 * Use {@link #getEvent}() to get the name of the effect's trigger event
 * (e.g., <code>"rollOver"</code>) as a String, and {@link #resolveEvent}()
 * to resolve it to an event definition.
 */
public interface IEffectDefinition extends IMetadataDefinition
{
    /**
     * Returns the {@link IEffectTagNode} from which this definition was
     * created, if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    @Override
    IEffectTagNode getNode();
    
    /**
     * Gets the value of this effect's <code>event</code> attribute.
     * <p>
     * This is the name of the event that will trigger this effect.
     * 
     * @return The name of the triggering event as a String.
     */
    String getEvent();

    /**
     * Resolves the value of this effect's <code>event</code> attribute
     * to an event definition.
     * <p>
     * This is the definition of the event that will trigger this effect.
     * 
     * @param project The {@link ICompilerProject} within which references
     * should be resolved.
     * @return An {@link IEventDefinition} for the triggering event,
     * or <code>null</code>.
     */
    IEventDefinition resolveEvent(ICompilerProject project);
}
