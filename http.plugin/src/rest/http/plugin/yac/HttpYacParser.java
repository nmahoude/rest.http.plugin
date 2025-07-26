package rest.http.plugin.yac;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;

public class HttpYacParser {

		List<String> verbs = List.of("GET",	"POST",	"PUT",	"DELETE",	"PATCH",
				"OPTIONS",	"CONNECT",	"TRACE",	"PROPFIND",	"PROPPATCH",
				"COPY",	"MOVE",	"LOCK",	"UNLOCK",	"CHECKOUT",
				"REPORT",	"MERGE",	"MKACTIVITY",	"MKWORKSPACE",	"VERSION-CONTROL");
	
	
    // Parse all requests in the document and return a list of HttpYac
    public List<YacBlock> parse(YacDocument doc, IDocument document) {
        List<String> lines = new ArrayList<>();
        try {
            int lineCount = document.getNumberOfLines();
            for (int i = 0; i < lineCount; i++) {
                int offset = document.getLineOffset(i);
                int length = document.getLineLength(i);
                String line = document.get(offset, length);
                lines.add(line);
            }
        } catch (Exception e) {
            // Handle exception or log
        }
        return parseLines(doc, lines);
    }
    
    public List<YacBlock> parseLines(YacDocument doc, List<String> lines) {
        List<YacBlock> blocks = new ArrayList<>();
        try {
            StringBuilder currentBlock = new StringBuilder();
            int blockStartLine = 0;
            boolean inBlock = false;
            boolean hasVerb = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.startsWith("###") || (hasVerb && verbs.stream().anyMatch(line::startsWith))) {
                	if (verbs.stream().anyMatch(line::startsWith)) hasVerb = true; // on marque qu'on a vu un verbe 
                	
                    if (inBlock && currentBlock.length() > 0) {
                        YacBlock block = new YacBlock(doc, blockStartLine, i - 1, currentBlock.toString().trim());
                        blocks.add(block);
                        currentBlock.setLength(0);
                        hasVerb = false; // Reset verb flag for new block
                    }
                    currentBlock.append(line).append("\n");
                    blockStartLine = i;
                    inBlock = true;
                } else {
                    if (!inBlock) {
                        blockStartLine = i;
                        inBlock = true;
                    }
                    currentBlock.append(line).append("\n");
                }
            }
            // Add last block if present
            if (inBlock && currentBlock.length() > 0) {
                YacBlock block = new YacBlock(doc, blockStartLine, lines.size()-1, currentBlock.toString().trim());
                blocks.add(block);
            }
        } catch (Exception e) {
            // Handle exception or log
        	e.printStackTrace();
        }
        return blocks;
    }

}