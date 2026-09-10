package tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;
import tech.ebp.oqm.plugin.imageSearch.model.Model;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageFinding;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;
import tech.ebp.oqm.plugin.imageSearch.service.imageSearch.ImageSearchService;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

@Slf4j
@ApplicationScoped
public class ResnetProvider extends ImageSearchProvider {
	private static final String RESNET_V2_MODEL_PATH = "models/resnetV2";
	public static final String inputTensorName = "serving_default_inputs";
	public static final String outputTensorName = "StatefulPartitionedCall";
	private static final URL dir = ImageSearchService.class.getClassLoader().getResource(RESNET_V2_MODEL_PATH);
	public static final Size modelImageSize = new Size(500, 500);

	static {
		log.info("Loading OpenCV");
		OpenCV.loadLocally();
		log.info("OpenCV loaded");
		//model = SavedModelBundle.load(dir.getFile().substring(1)); //Don't commit
	}

	@Inject
	ResnetVectorService resnetVectorService;

	@Inject
	MeterRegistry registry;

	private SavedModelBundle model;

	@PostConstruct
	public void setUp() {
		log.info("Loading ResNet Model: {}", dir);
		log.debug("Passing in: {}", dir.getFile());
		this.model = SavedModelBundle.load(dir.getFile());
	}

	/**
	 * Method for generating a feature vector from image data.
	 * <p>
	 * TODO:: validate/ integrate with rest
	 *
	 * @param imageBytes The bytes of image data
	 *
	 * @return The processes image feature vector
	 */
	@WithSpan
	public float[] generateImageFeatureVector(byte[] imageBytes) {
		//TODO:: need to release all `Mat` objects
		// Release temporary buffer.
		try (
			Tensor inputTensor = preprocessImage(imageBytes);
			Result outputTensor = model.session().runner().feed(inputTensorName, inputTensor).fetch(outputTensorName).run()
		) {
			return StdArrays.array2dCopyOf((FloatNdArray) outputTensor.get(0))[0];
		} catch(Exception e) {
			log.error("FAILED to build image feature vector: ", e);
			throw e;
		}
	}

	public float[] generateImageFeatureVector(InputStream imageStream) throws IOException {
		return this.generateImageFeatureVector(imageStream.readAllBytes());
	}

	/**
	 Converts image file to matrix, resize, normalize values,
	 convert to tensor type to prepare image for tensorflow model
	 */
	public static Tensor preprocessImage(byte[] imageBytes) {
		MatOfByte matOfBytes = new MatOfByte(imageBytes);
		Mat mat = Imgcodecs.imdecode(matOfBytes, Imgcodecs.IMREAD_UNCHANGED);

		//TODO:: Mat handling and buffers empty and release
		float[][][][] newImageData;
		try {
			if (mat.empty()) {//invalid image / unable to process image
				throw new RuntimeException("Failed to decode image!");
			}
			Imgproc.resize(mat, mat, modelImageSize);
			mat.convertTo(mat, CvType.CV_32FC3);
			Core.normalize(mat, mat, 0, 1, Core.NORM_MINMAX);
			float[] imageData = new float[(int) (mat.total() * mat.channels())];
			mat.get(0, 0, imageData);
			int heightVal = (int) modelImageSize.height;
			int widthVal = (int) modelImageSize.width;

			int oldIter = 0;
			newImageData = new float[1][heightVal][widthVal][3];
			for (int i = 0; i < heightVal; i++) {
				for (int j = 0; j < widthVal; j++) {
					for (int k = 0; k < 3; k++) {
						newImageData[0][i][j][k] = imageData[oldIter];
						oldIter++;
					}
				}
			}
		} finally {
			matOfBytes.release();
			mat.release();
		}

		return TFloat32.tensorOf(StdArrays.ndCopyOf(newImageData));
	}

	/**
	 Converts the two feature vector arrays to Mat, performs cosine similarity
	 Returns a similarity score between 0 and 1
	 */
	private static double cosineSimilarity(float[] img1, float[] img2) {
		Mat img1Mat = convertFloatArrtoMat(img1);
		Mat img2Mat = convertFloatArrtoMat(img2);

		try {
			double dotProd = img1Mat.dot(img2Mat);
			double mag1 = Core.norm(img1Mat, Core.NORM_L2);
			double mag2 = Core.norm(img2Mat, Core.NORM_L2);
			if (mag1 != 0.0 && mag2 != 0.0) {
				return ((dotProd / (mag1 * mag2)) + 1.0) / 2.0;
			}
			return 0.0;
		} finally {
			img1Mat.release();
			img2Mat.release();
		}
	}

	/**
	 Converts the float[] type image feature vectors to type Mat from the OpenCV
	 library, easier and more efficient math
	 */
	private static Mat convertFloatArrtoMat(float[] arr) {
		Mat newMat = new Mat(1, arr.length, CvType.CV_32F);
		newMat.put(0, 0, arr);
		return newMat;
	}

	@Override
	public void search(ImageSearch query, SearchResults output) {
		log.info("Getting similarities for query");

		float[] queryFeatures;
		try {
			queryFeatures = this.generateImageFeatureVector(query.file);
		} catch(IOException e) {
			throw new RuntimeException("Failed to get features for input file.", e);
		}

		long numComparisons = 0;
		for (Iterator<ImageVector> it = this.resnetVectorService.getAllVectors(query.oqmDbIdOrName); it.hasNext(); ) {
			ImageVector curData = it.next();
			numComparisons++;
			log.trace("Processing image comparison with image: {}", curData.getImageId());
			double simScore = cosineSimilarity(queryFeatures, curData.getVector());

			output.add(
				ImageFinding.builder()
					.model(Model.RESNET_v2)
					.itemId(curData.getImageId())
					.imageId(curData.getImageId())
					.score(simScore)
					.build()
			);

			log.trace("Done processing image comparison with image: {}", curData.getImageId());
		}

		log.info("Done getting similarities for query. Comparisons: {}", numComparisons);
	}
}
