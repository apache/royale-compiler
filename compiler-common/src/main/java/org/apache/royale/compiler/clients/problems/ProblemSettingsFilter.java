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

package org.apache.royale.compiler.clients.problems;

import java.util.HashSet;
import java.util.Set;

import org.apache.royale.compiler.config.ICompilerProblemSettings;
import org.apache.royale.compiler.internal.config.ICompilerSettings;
import org.apache.royale.compiler.problems.*;

/**
 * A problem filter that implements filtering based the values of the following
 * compiler options:
 * 
 * -allow-source-path-overlap
 * -show-actionscript-warnings
 * -show-binding-warnings
 * -show-deprecation-warnings
 * -show-multiple-definition-warnings
 * -show-unused-type-selector-warnings
 * -strict
 * -warn-assignment-within-conditional
 * -warn-bad-array-cast
 * -warn-bad-date-cast
 * -warn-bad-nan-comparision
 * -warn-bad-null-assignment
 * -warn-bad-undefined-comparision
 * -warn-const-not-initialized
 * -warn-duplicate-variable-def
 * -warn-instanceof-changes
 * -warn-missing-namespace-decl
 * -warn-no-type-decl
 * -warn-this-within-closure
 * 
 */
public class ProblemSettingsFilter implements IProblemFilter
{

    /**
     * Create a filter based on problem settings.
     * 
     * @param problemSettings The settings, may not be null.
     */
    public ProblemSettingsFilter(ICompilerProblemSettings problemSettings)
    {
        assert problemSettings != null : "Settings may not be null";
            
        this.problemSettings = problemSettings;
        
        init();
    }
    
    private final ICompilerProblemSettings problemSettings;
    private final Set<Class<? extends ICompilerProblem>> filter = new HashSet<Class<? extends ICompilerProblem>>();
    
    @Override
    public boolean accept(ICompilerProblem p)
    {
        for (Class<?> filterClass : filter)
        {
            //  This is equivalent to (p instanceof <filter class>)
            if (filterClass.isInstance(p))
                return false;
        }

        // accept it.
        return true;
    }

    /**
     * initialize filter from problem settings.
     */
    private void init()
    {
        setShowStrictSemantics(problemSettings.isStrict());
        setShowWarnings(problemSettings.showActionScriptWarnings());
        setShowDeprecationWarnings(problemSettings.showDeprecationWarnings());
        setShowBindingWarnings(problemSettings.showBindingWarnings());
        setShowMultipleDefinitionWarnings(problemSettings.showMultipleDefinitionWarnings());
        setShowUnusedTypeSelectorWarnings(problemSettings.showUnusedTypeSelectorWarnings());
        setAllowSourcePathOverlapWarnings(problemSettings.isSourcePathOverlapAllowed());
        setShowActionScriptWarnings();
    }

    /**
     *  Enable or disable strict semantics mode diagnostics.
     *  @param isStrict - if true, strict semantics mode 
     *    diagnostics will appear in the filtered diagnostics.
     */
    private void setShowStrictSemantics(boolean isStrict)
    {
        setShowProblemByClass(StrictSemanticsProblem.class, isStrict);
    }

    /**
     *  Enable or disable semantic warnings.
     *  @param showWarnings - if true, semantic warnings
     *    will appear in the filtered diagnostics.
     */
    private void setShowWarnings(boolean showWarnings)
    {
        setShowProblemByClass(SemanticWarningProblem.class, showWarnings);
    }

    private void setAllowSourcePathOverlapWarnings(boolean isSourcePathOverlapAllowed)
    {
        setShowProblemByClass(OverlappingSourcePathProblem.class, !isSourcePathOverlapAllowed);
    }

    private void setShowUnusedTypeSelectorWarnings(boolean showUnusedTypeSelectorWarnings)
    {
        // TODO: call setShowProblemByClass() with warnings relating to 
        // unused type selectors.
        // CMP-1422
    }

    private void setShowBindingWarnings(boolean showBindingWarnings)
    {
        setShowProblemByClass(MXMLDatabindingSourceNotBindableProblem.class, showBindingWarnings);
    }

    private void setShowMultipleDefinitionWarnings(boolean b)
    {
        setShowProblemByClass(DuplicateQNameInSourcePathProblem.class, b);
    }
    
    private void setShowActionScriptWarnings()
    {
        // Associate compiler problems with the compiler -warn-xxx options 
        // that enable/disable them.
        setShowActionScriptWarning(AssignmentInConditionalProblem.class, 
                        ICompilerSettings.WARN_ASSIGNMENT_WITHIN_CONDITIONAL);
        setShowActionScriptWarning(ArrayCastProblem.class, 
                ICompilerSettings.WARN_BAD_ARRAY_CAST);
        setShowActionScriptWarning(DateCastProblem.class, 
                ICompilerSettings.WARN_BAD_DATE_CAST);
        setShowActionScriptWarning(IllogicalComparionWithNaNProblem.class, 
                ICompilerSettings.WARN_BAD_NAN_COMPARISON);
        setShowActionScriptWarning(NullUsedWhereOtherExpectedProblem.class, 
                ICompilerSettings.WARN_BAD_NULL_ASSIGNMENT);
        setShowActionScriptWarning(IllogicalComparisonWithUndefinedProblem.class, 
                ICompilerSettings.WARN_BAD_UNDEFINED_COMPARISON);
        setShowActionScriptWarning(ConstNotInitializedProblem.class, 
                ICompilerSettings.WARN_CONST_NOT_INITIALIZED);
        setShowActionScriptWarning(DuplicateVariableDefinitionProblem.class, 
                ICompilerSettings.WARN_DUPLICATE_VARIABLE_DEF);
        setShowActionScriptWarning(InstanceOfProblem.class, 
                ICompilerSettings.WARN_INSTANCEOF_CHANGES);
        setShowActionScriptWarning(ScopedToDefaultNamespaceProblem.class, 
                ICompilerSettings.WARN_MISSING_NAMESPACE_DECL);
        setShowActionScriptWarning(VariableHasNoTypeDeclarationProblem.class, 
                ICompilerSettings.WARN_NO_TYPE_DECL);
        setShowActionScriptWarning(ThisUsedInClosureProblem.class, 
                ICompilerSettings.WARN_THIS_WITHIN_CLOSURE);
    }

    /**
     * Hide/show actionscript warnings based on compiler option settings.
     * 
     * @param problem
     * @param warningCode
     */
    private void setShowActionScriptWarning(Class<? extends ICompilerProblem> problem,
            int warningCode)
    {
        setShowProblemByClass(problem, 
                problemSettings.checkActionScriptWarning(warningCode));
    }
    
    /**
     *  Enable or disable deprecation warnings.
     *  @param showWarnings - if true, deprecation warnings
     *    will appear in the filtered diagnostics.
     */
    private void setShowDeprecationWarnings(boolean showWarnings)
    {
        setShowProblemByClass(AbstractDeprecatedAPIProblem.class, showWarnings);
        setShowProblemByClass(DeprecatedConfigurationOptionProblem.class, showWarnings);
    }

    /**
     * Enable or disable a compiler problem class.
     * 
     * @param clazz the class to enable the problem.
     * @param enable if true the problem is enabled and NOT filtered, otherwise
     * the problem is filtered.
     */
    private void setShowProblemByClass(Class<? extends ICompilerProblem> clazz, boolean enable)
    {
        if (enable)
            filter.remove(clazz);
        else
            filter.add(clazz);
    }
    
}
