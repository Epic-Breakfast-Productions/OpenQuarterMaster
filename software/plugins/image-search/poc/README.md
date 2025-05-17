# Technical Proof of Concept

https://github.com/Coletrane315/ImageComparisonProjectFinal

This project takes in a folder of images, along with an input image, uses a TensorFlow model to extract feature data from each, compare them, and determine which images in the folder are most similar to
the input. TensorFlow offers a variety of pre-trained models to use for various purposes, this project uses the ResNet model, which can be downloaded here:
https://www.kaggle.com/models/google/resnet-v2/tensorFlow2/152-feature-vector
This downloaded folder must be placed in the project folder in order to function. The default folder name of "resnet-v2-tensorflow2-152-feature-vector-v2" will be detected.

The variables inputTName and outputTName are specific to this model to function correctly; if another model is implemented, these variables must be altered, a tutorial in determining these values
can be found here (part way down the page, "Details of the SavedModel command line interface"): https://www.tensorflow.org/guide/saved_model 

The variable modelImageSize determines what all images will be resized to. This model recommends 224 x 224; in testing, increasing image size beyond this resulted in less than 1% difference 
in similarity of images, but massivesly increased execution time.

Once the model generates feature data for each image, an ImageData object is created, containing the file path, filename, and an array of the feature data (can be easily modified to include
other properties of the image). Subsequently, each ImageData object is stored in a HashMap, mapping the image's filename to the corresponding ImageData object. This HashMap is then stored in
a JSON file for use on later runs. 

The initial execution on a new folder of images may be quite lengthy in time (20 seconds for a folder of 60 images), but subsequent runs with JSON data already made,
the execution time decreases significantly. 

On each run, if there is existing JSON data (filepath stored in variable jsonFilePath), the program ensures that the HashMap contains a value for each image in the folder, adding new data for any images
that may have been added to the folder between runs. Additionally, the program ensures that the HashMap does not contain data for anything no longer in the folder.

After the folder of images is fully handled, image data must be extracted from the input image that you wish to compare to the folder of images. A Cosine Similarity function is applied
to the input image for each image in the folder, resulting in a score between 0 and 1, storing these images in a TreeMap sorted from highest to lowest similarity scores.

Currently, this project returns the top 10 (can adjust number of returned with variable numSimilar) most similar images, their filename and their similarity score. This information
is currently just printed to the console, but could be easily modified to return this information in another format.

A folder of test images is included, with the program pointing to that folder along with the inputImage pointing to the image "screw.jpg" in that folder.

Run with ` ./gradlew clean run`
