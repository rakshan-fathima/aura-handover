package com.example.afinal;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import joinery.*;

import android.content.Context;
import android.util.Log;


public class CSVReader {

    Context context;

    String file_name;



    public CSVReader(Context context, String file_name) {
        this.context = context;
        this.file_name = file_name;

    }

    public DataFrame<Object> ReadCSV() throws IOException {



        FileInputStream fis = context.openFileInput(file_name);

      //  InputStream is = context.getAssets().open(file_name);

        InputStreamReader isr = new InputStreamReader(fis);

        BufferedReader br = new BufferedReader(isr);

        String line;

        String cvsSplitBy = "\n";

        br.readLine();



        DataFrame<Object> df = new DataFrame<>("AID","TimeStamp");




        while ((line = br.readLine()) != null) {

            String[] row = line.split(cvsSplitBy);

            for (int i = 0 ; i< row.length; i++) {

                //splitting to create columns
                String[] list = row[i].split(",");
               // Log.d("CSV", list[0]);
               // Log.d("CSV-1", list[1]);
               // Log.d("CSV-2", list[2]);
                //adding columns
                df.append(Arrays.asList(list[0],list[2]));


            }

        }

        return df;

    }

}