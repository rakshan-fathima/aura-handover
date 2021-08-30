package com.example.afinal;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONException;
import org.json.JSONObject;

import joinery.DataFrame;


public class MainActivity extends AppCompatActivity {


    public static final String FILE_NAME = "AID.csv";

    //timestamp
    public static String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd , HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date
            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getEndpointList();
        new Mytask().execute();
        ListView lv = (ListView) findViewById(R.id.list);


        ArrayAdapter arrayadapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, al);
        lv.setAdapter(arrayadapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                logaid(i);
            }
        });

        getEndpointList();
    }

    ArrayList<String> al = new ArrayList<>();
    //parsing json
    private void getEndpointList() {
        try {
            ListView lv = findViewById(R.id.list);
            ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, al);
            lv.setAdapter(aa);
            //getting json from assets
            String jsonLoc = readJSONFromAsset();
            JSONObject jsonObj = new JSONObject(jsonLoc);
            JSONObject endpoints = jsonObj.getJSONObject("endpoints");
            Iterator<?> keys = endpoints.keys();
            al.clear();
            while (keys.hasNext()) {
                String keys1 = (String) keys.next();

                // if jsonobject keys has another object in endpoint make another object
                if (endpoints.get(keys1) instanceof JSONObject) {
                    // then create another object with its own keys
                    JSONObject jsonobj2 = endpoints.getJSONObject(keys1);

                    String id = jsonobj2.getString("id");
                    String name = jsonobj2.getString("name");
                    int floor = jsonobj2.getInt("floor");
                    String roomname = jsonobj2.getString("roomname");
                    String manufacturer = jsonobj2.getString("manufacturer");
                    String model = jsonobj2.getString("model");
                    String type = jsonobj2.getString("type");
                    String owner = jsonobj2.getString("owner");
                    int permission = jsonobj2.getInt("permissions");
                    String master = jsonobj2.getString("master");


                    Log.i("message", name);
                    al.add(id);
                    int position = aa.getPosition(id);
                    aa.notifyDataSetChanged();

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JsonParser", "Exception", e);
        }


    }

    //reading json
    public String readJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("jsonUI.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void logaid(int pos) {

        try {

            OutputStreamWriter out = new OutputStreamWriter(openFileOutput("AID.csv", MODE_APPEND));
            out.write(al.get(pos) + "," + getCurrentTimeStamp() + "\n");
            out.close();
            //popup that shows file path
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME,
                    Toast.LENGTH_LONG).show();
                readText();


            new Mytask().doInBackground();
            new Mytask().dataframes(al.get(pos));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void readText() {
        try {
            File file = new File(getFilesDir() + "/AID.csv");
            Log.d("filepath" , String.valueOf(getFilesDir()));
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            fr.close();

            Log.d("Read Text", sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    class Mytask extends AsyncTask<Void, Void, Void> {
        DataFrame<Object> df1 = new DataFrame<>("AID", "TimeStamp");


        @Override
        public Void doInBackground(Void... params) {
            CSVReader csvreader = new CSVReader(MainActivity.this, "AID.csv");

            try {
                //accessing reader class
                df1 = csvreader.ReadCSV();
                Log.d("df1", String.valueOf(df1));
                //array that contains unique AID
                DataFrame<Object> array = df1.unique("AID");
                Log.d("array", String.valueOf(array));
                Log.d("arraylength", String.valueOf(array.length()));


                for (int i = 0; i < array.length();i++ ) {

                    Log.d("ARRAY",(String)array.get(i, "AID"));


                    DataFrame<Object> dfsingle = dataframes((String)array.get(i, "AID"));
                    Log.d("df_single", String.valueOf(dfsingle));
                    timeframes(dfsingle);


                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        //this method is used to create unique dataframes wrt to the AID and calculates the seconds from each row.
        private DataFrame<Object> dataframes(String aid) {
            DataFrame<Object> df_new1 = new DataFrame<>("AID","TimeStamp","TimeDifference");

            for (int i = 0; i < df1.length(); i++) {
                String AID = (String) df1.get(i,"AID");


                if (AID.equals(aid)) {

                    String[] tm = ((String) df1.get(i,"TimeStamp")).split(" ");
                    String[] split =  tm[1].split(":");
                    int seconds = ((Integer.parseInt(split[0]))*60*60) + ((Integer.parseInt(split[1]))*(60))+ (Integer.parseInt(split[2]));


                    df_new1.append(Arrays.asList(df1.get(i,"AID"),df1.get(i,"TimeStamp"),seconds));
                }

            }

            return df_new1;
        }

        // this method is used to find the time difference in consecutive rows.
        private void timeframes(DataFrame<Object> dt) {
            DataFrame<Object> df_new2 = new DataFrame<>("AID","TimeStamp","TimeDifference");


            if(dt.length() > 1) {
                List<Object> ts = dt.col("TimeDifference");

                df_new2.append(Arrays.asList(dt.get(0,"AID"),dt.get(0,"TimeStamp"),0));
                for (int i = 1; i < ts.size(); i++) {


                    ts.indexOf(i - 1);
                    int index;
                    //Casting is done in order to find time difference.
                    index = ((Integer) ts.get(i)) - ((Integer)ts.get(i-1));

                    //time difference value is appended in its respective column.
                    df_new2.append(Arrays.asList(dt.get(i,"AID"), dt.get(i,"TimeStamp"),index));

                }

                dataprocessing(df_new2);
            }

        }



        // this method carries out the statistical calculations required in making appropriate assumptions towards user behaviour.
        private void dataprocessing(DataFrame<Object> timeframe) {
            new DataFrame<>("AID", "TimeStamp", "TimeDifference");
            new DataFrame<>();
            DataFrame<Object> cluster;
            new DataFrame<>();
            DataFrame<Object> remains;
            DataFrame<Object> finaldf = new DataFrame<>("AID","TimeStamp","TimeDifference","Minutes");

            List<Object> td = timeframe.col("TimeDifference");
            int start = 0;
            int end = 0;
            double x ;
            double y;
            float low;
            float high;
            int minlow ;
            int minhigh ;
            double minutes_mean ;
            int low_hour ;
            int high_hour ;
            Object mean;
            Object sdev;
            int mean_hours ;
            int mm ;


            finaldf.append(Arrays.asList(timeframe.get(0,"AID"),timeframe.get(0,"TimeStamp"),timeframe.get(0,"TimeDifference")));
            Log.d("ORIGINAL", String.valueOf(timeframe));

            for(int i=1 ; i< td.size() ; i++) {


                int difference = (Integer) td.get(i) - (Integer) td.get(0);
                String[] tm = timeframe.get(i, "TimeStamp").toString().split(" ");

                String[] split = tm[1].split(":");

                int min = ((Integer.parseInt(split[0])) * 60 + ((Integer.parseInt(split[1]))));


                finaldf.append(Arrays.asList(timeframe.get(i, "AID"), timeframe.get(i, "TimeStamp"), timeframe.get(i, "TimeDifference"), min));

                // if the time difference is greater than or equal to 1800 seconds, we require those rows of data.
                if (difference >= 1800) {


                    cluster = finaldf.slice(start, i + 1);


                    Log.d("si", start + "," + i);
                    Log.d("dataframe-cluster", String.valueOf(cluster));
                    start = i + 1;
                    end = start;

                    //if cluster has more than one row
                    if (cluster.length() > 1) {

                        mean = cluster.mean().col("Minutes").get(0);


                        double mean_in_hours = (double) mean/60;
                        mean_hours = (int) mean_in_hours;
                        minutes_mean = ((float) (mean_in_hours % 1) );
                        mm = (int) (minutes_mean*60);


                        sdev = cluster.stddev().col("Minutes").get(0);


                        double sdev_in_hours = (double) sdev/60;

                        x = mean_in_hours;
                        y=sdev_in_hours;

                        low =  (float)(x - y);
                        high = (float) (x + y);

                        low_hour = (int) low;
                        high_hour = (int) high;

                        minlow = (int) ((low % 1) *60);
                        minhigh = (int) ((high % 1) * 60);

                        Log.d("low-hour", low_hour+ ":" + String.format("%02d", minlow));
                        Log.d("mean-hour", mean_hours + ":" + String.format("%02d", mm));
                        Log.d("high-hour", high_hour + ":" + String.format("%02d", minhigh));
                    } else {

                        // if cluster has only one row, we just need to find the mean.

                        Object val = timeframe.get(i, "TimeStamp");
                        String[] date_time = val.toString().split(" ");

                        Log.d("mean-hour",date_time[1]);

                    }
                }

            }

            // data that is not a part of main cluster can be taken to make calculations too.
            int length = finaldf.length();

            // remains is a cluster that starts from the row of the last cluster that fit the range we provided, and goes on till the end of the dataframe.
            remains=  finaldf.slice(end,length);

            Log.d("dataframe-remaining", String.valueOf(remains));

            if(remains.length() != 0){

                //mean of minutes column in remains
                Object mean_rem = remains.mean().col("Minutes").get(0);
                Log.d("mean", String.valueOf(mean_rem));

                //converting minutes to 24 hour format
                double mean_in_hours = (double) mean_rem/60;
                mean_hours = (int) mean_in_hours;
                minutes_mean = ((float) (mean_in_hours % 1) );
                mm = (int) (minutes_mean*60);

                //standard deviation
                sdev = remains.stddev().col("Minutes").get(0);
                double sdev_in_hours = (double) sdev/60;

                x = mean_in_hours;
                y=sdev_in_hours;


                // finding the limits of the range around the mean wrt to the standard deviation.
                low =  (float)(x - y);
                high = (float) (x + y);

                low_hour = (int) low;
                high_hour = (int) high;

                minlow = (int) ((low % 1) *60);
                minhigh = (int) ((high % 1) * 60);


                Log.d("low-hour", low_hour+ ":" + String.format("%02d", minlow));
                Log.d("mean-hour", mean_hours + ":" + String.format("%02d", mm));
                Log.d("high-hour", high_hour + ":" + String.format("%02d", minhigh));
            }


        }





        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);



        }


    }

  }








