package rest.http.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import rest.http.plugin.editors.syntaxcoloring.ColorManager;
import rest.http.plugin.editors.syntaxcoloring.HttpScanner;

public class HttpSourceViewerConfiguration extends SourceViewerConfiguration {
	private ColorManager colorManager;
	
	
	public HttpSourceViewerConfiguration() {
    this.colorManager = new ColorManager();
    

	}
	
	@Override
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
      PresentationReconciler reconciler = new PresentationReconciler();

      HttpScanner scanner = new HttpScanner(colorManager);

      DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
      reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
      reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

      return reconciler;
  }
	
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
	    return new HttpTextHover();
	}

}
