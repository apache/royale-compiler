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
package org.apache.royale.test.ant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.apache.tools.ant.BuildException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class RoyaleUnitWebSocketServer extends WebSocketServer implements IRoyaleUnitServer
{
    private static final String START_OF_TEST_RUN_ACK = "<startOfTestRunAck/>";
    private static final String END_OF_TEST_RUN_ACK = "<endOfTestRunAck/>";

    public RoyaleUnitWebSocketServer(int port, int timeout)
    {
        super(new InetSocketAddress(port));
        this.timeout = timeout;
        //because we may be running many sets of tests in a short period of
        //time, and the socket can end up in a timeout state after it is closed,
        //this allows us to reuse the same port again quickly
        this.setReuseAddr(true);
    }

    private int timeout;
    private Timer timeoutTimer;
    private List<String> queue = new ArrayList<String>();
    private Exception resultException;

    public Exception getException()
    {
        return resultException;
    }

    @Override
    public void stop() throws IOException, InterruptedException
    {
        LoggingUtil.log("\nStopping server ...");

        if(timeoutTimer != null)
        {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
        
        for(WebSocket socket : getConnections())
        {
            sendTestRunEndAcknowledgement(socket);
        }
        super.stop();
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake)
    {
        if(timeoutTimer != null)
        {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }

        LoggingUtil.log("Client connected.");
        LoggingUtil.log("Receiving data ...");
        
        sendTestRunStartAcknowledgement(connection);
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote)
    {
        Thread.currentThread().interrupt();
    }

    @Override
    public void onMessage(WebSocket connection, String message)
    {
        queue.add(message);
    }

    @Override
    public void onError(WebSocket connection, Exception ex)
    {
        resultException = ex;
    }
    
    @Override
    public void onStart()
    {
        LoggingUtil.log("Starting server ...");
        LoggingUtil.log("Waiting for client connection ...");

        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask()
        {
            public void run()
            {
                resultException = new BuildException("Socket timeout waiting for royaleunit report");
            }
        }, timeout);
    }

    public boolean isPending()
    {
       return resultException == null && queue.size() == 0;
    }
   
    /**
     * Reads tokens from the web socket
     */
    public String readNextTokenFromSocket() throws IOException
    {
        return queue.remove(0);
    }
   
    private void sendOutboundMessage(WebSocket connection, String message)
    {
        connection.send(message);
    }

    /**
     * Generate and send message to inform test runner to begin sending test data
     */
    private void sendTestRunStartAcknowledgement(WebSocket connection)
    {
       LoggingUtil.log("Sending acknowledgement to player to start sending test data ...\n");
       
       sendOutboundMessage(connection, START_OF_TEST_RUN_ACK);
    }
   
    /**
     * Sends the end of test run to the listener to close the connection
     */
    private void sendTestRunEndAcknowledgement(WebSocket connection)
    {
       LoggingUtil.log("End of test data reached, sending acknowledgement to player ...");
       
       sendOutboundMessage(connection, END_OF_TEST_RUN_ACK);
    }
}