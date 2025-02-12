/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;

public class Validator {
    private Validator() {}

    public static void assertNotEmpty(Collection<?> collection, String messageNumber) {
        assertFalse(collection.isEmpty(), messageNumber);
    }

    public static void assertNotEmpty(Collection<?> collection, String messageNumber, String p1) {
        assertFalse(collection.isEmpty(), messageNumber, p1);
    }

    public static void assertFalse(boolean condition, String messageNumber) {
        if (condition) {
            throw new InvalidSqlException(Messages.getString(messageNumber));
        }
    }

    public static void assertFalse(boolean condition, String messageNumber, String p1) {
        if (condition) {
            throw new InvalidSqlException(Messages.getString(messageNumber, p1));
        }
    }

    public static void assertTrue(boolean condition, String messageNumber) {
        assertFalse(!condition, messageNumber);
    }

    public static void assertTrue(boolean condition, String messageNumber, String p1) {
        assertFalse(!condition, messageNumber, p1);
    }

    public static void assertNull(@Nullable Object object, String messageNumber) {
        if (object != null) {
            throw new InvalidSqlException(Messages.getString(messageNumber));
        }
    }
}
