package tech.ebp.oqm.plugin.imageSearch.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
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

@Slf4j
@ApplicationScoped
public class ImageSearchService {
	
	/**
	 *
	 * @param query
	 * @return
	 */
	public static SavedModelBundle model;
	public static String inputTensorName = "serving_default_inputs";
	public static String outputTensorName = "StatefulPartitionedCall";
	public static File queryImage;
	public static ImageData queryData;
	public static String imageFolderPath;
	public static File imageFolder;
	public static String jsonPath;
	public static File jsonFile;
	public static int numSimilar;
	public static Size modelImageSize;
	public static HashMap<String, ImageData> jsonMap;
	
	
	public LinkedHashMap<Double, String> search(String query) throws IOException {
		log.info("Searching for query: " + query);
		OpenCV.loadLocally();
		
		model = SavedModelBundle.load("../../../resources/main/models/resnetV2");
		modelImageSize = new Size(500, 500);
		imageFolderPath = "../../../resources/main/testImages";
		imageFolder = new File(imageFolderPath);
		jsonPath = "../../../resources/main/imageData.json";
		jsonFile = generateJson();
		queryImage = new File("../../../resources/main/testImages/Screw.jpg");
		//queryImage = new File(query);
		queryData = getImageData(queryImage);
		numSimilar = jsonMap.size();
		
		if (queryData == null) {
			System.out.println("Query Image could not be processed");
			return null;
		}
		
		TreeMap<Double, String> tree = getSimilarities(queryData);
		int tmpIter = 0;
		for (Map.Entry<Double, String> entry : tree.entrySet()) {
			System.out.println("Filename: " + entry.getValue() + ", Score: " + entry.getKey());
			tmpIter++;
			if (tmpIter > numSimilar) {
				break;
			}
		}
		
		return null;
	}
	
	//Checks if there is a pre-existing json file
	//if so, ensure it perfectly matches the contents of the images folder
	//if not, creates one with image data from all images in folder
	private static File generateJson() throws IOException {
		File jsonF = new File(jsonPath);
		log.info("Generating JSON file: {}", jsonF.getAbsolutePath());
		boolean changesMade;
		if (jsonF.exists() && jsonF.isFile()) {
			fetchJsonData(jsonF);
			changesMade = removeExtras() || addMissing();
		} else {
			jsonMap = new HashMap<>();
			for (File f : Objects.requireNonNull(imageFolder.listFiles())) {
				ImageData obj = getImageData(f);
				if (obj != null) {
					jsonMap.put(f.getName(), obj);
				}
			}
			try {
				jsonF.createNewFile();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			changesMade = true;
		}
		if (changesMade) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(jsonF, jsonMap);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return jsonF;
	}
	
	private static void fetchJsonData(File jsonFileObj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonMap = mapper.readValue(
				jsonFileObj, new TypeReference<HashMap<String, ImageData>>() {
				}
			);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Compares images in hashmap to images in the folder, removes any unnecessary
	//data from the hashmap
	private static boolean removeExtras() {
		ArrayList<String> extraKeys = new ArrayList<>();
		
		for (String cur : jsonMap.keySet()) {
			boolean existsInFolder = false;
			
			for (File f : Objects.requireNonNull(imageFolder.listFiles())) {
				if (cur.equals(f.getName())) {
					existsInFolder = true;
					break;
				}
			}
			
			if (!existsInFolder) {
				extraKeys.add(cur);
			}
		}
		
		for (String extraKey : extraKeys) {
			jsonMap.remove(extraKey);
		}
		
		return !extraKeys.isEmpty();
	}
	
	//Compares images in the hashmap to images in the folder
	//Adds images to the hashmap that were not already there
	private static boolean addMissing() {
		boolean changesMade = false;
		for (File f : Objects.requireNonNull(imageFolder.listFiles())) {
			String name = f.getName();
			if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
				if (!jsonMap.containsKey(name)) {
					ImageData obj = getImageData(f);
					if (obj != null) {
						jsonMap.put(name, obj);
						changesMade = true;
					}
				}
			}
		}
		return changesMade;
	}
	
	//Creates an ImageData object for provided image file
	//including the image path, filename, and the image features obtained from tensorflow
	private static ImageData getImageData(File imageFile) {
		try {
			if (imageFile.exists() && imageFile.isFile()) {
				float[] deepFeatures = extractDeepFeatures(imageFile);
				return new ImageData(imageFile.getAbsolutePath(), imageFile.getName(), deepFeatures);
			} else {
				throw new IOException("File not found: " + imageFile.getAbsolutePath());
			}
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Runs input image through TensorFlow model
	//Returns float array of image feature data
	private static float[] extractDeepFeatures(File imageFile) {
		try (Tensor inputTensor = preprocessImage(imageFile)) {
			try (
				Result outputTensor = model.session().runner()
										  .feed(inputTensorName, inputTensor)
										  .fetch(outputTensorName)
										  .run()
			) {
				return StdArrays.array2dCopyOf((FloatNdArray) outputTensor.get(0))[0];
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Converts image file to matrix, resize, normalize values,
	//convert to tensor type to prepare image for tensorflow model
	private static Tensor preprocessImage(File imageFile) {
		log.info("Preprocessing image: {}", imageFile);
		
		Mat tmp = Imgcodecs.imread(imageFile.getAbsolutePath());
		Imgproc.resize(tmp, tmp, modelImageSize);
		tmp.convertTo(tmp, CvType.CV_32FC3);
		Core.normalize(tmp, tmp, 0, 1, Core.NORM_MINMAX);
		float[] imageData = new float[(int) (tmp.total() * tmp.channels())];
		tmp.get(0, 0, imageData);
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
		log.debug("Done preprocessing image: {}", imageFile);
		return TFloat32.tensorOf(StdArrays.ndCopyOf(newImageData));
	}
	
	//Takes in the ImageData object prepared from the query image
	//Runs the cosineSimilarity function on the query feature vector against
	//every image present in the previously generated jsonData
	//Returns a reverse sorted TreeMap containing the similarity score and image filename
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
