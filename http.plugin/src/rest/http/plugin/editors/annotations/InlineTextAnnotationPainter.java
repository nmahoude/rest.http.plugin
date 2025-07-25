package rest.http.plugin.editors.annotations;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class InlineTextAnnotationPainter extends AnnotationPainter {

  public InlineTextAnnotationPainter(ISourceViewer sourceViewer, IAnnotationAccess annotationAccess) {
      super(sourceViewer, annotationAccess);

      
      // Ajoute la stratégie de dessin pour notre type d’annotation
      addDrawingStrategy(InlineTextAnnotation.TYPE, this::drawInlineText);
  }
  
  private void drawInlineText(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
    if (!(annotation instanceof InlineTextAnnotation inlineAnnotation)) {
        return;
    }

    // Position du texte dans la StyledText
    Point location = textWidget.getLocationAtOffset(offset + length);

    Color oldForeground = gc.getForeground();
    gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));

    String message = inlineAnnotation.getMessage();

    // Décale un peu à droite pour éviter de coller au texte existant
    int x = location.x + 5;
    int y = location.y + gc.getFontMetrics().getAscent();

    gc.drawText(message, x, y, true);

    gc.setForeground(oldForeground);
  }
}
