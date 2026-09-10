package tech.ebp.oqm.plugin.imageSearch.service.imageSearch;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.net.URL;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Result;
import org.tensorflow.Tensor;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;

import java.io.IOException;
import java.util.*;

import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.plugin.imageSearch.model.Model;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageFinding;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;
import tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers.ResnetProvider;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

@Slf4j
@ApplicationScoped
public class ImageSearchService {

	@Inject
	ResnetProvider resnetProvider;

	/**
	 *
	 * @param query
	 * @return
	 */
	@WithSpan
	public SearchResults search(ImageSearch query) throws IOException {
		log.info("Searching for query: {}", query);

		SearchResults output = new SearchResults(query.maxResults);

		if(query.models.isEmpty() || query.models.contains(Model.RESNET_v2)) {
			this.resnetProvider.search(query, output);
		}

		return output;
	}
}
