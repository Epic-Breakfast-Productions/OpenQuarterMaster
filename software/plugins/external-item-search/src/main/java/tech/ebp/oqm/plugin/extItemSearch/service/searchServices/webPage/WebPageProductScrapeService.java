package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.webPage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class WebPageProductScrapeService extends ItemSearchService {
	
	protected abstract ExtItemLookupResult scrapePageContent(Document webPageContent);
	
	
	public abstract List<String> supportedHosts();
	
	public Optional<CompletableFuture<ExtItemLookupResult>> scrapeWebPage(URL url){
		if(!this.isEnabled()){
			return Optional.empty();
		}
		CompletionStage<ExtItemLookupResult> stage = this.performWebPageScrape(url);
		
		return Optional.of(stage.toCompletableFuture());
	}
	
	protected CompletionStage<ExtItemLookupResult> performWebPageScrape(URL url){
		return CompletableFuture.supplyAsync(()->{
												 try {
													 return this.getPageContent(url);
												 } catch(IOException e) {
													 throw new RuntimeException(e);
												 }
											 }
		).thenApply(this::scrapePageContent);
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public boolean canParsePage(URL url){
		return this.supportedHosts().contains(url.getHost());
	}
	
	protected Document getPageContent(URL url) throws IOException {
		return Jsoup.newSession()
			 .followRedirects(true)
			 .url(url)
			 .userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:103" +
						".0) Gecko/20100101 Firefox/103.0")
			 .get();
	}
	
	protected ExtItemLookupResult.Builder<?, ?> getInitialBuilder(Document webPageContent){
		ExtItemLookupResult.Builder<?, ?> output = ExtItemLookupResult.builder();
		
		output.source(webPageContent.connection().request().url().getHost());
		
		return output;
	}
}
