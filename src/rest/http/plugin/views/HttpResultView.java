package rest.http.plugin.views;

import java.net.http.HttpResponse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import rest.http.plugin.Activator;
import rest.http.plugin.request.RequestData;
import rest.http.plugin.request.RequestExecutor;

public class HttpResultView extends ViewPart {
	public static final String ID = "rest.http.plugin.views.HttpResultView";

	private RequestData currentRequest;
	private HttpResponse currentResponse;

	private Combo methodCombo;
	private Text urlField;
	private Text requestHeadersField;
	private Text requestBodyField;
	private Text responseBodyField;
	private Text responseHeadersField;
	private Text responseCodeField;

	private CTabFolder tabFolder;
	private Button playButton;

	@Override
	public void createPartControl(Composite parent) {
		tabFolder = new CTabFolder(parent, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Request Tab
		CTabItem requestTab = new CTabItem(tabFolder, SWT.NONE);
		requestTab.setText("Requête");
		Composite requestComposite = new Composite(tabFolder, SWT.NONE);
		requestComposite.setLayout(new GridLayout(3, false));

		// Method dropdown
		Label methodLabel = new Label(requestComposite, SWT.NONE);
		methodLabel.setText("Method:");
		methodCombo = new Combo(requestComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		methodCombo.setItems(new String[] {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"});
		methodCombo.select(0); // Default to GET
		methodCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// Empty label for grid alignment
		new Label(requestComposite, SWT.NONE);

		Label urlLabel = new Label(requestComposite, SWT.NONE);
		urlLabel.setText("URL:");
		urlField = new Text(requestComposite, SWT.BORDER);
		urlField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// Play button next to URL field
		playButton = new Button(requestComposite, SWT.PUSH);
		playButton.setToolTipText("Exécuter la requête");
		Image playImage = Activator.getDefault().getImageRegistry().get("play");
		playButton.setImage(playImage);
		playButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executeRequest();
			}
		});
		GridData playButtonData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		playButton.setLayoutData(playButtonData);

		Label headersLabel = new Label(requestComposite, SWT.NONE);
		headersLabel.setText("Headers:");
		requestHeadersField = new Text(requestComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		requestHeadersField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		// Empty label for grid alignment
		new Label(requestComposite, SWT.NONE);

		Label bodyLabel = new Label(requestComposite, SWT.NONE);
		bodyLabel.setText("Body:");
		requestBodyField = new Text(requestComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		requestBodyField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		// Empty label for grid alignment
		new Label(requestComposite, SWT.NONE);

		requestTab.setControl(requestComposite);

		// Response Tab
		CTabItem responseTab = new CTabItem(tabFolder, SWT.NONE);
		responseTab.setText("Réponse");
		Composite responseComposite = new Composite(tabFolder, SWT.NONE);
		responseComposite.setLayout(new GridLayout(2, false));

		Label codeLabel = new Label(responseComposite, SWT.NONE);
		codeLabel.setText("Code HTTP:");
		responseCodeField = new Text(responseComposite, SWT.BORDER);
		responseCodeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		responseCodeField.setEditable(false);

		Label responseHeadersLabel = new Label(responseComposite, SWT.NONE);
		responseHeadersLabel.setText("Headers:");
		responseHeadersField = new Text(responseComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		responseHeadersField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		responseHeadersField.setEditable(false);

		Label responseBodyLabel = new Label(responseComposite, SWT.NONE);
		responseBodyLabel.setText("Body:");
		responseBodyField = new Text(responseComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		responseBodyField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		responseBodyField.setEditable(false);

		responseTab.setControl(responseComposite);
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
		if (methodCombo != null && !methodCombo.isDisposed()) {
			int idx = methodCombo.indexOf(requestData.request.method());
			methodCombo.select(idx >= 0 ? idx : 0);
		}
		if (urlField != null && !urlField.isDisposed())
			urlField.setText(requestData.request.uri().toString());
		if (requestHeadersField != null && !requestHeadersField.isDisposed()) {
			StringBuilder headersBuilder = new StringBuilder();
			requestData.request.headers().map().forEach((key, values) -> {
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
	
	public void setResponse(HttpResponse<?> data) {
		this.currentResponse = data;
		
		if (responseCodeField != null && !responseCodeField.isDisposed()) {
			responseCodeField.setText("" + data.statusCode());
		}
		
		if (responseHeadersField != null && !responseHeadersField.isDisposed()) {
			StringBuilder headersBuilder = new StringBuilder();
			data.headers().map().forEach((key, values) -> {
				for (String value : values) {
					headersBuilder.append(key).append(": ").append(value).append("\n");
				}
			});
			responseHeadersField.setText(headersBuilder.toString().trim());
		}

		if (responseBodyField != null && !responseBodyField.isDisposed()) {
			Object body = data.body();
			responseBodyField.setText(body != null ? body.toString() : "");
		}
		
		tabFolder.setSelection(1);
	}

	private void executeRequest() {
		RequestExecutor executor = new RequestExecutor();
		var response = executor.execute(currentRequest);
		setResponse(response);
  }
}