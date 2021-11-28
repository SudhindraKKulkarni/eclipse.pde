/*******************************************************************************
 * Copyright (c) 2008, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *     Hannes Wellmann - Bug 577541 - Clean up ClasspathHelper and TargetWeaver
 *******************************************************************************/
package org.eclipse.pde.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.osgi.framework.Constants;

/**
 * Supports target weaving (combining the target platform with workspace
 * projects to generate a woven target platform).
 *
 * @since 3.4
 */
public class TargetWeaver {
	private TargetWeaver() { // static use only
	}

	/**
	 * Location of dev.properties, {@code null} if the Platform is not in
	 * development mode.
	 */
	private static String fgDevPropertiesURL = null;

	/**
	 * Property file corresponding to dev.properties
	 */
	private static Properties fgDevProperties = null;

	/**
	 * Initializes system properties
	 */
	static {
		if (Platform.inDevelopmentMode()) {
			fgDevPropertiesURL = System.getProperty("osgi.dev"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the dev.properties as a property store.
	 *
	 * @return properties
	 */
	private static synchronized Properties getDevProperties() {
		if (fgDevPropertiesURL != null) {
			if (fgDevProperties == null) {
				fgDevProperties = new Properties();
				try {
					URL url = new URL(fgDevPropertiesURL);
					String path = url.getFile();
					if (path != null && path.length() > 0) {
						File file = new File(path);
						if (file.exists()) {
							try (InputStream stream = new FileInputStream(file)) {
								fgDevProperties.load(stream);
							}
						}
					}
				} catch (IOException e) {
					PDECore.log(e);
				}
			}
			return fgDevProperties;
		}
		return null;
	}

	/**
	 * Updates the bundle class path if this manifest refers to a project in development
	 * mode from the launching workspace.
	 *
	 * @param manifest manifest to update
	 */
	public static void weaveManifest(Map<String, String> manifest) {
		if (manifest != null && fgDevPropertiesURL != null) {
			Properties properties = getDevProperties();
			String id = manifest.get(Constants.BUNDLE_SYMBOLICNAME);
			if (id != null) {
				int index = id.indexOf(';');
				if (index != -1) {
					id = id.substring(0, index);
				}
				String property = (String) properties.get(id);
				if (property != null) {
					manifest.put(Constants.BUNDLE_CLASSPATH, property);
				}
			}
		}
	}

	/**
	 * When launching a secondary runtime workbench, all projects already in dev mode
	 * must continue in dev mode such that their class files are found.
	 *
	 * @param properties dev.properties
	 */
	public static void weaveDevProperties(Properties properties) {
		if (fgDevPropertiesURL != null) {
			Properties devProperties = getDevProperties();
			properties.putAll(devProperties);
		}
	}

	/**
	 * If a source annotation is pointing to a host project that is being wove, returns
	 * an empty string so that the source annotation is the root of the project.
	 * Otherwise returns the given library name.
	 *
	 * @param model plug-in we are attaching source for
	 * @param libraryName the standard library name
	 * @return empty string or the standard library name
	 */
	static String getWeavedSourceLibraryName(IPluginModelBase model, String libraryName) {
		// Note that if the host project has binary-linked libraries, these libraries appear in the dev.properties file with full path names,
		// and the library name must be returned as-is.
		if (fgDevPropertiesURL != null && !new File(libraryName).isAbsolute()) {
			Properties properties = getDevProperties();
			String id = null;
			if (model.getBundleDescription() != null) {
				id = model.getBundleDescription().getSymbolicName();
			}
			/*
			 * Workaround for bug 332112: Do not hack the source path for
			 * bundles that are not coming from the host workspace.
			 *
			 * Since we don't actually know what the host workspace is and where
			 * its projects are located, we have to guess:
			 *
			 * - If the bundle is not a folder, then it can't be a bundle from
			 * the host workspace.
			 *
			 * - If the model has an underlying resource, then it's probably
			 * from the local workspace.
			 *
			 * The architectural bug is that this weaving takes place at the
			 * wrong level. It should already be done while the target platform
			 * resolves bundles from the host workspace.
			 */
			if (id != null
					&& !new File(model.getInstallLocation()).isFile()
					&& model.getUnderlyingResource() == null) {
				String property = (String) properties.get(id);
				if (property != null) {
					return ""; //$NON-NLS-1$
				}
			}
		}
		return libraryName;
	}
}
