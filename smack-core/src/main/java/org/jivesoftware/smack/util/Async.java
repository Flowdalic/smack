/**
 *
 * Copyright 2014 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Async {

    /**
     * Creates an executor service just as {@link Executors#newCachedThreadPool()} would do, but
     * with a keep alive time of 5 minutes instead of 60 seconds. And a custom thread factory to set
     * a meaningful name to the thread and mark those daemon.
     */
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(
                    // @formatter:off
                    0,                                 // corePoolSize
                    Integer.MAX_VALUE,                 // maximumPoolSize
                    60L * 5,                           // keepAliveTime
                    TimeUnit.SECONDS,                  // keepAliveTime unit, note that MINUTES is Android API 9
                    new SynchronousQueue<Runnable>(),  // workQueue
                    new ThreadFactory() {              // threadFactory
                        @Override
                        public Thread newThread(Runnable runnable) {
                            Thread thread = new Thread(runnable);
                            thread.setName("Smack Async Thread");
                            thread.setDaemon(true);
                            return thread;
                        }
                    }
                    // @formatter:on
                    );

    public static void go(Runnable runnable) {
        EXECUTOR_SERVICE.submit(runnable);
    }
}
