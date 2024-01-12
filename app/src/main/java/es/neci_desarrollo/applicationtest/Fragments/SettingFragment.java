package es.neci_desarrollo.applicationtest.Fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.Store;


public class SettingFragment extends Fragment {

    Uri uri;
    File fileGlob;
    String nocProjectDirInDownload = "noc-project";
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nocProjectDirInDownload;

    SeekBar seekBar;
    Switch WriteNeighbors;
    TextView textView,text,selectedItemPreview;
    EditText login,pass;
    Button button3;
    Button Net;
    Button ch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        WriteNeighbors = view.findViewById(R.id.WriteNeighbors);
        seekBar = view.findViewById(R.id.seekBar);

        login = view.findViewById(R.id.Login_write);
        pass = view.findViewById(R.id.Pass_Write);
        button3 = view.findViewById(R.id.button3);
        Net = view.findViewById(R.id.button);
        text = view.findViewById(R.id.text);
        ch = view.findViewById(R.id.openAlertDialogButton);
        selectedItemPreview = view.findViewById(R.id.selectedItemPreview);
        Net.setVisibility(Store.isAuth ? View.VISIBLE: View.INVISIBLE);
        ch.setVisibility(Store.isAuth ? View.VISIBLE: View.INVISIBLE);
        textView = view.findViewById(R.id.seekBarValue);
        textView.setText("Настройка точности: "+Store.range+"  (м)");
        login.setText(String.valueOf(Store.LastName));
        pass.setText(String.valueOf(Store.Pass));
    button3.setText(Store.isAuth ? "Успешная авторизация": "Авторизация");
        View.OnClickListener writeNeighborsInSore = v -> {
            if (Store.isWriteNeighbors) {
                Store.disableWriteNeighbors();
            } else {
                Store.enableWriteNeighbors();
            }
        };
        WriteNeighbors.setOnClickListener(writeNeighborsInSore);
        seekBar.setProgress(Store.range);



        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Store.LastName = login.getText().toString();
                Store.Pass = pass.getText().toString();


                String url = "http://ss.sut.dchudinov.ru/api/v1/signals";

                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                client.setBasicAuth(Store.LastName, Store.Pass);
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d("UPLOAD_FILE","OK:"+new String(responseBody, StandardCharsets.UTF_8));
                        button3.setText("Успешная авторизация");
                        Net.setVisibility(View.VISIBLE);
                        ch.setVisibility(View.VISIBLE);
                        Store.successAuth();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d("UPLOAD_FILE","ERROR: "+statusCode);
                        if (statusCode == 401)
                        {
                            button3.setText("Ошибка авторизации");
                            Net.setVisibility(View.INVISIBLE);
                            ch.setVisibility(View.INVISIBLE);
                            Store.unsuccessAuth();
                        }
                        else {
                            button3.setText("Успешная авторизация");
                            Net.setVisibility(View.VISIBLE);
                            ch.setVisibility(View.VISIBLE);
                            Store.successAuth();
                        }

                    }
                });
            }
        });

        Net.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Store.LastNameFile == null || Store.LastNameFile == ""){
                    return;
                }
                String url = "http://ss.sut.dchudinov.ru/api/v1/signals";

                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                File file = new File(Store.LastNameFile);
                Log.d("Chekc file",file.toString());
                try {
//                    for(file:selctedFiles){
//                        params.put("main_cell", file);
//                    }
                    params.put("main_cell", file);
//                    params.put("main_cell", file2);
//                    params.put("model", model);
//                    params.put("SOC",String chip = Build.SOC_MODEL);
                    params.put("manufacturer", getDeviceName());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                client.setBasicAuth(Store.LastName, Store.Pass);
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d("UPLOAD_FILE","OK:"+new String(responseBody, StandardCharsets.UTF_8));
                        text.setText("Запись отправлена на сервер!");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d("UPLOAD_FILE","ERROR: "+new String(responseBody, StandardCharsets.UTF_8));
                        text.setText("Ошибка");
                    }
                });
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("Настройка точности: "+seekBar.getProgress()+"  (м)");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Store.setRange(seekBar.getProgress());
            }
        });

        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(intent,requestcode);
            }
        });

        return view;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        Log.d("Man","Model"+manufacturer+"  "+model);

        return manufacturer;
    }








    int requestcode = 1;

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == requestcode && resultCode == Activity.RESULT_OK)
        {
            if (data == null)
                return;
            if (null != data.getClipData())
            {
                String tempstring = "";
                for (int i=0; i<data.getClipData().getItemCount();i++)
                {
                    uri = data.getClipData().getItemAt(i).getUri();
                    tempstring = uri.getPath() + "\n";
                    fileGlob = new File(tempstring);
                }
                selectedItemPreview.setText(fileGlob.getName());
            }
            else {
                Uri uri = data.getData();
                selectedItemPreview.setText(uri.getPath());
                fileGlob = new File(uri.getPath());
                selectedItemPreview.setText(fileGlob.getName());
            }
        }
        Log.d("Check",fileGlob.toString());
    }



}
