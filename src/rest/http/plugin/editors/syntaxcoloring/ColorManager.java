package rest.http.plugin.editors.syntaxcoloring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ColorManager {
  protected Map<RGB, Color> colorTable = new HashMap<>();

  public void dispose() {
      for (Color color : colorTable.values()) {
          color.dispose();
      }
  }

  public Color getColor(RGB rgb) {
      return colorTable.computeIfAbsent(rgb, Color::new);
  }
}
