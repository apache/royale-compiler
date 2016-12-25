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

package org.apache.flex.compiler.internal.as.codegen;

import java.io.File;
import java.util.Date;

import org.apache.flex.compiler.clients.Optimizer;
import org.apache.flex.compiler.internal.testing.NodesToXMLStringFormatter;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class containing debug code that dumps BURM state to an XML file.
 */
final class DumpBURMState
{

    private static final Logger logger = LogManager.getLogger(DumpBURMState.class);

    static void dump(CmcEmitter burm, IASNode n)
    {
        // un-comment the following line to enable dumping of BURM errors.
        // doDump(burm, n);
    }
    
    /**
     * Helper method that dumps BURM state to aid in debugginer BURM errors.
     * There should be no calls to this method checked into version control.
     * @param burm
     * @param n
     */
    @SuppressWarnings("unused")
    private static synchronized void doDump(CmcEmitter burm, IASNode n)
    {
        java.io.PrintWriter dumper;
        
 
        try
        {
            File dump_file = File.createTempFile("failedBurm-", ".xml");
            
            dumper = new java.io.PrintWriter(new java.io.FileWriter(dump_file));
            dumper.println ( "<?xml version=\"1.0\"?>");
            dumper.println("<BurmDump date=\"" + new Date().toString() + "\">");
            burm.dump(dumper);
            dumper.println("<AST>");
            dumper.println(new NodesToXMLStringFormatter(n).toString());
            dumper.println("</AST>");
            dumper.println("</BurmDump>");
            dumper.flush();
            dumper.close();
        }
        catch (Exception e)
        {
            logger.error("Unable to dump due to: " + e.toString());
            try
            {
                logger.error(new NodesToXMLStringFormatter(n).toString());
            } 
            catch ( Exception cantformat)
            {
                //  Probably an error in the AST itself, diagnosed above.
            }
        }
        
    }
}
