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

package flex2.tools.oem;


/**
 * A cache which allows SourceList, SourcePath, and ResourceContainer
 * based Sources, which are common between compilations, to be shared.
 * When a Flash Builder "user triggered clean" occurs,
 * ApplicationCache.clear() should be called.  When Flash Builder
 * calls Builder.clean() after writing out a PersistenceStore cache,
 * ApplicationCache.clear() should not be called.  Otherwise, the
 * benefit of the application cache would be lost.
 *
 * @since 4.5
 * @author Paul Reilly
 */
public class ApplicationCache
{
    public void clear()
    {
        
    }
}
