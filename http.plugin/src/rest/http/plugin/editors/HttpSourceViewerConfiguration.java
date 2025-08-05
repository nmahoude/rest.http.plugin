package rest.http.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
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
		return new ITextHover() {
			@Override
			public String getHoverInfo(ITextViewer viewer, IRegion region) {
				return "TODO : retourner le verb + url réelle si on execute via request/httpblock";
			}

			@Override
			public IRegion getHoverRegion(ITextViewer viewer, int offset) {
				return new Region(offset, 1);
			}
		};
	}

}
