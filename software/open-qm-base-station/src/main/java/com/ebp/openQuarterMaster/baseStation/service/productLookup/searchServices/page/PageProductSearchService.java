package com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.page;

import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
@Slf4j
@Traced
@NoArgsConstructor
public class PageProductSearchService {
	
	private static ProductLookupResult.Builder<?, ?> getProdDetailsAdafruit(
		ProductLookupResult.Builder<?, ?> resultBuilder,
		Document results
	) {
		Map<String, String> atts = new HashMap<>();
		resultBuilder = resultBuilder.unifiedName(results.getElementsByClass("products_name").text());
		
		resultBuilder = resultBuilder.description(results.getElementById("tab-description-content").text());
		
		atts.put("price", results.getElementById("prod-price").text());
		
		resultBuilder.attributes(atts);
		return resultBuilder;
	}
	
	private static ProductLookupResult.Builder<?, ?> getProdDetailsAmazon(
		ProductLookupResult.Builder<?, ?> resultBuilder,
		Document results
	) {
		Map<String, String> atts = new HashMap<>();
		resultBuilder = resultBuilder.unifiedName(results.getElementById("productTitle").text());
		
		try {
			//TODO:: this isn't working... why?
			resultBuilder = resultBuilder.description(results.getElementById("productDescription").text());
		} catch(NullPointerException e){
			log.warn("Unable to find description...");
			//			log.debug("Found price? {}", results.text().contains("$94.49"));
		}
		
		
		//		resultBuilder = resultBuilder.description(results.getElementById("tab-description-content").text());
		
		try {
			//TODO:: this isn't working... why?
			atts.put(
				"price",
				results.getElementById("corePriceDisplay_desktop_feature_div")
					   .text()
			);
		} catch(NullPointerException e){
			log.warn("Unable to find price...");
			//			log.debug("Found price? {}", results.text().contains("$94.49"));
		}
		
		resultBuilder.attributes(atts);
		return resultBuilder;
	}
	
	
	public ProductLookupResult documentToSearchResult(URL page, Document results) {
		ProductLookupResult.Builder<?, ?> output = ProductLookupResult.builder();
		output = output.source(page.getHost());
		
		log.debug("Web page size: {}", results.data().length());
		log.debug("Web page host: {}", page.getHost());
		//		log.debug("Web page: {}", results.data());
		
		
		//TODO:: detect if actually got to product page, or on some bot blocker page
		switch (page.getHost()) {
			case "www.adafruit.com":
				output = getProdDetailsAdafruit(output, results);
				break;
			case "www.amazon.com":
				output = getProdDetailsAmazon(output, results);
				break;
			default:
				output = output.unifiedName(results.title());
		}
		
		return output.build();
	}
	
	public CompletableFuture<ProductLookupResult> scanWebpage(URL page) {
		return CompletableFuture.supplyAsync(()->{
												 try {
													 return Jsoup.newSession()
																 .followRedirects(true)
																 .url(page)
																 .userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:103" +
																			".0) Gecko/20100101 Firefox/103.0")
																 .get();
												 } catch(IOException e) {
													 throw new RuntimeException(e);
												 }
											 }
		).thenApply((Document doc)->{
			return this.documentToSearchResult(page, doc);
		});
	}
}
