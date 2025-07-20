package rest.http.plugin.editors.annotations;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

public class MarkerAnnotationModel extends AbstractMarkerAnnotationModel {

	private IWorkspace workspace;
	private IResource resource;

	public MarkerAnnotationModel(IResource resource) {
		super();
		this.resource = resource;
		this.workspace= resource.getWorkspace();
	}

	@Override
	protected IMarker[] retrieveMarkers() throws CoreException {
		return resource.findMarkers(IMarker.MARKER, true, IResource.DEPTH_ZERO);
	}

	@Override
	protected void deleteMarkers(IMarker[] markers) throws CoreException {
		workspace.run((IWorkspaceRunnable) monitor -> {
			for (IMarker marker : markers) {
				marker.delete();
			}
		}, null, IWorkspace.AVOID_UPDATE, null);
	}

	@Override
	protected void listenToMarkerChanges(boolean listen) {
	}

	@Override
	protected boolean isAcceptable(IMarker marker) {
		return marker != null && resource.equals(marker.getResource());
	}

}
