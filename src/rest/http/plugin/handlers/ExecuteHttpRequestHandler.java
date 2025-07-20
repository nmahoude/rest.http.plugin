package rest.http.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

import rest.http.plugin.editors.HttpFileEditor;

public class ExecuteHttpRequestHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
      IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                     .getActivePage().getActiveEditor();

      if (!(editor instanceof HttpFileEditor httpEditor)) {
        return null;
      }

      IDocumentProvider dp = httpEditor.getDocumentProvider();
      IDocument doc = dp.getDocument(httpEditor.getEditorInput());

      ITextSelection selection = (ITextSelection) httpEditor.getSelectionProvider().getSelection();
      int lineNumber = selection.getStartLine(); // ligne de départ de la sélection (0-based)
      try {
				String lineText = doc.get(doc.getLineOffset(lineNumber), doc.getLineLength(lineNumber)).trim();
				//httpEditor.executeRequest(lineText);
			} catch (BadLocationException e) {
			}
      
      return null;
  }
}
