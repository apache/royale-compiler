////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package org.apache.royale.linter.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.MXMLTagVisitor;
import org.apache.royale.linter.MXMLTokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that MXML attribute values are not empty.
 */
public class MXMLEmptyAttributeRule extends LinterRule {
	@Override
	public List<MXMLTagVisitor> getMXMLTagVisitors() {
		List<MXMLTagVisitor> result = new ArrayList<>();
		result.add((tag, tokenQuery, problems) -> {
			checkTag(tag, tokenQuery, problems);
		});
		return result;
	}

	private void checkTag(IMXMLTagData tag, MXMLTokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		for (IMXMLTagAttributeData attribute : tag.getAttributeDatas()) {
			if (attribute.getRawValue().trim().length() == 0) {
				problems.add(new MXMLEmptyAttributeLinterProblem(attribute));
			}
		}
	}

	public static class MXMLEmptyAttributeLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "MXML attribute '${attributeName}' value is empty";

		public MXMLEmptyAttributeLinterProblem(IMXMLTagAttributeData attribute) {
			super(attribute);
			this.attributeName = attribute.getName();
		}

		public String attributeName;
	}
}
