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

package org.apache.royale.compiler.internal.units.requests;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.compiler.units.requests.IRequestResult;

/**
 * Creates implementations of the IRequest, where the object returned from the
 * get method of the IRequest is returned from a Callable returned from the
 * getCallable method implemented by subclasses of this class.
 * <p>
 * This class hides the complicated process of creating and reusing request
 * objects. Sub classes just need to return a Callable from getCallable that
 * will return the result of the request.
 * 
 * @param <ResultType> The class that is used for the result of the request.
 * @param <RequesteeType> The type of a context parameter to pass from calls to getRequest
 * to getCallable.
 */
public abstract class RequestMaker<ResultType extends IRequestResult, RequesteeInterfaceType, RequesteeType extends RequesteeInterfaceType >
{
    /**
     * If you run with -Dthrow.assertions=true, then an AssertionError
     * will not get turned into an internal compiler problem
     * and instead will be caught by JUnit as an error.
     */
    private static final boolean THROW_ASSERTIONS = System.getProperty("throw.assertions", "false").equals("true");
    
    private static class Request<V extends IRequestResult, W> implements IRequest<V, W>
    {
        private Future<V> future;
        private Lock lock;
        private Condition haveFuture;
        private final long timestamp;
        private final W requestee;

        public Request(W requestee)
        {
            lock = new ReentrantLock();
            haveFuture = lock.newCondition();
            timestamp = System.currentTimeMillis();
            this.requestee = requestee;
        }

        @Override
        public V get() throws InterruptedException
        {
            V result = null;
            try
            {
                // blocks till request is done
                result = getFuture().get();
            }
            catch (ExecutionException executionException)
            {
                Throwable cause = executionException.getCause();
                if (THROW_ASSERTIONS && (cause instanceof AssertionError))
                    throw (AssertionError)cause;
                /*
                 * We don't expect to ever get an ExecutionException because we
                 * eat all the Throwable's that are not the InterruptedException
                 * in the Callable we wrap around the Callable we got from the
                 * abstract getCallable method.
                 */
                assert false : "Unexpected ExecutionException!";
                executionException.printStackTrace();
            }

            return result;
        }
        
        @Override
        public boolean isDone()
        {
            lock.lock();
            try
            {
                if (future == null)
                    return false;
                return future.isDone();
            }
            finally
            {
                lock.unlock();
            }
        }

        private Future<V> getFuture()
        {
            lock.lock();
            try
            {
                while (future == null)
                    haveFuture.awaitUninterruptibly();
                return future;
            }
            finally
            {
                lock.unlock();
            }
        }

        private void setFuture(Future<V> future)
        {
            lock.lock();
            try
            {
                this.future = future;
                haveFuture.signalAll();
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public long getTimeStamp()
        {
            return timestamp;
        }

        @Override
        public W getRequestee()
        {
            return requestee;
        }
    }

    /**
     * Gets a reference to a request object, by either creating a new IRequest
     * or returning an existing one from the specified AtomicReference.
     * <p>
     * If a new IRequest is created the atomicRef is updated to point at it.
     * 
     * @param u Parameter that is passed through to getCallable if a new Request
     * is created.
     * @param atomicRef An AtomicReference which contains a reference to an
     * existing IRequest or which will be updated to point to the IRequest this
     * method creates.
     * @param workspace The workspace which contains the ExecutorService which
     * is used to schedule processing to compute the IRequestResult.
     * @param isNeededForFileScope true if the request is needed to build a file
     * scope.
     * @return An IRequest referenced by the specified AtomicReference, or a new
     * IRequest.
     */
    public final IRequest<ResultType, RequesteeInterfaceType> getRequest(RequesteeType u, AtomicReference<IRequest<ResultType, RequesteeInterfaceType>> atomicRef, Workspace workspace, boolean isNeededForFileScope)
    {
        // This check is purely an optimization to avoid the alloc of the request.
        final IRequest<ResultType, RequesteeInterfaceType> existingRequest = atomicRef.get();
        if (existingRequest == null)
        {
            workspace.startRequest(isNeededForFileScope);
            
            final Request<ResultType, RequesteeInterfaceType> request = new Request<ResultType, RequesteeInterfaceType>(u);
            if (atomicRef.compareAndSet(null, request))
            {
                ExecutorService exec = workspace.getExecutorService();
                request.setFuture(exec.submit(wrapCallable(u, getCallable(u), workspace)));
            }
            else
            {
                workspace.endRequest();
            }
            assert atomicRef.get() != null;
            return atomicRef.get();
        }
        else
        {
            return existingRequest;
        }
    }

    /**
     * Creates a new Callable that calls the specified Callable and catches any
     * Throwable's except for a InterruptedException that were not caught by the
     * specified Callable. When a Throwable other than InterruptedException was
     * not caught specified Callable, the protected abstract
     * getResultForThrowable method of this class is called to construct a
     * result object for the new Callable.
     * 
     * @param u Parameter that is passed through to getResultForThrowable if the
     * specified Callable does not catch a thrown Throwable.
     * @param c Callable the resulting Callable calls and that may not catch all
     * Throwable that are thrown.
     * @param workspace The workspace to notify once this callable finishes.
     * @return A new callable that will not throw any Throwable other than
     * InterruptedException.
     */
    private Callable<ResultType> wrapCallable(final RequesteeType u, final Callable<ResultType> c, final Workspace workspace)
    {
        return new Callable<ResultType>()
        {
            @Override
            public ResultType call() throws InterruptedException
            {
                try
                {
                    return c.call();
                }
                catch (InterruptedException e)
                {
                    throw e;
                }
                catch (AssertionError ae)
                {
                    if (THROW_ASSERTIONS)
                        throw ae;
                    return getResultForThrowable(u, ae);
                }
                catch (Exception e)
                {
                    return getResultForThrowable(u, e);
                }
                finally
                {
                    workspace.endRequest();
                }
            }
        };
    }

    /**
     * Called to get the callable that computes the result of the request.
     * 
     * @param u Parameter that was passed to getRequest.
     * @return A Callable that returns the result of the request.
     */
    protected abstract Callable<ResultType> getCallable(RequesteeType u);

    /**
     * Called to get the Result when an uncaught throwable was detected.
     * 
     * @param u Parameter that was passed to getRequest.
     * @param t Throwable that was thrown from the Callable returned from
     * getCallable.
     */
    protected abstract ResultType getResultForThrowable(RequesteeType u, Throwable t);

}
