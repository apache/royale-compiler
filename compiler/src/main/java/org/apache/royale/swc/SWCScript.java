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

package org.apache.royale.swc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.royale.compiler.common.DependencyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Implementation for {@link ISWCScript} model.
 */
public class SWCScript implements ISWCScript
{
    public SWCScript()
    {
        definitions = new HashSet<String>();
        dependencies = HashMultimap.<String, DependencyType>create();
    }

    private String name;
    private long lastModified;
    private String signatureChecksum;
    private byte[] abcData;
    private final Set<String> definitions;
    private final SetMultimap<String, DependencyType> dependencies;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getLastModified()
    {
        return lastModified;
    }

    @Override
    public String getSignatureChecksum()
    {
        return signatureChecksum;
    }

    @Override
    public void addDefinition(String id)
    {
        definitions.add(id);
    }

    @Override
    public void addDependency(String id, DependencyType type)
    {
        dependencies.put(id, type);
        
        removeUnnecessaryDependencies(id);
    }
    
    /**
     * Removes unnecessary dependencies so that they won't
     * produce unnecessary <code>&lt;dep&gt;</code> tags.
     * The presence of certain dependencies makes other unnecessary
     * to write out, thereby reducing the size of the SWC catalog.
     */
    private void removeUnnecessaryDependencies(String id)
    {
        Set<DependencyType> set = dependencies.get(id);
        
        // INHERITANCE makes SIGNATURE, NAMESPACE, and EXPRESSION unnecessary.
        if (set.contains(DependencyType.INHERITANCE))
        {
            set.remove(DependencyType.SIGNATURE);
            set.remove(DependencyType.NAMESPACE);
            set.remove(DependencyType.EXPRESSION);
        }

        // SIGNATURE or NAMESAPCE makes EXPRESSION unnecessary.
        else if (set.contains(DependencyType.SIGNATURE) ||
                 set.contains(DependencyType.NAMESPACE))
        {
            set.remove(DependencyType.EXPRESSION);
        }
    }

    @Override
    public Set<String> getDefinitions()
    {
        return definitions;
    }

    @Override
    public SetMultimap<String, DependencyType> getDependencies()
    {
        return dependencies;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param lastModified the lastModified to set
     */
    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * @param signatureChecksum the signatureChecksum to set
     */
    public void setSignatureChecksum(String signatureChecksum)
    {
        this.signatureChecksum = signatureChecksum;
    }

    @Override
    public void setSource(byte[] abcData)
    {
        assert abcData != null;
        this.abcData = abcData;
    }

    @Override
    public byte[] getSource()
    {
        return abcData;
    }
    
    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("name: ");
        sb.append(getName());
        
        sb.append('\n');
        
        sb.append("definitions:\n");
        String[] defs = getDefinitions().toArray(new String[0]);
        Arrays.sort(defs);
        for (String def : defs)
        {
            sb.append(def);
            sb.append('\n');
        }
        
        return sb.toString();
    }
}
