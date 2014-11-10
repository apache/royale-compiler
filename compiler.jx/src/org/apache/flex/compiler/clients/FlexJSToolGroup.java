package org.apache.flex.compiler.clients;

import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by christoferdutz on 10.11.14.
 */
public class FlexJSToolGroup implements FlexToolGroup {

    Map<String, FlexTool> tools;

    public FlexJSToolGroup() {
        tools = new HashMap<String, FlexTool>();
        tools.put("COMPC", new COMPJSC(new MXMLFlexJSBackend()));
        tools.put("MXMLC", new MXMLJSC(new MXMLFlexJSBackend()));
    }

    @Override
    public String getName() {
        return "FlexJS";
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
