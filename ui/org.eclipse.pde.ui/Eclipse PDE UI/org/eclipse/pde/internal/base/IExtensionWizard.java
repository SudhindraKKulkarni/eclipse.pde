package org.eclipse.pde.internal.base;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.pde.internal.base.model.plugin.*;
import org.eclipse.core.resources.*;

/**
 * An interface for extension wizards. Clients should
 * implement this interface if they are plugging into
 * PDE using "newExtension" extension point.
 */
public interface IExtensionWizard extends IBasePluginWizard {
/**
 * Initializes the wizard with the project of the plug-in and
 * the model object for the manifest file. Java code and
 * other resorces should be released directly under the
 * provided project. Changes in the plug-in manifest
 * should be made using the APIs of the provided
 * model.
 *
 * @param project the plug-in project where the new
 * code and resources should go
 * @param pluginModel the model instance that should
 * be used to modify the plug-in manifest
 */
public void init(IProject project, IPluginModelBase pluginModel);
}
