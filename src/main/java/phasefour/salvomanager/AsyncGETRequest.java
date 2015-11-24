package phasefour.salvomanager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class AsyncGETRequest extends AsyncTask<String, Void, String> {

    private static String encodedCredentials = "Basic ci5yZXlyaW5rOjY3ODY5NA==";

    private Context mContext;
    private String content;
    public static String TAG = "DEBUGGING";
    public String url;
    private String json = "_";

    public AsyncGETRequest(Context context){

        this.mContext = context;

    }

    protected String doInBackground(String... urls) {

        url = urls[0];
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", encodedCredentials);
        HttpResponse response;

        try {

            response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                content = "OK";
                Log.v(TAG, "Success!");
            } else {
                content = "BAD";
                Log.v(TAG, "Failure...");
            }
            Log.v(TAG, String.valueOf(response.getStatusLine().getStatusCode()) + "___");
            json = EntityUtils.toString(response.getEntity());
            Log.v(TAG, String.valueOf(response.getStatusLine().getStatusCode()));

        }

        catch (Exception e) {

        }

        return content;
    }

    protected void onPostExecute(String content) {
        Log.v(TAG, json);
    }


}