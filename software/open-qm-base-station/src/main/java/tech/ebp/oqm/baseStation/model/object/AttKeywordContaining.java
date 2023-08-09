package tech.ebp.oqm.baseStation.model.object;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface AttKeywordContaining {
	
	public Map<@NotBlank @NotNull String, String> getAttributes();
	
	public AttKeywordContaining setAttributes(Map<@NotBlank @NotNull String, String> attributes);
	
	public List<@NotBlank String> getKeywords();
	
	public AttKeywordContaining setKeywords(List<@NotBlank String> keywords);
	
}
