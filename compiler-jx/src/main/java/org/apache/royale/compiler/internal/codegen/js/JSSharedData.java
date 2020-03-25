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

package org.apache.royale.compiler.internal.codegen.js;

// TODO (mschmalle) This class sucks, figure out some other way instead of using
// a static singleton class like this, change when implementing Configuration
public class JSSharedData
{

    public static final String COMPILER_NAME = "MXMLJSC";
    public static final boolean OUTPUT_TIMESTAMPS = true;
    public static final String COMPILER_VERSION = "329449.1";

/*    public final static JSSharedData instance = new JSSharedData();

    public static Workspace workspace;
*/
/*    public static PrintStream STDOUT = System.out;
    public static PrintStream STDERR = System.err;*/

/*    private Boolean m_verbose = false;
    private final ReadWriteLock m_verboseLock = new ReentrantReadWriteLock();

    private Object m_codeGenMonitor = new Object();
    private long m_codeGenCounter = 0;
    private final ReadWriteLock m_codeGenCounterLock = new ReentrantReadWriteLock();
*/
/*    public static String now()
    {
        final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(cal.getTime());
    }
*/
/*    public void stdout(String s)
    {
        if (STDOUT != null)
        {
            m_verboseLock.writeLock().lock();
            STDOUT.println(s);
            m_verboseLock.writeLock().unlock();
        }
    }

    public void stderr(String s)
    {
        if (STDERR != null)
        {
            m_verboseLock.writeLock().lock();
            STDERR.println(s);
            m_verboseLock.writeLock().unlock();
        }
    }*/

/*    public void beginCodeGen()
    {
        m_codeGenCounterLock.writeLock().lock();
        m_codeGenCounter++;
        m_codeGenCounterLock.writeLock().unlock();
    }

    public void endCodeGen()
    {
        m_codeGenCounterLock.writeLock().lock();
        final long currentCounter = --m_codeGenCounter;
        m_codeGenCounterLock.writeLock().unlock();

        if (currentCounter == 0)
        {
            synchronized (m_codeGenMonitor)
            {
                m_codeGenMonitor.notifyAll();
            }
        }
    }*/

}
