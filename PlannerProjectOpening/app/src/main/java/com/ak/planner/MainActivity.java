package com.ak.planner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    private BottomSheetDialog mBottomSheetDialog; //переменная класса BottomSheetDialog
    private View sheetView;
    private ProgressBar progressBar;
    private String text = null;
    private Button pickfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mBottomSheetDialog = new BottomSheetDialog(MainActivity.this);
            sheetView = getLayoutInflater().inflate(R.layout.bottom_dialog_notification, null);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.setCancelable(false);
            mBottomSheetDialog.show();

            Button iRequest = sheetView.findViewById(R.id.iRequest);
            iRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    {
                        mBottomSheetDialog.setCancelable(true);
                        mBottomSheetDialog.dismiss();
                    }
                }
            });
        }

        //прослушиватель FAB
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //окно выбора файла для редактирования
                mBottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                sheetView = getLayoutInflater().inflate(R.layout.bottom_dialog_constructor_setup, null);
                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();

                final TextView txt = sheetView.findViewById(R.id.Title);

                progressBar = sheetView.findViewById(R.id.progressBar);
                progressBar.setVisibility(ProgressBar.INVISIBLE);

                pickfile = sheetView.findViewById(R.id.pickfile);
                pickfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressBar.setVisibility(ProgressBar.VISIBLE);
                        pickfile.setEnabled(false);

                        performFileSearch();
                    }
                });


            }
        });
    }

    private static final int READ_REQUEST_CODE = 42;

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("text/plain");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri selectedfile = null;
            if (resultData != null) {
                selectedfile = resultData.getData();

            }


            try (InputStream inputStream = getContentResolver().openInputStream(selectedfile);) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        inputStream));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                Log.e("TAG",everything);

                String newStr = everything.replaceAll("[,.!?;:]", "$0 ").replaceAll("\\s+", " ").replaceAll("\\s+(?=\\p{Punct})", "");
                if (everything.contains(newStr))
                    Log.e("YES",everything);
                else Log.e("NO",everything);

                OutputStream outputStream = getContentResolver().openOutputStream(selectedfile);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write(newStr);
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


            mBottomSheetDialog.dismiss();
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
        else
        {
            pickfile.setEnabled(true);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }
}