package rest.http.plugin.views;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import rest.http.plugin.Activator;
import rest.http.plugin.request.RequestData;
import rest.http.plugin.request.RequestExecutor;
import rest.http.plugin.request.ResponseData;

public class HttpResultView extends ViewPart {
	public static final String ID = "rest.http.plugin.views.HttpResultView";

	private final Image playImage;

	private RequestData currentRequest;
	private ResponseData currentResponse;

	private CTabFolder tabFolder;
	private Combo methodCombo;
	private Text urlField;
	private Button playButton;
	private Button proxyCheckbox;
	private Label headersLabel;
	private Text requestHeadersField;
	private Text requestBodyField;



	private Text responseCodeField;
	private Text responseDurationField;
	private Text responseSizeField;
	private Label responseHeadersLabel;
	private Table responseHeadersTable;
	private Text responseBodyField;

	private Label loadingLabel;

	public HttpResultView() {
		createResourceManager();
		playImage = Activator.getDefault().getImageRegistry().get("play");
	}
	
	
	private void createResourceManager() {
	}

	@Override
	public void createPartControl(Composite parent) {
		tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.FLAT);
		tabFolder.setSimple(true); // Pour avoir un rendu à la Eclipse (onglet moderne)
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder.setSelectionForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));

		// Request Tab
		CTabItem requestTab = new CTabItem(tabFolder, SWT.NONE);
		requestTab.setText("Requête");
		Composite requestComposite = new Composite(tabFolder, SWT.NONE);
		requestTab.setControl(requestComposite);
		requestComposite.setLayout(new GridLayout(3, false));
		methodCombo = new Combo(requestComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		methodCombo.setItems(new String[] { "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS" });
		methodCombo.select(0);
		urlField = new Text(requestComposite, SWT.BORDER);
		urlField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// Play button next to URL field
		playButton = new Button(requestComposite, SWT.PUSH);
		playButton.setToolTipText("Exécuter la requête");
		playButton.setImage(playImage);
		playButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executeRequest();
			}
		});
		proxyCheckbox = new Button(requestComposite, SWT.CHECK);
		proxyCheckbox.setText("Utiliser le proxy");
		proxyCheckbox.setSelection(false); // Default unchecked
		proxyCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		new Label(requestComposite, SWT.NONE);
		new Label(requestComposite, SWT.NONE);

		headersLabel = new Label(requestComposite, SWT.NONE);
		headersLabel.setText("Headers:");
		new Label(requestComposite, SWT.NONE);
		new Label(requestComposite, SWT.NONE);
		requestHeadersField = new Text(requestComposite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_requestHeadersField = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd_requestHeadersField.heightHint = 50;
		gd_requestHeadersField.minimumHeight = 50;
		requestHeadersField.setLayoutData(gd_requestHeadersField);
		new Label(requestComposite, SWT.NONE);

		Label bodyLabel = new Label(requestComposite, SWT.NONE);
		bodyLabel.setText("Body:");
		new Label(requestComposite, SWT.NONE);
		new Label(requestComposite, SWT.NONE);
		requestBodyField = new Text(requestComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		requestBodyField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		Image playImage = Activator.getDefault().getImageRegistry().get("play");

		// Response Tab
		CTabItem responseTab = new CTabItem(tabFolder, SWT.NONE);
		responseTab.setText("Réponse");
		Composite responseComposite = new Composite(tabFolder, SWT.NONE);
		GridLayout gl_responseComposite = new GridLayout(3, false);
		gl_responseComposite.horizontalSpacing = 15;
		responseComposite.setLayout(gl_responseComposite);
		responseCodeField = new Text(responseComposite, SWT.BORDER | SWT.READ_ONLY);
		responseCodeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		responseCodeField.setBackground(new Color(255, 255, 0)); // Default color
		
		
		responseDurationField = new Text(responseComposite, SWT.BORDER | SWT.READ_ONLY);
		responseDurationField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		responseSizeField = new Text(responseComposite, SWT.BORDER | SWT.READ_ONLY);

		responseHeadersLabel = new Label(responseComposite, SWT.NONE);
		responseHeadersLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		responseHeadersLabel.setText("Headers:");
		
		responseHeadersTable = new Table(responseComposite, SWT.BORDER | SWT.FULL_SELECTION);
		responseHeadersTable.setHeaderBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		responseHeadersTable.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		responseHeadersTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));
		responseHeadersTable.setHeaderVisible(true);
		responseHeadersTable.setLinesVisible(true);
		// Add columns: Name and Value
		TableColumn nameColumn = new TableColumn(responseHeadersTable, SWT.LEFT);
		nameColumn.setWidth(150);
		nameColumn.setText("Name");
		TableColumn valueColumn = new TableColumn(responseHeadersTable, SWT.LEFT);
		valueColumn.setWidth(350);
		valueColumn.setText("Value");

		Label responseBodyLabel = new Label(responseComposite, SWT.NONE);
		responseBodyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		responseBodyLabel.setText("Body:");

		responseTab.setControl(responseComposite);
		responseBodyField = new Text(responseComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		responseBodyField.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridData gd_responseBodyField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_responseBodyField.horizontalSpan = 3;
		responseBodyField.setLayoutData(gd_responseBodyField);
		responseBodyField.setEditable(false);
		
		
		tabFolder.setSelection(0);
	}

	@Override
	public void setFocus() {
		// Focus on the first field in the selected tab
		// ...existing code...
	}

	// Setters for updating fields
	public void setRequest(RequestData requestData) {
		this.currentRequest = requestData;

		proxyCheckbox.setSelection(!requestData.noProxy);

		if (methodCombo != null && !methodCombo.isDisposed()) {
			int idx = methodCombo.indexOf(requestData.method);
			methodCombo.select(idx >= 0 ? idx : 0);
		}
		if (urlField != null && !urlField.isDisposed())
			urlField.setText(requestData.url.toString());
		if (requestHeadersField != null && !requestHeadersField.isDisposed()) {
			StringBuilder headersBuilder = new StringBuilder();
			requestData.headers.forEach((key, values) -> {
				for (String value : values) {
					headersBuilder.append(key).append(": ").append(value).append("\n");
				}
			});
			requestHeadersField.setText(headersBuilder.toString().trim());
		}
		if (requestBodyField != null && !requestBodyField.isDisposed()) {
			requestBodyField.setText(requestData.body);
		}
	}

	public void setResponse(ResponseData data) {
		this.currentResponse = data;
		
		// Masquer l'indicateur de chargement
		if (loadingLabel != null && !loadingLabel.isDisposed()) {
			loadingLabel.setVisible(false);
		}
		
		responseCodeField.setText("" + data.code);
		// Set background color using setBackground, but workaround for SWT Text bug
		Display display = Display.getCurrent();
		if (data.code >= 200 && data.code < 300) {
			responseCodeField.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		} else if (data.code >= 400) {
			responseCodeField.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
		} else {
			responseCodeField.setForeground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
		}
		
		responseDurationField.setText(data.duration + " ms");
		responseSizeField.setText(data.size + "/TODO2 ko");
		
		
		responseHeadersLabel.setText("Headers (" + data.headers.size() + ")");

		// Remove all items from the table before adding new ones
		responseHeadersTable.removeAll();
		data.headers.entrySet().stream()
    .sorted(Map.Entry.comparingByKey())
    .forEach(entry -> {
        String key = entry.getKey();
        for (String value : entry.getValue()) {
            TableItem item = new TableItem(responseHeadersTable, SWT.NONE);
            item.setText(new String[] { key, value });
        }
    });
		responseHeadersTable.redraw();
		
		Object body = data.body;
		responseBodyField.setText(body != null ? body.toString() : "");
		
		tabFolder.setSelection(1);
	}

	public void setLoading(boolean loading) {
		if (loading) {
			// Créer l'indicateur de chargement s'il n'existe pas
			if (loadingLabel == null || loadingLabel.isDisposed()) {
				// Trouver le composite de l'onglet réponse
				CTabItem responseTab = tabFolder.getItem(1);
				Composite responseComposite = (Composite) responseTab.getControl();
				
				loadingLabel = new Label(responseComposite, SWT.NONE);
				loadingLabel.setText("Chargement en cours...");
				loadingLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
				loadingLabel.moveAbove(responseCodeField);
				responseComposite.layout(true);
			}
			loadingLabel.setVisible(true);
			
			// Basculer vers l'onglet réponse
			tabFolder.setSelection(1);
		} else {
			// Masquer l'indicateur de chargement
			if (loadingLabel != null && !loadingLabel.isDisposed()) {
				loadingLabel.setVisible(false);
			}
		}
	}

	public void setError(String errorMessage) {
		// Masquer l'indicateur de chargement
		setLoading(false);
		
		// Afficher l'erreur dans le champ de réponse
		responseCodeField.setText("ERROR");
		responseCodeField.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
		responseDurationField.setText("N/A");
		responseSizeField.setText("N/A");
		responseHeadersTable.removeAll();
		responseBodyField.setText("Erreur: " + errorMessage);
		
		// Basculer vers l'onglet réponse
		tabFolder.setSelection(1);
	}

	private void executeRequest() {
		// Afficher l'indicateur de chargement
		setLoading(true);
		
		// Exécuter la requête dans un thread séparé
		new Thread(() -> {
			try {
				RequestExecutor executor = new RequestExecutor();
				var response = executor.execute(currentRequest);
				
				// Retourner sur le thread UI pour mettre à jour l'interface
				Display.getDefault().asyncExec(() -> {
					setResponse(response);
				});
				
			} catch (Exception e) {
				// Gérer les erreurs sur le thread UI
				Display.getDefault().asyncExec(() -> {
					setError(e.getMessage());
				});
			}
		}).start();
	}
}