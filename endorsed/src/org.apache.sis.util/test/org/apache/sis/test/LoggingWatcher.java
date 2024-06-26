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
package org.apache.sis.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

// Test dependencies
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


/**
 * Watches the logs sent to the given logger.
 * For using, create a rule in the JUnit test class like below:
 *
 * {@snippet lang="java" :
 *     @ResourceLock(LoggingWatcher.LOCK)
 *     public MyTest {
 *         @RegisterExtension
 *         public final LoggingWatcher loggings = new LoggingWatcher(Logger.getLogger(Loggers.XML));
 *         }
 *     }
 *
 * In tests that are expected to emit warnings, add the following lines:
 *
 * {@snippet lang="java" :
 *     // Do the test here.
 *     loggings.assertNextLogContains("Some keywords", "that are expected", "to be found in the message");
 *     loggings.assertNoUnexpectedLog();
 *     }
 *
 * Callers should invoke {@link #assertNoUnexpectedLog()} at the end of each test method.
 * Alternatively, the check can also be done automatically as below (but the stack trace
 * may be more confusing):
 *
 * {@snippet lang="java" :
 *     @AfterEach
 *     public void assertNoUnexpectedLog() {
 *         loggings.assertNoUnexpectedLog();
 *     }
 *     }
 *
 * <h2>Multi-threading</h2>
 * By default, {@code LoggingWatcher} handles only the log records emitted by the same thread as the one
 * that constructed this watcher. If tests are running in parallel, log records emitted by other threads
 * will not interfere. For disabling this filtering (which is necessary if the test starts many threads),
 * invoke {@link #setMultiThread()}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final class LoggingWatcher implements BeforeEachCallback, AfterEachCallback, Filter {
    /**
     * Name of the lock to use in a JUnit {@code ResourceLock} annotation.
     * Tests that are executed in a single thread can be run in parallel
     * and should be declared with the following annotation:
     *
     * {@snippet lang="java" :
     *     @ResourceLock(value=LoggingWatcher.LOCK, mode=ResourceAccessMode.READ)
     *     public class MyTest {
     *         }
     *     }
     *
     * Tests that may start worker threads shall be executed in isolation with other tests
     * that may do logging. These tests should have the following annotations:
     *
     * {@snippet lang="java" :
     *     @ResourceLock(value=LoggingWatcher.LOCK, mode=ResourceAccessMode.READ_WRITE)
     *     @Execution(ExecutionMode.SAME_THREAD)
     *     public class MyTest {
     *     }
     *     }
     */
    public static final String LOCK = "Logging";

    /**
     * The logged messages. All accesses to this list shall be synchronized on {@code this}.
     */
    private final Queue<Message> messages = new LinkedList<>();

    /**
     * Elements in the {@link #messages} queue.
     */
    private static final class Message extends org.apache.sis.pending.jdk.Record {
        /** Formatted text of the log record. */
        final String text;

        /** Stack trace that we can use for finding the emitter, or {@code null} if none. */
        final Throwable trace;

        /** Creates a new message. */
        Message(final String text, Throwable trace) {
            this.text  = text;
            this.trace = trace;
        }

        /**
         * Returns the formatted log message together with its source.
         * This is the string to show if an assertion fail.
         */
        @Override public String toString() {
            if (trace == null) {
                return text;
            }
            final var buffer = new StringWriter();
            buffer.write(text);
            buffer.write(System.lineSeparator());
            buffer.write("Caused by: ");
            trace.printStackTrace(new PrintWriter(buffer));
            return buffer.toString();
        }
    }

    /**
     * The logger to watch.
     */
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;

    /**
     * The formatter to use for formatting log messages.
     */
    private final SimpleFormatter formatter = new SimpleFormatter();

    /**
     * Identifier of the thread to watch.
     */
    private final long threadId = Thread.currentThread().getId();

    /**
     * Whether the test will be multi-threaded. If this flag is set to {@code true},
     * then this {@code LoggingWatcher} will intercept all logs, not only the ones
     * produced by the thread that initialized this instance.
     *
     * @see #setMultiThread()
     */
    private boolean isMultiThread;

    /**
     * All filters registered on the logger. This is a singleton containing only {@code this},
     * unless many tests are running in parallel with their own {@code LoggingWatcher}.
     */
    private Queue<LoggingWatcher> allFilters;

    /**
     * Creates a new watcher for the given logger.
     *
     * @param logger  the logger to watch.
     */
    public LoggingWatcher(final Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    /**
     * Creates a new watcher for the given logger.
     *
     * @param logger  the name of logger to watch.
     */
    public LoggingWatcher(final String logger) {
        this.logger = Logger.getLogger(logger);
    }

    /**
     * Notifies that the test will be multi-threaded. If this method is invoked,
     * then this {@code LoggingWatcher} will intercept all logs, not only the ones
     * produced by the thread that initialized this instance.
     *
     * <p>If this method is invoked, then the caller is responsible to ensure that
     * no there test running in parallel may do logging. It can be done with JUnit
     * {@code Execution} and {@code ResourceLock} annotations. Example:</p>
     *
     * {@snippet lang="java" :
     *     @ResourceLock(value=LoggingWatcher.LOCK, mode=ResourceAccessMode.READ_WRITE)
     *     @Execution(value=ExecutionMode.SAME_THREAD, reason="For verification of logs emitted by worker threads.")
     *     public class MyTest extends TestCase {
     *     }
     *     }
     */
    final void setMultiThread() {
        isMultiThread = true;
    }

    /**
     * Invoked when a test is about to start. This method installs this {@link Filter}
     * for the log messages before the tests are run. This installation will cause the
     * {@link #isLoggable(LogRecord)} method to be invoked when a message is logged.
     *
     * @param  description  a description of the JUnit test which is starting.
     *
     * @see #isLoggable(LogRecord)
     */
    @Override
    public final void beforeEach(final ExtensionContext description) {
        synchronized (logger) {
            assertNull(allFilters);
            final Filter current = logger.getFilter();
            if (current != null) {
                var w = assertInstanceOf(LoggingWatcher.class, current, () -> "The \"" + logger.getName()
                                + "\" logger has a " + current.getClass().getCanonicalName() + " filter.");
                allFilters = w.allFilters;
                assertNotNull(allFilters);
            } else {
                allFilters = new LinkedList<>();
                logger.setFilter(this);
            }
            allFilters.add(this);
        }
    }

    /**
     * Invoked when a test method finishes (whether passing or failing)
     * This method removes the filter which had been set for testing purpose.
     *
     * @param  description  a description of the JUnit test that finished.
     */
    @Override
    public final void afterEach(final ExtensionContext description) {
        synchronized (logger) {
            assertTrue(allFilters.remove(this));
            logger.setFilter(allFilters.peek());
            allFilters = null;
            // Note: references to `allFilters` may still exist in other `LoggingWatcher` instances.
        }
    }

    /**
     * Invoked (indirectly) when a tested method has emitted a log message.
     * This method adds the logging message to the {@link #messages} list.
     *
     * @param  record  the intercepted log record.
     * @return {@code true} if verbose mode, or {@code false} if quiet mode.
     */
    @Override
    public final boolean isLoggable(final LogRecord record) {
        if (record.getLevel().intValue() >= Level.INFO.intValue()) {
            /*
             * In the simple mono-thread case, everything use fields of `this`.
             * However, if many tests are running in parallel, we need to check
             * which `LoggingWatcher` should take the given log record.
             */
            LoggingWatcher owner = null;
            synchronized (logger) {
                if (allFilters == null) {   // Should never be null, but sometime happens for an unknown reason.
                    return true;
                }
                for (final LoggingWatcher w : allFilters) {
                    if (w.isMultiThread || w.threadId == record.getLongThreadID()) {
                        owner = w;
                        break;
                    }
                }
            }
            if (owner == null) {
                return true;
            }
            synchronized (owner) {
                owner.messages.add(new Message(owner.formatter.formatMessage(record), record.getThrown()));
            }
        }
        return TestCase.VERBOSE;
    }

    /**
     * Skips the next log messages if it contains all the given keywords.
     * This method is used instead of {@link #assertNextLogContains(String...)} when a log message may or
     * may not be emitted during a test, depending on circumstances that the test method does not control.
     *
     * @param  keywords  the keywords that are expected to exist in the next log message
     *                   if that log message has been emitted.
     */
    @SuppressWarnings("StringEquality")
    public synchronized void skipNextLogIfContains(final String... keywords) {
        final Message message = messages.peek();
        if (message != null) {
            for (final String word : keywords) {
                if (!message.text.contains(word)) {
                    return;
                }
            }
            if (messages.remove() != message) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Verifies that the next logging message contains the given keywords.
     * Each call of this method advances to the next log message.
     *
     * @param  keywords  the keywords that are expected to exist in the next log message.
     *         May be an empty array for requesting only the existence of a log with any message.
     */
    public synchronized void assertNextLogContains(final String... keywords) {
        if (messages.isEmpty()) {
            fail("Expected a logging messages but got no more.");
        }
        final Message message = messages.remove();
        for (final String word : keywords) {
            if (!message.text.contains(word)) {
                fail("Expected the logging message to contains the “" + word + "” word but got:\n" + message);
            }
        }
    }

    /**
     * Verifies that there is no more log message.
     */
    public synchronized void assertNoUnexpectedLog() {
        final Message message = messages.peek();
        if (message != null) {
            fail("Unexpected logging message: " + message);
        }
    }

    /**
     * Discards all logging messages.
     */
    public synchronized void clear() {
        messages.clear();
    }
}
