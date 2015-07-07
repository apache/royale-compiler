package org.apache.flex.compiler.internal.codegen.externals.reference;

import com.google.javascript.rhino.Node;

/**
 * @author: Frederic Thomas Date: 05/07/2015 Time: 19:34
 */
public class ParameterReference extends BaseReference
{

    private String name;

    public ParameterReference(final ReferenceModel model, final Node node, final String qualifiedName)
    {
        super(model, node, qualifiedName, null);
        name = node.getString();
    }

    public ParameterReference(final ReferenceModel model, final Node parameterNode)
    {
        this(model, parameterNode, "Object");
    }

    @Override
    public void emit(final StringBuilder sb)
    {
        // Emitted by the Method / Function reference.
    }

    public String getName()
    {
        return name;
    }
}
