package com.example.languagetranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner toSpinner, fromSpinner;
    private TextInputEditText txtSrc;
    private ImageView mic;
    private MaterialButton btnTranslate;
    private TextView txtTranslate;
    String[] fromLanguage = {"From", "Hindi", "English", "Tamil", "Telugu", "Bengali", "Marathi"};
    String[] toLanguage = {"To", "Hindi", "English", "Tamil", "Telugu", "Bengali", "Marathi"};
    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, toLanguageCode, fromLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner = findViewById(R.id.fromSpinner);
        toSpinner = findViewById(R.id.toSpinner);
        txtSrc = findViewById(R.id.edtSrc);
        mic = findViewById(R.id.vMic);
        btnTranslate = findViewById(R.id.translateButton);
        txtTranslate = findViewById(R.id.translatedText);
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode=getLanguageCode(toLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toAdapter=new ArrayAdapter(this,R.layout.spinner_item,toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        btnTranslate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                txtTranslate.setText("");
                if(txtSrc.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter the message",Toast.LENGTH_SHORT).show();
                }else if(fromLanguageCode==0){
                    Toast.makeText(MainActivity.this,"Please enter the source of language",Toast.LENGTH_SHORT).show();
                }else if(toLanguageCode==0){
                    Toast.makeText(MainActivity.this,"Please enter the language to translate the text",Toast.LENGTH_SHORT).show();
                }else{
                    translateText(fromLanguageCode,toLanguageCode,txtSrc.getText().toString());
                }
            }
        });
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try{
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE && data!=null){
            ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            txtSrc.setText(result.get(0));
        }
    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String src){
        txtTranslate.setText("Downloading Model...");
        FirebaseTranslatorOptions options=new FirebaseTranslatorOptions.Builder().setSourceLanguage(fromLanguageCode).setTargetLanguage(toLanguageCode).build();
        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions=new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                txtTranslate.setText("Translating...");
                translator.translate(src).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        txtTranslate.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Fail to translate: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Fail to download translator: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }


    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "Hindi":
                languageCode= FirebaseTranslateLanguage.HI;
                break;
            case "English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
            case "Tamil":
                languageCode= FirebaseTranslateLanguage.TA;
                break;
            case "Telugu":
                languageCode= FirebaseTranslateLanguage.TE;
                break;
            case "Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;
            case "Marathi":
                languageCode= FirebaseTranslateLanguage.MR;
                break;
            default:
                languageCode=0;
        }
        return languageCode;
    }
}