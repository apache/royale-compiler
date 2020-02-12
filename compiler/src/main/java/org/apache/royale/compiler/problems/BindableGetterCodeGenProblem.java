package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.definitions.IGetterDefinition;

public class BindableGetterCodeGenProblem extends CodegenProblem {

    public static final String DESCRIPTION =
            "[Bindable] on '${getterName}' getter requires a locally defined setter.";

    public static final int errorCode = 1054;


    public BindableGetterCodeGenProblem(IGetterDefinition site){
        super(site);
        getterName = site.getBaseName();
    }


    public final String getterName;
}
