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

/**
 * Component tag in catalog.xml file.
 */
public class SWCComponent implements ISWCComponent
{
    private String name;
    private String qname;
    private String uri;
    private String preview;
    private String icon;
    private ISWCScript script;
    
    /**
     * @return the name
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the qname
     */
    @Override
    public String getQName()
    {
        return qname;
    }

    /**
     * @param qname the qname to set
     */
    public void setQName(String qname)
    {
        this.qname = qname;
    }

    /**
     * @return the uri
     */
    @Override
    public String getURI()
    {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setURI(String uri)
    {
        this.uri = uri;
    }

    /**
     * @return the preview
     */
    @Override
    public String getPreview()
    {
        return preview;
    }

    /**
     * @param preview the preview to set
     */
    public void setPreview(String preview)
    {
        this.preview = preview;
    }

    /**
     * @return the icon
     */
    @Override
    public String getIcon()
    {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    @Override
    public ISWCScript getScript()
    {
        return script;
    }
    
    public void setScript(ISWCScript script)
    {
        this.script = script;
    }

}
