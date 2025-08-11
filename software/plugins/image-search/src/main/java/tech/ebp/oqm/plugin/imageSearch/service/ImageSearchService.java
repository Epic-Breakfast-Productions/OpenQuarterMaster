package tech.ebp.oqm.plugin.imageSearch.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.Closeable;
import java.util.LinkedHashMap;

@ApplicationScoped
public class ImageSearchService {
	
	/**
	 *
	 * @param query
	 * @return
	 */
	public LinkedHashMap<Double, String> search(String query) {
		
//		getClass().getClassLoader().getResourceAsStream("imageData.json");
//		getClass().getClassLoader().getResource("imageData.json");
//
//		try (
//			InputStream is = getClass().getResourceAsStream("/data/my-data.json")
//		) { // Absolute path from classpath root
//			// Read from InputStream
//		} catch (IOException e) {
//			// Handle exception
//		}
		
		return null;
	}
}
