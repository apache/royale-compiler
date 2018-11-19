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

package org.apache.royale.compiler.tree.as.decorators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.tree.as.IVariableNode;

/**
 * This provider provides a way to decorate symbols with additional information,
 * based on various conditions
 */
public class SymbolDecoratorProvider
{
    private static SymbolDecoratorProvider provider;

    public static SymbolDecoratorProvider getProvider()
    {
        if (provider == null)
            provider = new SymbolDecoratorProvider();
        return provider;
    }

    /**
     * List of {@link IVariableTypeDecorator} decorators
     */
    private List<IVariableTypeDecorator> variableTypeDecorators;

    private SymbolDecoratorProvider()
    {
        variableTypeDecorators = new ArrayList<IVariableTypeDecorator>();
    }

    /**
     * Adds an {@link IVariableTypeDecorator} to this manager
     * 
     * @param decorator an {@link IVariableTypeDecorator} that will decorate
     * variables
     */
    public void addVariableTypeDecorator(IVariableTypeDecorator decorator)
    {
        variableTypeDecorators.add(decorator);
    }

    /**
     * Returns all the applicable {@link IVariableTypeDecorator} decorators for
     * a given variable
     * 
     * @param context the {@link IVariableNode} to be used as context
     * @return a list of {@link IVariableTypeDecorator} decorators
     */
    public List<IVariableTypeDecorator> getVariableTypeDecorators(IDefinition context)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYMBOL_DECORATOR_PROVIDER) == CompilerDiagnosticsConstants.SYMBOL_DECORATOR_PROVIDER)
    		System.out.println("SymbolDecoratorProvider waiting for lock in getVariableTypeDecorators");
        synchronized (variableTypeDecorators)
        {
            Iterator<IVariableTypeDecorator> it = variableTypeDecorators.iterator();
            ArrayList<IVariableTypeDecorator> retVal = new ArrayList<IVariableTypeDecorator>();
            while (it.hasNext())
            {
                IVariableTypeDecorator next = it.next();
                if (next.isApplicable(context))
                {
                    retVal.add(next);
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYMBOL_DECORATOR_PROVIDER) == CompilerDiagnosticsConstants.SYMBOL_DECORATOR_PROVIDER)
        		System.out.println("SymbolDecoratorProvider done with lock in getVariableTypeDecorators");
            return retVal;
        }
    }
}
