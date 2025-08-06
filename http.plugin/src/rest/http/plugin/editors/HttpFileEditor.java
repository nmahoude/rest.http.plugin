package rest.http.plugin.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
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
import rest.http.plugin.request.RequestData;
import rest.http.plugin.request.RequestExecutor;
import rest.http.plugin.request.ResponseData;
import rest.http.plugin.views.HttpResultView;
import rest.http.plugin.yac.YacBlock;
import rest.http.plugin.yac.YacDocument;

public class HttpFileEditor extends TextEditor {
	private static final String HTTP_ANNOTATION = "rest.http.plugin.ExecuteAnnotation";
	private static final String HTTP_ANNOTATION_INLINE = "rest.http.plugin.inlineHttpAnnotation";
	

	private final YacDocument yac= new YacDocument();
	
	private RequestData requestData = new RequestData();
	private ResponseData responseData = new ResponseData();
	private ProjectionAnnotationModel projectionAnnotationModel;
	
	public HttpFileEditor() {
    super();
    setSourceViewerConfiguration(new HttpSourceViewerConfiguration());
	}

	public YacDocument yac() {
		return yac;
	}
	
	
	@Override
	protected void handleElementContentReplaced() {
		super.handleElementContentReplaced();
		
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ProjectionViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		return viewer;
	}
	
	private void updateFoldingStructure() {
    if (projectionAnnotationModel == null) return;

    Map<ProjectionAnnotation, Position> newAnnotations = new HashMap<>();
    
    // Récupérer les anciennes annotations de projection
    Map<Annotation, Position> oldAnnotations = new HashMap<>();
    Iterator<Annotation> annotationIterator = projectionAnnotationModel.getAnnotationIterator();
    while (annotationIterator.hasNext()) {
        Annotation annotation = annotationIterator.next();
        if (annotation instanceof ProjectionAnnotation) {
            Position position = projectionAnnotationModel.getPosition(annotation);
            oldAnnotations.put(annotation, position);
        }
    }

    try {
        IDocument doc = getSourceViewer().getDocument();
        int lineCount = doc.getNumberOfLines();

        for (int i = 0; i < lineCount; i++) {
            String line = doc.get(doc.getLineOffset(i), doc.getLineLength(i)).trim();
            if (line.startsWith("###{{")) { // Exemple de déclencheur de zone
                int startOffset = doc.getLineOffset(i);
                int endOffset = findEndOfFoldableBlock(doc, i);
                int length = endOffset - startOffset;

                ProjectionAnnotation annotation = new ProjectionAnnotation();
                Position position = new Position(startOffset, length);
                newAnnotations.put(annotation, position);
            }
        }

        // Remplacer les anciennes annotations par les nouvelles
        projectionAnnotationModel.replaceAnnotations(oldAnnotations.keySet().toArray(new Annotation[0]), newAnnotations);

    } catch (BadLocationException e) {
        e.printStackTrace();
    }
	}
	
	private int findEndOfFoldableBlock(IDocument doc, int startLine) throws BadLocationException {
	    int lineCount = doc.getNumberOfLines();
	    int endLine = startLine + 1;
	    boolean found = false;
	    while (endLine < lineCount) {
	        String line = doc.get(doc.getLineOffset(endLine), doc.getLineLength(endLine)).trim();
	        if (line.startsWith("###}}")) {
	        	found =true;
	        	break; // Nouvelle section
	        }
	        endLine++;
	    }
	    if (found) {
	    	return doc.getLineOffset(endLine);
	    } else {
	    	return doc.getLineOffset(lineCount-1);
	    }
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// set the font
		getSourceViewer().getTextWidget().setFont(JFaceResources.getFont(HttpPreferencePage.FONT_ID));
		getSourceViewer().getTextWidget().getShell().setData(yac); // référence au YacDocument
		
		JFaceResources.getFontRegistry().addListener(fontChangeListener);
		
		setRulerContextMenuId("httpRulerContext");

		
		// Active le support du pliage
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
    ProjectionSupport projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
    projectionSupport.install();
    viewer.enableProjection();

    // Mise en place du fournisseur de folding
    projectionAnnotationModel = viewer.getProjectionAnnotationModel();

    updateFoldingStructure();
    updateHttpAnnotations();
		
		
		IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    doc.addDocumentListener(new IDocumentListener() {
        @Override
        public void documentChanged(DocumentEvent event) {
            yac.load(event.getDocument());
            updateFoldingStructure();
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

								var result = yac.getAt(line);
								
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
    yac.load(document);
    for (YacBlock block : yac.blocks) {
				if (block.isValidRequest()) {
						try {
								int offset = document.getLineOffset(block.startingLine+block.verbLine);
								
								Annotation annotation = new Annotation(HTTP_ANNOTATION, false, "[Executer]");
								model.addAnnotation(annotation, new Position(offset, 0));
						} catch (BadLocationException e) {
								e.printStackTrace();
						}
				}
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
      
      // Afficher un indicateur de chargement
      view.resetResponse();
      view.setLoading(true);
      
      // Exécuter la requête dans un thread séparé
      new Thread(() -> {
          try {
              var responseData = new RequestExecutor().execute(requestData);
              
              // Retourner sur le thread UI pour mettre à jour l'interface
              PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
                  view.setResponse(responseData);
                  view.setLoading(false);
              });
              
          } catch (Exception e) {
              // Gérer les erreurs sur le thread UI
              PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
                  view.setError("Erreur lors de l'exécution: " + e.getMessage());
                  view.setLoading(false);
              });
          }
      }).start();
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
