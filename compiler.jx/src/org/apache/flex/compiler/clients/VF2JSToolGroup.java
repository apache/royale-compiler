package org.apache.flex.compiler.clients;

import org.apache.flex.compiler.internal.driver.js.vf2js.VF2JSBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by christoferdutz on 10.11.14.
 */
public class VF2JSToolGroup implements FlexToolGroup {

    Map<String, FlexTool> tools;

    public VF2JSToolGroup() {
        tools = new HashMap<String, FlexTool>();
        tools.put("COMPC", new COMPJSC(new VF2JSBackend()));
        tools.put("MXMLC", new MXMLJSC(new VF2JSBackend()));
    }

    @Override
    public String getName() {
        return "VF2JS";
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
