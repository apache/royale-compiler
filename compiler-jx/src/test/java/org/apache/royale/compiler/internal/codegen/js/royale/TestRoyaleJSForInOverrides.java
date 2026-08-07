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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.junit.Test;

public class TestRoyaleJSForInOverrides extends ASTestBase
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
        project.config = new JSGoogConfiguration();
        super.setUp();
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorNextMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSForInOverride(iteratorMethod=\"iterator\",iteratorNextMethod=\"next\")] class A { public function A() { for (var key in this) {} } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (true)\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      if (foreachiter0_key == undefined) break;\n      var key = foreachiter0_key;\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorNextMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { for (var key in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorNextMethod=\"next\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (true)\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      if (foreachiter0_key == undefined) break;\n      var key = foreachiter0_key;\n      {\n      }}\n    }\n  \n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorNextMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { for (var key in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorNextMethod=\"next\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (true)\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      if (foreachiter0_key == undefined) break;\n      var key = foreachiter0_key;\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorHasNextMethod_iteratorNextMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSForInOverride(iteratorMethod=\"iterator\",iteratorHasNextMethod=\"hasNext\",iteratorNextMethod=\"next\")] class A { public function A() { for (var key in this) {} } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (foreachiter0_iterator.hasNext())\n    {\n      var key = foreachiter0_iterator.next();\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorHasNextMethod_iteratorNextMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { for (var key in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorHasNextMethod=\"hasNext\",iteratorNextMethod=\"next\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (foreachiter0_iterator.hasNext())\n    {\n      var key = foreachiter0_iterator.next();\n      {\n      }}\n    }\n  \n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorHasNextMethod_iteratorNextMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { for (var key in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorHasNextMethod=\"hasNext\",iteratorNextMethod=\"next\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (foreachiter0_iterator.hasNext())\n    {\n      var key = foreachiter0_iterator.next();\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorDoneMethod_iteratorNextMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSForInOverride(iteratorMethod=\"iterator\",iteratorDoneMethod=\"done\",iteratorNextMethod=\"next\")] class A { public function A() { for (var key in this) {} } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (!foreachiter0_iterator.done())\n    {\n      var key = foreachiter0_iterator.next();\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorDoneMethod_iteratorNextMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { for (var key in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorDoneMethod=\"done\",iteratorNextMethod=\"next\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (!foreachiter0_iterator.done())\n    {\n      var key = foreachiter0_iterator.next();\n      {\n      }}\n    }\n  \n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testForInOverride_iteratorMethod_iteratorDoneMethod_iteratorNextMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { for (var key in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorDoneMethod=\"done\",iteratorNextMethod=\"next\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (!foreachiter0_iterator.done())\n    {\n      var key = foreachiter0_iterator.next();\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorNextMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSForInOverride(iteratorMethod=\"iterator\",iteratorNextMethod=\"next\")] class A { public function A() { for each (var value in this) {} } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (true)\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      if (foreachiter0_key == undefined) break;\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorNextMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { for each (var value in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorNextMethod=\"next\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (true)\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      if (foreachiter0_key == undefined) break;\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorNextMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { for each (var value in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorNextMethod=\"next\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (true)\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      if (foreachiter0_key == undefined) break;\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorHasNextMethod_iteratorNextMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSForInOverride(iteratorMethod=\"iterator\",iteratorHasNextMethod=\"hasNext\",iteratorNextMethod=\"next\")] class A { public function A() { for each (var value in this) {} } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (foreachiter0_iterator.hasNext())\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorHasNextMethod_iteratorNextMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { for each (var value in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorHasNextMethod=\"hasNext\",iteratorNextMethod=\"next\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (foreachiter0_iterator.hasNext())\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorHasNextMethod_iteratorNextMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { for each (var value in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorHasNextMethod=\"hasNext\",iteratorNextMethod=\"next\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (foreachiter0_iterator.hasNext())\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorDoneMethod_iteratorNextMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSForInOverride(iteratorMethod=\"iterator\",iteratorDoneMethod=\"done\",iteratorNextMethod=\"next\")] class A { public function A() { for each (var value in this) {} } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (!foreachiter0_iterator.done())\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorDoneMethod_iteratorNextMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { for each (var value in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorDoneMethod=\"done\",iteratorNextMethod=\"next\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (!foreachiter0_iterator.done())\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testForInOverride_forEach_iteratorMethod_iteratorDoneMethod_iteratorNextMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { for each (var value in this) {} } }; [JSForInOverride(iteratorMethod=\"iterator\",iteratorDoneMethod=\"done\",iteratorNextMethod=\"next\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  var foreachiter0_target = this;\n  if (foreachiter0_target)\n  {\n    var foreachiter0_iterator = foreachiter0_target.iterator();\n    while (!foreachiter0_iterator.done())\n    {\n      var foreachiter0_key = foreachiter0_iterator.next();\n      var value = foreachiter0_target[foreachiter0_key];\n      {\n      }}\n    }\n  \n};");
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

}
