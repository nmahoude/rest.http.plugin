package rest.http.plugin.editors.listeners;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.swt.widgets.Menu;

public class HttpListener implements IVerticalRulerListener {



	@Override
	public void annotationSelected(VerticalRulerEvent event) {
		System.out.println("Selected annotation: " + event.getSelectedAnnotation().getText());
	}

//	protected IAnnotationModel getAnnotationModel() {
//		IDocumentProvider provider= getTextEditor().getDocumentProvider();
//		return provider.getAnnotationModel(getTextEditor().getEditorInput());
//	}
	
	@Override
	public void annotationDefaultSelected(VerticalRulerEvent event) {
		Annotation a= event.getSelectedAnnotation();
		System.out.println("Default selected annotation: " + a.getText());
	}

	@Override
	public void annotationContextMenuAboutToShow(VerticalRulerEvent event, Menu menu) {
		System.out.println("Context menu about to show for annotation: " + event.getSelectedAnnotation().getText());
	}
	

}
