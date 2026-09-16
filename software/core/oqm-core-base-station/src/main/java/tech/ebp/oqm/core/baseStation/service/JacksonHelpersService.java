package tech.ebp.oqm.core.baseStation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
@Named("JacksonHelpersService")
public class JacksonHelpersService {

	public Stream<JsonNode> getStreamFromJsonArr(ArrayNode jsonArr){
		return StreamSupport.stream(jsonArr.spliterator(), false);
	}
}
