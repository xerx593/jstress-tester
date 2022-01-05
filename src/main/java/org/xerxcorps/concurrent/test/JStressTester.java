/*
 * Copyright 2021-2099 Xerxcorps Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xerxcorps.concurrent.test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utility class for stress tests.
 *
 * @param <R> the Callable result type.
 * @author Alex Lutz
 */
public class JStressTester<R> {

    /**
     * Class of R.
     */
    private final Class<R> daCLazz;

    /**
     * Number of cycles/iterations to test.
     */
    private final int cycles;

    /**
     * Collected Exceptions.
     */
    private final List<Exception> exceptions;

    /**
     * Collected results.
     */
    private final List<R> results;

    /**
     * Executor Service.
     */
    private final ExecutorService executor;

    /**
     * Jobs.
     */
    private final Collection<Callable<R>> callables;

    /**
     * TODO: Write doc &amp; example.
     * @param pClazz for printing
     * @param pCycles iteration count
     * @param pPoolSize threadCount
     * @param pCallables jobs of.
     */
    @SafeVarargs
    public JStressTester(Class<R> pClazz, int pCycles, int pPoolSize, Callable<R>... pCallables) {
        daCLazz = pClazz;
        callables = Arrays.asList(pCallables);
        cycles = pCycles;
        final int poolSize = pPoolSize <= 0 ? Runtime.getRuntime().availableProcessors() : pPoolSize;
        executor = Executors.newFixedThreadPool(poolSize);
        exceptions = Collections.synchronizedList(new ArrayList<>());
        results = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Submits all &lt;code&gt;callables&lt;/code&gt; &lt;code&gt;cycle&lt;/code&gt;
     * times. And (tries to) collets the result using an ExecutorCompletionService.
     */
    @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
    public void test() {
        List<Future<R>> futures = new ArrayList<>();
        ExecutorCompletionService<R> completer = new ExecutorCompletionService<>(executor);
        for (int i = 0; i < cycles; i++) {
            for (Callable<R> call : callables) {
                futures.add(completer.submit(call));
            }
        }
        futures.forEach((var fut) -> {
            try {
                R result = completer.take().get();
                results.add(result);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException ex) {
                exceptions.add(ex);
            }
        });
    }

    /**
     * The exceptions, occured during &lt;code&gt;test()&lt;/code&gt;.
     * @return unmodifiable copy of exceptions.
     */
    public List<Exception> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    /**
     * The Results, produces by &lt;code&gt;test()&lt;/code&gt;.
     * @return unmodifiable copy of results.
     */
    public List<R> getResuls() {
        return Collections.unmodifiableList(results);
    }

    /**
     * Prints all (occured) exceptions to PrintStream.
     * @param pPrintStream print stream.
     * @throws NullPointerException , when ps == null.
     */
    public void printErrors(PrintStream pPrintStream) {
        if (exceptions.isEmpty()) {
            pPrintStream.println("no exceptions");
            return;
        }
        exceptions.forEach(pPrintStream::println);
    }

    /**
     * Prints all (collected) results to PrintStream, when &lt;R&gt; is not Void.
     * @param pPrintStream print stream.
     * @throws NullPointerException , when ps == null.
     */
    public void printResults(PrintStream pPrintStream) {
        if (results.isEmpty() || Void.class == daCLazz) {
            pPrintStream.println("no results");
            return;
        }
        results.forEach(pPrintStream::println);
    }

}
