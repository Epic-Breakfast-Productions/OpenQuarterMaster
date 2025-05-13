import com.fasterxml.jackson.core.type.TypeReference;
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

public class ImageComparisonProjectFinal {

    public static String inputImagePath; //path desired image to search for
    public static File inputImage;
    public static String imageFolderPath; //path to folder of images to compare to input
    public static File imageFolder;
    public static SavedModelBundle model; //tensorflow model to compare features
    public static int numSimilar; //number of similar images you want to receive
    //See README file for information on the next two values
    public static String inputTName; //specific name needed to pass to the model
    public static String outputTName; //specific name needed to pass to the model
    public static Size modelImageSize; //width and height that each image will be resized to
    public static String jsonFilePath; //Path to the json file containing image data
    public static File jsonFile;
    public static HashMap<String, ImageData> jsonDataMap; //Structure where the json file data will be stored


    public static void main(String[] args) throws IOException {
        OpenCV.loadLocally();
        inputImagePath = "testImages/screw.jpg";
        inputImage = new File(inputImagePath);
        imageFolderPath = "testImages";
        imageFolder = new File(imageFolderPath);
        model = SavedModelBundle.load("resnet-v2-tensorflow2-152-feature-vector-v2");
        inputTName = "serving_default_inputs";
        outputTName = "StatefulPartitionedCall";
        numSimilar = 10;
        modelImageSize = new Size(224, 224);
        jsonFilePath = "testImages/imageData.json";
        jsonFile = generateJson();
        ImageData inputData = getImageData(inputImage);
        if(inputData != null){
            TreeMap<Double, String> tree = getSimilarities(inputData);
            int tmpIter = 0;
            for(Map.Entry<Double, String> entry : tree.entrySet()) {
                System.out.println("Filename: " + entry.getValue() + ", Score: " + entry.getKey());
                tmpIter++;
                if(tmpIter > numSimilar){
                    break;
                }
            }
        }else{
            System.out.println("Input image not found");
        }
    }

    //Ensures the JSON file contains a HashMap of all images in the folder, no more, no fewer
    //Returns this file
    private static File generateJson() throws IOException {
        File jsonF = new File(jsonFilePath);
        boolean changesMade; //Tracks if any modifications are made to the JSON file
        //If file already exists at provided path
        if(jsonF.exists() && jsonF.isFile()){
            //Recreates the HashMap from the JSON file
            fetchJSONData();
            //Removes extra image data and adds missing image data
            changesMade = removeExtras() || addMissing();
            //If the JSON file does not already exist
        }else{
            jsonDataMap = new HashMap<>();
            //Adds image data for all images in folder to the hashmap
            for(File f : Objects.requireNonNull(imageFolder.listFiles())){
                ImageData obj = getImageData(f);
                if(obj != null){
                    jsonDataMap.put(f.getName(), obj);
                }
            }
            try{
                jsonF.createNewFile();
            }catch (IOException e){
                throw new RuntimeException(e);
            }
            changesMade = true;
        }
        if(changesMade) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(jsonF, jsonDataMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonF;
    }

    //Reads data from JSON file, restructures data back into HashMap and stores
    //in variable jsonDataMap
    private static void fetchJSONData(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            jsonDataMap = mapper.readValue(new File(jsonFilePath), new TypeReference<HashMap<String, ImageData>>() {});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Compares data in the hashmap to the images currently in the folder
    //Removes hashmap data for images no longer in the folder
    //Returns true if any modifications were made to the hashmap
    private static boolean removeExtras(){
        ArrayList<String> extraKeys = new ArrayList<>();
        for(String cur : jsonDataMap.keySet()){
            boolean existsInFolder = false;
            for(File f : Objects.requireNonNull(imageFolder.listFiles())) {
                if(cur.equals(f.getName())){
                    existsInFolder = true;
                    break;
                }
            }
            if(!existsInFolder){
                extraKeys.add(cur);
            }
        }
        for(String extraKey : extraKeys){
            jsonDataMap.remove(extraKey);
        }
        return !extraKeys.isEmpty();
    }

    //Checks if there are any images in the folder without corresponding hashmap data
    //If so, creates it and stores in hashmap
    //Returns true if any modifications were made to the hashmap
    private static boolean addMissing() {
        boolean changesMade = false;
        for(File f : Objects.requireNonNull(imageFolder.listFiles())) {
            String n = f.getName();
            if(n.endsWith("jpg") || n.endsWith("jpeg") || n.endsWith("png")) {
                if (jsonDataMap.get(f.getName()) == null) {
                    ImageData obj = getImageData(f);
                    if(obj != null){
                        jsonDataMap.put(f.getName(), obj);
                        changesMade = true;
                    }
                }
            }
        }
        return changesMade;
    }

    //Takes in the ImageData object for the query image, along with the json file object
    //Returns a TreeMap that is sorted by similarity score highest to lowest
    private static TreeMap<Double, String> getSimilarities(ImageData inputObject) {
        float[] inputFeatures = inputObject.getImageFeatureVector();
        TreeMap<Double, String> similarityMap = new TreeMap<>(Collections.reverseOrder());
        for(ImageData imageData : jsonDataMap.values()){
            float[] compFeatures = imageData.getImageFeatureVector();
            double simScore = cosineSimilarity(inputFeatures, compFeatures);
            similarityMap.put(simScore, imageData.getFilename());
        }
        return similarityMap;
    }

    //Takes in image file, get feature data from image
    //Returns ImageData object or null if file not found
    private static ImageData getImageData(File imageFile){
        try {
            if (imageFile.exists() && imageFile.isFile()) {
                float[] deepFeatures = extractDeepFeatures(imageFile);
                return new ImageData(imageFile.getAbsolutePath(), imageFile.getName(), deepFeatures);
            } else {
                throw new IOException("File not found");
            }
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    //Uses tensorflow model to get feature vectors for given image
    //Returns float array of the feature data for the provided image
    private static float[] extractDeepFeatures(File imageData) {
        try (Tensor inputTensor = preprocessImage(imageData)){
            try (Result outputTensor = model.session().runner()
                    .feed(inputTName, inputTensor)
                    .fetch(outputTName)
                    .run()) {
                return StdArrays.array2dCopyOf((FloatNdArray) outputTensor.get(0))[0];
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Converts array of floats to type of OpenCV Mat, needed for image comparison
    private static Mat convertFloatArrtoMat(float[] floatArr){
        Mat newMat = new Mat(1, floatArr.length, CvType.CV_32F);
        newMat.put(0, 0, floatArr);
        return newMat;
    }

    //Convert image file to matrix, resize, normalize values, convert to tensor
    private static Tensor preprocessImage(File imgFile){
        Mat tmp = Imgcodecs.imread(imgFile.getAbsolutePath());
        Imgproc.resize(tmp, tmp, modelImageSize);
        tmp.convertTo(tmp, CvType.CV_32FC3); // Convert to float32
        Core.normalize(tmp, tmp, 0, 1, Core.NORM_MINMAX);
        float[] imgData = new float[(int) (tmp.total() * tmp.channels())];
        tmp.get(0, 0, imgData);
        int heightVal = (int) modelImageSize.height;
        int widthVal = (int) modelImageSize.width;

        //The method to convert float[] to type Tensor is currently deprecated
        //This series of loops restructures the 1-dimensional image data into 4-dimensional data
        //that can be converted to Tensor
        int oldIter = 0;
        float[][][][] newImageData = new float[1][heightVal][widthVal][3];
        for(int i = 0; i < heightVal; i++){
            for(int j = 0; j < widthVal; j++){
                for(int k = 0; k < 3; k++){
                    newImageData[0][i][j][k] = imgData[oldIter];
                    oldIter++;
                }
            }
        }
        return TFloat32.tensorOf(StdArrays.ndCopyOf(newImageData));
    }

    //calculates cosine similarity of two matrices
    //normally in range -1 to 1, shifted to be 0-1
    private static double cosineSimilarity(float[] img1, float[] img2) {
        Mat img1Mat = convertFloatArrtoMat(img1);
        Mat img2Mat = convertFloatArrtoMat(img2);
        double dotProd =  img1Mat.dot(img2Mat);
        double mag1 =  Core.norm(img1Mat, Core.NORM_L2);
        double mag2 =  Core.norm(img2Mat, Core.NORM_L2);
        if(mag1 != 0.0 && mag2 != 0.0){
            return ((dotProd / (mag1 * mag2)) + 1.0) / 2.0;
        }
        return 0.0;
    }
}