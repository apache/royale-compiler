/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.royale.maven;

/**
 * Created by joshtynjala on 10.12.19.
 */
public class ManifestComponent {
	public ManifestComponent(String id, String qualifiedName, boolean lookupOnly) {
		this.id = id;
		this.qualifiedName = qualifiedName;
		this.lookupOnly = lookupOnly;
	}

	private String id;
	private String qualifiedName;
	private boolean lookupOnly;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public boolean getLookupOnly() {
        return lookupOnly;
    }

    public void setLookupOnlys(boolean lookupOnly) {
        this.lookupOnly = lookupOnly;
    }
}
