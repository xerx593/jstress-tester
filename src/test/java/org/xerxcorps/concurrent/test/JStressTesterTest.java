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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Alex Lutz
 */
class JStressTesterTest {

    /**
     * TimeUnit Seconds.
     */
    static final TimeUnit SECONDS = java.util.concurrent.TimeUnit.SECONDS;

    @Test
    void test1() {
        final JStressTester testee = new JStressTester(Void.class, 0, 0);
        assertThat(testee, is(notNullValue()));
        testee.test();
        testee.printResults(System.out);
        testee.printErrors(System.out);
        assertThat(testee.getExceptions().isEmpty(), is(true));
        assertThat(testee.getResuls().isEmpty(), is(true));
    }

    @Test
    void test2() {
        final JStressTester testee = new JStressTester(String.class, 2, 2, () -> UUID.randomUUID().toString());
        assertThat(testee, is(notNullValue()));
        testee.test();
        testee.printResults(System.out);
        testee.printErrors(System.out);
        assertThat(testee.getExceptions().isEmpty(), is(true));
        assertThat(testee.getResuls().isEmpty(), is(false));
        assertThat(testee.getResuls().size(), is(2));
    }

    @Test
    void testExceptions() {
        final JStressTester testee = new JStressTester(String.class, 2, 2, () -> {
            throw new TestException("foo");
        });
        assertThat(testee, is(notNullValue()));
        testee.test();
        testee.printResults(System.out);
        testee.printErrors(System.out);
        assertThat(testee.getExceptions().isEmpty(), is(false));
        assertThat(testee.getExceptions().size(), is(2));
        assertThat(testee.getResuls().isEmpty(), is(true));
    }

    @Test
    void testInterrupt() {
        final JStressTester testee = new JStressTester(String.class, 2, 2, () -> {
            await().dontCatchUncaughtExceptions().atLeast(4, SECONDS);
            return UUID.randomUUID().toString();
        });
        assertThat(testee, is(notNullValue()));
        Thread.currentThread().interrupt();
        testee.test();
        testee.printResults(System.out);
        testee.printErrors(System.out);
        assertThat(testee.getExceptions().isEmpty(), is(true));
        assertThat(testee.getResuls().isEmpty(), is(true));
    }

    private static class TestException extends RuntimeException {

        TestException(String msg) {
            super(msg);
        }

    }

}
