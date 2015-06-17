package byld.hackground;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    String GETFormURL = "http://heathcarerural.appspot.com/addRequest/";
    String POSTFormURL = "http://heathcarerural.appspot.com/addRequest/";
    RelativeLayout myLinearLayout;

    String mCurrentPhotoPath;
    String absolutePhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myLinearLayout = (RelativeLayout) findViewById( R.id.mainLayout );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public List<NameValuePair> collectDataFromForm(){
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        TelephonyManager mngr = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        String imei = mngr.getDeviceId();
        params.add(new BasicNameValuePair("auth", imei));

        EditText field = (EditText) findViewById(R.id.patientName);
        params.add(new BasicNameValuePair("name", field.getText().toString()));

        field = (EditText) findViewById(R.id.patientPhoneNumber);
        params.add(new BasicNameValuePair("phone", field.getText().toString()));

        StringBuilder symptoms = new StringBuilder();
        CheckBox fever = (CheckBox) findViewById(R.id.FeverCheckBox);
        if (fever.isChecked()){
            symptoms.append("Fever,");
        }
        CheckBox cough = (CheckBox) findViewById(R.id.CoughCheckBox);
        if (cough.isChecked()){
            symptoms.append("Cough,");
        }

        CheckBox cold = (CheckBox) findViewById(R.id.ColdCheckBox);
        if (cold.isChecked()){
            symptoms.append("Cold,");
        }

        CheckBox StomachAche = (CheckBox) findViewById(R.id.StomachAcheCheckBox);
        if (StomachAche.isChecked()){
            symptoms.append("StomachAche,");
        }

        EditText otherSymptoms = (EditText) findViewById(R.id.otherSymptoms);
        symptoms.append(otherSymptoms);

        if (symptoms.charAt(symptoms.length()-1) == ','){
            symptoms.deleteCharAt(symptoms.length() - 1);
        }

        params.add(new BasicNameValuePair("symptoms", symptoms.toString()));

        field = (EditText) findViewById(R.id.descriptionText);
        params.add(new BasicNameValuePair("symptoms", field.getText().toString()));

        RadioButton button = (RadioButton) findViewById(R.id.GenderFemale);
        if (button.isChecked()){
            params.add(new BasicNameValuePair("gender", "Female"));
        }
        else{
            params.add(new BasicNameValuePair("gender", "Male"));
        }

        button = (RadioButton) findViewById(R.id.HighPriority);
        if (button.isChecked()){
            params.add(new BasicNameValuePair("priority", "0"));
        }
        else{
            button = (RadioButton) findViewById(R.id.MediumPriority);
            if (button.isChecked()){
                params.add(new BasicNameValuePair("priority", "1"));
            }
            else{
                params.add(new BasicNameValuePair("priority", "2"));
            }
        }

        //params.add(new BasicNameValuePair("image", absolutePhotoPath));

        /*
        params.add(new BasicNameValuePair("auth", "12345555555551"));
        params.add(new BasicNameValuePair("symptoms", "cough,cold,fever,sexiness"));
        params.add(new BasicNameValuePair("name", "bromil"));
        params.add(new BasicNameValuePair("dob", "12/01/1994"));
        params.add(new BasicNameValuePair("description", "Niggas in town"));
        params.add(new BasicNameValuePair("priority", "1"));
        params.add(new BasicNameValuePair("phone", "9654657648"));*/
        return params;
    }

    public void httpButtonPress(View view) {
        //httpSendGET(view);
        Toast toast = Toast.makeText(this, "Patient data has been sent.", Toast.LENGTH_LONG);
        toast.show();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void httpSendPOST(View view) {

        List<NameValuePair> data = collectDataFromForm();
        String SubmitFormURL = POSTFormURL;
        Toast toast = Toast.makeText(this, SubmitFormURL, Toast.LENGTH_LONG);
        toast.show();

        NetworkOps networkOp = new NetworkOps(this);
        networkOp.executeHTTPPOSTRequest(SubmitFormURL, data);
    }

    public void httpSendGET(View view) {

        List<NameValuePair> data = collectDataFromForm();
        String SubmitFormURL = NetworkOps.addParamsToUrl(GETFormURL, data);
        Toast toast = Toast.makeText(this, SubmitFormURL, Toast.LENGTH_LONG);
        toast.show();

        NetworkOps networkOp = new NetworkOps(this);
        networkOp.executeHTTPGetRequest(SubmitFormURL);
    }

    public void takeImage(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView camPhotoView = (ImageView) findViewById(R.id.camPhotoView);
            camPhotoView.setImageBitmap(imageBitmap);
        }
    }

}