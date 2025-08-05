package rest.http.plugin.editors;

import java.net.MalformedURLException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import rest.http.plugin.request.RequestData;
import rest.http.plugin.yac.YacDocument;

public class HttpTextHover implements ITextHover {

	@Override
	public String getHoverInfo(ITextViewer viewer, IRegion hoverRegion) {
		final YacDocument[] yacRef = new YacDocument[1];
		
		// Access SWT widgets on the UI thread
		Display.getDefault().syncExec(() -> {
			Shell shell = viewer.getTextWidget().getShell();
			Object data = shell.getData();
			if (data instanceof YacDocument) {
				yacRef[0] = (YacDocument) data;
			}
		});
		
		YacDocument yac = yacRef[0];
		if (yac == null) {
			return null; // Assure que l'éditeur est bien un HttpFileEditor
		}
		
		try {
			IDocument document = viewer.getDocument();
			int offset = hoverRegion.getOffset();

			int lineNumber = document.getLineOfOffset(offset);
			IRegion lineRegion = document.getLineInformation(lineNumber);
			String lineText = document.get(lineRegion.getOffset(), lineRegion.getLength());

			if (lineText.matches("^(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD)\\s+.*")) {
				RequestData request = yac.getAt(lineNumber);
				String[] parts = lineText.split("\\s+");
				String method = parts[0];

				try {
					return " "+ method + " " + request.url().toString();
				} catch (MalformedURLException e) {
					return "**Méthode :** " + method + "\n**URL :** " + parts[1];
				}
			}

			return null;

		} catch (BadLocationException e) {
			return null;
		}
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		// Simple : survole n’importe quel caractère
		return new Region(offset, 1);
	}
	
	
}