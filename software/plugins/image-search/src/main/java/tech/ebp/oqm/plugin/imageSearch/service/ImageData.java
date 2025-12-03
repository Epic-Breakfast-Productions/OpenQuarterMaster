package tech.ebp.oqm.plugin.imageSearch.service;

//Stores full filepath to the image, name of the image, and the feature vector data
//Easily modifiable to include other image metadata
public class ImageData {
    String filepath;
    String filename;
    float[] imageFeatureVector;

    //This default constructor is necessary for these objects to be serialized into JSON files
    public ImageData(){}

    public ImageData(String filepath, String filename, float[] imageFeatureVector) {
        this.filepath = filepath;
        this.filename = filename;
        this.imageFeatureVector = imageFeatureVector;
    }

    public String getFilepath(){
        return this.filepath;
    }

    public void setFilepath(String filepath){
        this.filepath = filepath;
    }

    public String getFilename(){
        return this.filename;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }

    public float[] getImageFeatureVector(){
        return this.imageFeatureVector;
    }

    public void setImageFeatureVector(float[] imageFeatureVector){
        this.imageFeatureVector = imageFeatureVector;
    }
}
