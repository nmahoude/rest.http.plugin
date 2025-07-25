package rest.http.plugin.yac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rest.http.plugin.request.JsonPrettyPrinter;

class YacBlockTest {
		YacDocument document = new YacDocument();
		
    @Test
    void testSimpleBlock() {
    	YacBlock block = new YacBlock(document, 0, 7, 
        		"""
        		GET https://example.com
        		Header: value
        		""");
        var result = block.request();
        assertEquals(result.method, "GET");
        assertEquals(result.url, "https://example.com");
        assertEquals(result.headers.size(), 1);
        assertTrue(result.headers.containsKey("Header"));
        assertEquals(result.headers.get("Header").get(0), "value");
    }
    
    @Test
		void withoutVerbSetGET() {
    	YacBlock block = new YacBlock(document, 0, 7, 
    		  """
      		https://example.com
      		""");
      
      var result = block.request();
      assertEquals("GET", result.method);
      assertEquals("https://example.com", result.url);
		}
    
    @Test
    void withBody() {
        YacBlock block = new YacBlock(document, 5, 6, 
        """
        		### Request with body
        		GET https://example.com
        		
        		{
        			"key": "value"
        		}
        		""");
        
        var result = block.request();
        assertEquals("GET", result.method);
        assertEquals("https://example.com", result.url);
        assertEquals(
        		"""
        		{"key": "value"}
        		""".strip(), JsonPrettyPrinter.prettyPrint(result.body));
    }
}