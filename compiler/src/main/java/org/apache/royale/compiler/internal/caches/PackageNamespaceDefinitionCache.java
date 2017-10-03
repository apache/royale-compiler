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

package org.apache.royale.compiler.internal.caches;

import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;

public final class PackageNamespaceDefinitionCache extends ConcurrentCacheStoreBase<NamespaceDefinition.ILanguageNamespaceDefinition>
{
    public PackageNamespaceDefinitionCache()
    {
        
    }

    public NamespaceDefinition.ILanguageNamespaceDefinition get(String packageName, boolean internal)
    {
        Key k = new Key(packageName, internal);
        return this.get(k);
    }
    
    @Override
    protected NamespaceDefinition.ILanguageNamespaceDefinition createEntryValue(CacheStoreKeyBase key)
    {
        Key k = (Key)key;
        if (k.internal)
            return NamespaceDefinition.createInternalNamespaceDefinition(k.packageName);
        return NamespaceDefinition.createPackagePublicNamespaceDefinition(k.packageName);
    }
    
    private static final class Key extends CacheStoreKeyBase
    {
        public Key(String packageName, boolean internal)
        {
            this.packageName = packageName;
            this.internal = internal;
        }
        
        private final String packageName;
        private final boolean internal;
        
        @Override
        public String generateKey()
        {
            return String.valueOf(internal) + "/" + packageName;
        }
    }
}
