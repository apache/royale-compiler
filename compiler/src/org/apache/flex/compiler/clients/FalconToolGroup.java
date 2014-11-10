package org.apache.flex.compiler.clients;

import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * FlexToolGroup exposing the implementations of the Falcon compilers tools.
 */
public class FalconToolGroup implements FlexToolGroup {

    Map<String, FlexTool> tools;

    public FalconToolGroup() {
        tools = new HashMap<String, FlexTool>();
        tools.put("COMPC", new COMPC());
        tools.put("MXMLC", new MXMLC());
        tools.put("OPTIMIZER", new Optimizer());
    }

    @Override
    public String getName() {
        return "Falcon";
    }

    @Override
    public Collection<String> getFlexToolNames() {
        return tools.keySet();
    }

    @Override
    public FlexTool getFlexTool(String toolName) {
        return tools.get(toolName);
    }

}
