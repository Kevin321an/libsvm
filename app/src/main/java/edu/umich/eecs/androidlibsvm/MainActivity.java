package edu.umich.eecs.androidlibsvm;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

public class MainActivity extends Activity {
    public static final String LOG_TAG = "AndroidLibSvm";

    String appFolderPath;
    String systemPath;
    String outputPath = appFolderPath+"predict ";
    String modelPath;
    // link jni library
    static {
        System.loadLibrary("jnilibsvm");
    }

    // connect the native functions
    private native void jniSvmTrain(String cmd);
    private native void jniSvmPredict(String cmd);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        Log.v("system Path", systemPath);
        appFolderPath = systemPath+"libsvm/";
        Log.v("appPath", appFolderPath);

        // 1. create necessary folder to save model files
        CreateAppFolderIfNeed();
        copyAssetsDataIfNeed();

        // 2. assign model/output paths
        String dataTrainPath = appFolderPath+"heart_scale ";
        String dataPredictPath = appFolderPath+"heart_scale ";
        modelPath = appFolderPath+"model ";
        outputPath = appFolderPath+"predict ";

        // 3. make SVM train
        String svmTrainOptions = "-t 2 ";
        jniSvmTrain(svmTrainOptions+dataTrainPath+modelPath);

        //for scale frist then

        // 4. make SVM predict
        jniSvmPredict(dataPredictPath+modelPath+outputPath);


        Log.v("value from scaleC", Double.toString(scaleCalculation(1.208178869830869)));

        getZone(scaleCalculation(7.738161225497862));



        File file = new File(appFolderPath+"predict");

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        Log.i(LOG_TAG, text.toString());
    }


    /***
     *
     * @param distance the calculated distance from {@link #scaleCalculation(double)}
     * @return zoom number from 1 to 3
     */

    private int getZone(double distance) {

        //File root = Environment.getExternalStorageDirectory();
        File outputFile;
        int zoomNumber = 0;

        String fileName="test";

        File outDir = new File(appFolderPath);
         Writer writer;

        //generate the text
        StringBuilder text = new StringBuilder();
        text.append("1 1:");
        text.append(distance);

        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        try {
            if (!outDir.isDirectory()) {
                throw new IOException("Unable to create directory. Maybe the SD card is mounted?");
            }
            outputFile = new File(outDir, fileName);
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(text.toString());
            Log.v(LOG_TAG, "Report successfully saved to: " + outputFile.getAbsolutePath());
            writer.close();

            //read the file
            //File file = new File(appFolderPath+"predict");

            StringBuilder textRead = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(outputFile));
                String line;

                while ((line = br.readLine()) != null) {
                    textRead.append(line);
                    textRead.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }

            Log.i("readfile output", textRead.toString());

            String dataPredictPath = appFolderPath + "test";
            String modelPath = appFolderPath + "data_model";
            String outputPath = appFolderPath + "predict";

            jniSvmPredict(String.format("%s %s %s", dataPredictPath, modelPath, outputPath));

            zoomNumber = getrange(outputPath);


        } catch (IOException e) {
            Log.w("eztt", e.getMessage(), e);
            Log.v(LOG_TAG, " Unable to write to external storage.");
        }

        //jniSvmPredict(String.format("%s %s %s", appFolderPath, out, outputPath));
        return zoomNumber;
    }


    //return the zoom number
    private int getrange(String outputFile){
        int range = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(outputFile));
            String line;
            if ((line = br.readLine()) != null) {
                range = Integer.parseInt(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        Log.i("range output", Integer.toString(range));
        return range;
    }


    /***
     * get the scale value
     * @param distance getting from Estimo sdk
     * @return
     */

    private double scaleCalculation(double distance){

        //return y_lower + (y_upper - y_lower) * (value - y_min)/(y_max-y_min);
        return 0 + (1 - 0) * (distance - 0.507436303138359) / (7.738161225497862 - 0.507436303138359);
    }


    /*
    * Some utility functions
    * */
    private void CreateAppFolderIfNeed(){
        // 1. create app folder if necessary
        File folder = new File(appFolderPath);

        if (!folder.exists()) {
            folder.mkdir();
            Log.d(LOG_TAG,"Appfolder is not existed, create one");
        } else {
            Log.w(LOG_TAG,"WARN: Appfolder has not been deleted");
        }


    }

    private void copyAssetsDataIfNeed(){
        String assetsToCopy[] = {"heart_scale_predict","heart_scale_train","heart_scale","data_model"};
        //String targetPath[] = {C.systemPath+C.INPUT_FOLDER+C.INPUT_PREFIX+AudioConfigManager.inputConfigTrain+".wav", C.systemPath+C.INPUT_FOLDER+C.INPUT_PREFIX+AudioConfigManager.inputConfigPredict+".wav",C.systemPath+C.INPUT_FOLDER+"SomeoneLikeYouShort.mp3"};

        for(int i=0; i<assetsToCopy.length; i++){
            String from = assetsToCopy[i];
            String to = appFolderPath+from;

            // 1. check if file exist
            File file = new File(to);
            if(file.exists()){
                Log.d(LOG_TAG, "copyAssetsDataIfNeed: file exist, no need to copy:"+from);
            } else {
                // do copy
                boolean copyResult = copyAsset(getAssets(), from, to);
                Log.d(LOG_TAG, "copyAssetsDataIfNeed: copy result = "+copyResult+" of file = "+from);
            }
        }
    }

    private boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "[ERROR]: copyAsset: unable to copy file = "+fromAssetPath);
            return false;
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
