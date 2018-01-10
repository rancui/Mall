/*
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
 */

package com.mmall.common;

/*
 * #%L
 * sudoor-server-lib
 * %%
 * Copyright (C) 2013 - 2014 Shark Xu
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.text.MessageFormat;
import java.util.*;

/**
 * An internationalization / localization helper class which reduces
 * the bother of handling ResourceBundles and takes care of the
 * common cases of message formating which otherwise require the
 * creation of Object arrays and such.
 *
 * <p>The StringManager operates on a package basis. One StringManager
 * per package can be created and accessed via the getManager method
 * call.
 *
 * <p>The StringManager will look for a ResourceBundle named by
 * the package name given plus the suffix of "LocalStrings". In
 * practice, this means that the localized information will be contained
 * in a LocalStrings.properties file located in the package
 * directory of the classpath.
 *
 * <p>Please see the documentation for java.util.ResourceBundle for
 * more information.
 *
 * @version $Id: StringManager.java 1521871 2013-09-11 14:27:27Z markt $
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Mel Martinez [mmartinez@g1440.com]
 * @see ResourceBundle
 */

public class StringManager {

	private static int LOCALE_CACHE_SIZE = 10;

	/**
	 * The ResourceBundle for this StringManager.
	 */
	private final ResourceBundle bundle;
	private final Locale locale;

	/**
	 * Creates a new StringManager for a given package. This is a
	 * private method and all access to it is arbitrated by the
	 * static getManager method call so that only one StringManager
	 * per package will be created.
	 *
	 * @param packageName Name of package to create StringManager for.
	 */
	private StringManager(String packageName, Locale locale) {
		String bundleName = packageName + ".LocalStrings";
		ResourceBundle bnd = null;
		try {
			bnd = ResourceBundle.getBundle(bundleName, locale);
		} catch( MissingResourceException ex ) {
			// Try from the current loader (that's the case for trusted apps)
			// Should only be required if using a TC5 style classloader structure
			// where common != shared != server
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if( cl != null ) {
				try {
					bnd = ResourceBundle.getBundle(bundleName, locale, cl);
				} catch(MissingResourceException ex2) {
					// Ignore
				}
			}
		}
		bundle = bnd;
		// Get the actual locale, which may be different from the requested one
		if (bundle != null) {
			Locale bundleLocale = bundle.getLocale();
			if (bundleLocale.equals(Locale.ROOT)) {
				this.locale = Locale.ENGLISH;
			} else {
				this.locale = bundleLocale;
			}
		} else {
			this.locale = null;
		}
	}

	/**
	 Get a string from the underlying resource bundle or return
	 null if the String is not found.

	 @param key to desired resource String
	 @return resource String matching <i>key</i> from underlying
	 bundle or null if not found.
	 @throws IllegalArgumentException if <i>key</i> is null.
	 */
	public String getString(String key) {
		if(key == null){
			String msg = "key may not have a null value";

			throw new IllegalArgumentException(msg);
		}

		String str = null;

		try {
			// Avoid NPE if bundle is null and treat it like an MRE
			if (bundle != null) {
				str = bundle.getString(key);
			}
		} catch(MissingResourceException mre) {
			//bad: shouldn't mask an exception the following way:
			//   str = "[cannot find message associated with key '" + key +
			//         "' due to " + mre + "]";
			//     because it hides the fact that the String was missing
			//     from the calling code.
			//good: could just throw the exception (or wrap it in another)
			//      but that would probably cause much havoc on existing
			//      code.
			//better: consistent with container pattern to
			//      simply return null.  Calling code can then do
			//      a null check.
			str = null;
		}

		return str;
	}

	/**
	 * Get a string from the underlying resource bundle and format
	 * it with the given set of arguments.
	 *
	 * @param key
	 * @param args
	 */
	public String getString(final String key, final Object... args) {
		String value = getString(key);
		if (value == null) {
			value = key;
		}

		MessageFormat mf = new MessageFormat(value);
		mf.setLocale(locale);
		return mf.format(args, new StringBuffer(), null).toString();
	}

	/**
	 * Identify the Locale this StringManager is associated with
	 */
	public Locale getLocale() {
		return locale;
	}

	// --------------------------------------------------------------
	// STATIC SUPPORT METHODS
	// --------------------------------------------------------------

	private static final Map<String, Map<Locale,StringManager>> managers =
			new Hashtable<String, Map<Locale,StringManager>>();

	/**
	 * Get the StringManager for a particular package. If a manager for
	 * a package already exists, it will be reused, else a new
	 * StringManager will be created and returned.
	 *
	 * @param packageName The package name
	 */
	public static final synchronized StringManager getManager(
			String packageName) {
		return getManager(packageName, Locale.getDefault());
	}

	/**
	 * Get the StringManager for a particular package and Locale. If a manager
	 * for a package/Locale combination already exists, it will be reused, else
	 * a new StringManager will be created and returned.
	 *
	 * @param packageName The package name
	 * @param locale      The Locale
	 */
	public static final synchronized StringManager getManager(
			String packageName, Locale locale) {

		Map<Locale,StringManager> map = managers.get(packageName);
		if (map == null) {
            /*
             * Don't want the HashMap to be expanded beyond LOCALE_CACHE_SIZE.
             * Expansion occurs when size() exceeds capacity. Therefore keep
             * size at or below capacity.
             * removeEldestEntry() executes after insertion therefore the test
             * for removal needs to use one less than the maximum desired size
             *
             */
			map = new LinkedHashMap<Locale,StringManager>(LOCALE_CACHE_SIZE, 1, true) {
				private static final long serialVersionUID = 1L;
				@Override
				protected boolean removeEldestEntry(
						Map.Entry<Locale,StringManager> eldest) {
					if (size() > (LOCALE_CACHE_SIZE - 1)) {
						return true;
					}
					return false;
				}
			};
			managers.put(packageName, map);
		}

		StringManager mgr = map.get(locale);
		if (mgr == null) {
			mgr = new StringManager(packageName, locale);
			map.put(locale, mgr);
		}
		return mgr;
	}

	/**
	 * Retrieve the StringManager for a list of Locales. The first StringManager
	 * found will be returned.
	 *
	 * @param requestedLocales the list of Locales
	 *
	 * @return the found StringManager or the default StringManager
	 */
	public static StringManager getManager(String packageName,
	                                       Enumeration<Locale> requestedLocales) {
		while (requestedLocales.hasMoreElements()) {
			Locale locale = requestedLocales.nextElement();
			StringManager result = getManager(packageName, locale);
			if (result.getLocale().equals(locale)) {
				return result;
			}
		}
		// Return the default
		return getManager(packageName);
	}
}
