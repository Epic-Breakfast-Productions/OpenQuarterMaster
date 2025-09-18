package tech.ebp.oqm.lib.core.api.java.utils.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;

public class JacksonObjectNodeBodyHandler implements HttpResponse.BodyHandler<ObjectNode> {
	public static final JacksonObjectNodeBodyHandler INSTANCE = new JacksonObjectNodeBodyHandler();
	
	@Override
	public HttpResponse.BodySubscriber<ObjectNode> apply(HttpResponse.ResponseInfo responseInfo) {
		return HttpResponse.BodySubscribers.mapping(
			HttpResponse.BodySubscribers.ofInputStream(),
			inputStream->{
				try (InputStream is = inputStream) {
					return (ObjectNode) JacksonUtils.MAPPER.readTree(is);
				} catch(IOException e) {
					throw new UncheckedIOException(e); // Or handle more gracefully
				}
			}
		);
	}
}
