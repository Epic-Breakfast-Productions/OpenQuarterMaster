package tech.ebp.oqm.plugin.imageSearch.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.core.type.TypeReference;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

@Slf4j
@ApplicationScoped
public class ImageSearchService {
	
	private static final String RESNET_V2_MODEL_PATH = "models/resnetV2";
	public static final String inputTensorName = "serving_default_inputs";
	public static final String outputTensorName = "StatefulPartitionedCall";
	private static final URL dir = ImageSearchService.class.getClassLoader().getResource(RESNET_V2_MODEL_PATH);

	public static final Size modelImageSize = new Size(500, 500);
	public static String imageFolderPath = "./dev/testImages";
	public static File imageFolder = new File(imageFolderPath);
	public static ImageData queryData;
	public static int numSimilar;
	public static HashMap<String, ImageData> jsonMap;
	public static final SavedModelBundle model;

	static {
		log.info("Loading OpenCV");
		OpenCV.loadLocally();
		log.info("OpenCV loaded");
		log.info("Loading ResNet Model: {}", dir);
		log.debug("Passing in: {}", dir.getFile());
		model = SavedModelBundle.load(dir.getFile().substring(1)); //Don't commit
	}


	@RestClient
	OqmCoreApiClientService coreApiClient;
	
	@Inject
	ResnetVectorService resnetVectorService;
	
	/**
	 *
	 * @param query
	 * @return
	 */



	public Map<Double, String> search(String query) throws IOException {
		log.info("Searching for query: " + query);
		
		File queryImage = new File("./dev/testImages/" + query);
		
		if (!queryImage.exists() || !queryImage.isFile()) {
			log.warn("Query Image does not exist: {}", queryImage.getAbsolutePath());
			return null;
		}
		
		
		//queryImage = new File(query);
		queryData = null; //TODO
		
		TreeMap<Double, String> tree = getSimilarities(queryData);
		int tmpIter = 0;
		for (Map.Entry<Double, String> entry : tree.entrySet()) {
			log.info("Filename: {}, Score: {}", entry.getValue(), entry.getKey());
			tmpIter++;
			if (tmpIter > numSimilar) {
				break;
			}
		}
		
		return tree;
	}

	//Creates an ImageData object for provided image file
	//including the image path, filename, and the image features obtained from tensorflow
	
	//get rid of ImageData class, just push float[] to mongo
	
	//Runs input image through TensorFlow model
	//Returns float array of image feature data
	
	
	/**
	 * Method for generating a feature vector from image data.
	 * <p>
	 * TODO:: validate/ integrate with rest
	 *
	 * @param imageBytes The bytes of image data
	 *
	 * @return The processes image feature vector
	 */
	public static float[] generateImageFeatureVector(byte[] imageBytes) {
		
		//TODO:: need to release all `Mat` objects
		// Release temporary buffer.
		try (
				Tensor inputTensor = preprocessImage(imageBytes);
				Result outputTensor = model.session().runner()
						.feed(inputTensorName, inputTensor)
						.fetch(outputTensorName)
						.run()
		) {
			return StdArrays.array2dCopyOf((FloatNdArray) outputTensor.get(0))[0];
		} catch(Exception e) {
				e.printStackTrace(); //better logging and exception
				return null;
		}
	}
	
	public static float[] generateImageFeatureVector(InputStream imageStream) throws IOException {
		return generateImageFeatureVector(imageStream.readAllBytes());
	}
	
	//Converts image file to matrix, resize, normalize values,
	//convert to tensor type to prepare image for tensorflow model
	public static Tensor preprocessImage(byte[] imageBytes) {
		MatOfByte matOfBytes= new MatOfByte(imageBytes);
		Mat mat = Imgcodecs.imdecode(matOfBytes, Imgcodecs.IMREAD_UNCHANGED);

		//TODO:: Mat handling and buffers empty and release
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
		float[][][][] newImageData = new float[1][heightVal][widthVal][3];
		for (int i = 0; i < heightVal; i++) {
			for (int j = 0; j < widthVal; j++) {
				for (int k = 0; k < 3; k++) {
					newImageData[0][i][j][k] = imageData[oldIter];
					oldIter++;
				}
			}
		}
		return TFloat32.tensorOf(StdArrays.ndCopyOf(newImageData));
	}
	
	//Takes in the ImageData object prepared from the query image
	//Runs the cosineSimilarity function on the query feature vector against
	//every image present in the previously generated jsonData
	//Returns a reverse sorted TreeMap containing the similarity score and image filename
	//TODO: Change JSONMap to ResnetDB
	private static TreeMap<Double, String> getSimilarities(ImageData queryObject) {
		log.info("Getting similarities for query.");
		float[] queryFeatures = queryData.getImageFeatureVector();
		TreeMap<Double, String> similarityMap = new TreeMap<>(Collections.reverseOrder());
		for (ImageData curData : jsonMap.values()) {
			float[] curFeatures = curData.getImageFeatureVector();
			double simScore = cosineSimilarity(queryFeatures, curFeatures);
			similarityMap.put(simScore, curData.getFilename());
		}
		log.debug("Done getting similarities for query.");
		return similarityMap;
	}
	
	//Converts the two feature vector arrays to Mat, performs cosine similarity
	//Returns a similarity score between 0 and 1
	private static double cosineSimilarity(float[] img1, float[] img2) {
		Mat img1Mat = convertFloatArrtoMat(img1);
		Mat img2Mat = convertFloatArrtoMat(img2);
		double dotProd = img1Mat.dot(img2Mat);
		double mag1 = Core.norm(img1Mat, Core.NORM_L2);
		double mag2 = Core.norm(img2Mat, Core.NORM_L2);
		if (mag1 != 0.0 && mag2 != 0.0) {
			return ((dotProd / (mag1 * mag2)) + 1.0) / 2.0;
		}
		return 0.0;
	}
	
	//Converts the float[] type image feature vectors to type Mat from the OpenCV
	//library, easier and more efficient math
	private static Mat convertFloatArrtoMat(float[] arr) {
		Mat newMat = new Mat(1, arr.length, CvType.CV_32F);
		newMat.put(0, 0, arr);
		return newMat;
	}
}
