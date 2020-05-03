package com.example.sdaassign32019johndoe;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.provider.MediaStore.EXTRA_OUTPUT;

/*
 * A simple {@link Fragment} subclass.
 * @author Chris Coughlan 2019
 */
public class OrderTshirt extends Fragment {

    //class wide variables
    private String mPhotoPath;
    private Spinner mSpinner;
    private EditText mCustomerName;
    private EditText meditDelivery;
    private ImageView mCameraImage;
    private CheckBox checkBoxCollect, checkBoxDeliver;
    private Bitmap imageBitmap;

    private boolean customerName, deliveryAddress, daySelected, imageMissing;

    //static keys
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String TAG = "OrderTshirt";

    // static key for getting the thumbnail
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     *
     * @param inflater instantiates the xml file into corresponding view objects
     * @param container collection of view classes
     * @param savedInstanceState Bundle containing state information
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment get the root view.
        final View root = inflater.inflate(R.layout.fragment_order_tshirt, container, false);

        mCustomerName = root.findViewById(R.id.editCustomer);
        meditDelivery = root.findViewById(R.id.editDeliver);
        checkBoxCollect = root.findViewById(R.id.checkBoxCollection);
        checkBoxDeliver = root.findViewById(R.id.checkBoxAddress);

        meditDelivery.setImeOptions(EditorInfo.IME_ACTION_DONE);
        meditDelivery.setRawInputType(InputType.TYPE_CLASS_TEXT);

        mCameraImage = root.findViewById(R.id.imageView);;
        Button mSendButton = root.findViewById(R.id.sendButton);

        //set a listener on the the camera image
        mCameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(v);
            }
        });

        //initialise spinner using the integer array
        mSpinner = root.findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(root.getContext(), R.array.ui_time_entries, R.layout.spinner_days);
        mSpinner.setAdapter(adapter);

        //initial state for deliver/collection disabled to force user to pick one
        meditDelivery.setEnabled(false);

         //to select delivery and to provide address, enable Delivery objects disable Collection objects
        checkBoxDeliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkBoxDeliver.isChecked()) {

                    mSpinner.setSelection(0);
                    mSpinner.setEnabled(false);
                    checkBoxCollect.setChecked(false);
                    meditDelivery.setEnabled(true);
                    checkBoxDeliver.setChecked(true);
                }
            }
        });

        //to select collection and to provide days
        checkBoxCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkBoxDeliver.isChecked()) {

                    checkBoxCollect.setChecked(true);
                    meditDelivery.setText("");
                    meditDelivery.setEnabled(false);
                    checkBoxDeliver.setChecked(false);
                    mSpinner.setEnabled(true);
                }
            }
        });

        //conditions set for not proceeding with sending email
        customerName = mCustomerName.getText().toString().equals("") || mCustomerName == null;
        deliveryAddress = meditDelivery.getText().toString().equals("") || meditDelivery == null;
        daySelected = mSpinner.getSelectedItem().toString().equals("SELECT");
        imageMissing = !(((BitmapDrawable)mCameraImage.getDrawable()).getBitmap() == imageBitmap);

        //set a listener to start the email intent.
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //use a dialog notify about missing details
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.MyDialogTheme);
                builder.setTitle("Notification!").setMessage(getNotificationText()).setPositiveButton("OK", null);

                if (customerName || (deliveryAddress && daySelected) || imageMissing) {
                    builder.show();
                }

                else {

                    sendEmail(v);
                    Log.d(TAG, "sendEmail: should be sending an email with "+createOrderSummary(v));
                }
            }
        });

        return root;
    }

    String currentPhotoPath;
    String imageFileName;

    /**
     * method to save the picture taken under a unique file name
     * (code snippet: https://developer.android.com/training/camera/photobasics)
     *
     * @return a file object for the the photo taken
     * @throws IOException
     */
    public File createImageFile() throws IOException {

        // Create a collision resistant image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";

        //photos to remain private to this app only
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public Uri photoURI;

    /**
     * method to take the picture and save the file
     * updates the photoUri that is accessed by the emai intent
     * (code snippet: https://developer.android.com/training/camera/photobasics)
     * @param v
     */
    private void dispatchTakePictureIntent(View v) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(v.getContext().getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.sdaassign32019johndoe.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //

    /**
     * method to pass back the image to an imageView
     * (code snippet: https://developer.android.com/training/camera/photobasics)
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            mCameraImage.setImageBitmap(imageBitmap);
        }
    }

    /*
     * Returns the Email Body Message, update this to handle either collection or delivery
     */
    private String createOrderSummary(View v)
    {
        String orderMessage = "";
        String deliveryInstruction = meditDelivery.getText().toString();
        String customerName = getString(R.string.customer_name) + " " + mCustomerName.getText().toString();
        orderMessage += customerName + "\n" + "\n" + getString(R.string.order_message_1);

        if(daySelected){
            orderMessage += "\n" + "Deliver my order to the following address: ";
            orderMessage += "\n" + deliveryInstruction;
        }
        else {
            orderMessage += "\n" + getString(R.string.order_message_collect) + mSpinner.getSelectedItem().toString();
        }
        orderMessage += "\n" + getString(R.string.order_message_end) + "\n" + mCustomerName.getText().toString();

        return orderMessage;
    }

    /**
     * email intent with linked extras
     * @param v
     */
    private void sendEmail(View v) {

        //Implicit intent to send an email with no attachment
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        //only messaging app that is capable to send email is opened
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("*/*");

        //linking CallAnActivity email data with the email intent
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "artur.jolsvai@gmail.com");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"my-tshirt@sda.ie"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, createOrderSummary(v));
        emailIntent.putExtra(Intent.EXTRA_STREAM,photoURI);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            //finish();
            Log.i("Finished sending email.", "");
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * method to prepare the text displayed by the AlertDialog
     * @return notification text
     */
    public String getNotificationText(){

        //conditions set for not proceeding with sending email
        customerName = mCustomerName.getText().toString().equals("") || mCustomerName == null;
        deliveryAddress = meditDelivery.getText().toString().equals("") || meditDelivery == null;
        daySelected = mSpinner.getSelectedItem().toString().equals("SELECT");
        imageMissing = !(((BitmapDrawable)mCameraImage.getDrawable()).getBitmap() == imageBitmap);

        //append the alert dialog text if a certain contiditon is not met
        StringBuilder stringBuilder = new StringBuilder();

        if(customerName){
            stringBuilder.append("Please enter your customer name.\n\n");
        }
        if(imageMissing){
            stringBuilder.append("Please take a picture of the print you require on the t-shirt.\n\n");
        }
        if(deliveryAddress && daySelected){
            stringBuilder.append("Please provide your delivery address or the days for collection.\n\n");
        }

        String notificationText = stringBuilder.toString();

    //display the notification text
    return notificationText;
    }
}
