package rest.http.plugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import rest.http.plugin.editors.HttpFileEditor;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "rest.http.plugin"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private IResourceChangeListener resourceChangeListener = null;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		getImageRegistry().put("play", imageDescriptorFromPlugin(PLUGIN_ID, "icons/play.png"));
		
		IPreferenceStore store = getPreferenceStore();
    FontData[] fontData = PreferenceConverter.getFontDataArray(store, HttpPreferencePage.FONT_ID);
    
    JFaceResources.getFontRegistry().put(HttpPreferencePage.FONT_ID, fontData);
    
    handleFileChange();
	}

	
	
	private void handleFileChange() {
		resourceChangeListener  = event -> {
	    if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
	        IResourceDelta rootDelta = event.getDelta();
	        try {
	            rootDelta.accept(delta -> {
	                if (delta.getKind() == IResourceDelta.REPLACED) {
	                    IResource resource = delta.getResource();
	                    if (resource.getType() == IResource.FILE && resource.getName().endsWith(".http")) {
	                        updateReference(resource, delta);
	                    }
	                }
	                return true;
	            });
	        } catch (CoreException e) {
	            e.printStackTrace();
	        }
	    }
	};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener );		
	}

	private void updateReference(IResource resource, IResourceDelta delta) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IEditorReference ref : page.getEditorReferences()) {
			IEditorPart editor = ref.getEditor(false);
			if (editor != null && editor.getEditorInput() instanceof HttpFileEditor e) {
				//		        if (e.getFile().equals(delta.getResource())) {
				//		        	try {
				//		      	    IEditorDescriptor desc = ref.getEditor(false).getEditorSite().getEditorDescriptor();
				//		      	    page.openEditor(new HttpFileEditor(newFile), desc.getId());
				//		      	    page.closeEditor(ref.getEditor(false), false);
				//		      	} catch (PartInitException e) {
				//		      	    e.printStackTrace();
				//		      	}
			}
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (resourceChangeListener != null) {
      ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
		super.stop(context);
	}

	
	
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
