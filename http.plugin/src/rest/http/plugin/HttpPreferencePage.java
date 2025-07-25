package rest.http.plugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class HttpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String FONT_ID = "rest.http.plugin.fontDefinition";
	public static final String SOCKS_PROXY_HOST_ID = "rest.http.plugin.socksProxy.host";
	public static final String SOCKS_PROXY_PORT_ID = "rest.http.plugin.socksProxy.port";

  public HttpPreferencePage() {
      super(GRID);
      setPreferenceStore(Activator.getDefault().getPreferenceStore());
      setDescription("Préférences de l'éditeur HTTP");
  }

  @Override
  protected void createFieldEditors() {
      addField(new FontFieldEditor(FONT_ID, "Police de l'éditeur HTTP :", getFieldEditorParent()));
      addField(new StringFieldEditor(SOCKS_PROXY_HOST_ID, "Proxy SOCKS (host) :", getFieldEditorParent()));
      addField(new IntegerFieldEditor(SOCKS_PROXY_PORT_ID, "Proxy SOCKS (port) :", getFieldEditorParent()));
  }

  @Override
  public void init(IWorkbench workbench) {}
}