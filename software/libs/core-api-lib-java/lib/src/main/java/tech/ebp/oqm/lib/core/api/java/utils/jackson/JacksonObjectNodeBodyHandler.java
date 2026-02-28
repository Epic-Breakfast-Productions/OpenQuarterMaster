package tech.ebp.oqm.lib.core.api.java.utils.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;

public class JacksonObjectNodeBodyHandler implements HttpResponse.BodyHandler<ObjectNode> {
	public static final JacksonObjectNodeBodyHandler INSTANCE = new JacksonObjectNodeBodyHandler();
	
	@Override
	public HttpResponse.BodySubscriber<ObjectNode> apply(HttpResponse.ResponseInfo responseInfo) {
		return HttpResponse.BodySubscribers.mapping(
			HttpResponse.BodySubscribers.ofString(Charset.defaultCharset()),
			json->{
				try {
					JsonNode jsonNode = new ObjectMapper().readTree(json);
					
					if(jsonNode.isMissingNode()) {
						return null;
					}
					
					return (ObjectNode) jsonNode;
				} catch(JsonProcessingException e) {
					throw new RuntimeException("Failed to parse JSON string: " + json, e);
				}
			}
		);
	}
}
