package rest.http.plugin.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.texteditor.AnnotationColumn;

import rest.http.plugin.HttpPreferencePage;
import rest.http.plugin.request.HttpYacExtractor;
import rest.http.plugin.request.RequestData;
import rest.http.plugin.request.RequestExecutor;
import rest.http.plugin.request.ResponseData;
import rest.http.plugin.views.HttpResultView;

public class HttpFileEditor extends TextEditor {
	private static final String HTTP_ANNOTATION = "rest.http.plugin.ExecuteAnnotation";
	private static final String HTTP_ANNOTATION_INLINE = "rest.http.plugin.inlineHttpAnnotation";
	

	private final HttpYacExtractor yacExtractor = new HttpYacExtractor();
	
	private RequestData requestData = new RequestData();
	private ResponseData responseData = new ResponseData();
	
	public HttpFileEditor() {
    super();
    setSourceViewerConfiguration(new HttpSourceViewerConfiguration());
	}

	@Override
	protected void handleElementContentReplaced() {
		super.handleElementContentReplaced();
		
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// set the font
		getSourceViewer().getTextWidget().setFont(JFaceResources.getFont(HttpPreferencePage.FONT_ID));
		JFaceResources.getFontRegistry().addListener(fontChangeListener);
		
		setRulerContextMenuId("httpRulerContext");

		updateHttpAnnotations();
		IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    doc.addDocumentListener(new IDocumentListener() {
        @Override
        public void documentChanged(DocumentEvent event) {
            yacExtractor.refreshDoc(event.getDocument());
        		updateHttpAnnotations();
        }

        @Override
        public void documentAboutToBeChanged(DocumentEvent event) {}
    });

    CompositeRuler compositeRuler = (CompositeRuler) getVerticalRuler();
    
    var ite = compositeRuler.getDecoratorIterator();
    while (ite.hasNext()) {
			var decorator = ite.next();
			if (decorator instanceof AnnotationColumn ac) {
				ac.getControl().addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						super.mouseDown(e);
						
						IVerticalRulerInfo rulerInfo = getVerticalRuler();
						int line = rulerInfo.getLineOfLastMouseButtonActivity();
						IDocument document = getDocumentProvider().getDocument(getEditorInput());
						try {
							String lineText = document.get(document.getLineOffset(line), document.getLineLength(line)).trim();
							if (lineText.startsWith("GET") 
									|| lineText.startsWith("POST") 
									|| lineText.startsWith("DELETE") 
									|| lineText.startsWith("PUT")) {
								
								
								var result = yacExtractor.extract(document, line);
								
								executeRequest(result);
							}
						} catch (BadLocationException ex) {
							ex.printStackTrace();
						}
					}
				});
			}
		}
    
	}
	
	public void updateHttpAnnotations() {
    IDocument document = getDocumentProvider().getDocument(getEditorInput());
    IAnnotationModel model = getDocumentProvider().getAnnotationModel(getEditorInput());

    // Supprime les anciennes annotations du même type
    List<Annotation> toRemove = new ArrayList<>();
    for (Iterator<?> it = model.getAnnotationIterator(); it.hasNext();) {
        Annotation ann = (Annotation) it.next();
        if (HTTP_ANNOTATION.equals(ann.getType()) || HTTP_ANNOTATION_INLINE.equals(ann.getType())) {
            toRemove.add(ann);
        }
    }
    for (Annotation ann : toRemove) {
        model.removeAnnotation(ann);
    }

    // Scanne le document
    try {
        int lines = document.getNumberOfLines();
        for (int i = 0; i < lines; i++) {
            String lineText = document.get(document.getLineOffset(i), document.getLineLength(i)).trim();
            if (lineText.startsWith("GET") 
            		|| lineText.startsWith("POST") 
            		|| lineText.startsWith("DELETE") 
            		|| lineText.startsWith("PUT")
            		) {
                int offset = document.getLineOffset(i);
                //Annotation annotation = new Annotation(HTTP_ANNOTATION, false, "Run HTTP request");
                
                Annotation annotation = new Annotation(HTTP_ANNOTATION, false, "[Executer]");
                offset = document.getLineOffset(i) + document.getLineLength(i) - 1;
                model.addAnnotation(annotation, new Position(offset, 0));
            }
        }
    } catch (BadLocationException e) {
      // TODO : better
  		e.printStackTrace();
    }
	}
	
	public void executeRequest(RequestData requestData) {
		// MessageDialog.openInformation(null, "Result http", selection);
		
		IWorkbenchPage page = PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow()
		    .getActivePage();

		try {
		    HttpResultView view = (HttpResultView) page.showView(HttpResultView.ID);
		    view.setRequest(requestData);
		    var responseData = new RequestExecutor().execute(requestData);
		    view.setResponse(responseData);
		    
		} catch (PartInitException e) {
		    e.printStackTrace();
		}
	}
	
	
	private final IPropertyChangeListener fontChangeListener = new IPropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (HttpPreferencePage.FONT_ID.equals(event.getProperty())) {
            getSourceViewer().getTextWidget().setFont(
                JFaceResources.getFont(HttpPreferencePage.FONT_ID)
            );
        }
    }
};

}
