package rest.http.plugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class HttpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String FONT_ID = "rest.http.plugin.fontDefinition";

  public HttpPreferencePage() {
      super(GRID);
      setPreferenceStore(Activator.getDefault().getPreferenceStore());
      setDescription("Préférences de l'éditeur HTTP");
  }

  @Override
  protected void createFieldEditors() {
      addField(new FontFieldEditor(FONT_ID, "Police de l'éditeur HTTP :", getFieldEditorParent()));
  }

  @Override
  public void init(IWorkbench workbench) {}
}
