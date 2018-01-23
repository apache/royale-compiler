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

package org.apache.royale.compiler.codegen.js;

import java.io.File;
import java.io.IOException;

import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.driver.IPublisher;

/**
 * The {@link IJSPublisher} interface allows the abstraction of project output
 * generation.
 * 
 * @author Erik de Bruin
 */
public interface IJSPublisher extends IPublisher
{

    File getOutputFolder();

    boolean publish(ProblemQuery problems) throws IOException;

}
