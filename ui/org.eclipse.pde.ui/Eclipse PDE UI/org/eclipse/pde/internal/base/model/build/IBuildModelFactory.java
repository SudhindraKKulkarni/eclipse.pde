package org.eclipse.pde.internal.base.model.build;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.w3c.dom.*;
/**
 * This model factory should be used to
 * create new instances of plugin.jars model
 * objects.
 */
public interface IBuildModelFactory {
/**
 * Creates a new build entry with
 * the provided name.
 * @return a new jar entry instance
 */
IBuildEntry createEntry(String name);
}
