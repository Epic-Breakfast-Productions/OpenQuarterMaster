package tech.ebp.oqm.lib.core.object;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface AttKeywordContaining {
	
	public Map<@NotBlank @NotNull String, String> getAttributes();
	
	public AttKeywordContaining setAttributes(Map<@NotBlank @NotNull String, String> attributes);
	
	public List<@NotBlank String> getKeywords();
	
	public AttKeywordContaining setKeywords(List<@NotBlank String> keywords);
	
}
