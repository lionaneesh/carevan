package byld.hackground;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Romil on 03/06/2015.
 */
public class NetworkOps {
    final Context viewContext;
    ConnectivityManager connMgr;
    NetworkInfo networkInfo;
    List<NameValuePair> nameValuePairs;

    public NetworkOps(Context context){
        viewContext = context;
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
    }

    public void executeHTTPPOSTRequest(String url, List<NameValuePair> data){
        nameValuePairs = data;
        ConnectivityManager connMgr = (ConnectivityManager)
                viewContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new HTTPGetTask().execute(url);
        } else {
            Toast toast = Toast.makeText(viewContext, "No Internet Connection, Using SMS Gateway.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Double>{

        @Override
        protected Double doInBackground(String... params) {
            post(params[0]);
            return null;
        }

        protected void onPostExecute(Double result){
            Toast.makeText(viewContext, "command sent", Toast.LENGTH_LONG).show();
        }
    }

    public void post(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(url);
        /*try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addTextBody(USER_ID, userId);
            entityBuilder.addTextBody(NAME, name);
            entityBuilder.addTextBody(TYPE, type);
            entityBuilder.addTextBody(COMMENT, comment);
            entityBuilder.addTextBody(LATITUDE, String.valueOf(User.Latitude));
            entityBuilder.addTextBody(LONGITUDE, String.valueOf(User.Longitude));

            if(file != null)
            {
                entityBuilder.addBinaryBody("someName", file, ContentType.create("image/jpeg"), file.getName());
            }

            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            result = EntityUtils.toString(httpEntity);
            Log.v("result", result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
*/
        try {
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            for(int index=0; index < nameValuePairs.size(); index++) {
                if(nameValuePairs.get(index).getName().equalsIgnoreCase("image")) {
                    // If the key equals to "image", we use FileBody to transfer the data
                    entity.addPart("file", new FileBody(new File(nameValuePairs.get(index).getValue())));
                } else {
                    // Normal string data
                    entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
                }
            }

            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost, localContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void executeHTTPGetRequest(String url){
        ConnectivityManager connMgr = (ConnectivityManager)
                viewContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new HTTPGetTask().execute(url);
        } else {
            Toast toast = Toast.makeText(viewContext, "No Internet Connection, Using SMS Gateway.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class HTTPGetTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog = new ProgressDialog(viewContext);

        private HTTPGetTask() {
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Toast toast = Toast.makeText(viewContext, result, Toast.LENGTH_LONG);
            toast.show();
            toast = Toast.makeText(viewContext, "Posted", Toast.LENGTH_SHORT);
            toast.show();
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("Hackathon", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }


    public static String addParamsToUrl(String url, List<NameValuePair> params){
        if(!url.endsWith("?"))
            url += "?";

        //params.add(new BasicNameValuePair("user", agent.uniqueId));

        String paramString = URLEncodedUtils.format(params, "utf-8");

        url += paramString;
        return url;
    }
}
