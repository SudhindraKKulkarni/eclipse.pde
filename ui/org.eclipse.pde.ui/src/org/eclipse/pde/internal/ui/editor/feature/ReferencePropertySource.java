package org.eclipse.pde.internal.ui.editor.feature;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.util.Choice;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.*;

public class ReferencePropertySource extends FeaturePropertySource {
	private Vector descriptors;
	private IPluginBase pluginBase;
	private Image errorImage;
	public final static String KEY_ID = "FeatureEditor.ReferenceProp.id";
	public final static String KEY_NAME = "FeatureEditor.ReferenceProp.name";
	public final static String KEY_VERSION =
		"FeatureEditor.ReferenceProp.version";
	public final static String KEY_ORIGINAL_VERSION =
		"FeatureEditor.ReferenceProp.originalVersion";
	public final static String KEY_DOWNLOAD_SIZE =
		"FeatureEditor.ReferenceProp.download-size";
	public final static String KEY_INSTALL_SIZE =
		"FeatureEditor.ReferenceProp.install-size";

	private final static String P_NAME = "name";
	private final static String P_ID = "id";
	private final static String P_VERSION = "version";
	private final static String P_REF_VERSION = "ref_version";
	private final static String P_OS = "os";
	private final static String P_WS = "ws";
	private final static String P_NL = "nl";
	private final static String P_ARCH = "arch";
	private final static String P_INSTALL_SIZE = "install-size";
	private final static String P_DOWNLOAD_SIZE = "download-size";

	public class VersionProvider extends LabelProvider {
		public Image getImage(Object obj) {
			String originalVersion = getOriginalVersion();
			IFeaturePlugin ref = getPluginReference();
			boolean inSync = ref.getVersion().equals(originalVersion);
			return inSync ? null : errorImage;
		}
	}

	public ReferencePropertySource(
		IFeaturePlugin reference,
		Image errorImage) {
		super(reference);
		this.errorImage = errorImage;
	}
	private String getOriginalVersion() {
		IPluginBase pluginBase = getPluginBase();
		if (pluginBase == null)
			return "";
		return pluginBase.getVersion();
	}
	private IPluginBase getPluginBase() {
		if (pluginBase == null) {
			IFeaturePlugin reference = getPluginReference();
			if (reference.getModel().getUnderlyingResource() == null)
				return null;
			String id = reference.getId();
			WorkspaceModelManager manager =
				PDECore.getDefault().getWorkspaceModelManager();
			IPluginModelBase[] models = null;
			if (reference.isFragment()) {
				models = manager.getWorkspaceFragmentModels();
			} else {
				models = manager.getWorkspacePluginModels();
			}
			for (int i = 0; i < models.length; i++) {
				IPluginModelBase modelBase = models[i];
				IPluginBase candidate = modelBase.getPluginBase();
				if (candidate.getId().equals(id)) {
					pluginBase = candidate;
					break;
				}
			}
		}
		return pluginBase;
	}
	public IFeaturePlugin getPluginReference() {
		return (IFeaturePlugin) object;
	}
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (descriptors == null) {
			descriptors = new Vector();
			PropertyDescriptor desc =
				new PropertyDescriptor(
					P_ID,
					PDEPlugin.getResourceString(KEY_ID));
			descriptors.addElement(desc);
			desc =
				new PropertyDescriptor(
					P_NAME,
					PDEPlugin.getResourceString(KEY_NAME));
			descriptors.addElement(desc);
			desc =
				createTextPropertyDescriptor(
					P_VERSION,
					PDEPlugin.getResourceString(KEY_VERSION));
			//desc.setLabelProvider(new VersionProvider());
			descriptors.addElement(desc);
			desc =
				createTextPropertyDescriptor(
					P_INSTALL_SIZE,
					PDEPlugin.getResourceString(KEY_INSTALL_SIZE));
			descriptors.addElement(desc);
			desc =
				createTextPropertyDescriptor(
					P_DOWNLOAD_SIZE,
					PDEPlugin.getResourceString(KEY_DOWNLOAD_SIZE));
			//desc.setLabelProvider(new VersionProvider());
			descriptors.addElement(desc);
			desc =
				new PropertyDescriptor(
					P_REF_VERSION,
					PDEPlugin.getResourceString(KEY_ORIGINAL_VERSION));
			descriptors.addElement(desc);
			desc = createChoicePropertyDescriptor(P_OS, P_OS, getOSChoices());
			descriptors.addElement(desc);
			desc = createChoicePropertyDescriptor(P_WS, P_WS, getWSChoices());
			descriptors.addElement(desc);
			desc = createChoicePropertyDescriptor(P_NL, P_NL, getNLChoices());
			descriptors.addElement(desc);
			desc =
				createChoicePropertyDescriptor(
					P_ARCH,
					P_ARCH,
					getArchChoices());
			descriptors.addElement(desc);
		}
		return toDescriptorArray(descriptors);
	}

	private PropertyDescriptor createChoicePropertyDescriptor(
		String name,
		String displayName,
		Choice[] choices) {
		return new PortabilityChoiceDescriptor(
			name,
			displayName,
			choices,
			!isEditable());
	}

	public Object getPropertyValue(Object name) {
		if (name.equals(P_ID)) {
			return getPluginReference().getId();
		}
		if (name.equals(P_NAME)) {
			return getPluginReference().getLabel();
		}
		if (name.equals(P_VERSION)) {
			return getPluginReference().getVersion();
		}
		if (name.equals(P_REF_VERSION)) {
			return getOriginalVersion();
		}
		if (name.equals(P_INSTALL_SIZE)) {
			long installSize = getPluginReference().getInstallSize();
			if (installSize == -1)
				return "";
			else
				return "" + installSize;
		}
		if (name.equals(P_DOWNLOAD_SIZE)) {
			long downloadSize = getPluginReference().getDownloadSize();
			if (downloadSize == -1)
				return "";
			else
				return "" + downloadSize;
		}
		if (name.equals(P_OS)) {
			return getPluginReference().getOS();
		}
		if (name.equals(P_WS)) {
			return getPluginReference().getWS();
		}
		if (name.equals(P_NL)) {
			return getPluginReference().getNL();
		}
		if (name.equals(P_ARCH)) {
			return getPluginReference().getArch();
		}
		return null;
	}
	public void setElement(IFeaturePlugin plugin) {
		object = plugin;
	}
	public void setPropertyValue(Object name, Object value) {
		String svalue = value.toString();
		String realValue =
			svalue == null | svalue.length() == 0 ? null : svalue;
		try {
			if (name.equals(P_NAME)) {
				getPluginReference().setLabel(realValue);
			} else if (name.equals(P_VERSION)) {
				getPluginReference().setVersion(realValue);
			} else if (name.equals(P_OS)) {
				getPluginReference().setOS(realValue);
			} else if (name.equals(P_WS)) {
				getPluginReference().setWS(realValue);
			} else if (name.equals(P_NL)) {
				getPluginReference().setNL(realValue);
			} else if (name.equals(P_ARCH)) {
				getPluginReference().setArch(realValue);
			} else if (name.equals(P_DOWNLOAD_SIZE)) {
				long lvalue = getLong(realValue);
				getPluginReference().setDownloadSize(lvalue);
			} else if (name.equals(P_INSTALL_SIZE)) {
				long lvalue = getLong(realValue);
				getPluginReference().setInstallSize(lvalue);
			}
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}
	
	private long getLong(String svalue) {
		if (svalue==null) return -1;
		try {
			return Long.parseLong(svalue);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

	public static Choice[] getOSChoices() {
		return TargetPlatform.getOSChoices();
	}

	public static Choice[] getWSChoices() {
		return TargetPlatform.getWSChoices();
	}

	public static Choice[] getArchChoices() {
		return TargetPlatform.getArchChoices();
	}

	public static Choice[] getNLChoices() {
		return TargetPlatform.getNLChoices();
	}
}