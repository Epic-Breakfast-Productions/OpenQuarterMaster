package com.ebp.openQuarterMaster.lib.core.rest.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageCreateRequest {
    public String title;
    public String description;
    public String imageData;

    public List<String> keywords = null;
    public Map<String, String> attributes = null;
}
