package org.jolokia.server.core.util;

/*
 * Copyright 2009-2013 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility for class lookup.
 *
 * @author roland
 * @since 19.04.11
 */
public final class ClassUtil {

    private ClassUtil() {}

    /**
     * Load a certain class. Several class loader are tried: Fires the current thread's context
     * class loader, then its parents. If this doesn't work, the class loader which
     * loaded this class is used (and its parents)
     *
     * @param pClassName class name to load
     * @param pInitialize whether the class must be initialized
     * @return the class class found or null if no class could be loaded
     */
    public static Class classForName(String pClassName,boolean pInitialize) {
        Set<ClassLoader> tried = new HashSet<ClassLoader>();
        for (ClassLoader loader : new ClassLoader[] {
                Thread.currentThread().getContextClassLoader(),
                ClassUtil.class.getClassLoader()
        } ) {
            // Go up the classloader stack to eventually find the server class. Sometimes the WebAppClassLoader
            // hide the server classes loaded by the parent class loader.
            while (loader != null) {
                try {
                    if (!tried.contains(loader)) {
                        return Class.forName(pClassName,pInitialize, loader);
                    }
                } catch (ClassNotFoundException e) {}
                tried.add(loader);
                loader = loader.getParent();
            }
        }
        return null;
    }

    /**
     * Get the given path as an input stream or return <code>null</code> if not found
     *
     * @param pPath path to lookup
     * @return input stream or null if not found.
     */
    public static InputStream getResourceAsStream(String pPath) {
        for (ClassLoader loader : new ClassLoader[] {
                Thread.currentThread().getContextClassLoader(),
                ClassUtil.class.getClassLoader()
        } ) {
            if (loader != null) {
                InputStream is = loader.getResourceAsStream(pPath);
                if (is != null) {
                    return is;
                }
            }
        }
        return null;
    }

    /**
     * Lookup a class. See {@link ClassUtil#classForName(String, boolean)} for details. The class
     * gets initialized during lookup.
     *
     * @param pClassName name to lookup.
     * @return the class found or null if no class could be found.
     */
    public static Class classForName(String pClassName) {
        return classForName(pClassName,true);
    }

    /**
     * Check for the existence of a given class
     *
     * @param pClassName class name to check
     * @return true if the class could be loaded by the thread's conext class loader, false otherwise
     */
    public static boolean checkForClass(String pClassName) {
        return ClassUtil.classForName(pClassName,false) != null;
    }

    /**
     * Instantiate an instance of the given class with its default constructor
     *
     * @param pClass name of class to instantiate
     * @param <T> type object type
     * @return instantiated class
     * @throws IllegalArgumentException if the class could not be found or instantiated
     */
    public static <T> T newInstance(String pClass) {
        try {
            Class<T> clazz = classForName(pClass);
            if (clazz != null) {
                return clazz.newInstance();
            } else {
                throw new IllegalArgumentException("Cannot find " + pClass);
            }
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Cannot instantiate " + pClass + ": " + e,e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot instantiate " + pClass + ": " + e,e);
            }
    }


}
