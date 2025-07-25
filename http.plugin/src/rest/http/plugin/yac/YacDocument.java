package rest.http.plugin.yac;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;

import rest.http.plugin.request.RequestData;

public class YacDocument {
	public List<YacBlock> blocks = new ArrayList<>();
	Map<String, String> variables = new java.util.HashMap<>();
	
	public void load(IDocument document) {
		blocks.clear();
		HttpYacParser parser = new HttpYacParser();
		List<YacBlock> parsedBlocks = parser.parse(document);


		for (YacBlock block : parsedBlocks) {
			block.init(variables);
			
			if (!block.isValidRequest() && block == parsedBlocks.get(0)) {
				variables.putAll(block.extractVariables());
			}
			if (block.isValidRequest()) {
				blocks.add(block);
			} else {
			}
		}
	}


	public RequestData getAt(int line) {
		for (YacBlock block : blocks) {
			if (line >= block.startingLine && line <= block.endLine) {
				return block.request();
			}
		}
		return null;
	}
}
