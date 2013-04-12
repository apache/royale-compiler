package org.apache.flex.compiler.internal.codegen.js.flexjs;

import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.flex.compiler.tree.as.IFunctionNode;

public class JSFlexJSGoogDocEmitter extends JSGoogDocEmitter {

	public JSFlexJSGoogDocEmitter(IJSEmitter emitter) {
		super(emitter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void emitMethodAccess(IFunctionNode node)
	{
        String ns = node.getNamespace();
        if (ns == IASKeywordConstants.PRIVATE)
        {
            emitPrivate(node);
        }
        else if (ns == IASKeywordConstants.PROTECTED)
        {
            emitProtected(node);
        }
        else if (ns == IASKeywordConstants.PUBLIC)
        {
        	emitPublic(node);
        }
	}
}
