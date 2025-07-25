package rest.http.plugin.yac;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HttpYacParserTest {

	@Test
	void justeOneLine() {

		String input = """
				GET https://example.com/api/resource
				""";

		HttpYacParser parser = new HttpYacParser();
		List<YacBlock> requests = parser.parseLines(null, toLines(input));

		Assertions.assertEquals(requests.size(), 1);
		var req = requests.get(0);
		assertEquals(req.startingLine, 0);
		assertEquals(req.endLine, 0);
	}
	
	
	@Test
	void multipleRequests() {

		String input = """
				### Request 1
				GET https://example.com/api/resource1
				Header1: Value1
				Header2: Value2
				
				### Request 2
				POST https://example.com/api/resource2
				Content-Type: application/json
				
				{"key": "value"}
				
				### Request 3
				PUT https://example.com/api/resource3
				""";

		HttpYacParser parser = new HttpYacParser();
		List<YacBlock> requests = parser.parseLines(null, toLines(input));

		assertEquals(requests.size(), 3);
		var req1 = requests.get(0);
		assertEquals(req1.startingLine, 0);
		assertEquals(req1.endLine, 4);
		
		var req2 = requests.get(1);
		assertEquals(req2.startingLine, 5);
		assertEquals(req2.endLine, 10);
		
		
		var req3 = requests.get(2);
		assertEquals(req3.startingLine, 11);
		assertEquals(req3.endLine, 12);
	}

	private List<String> toLines(String input) {
		return Arrays.asList(input.split("\n"));
	}
}
