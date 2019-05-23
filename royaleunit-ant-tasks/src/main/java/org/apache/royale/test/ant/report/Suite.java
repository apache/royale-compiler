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
package org.apache.royale.test.ant.report;

public class Suite
{
    private String _name;
    private int _tests = 0;
    private int _failures = 0;
    private int _errors = 0;
    private int _skips = 0;
    private long _time = 0;

    public Suite(String name)
    {
        super();
        _name = name;
    }

    public void addTest()
    {
        _tests++;
    }

    public void addFailure()
    {
        _failures++;
    }

    public void addError()
    {
        _errors++;
    }
    
    public void addSkip()
    {
        _skips++;
    }

    public String getName()
    {
        return _name;
    }

    public int getTests()
    {
        return _tests;
    }

    public int getFailures()
    {
        return _failures;
    }

    public int getErrors()
    {
        return _errors;
    }
    
    public int getSkips()
    {
        return _skips;
    }

    public long getTime()
    {
        return _time;
    }
    
    public void addTime(long time)
    {
        _time += time;
    }

    @Override
    public String toString()
    {
        return _name;
    }
}
