/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.royale.test.ant.launcher.commands;

import java.io.File;

import org.apache.tools.ant.Project;

/**
 * Class that has its own handle to a {@link Project} and can setup context for
 * using the project
 */
public interface TestRunCommand
{
    public void setProject(Project project);
    public void setEnvironment(String[] variables);
    public void setUrl(String url);
    public void setSwf(File swf);
    public void prepare();
}
