package trendly.cs403x.trendly;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MyFragment extends Fragment {

    private ArrayList<String> al;
    private ArrayList<String> idArray;
    private ArrayAdapter<String> arrayAdapter;

    @InjectView(R.id.frame) SwipeFlingAdapterView flingContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        al = new ArrayList<>();
        idArray = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my, container, false);
        ButterKnife.inject(this, view);

        getJSONList("nearby", "56", "90", "20", "5");

        return view;
    }

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }

    private void getJSONList(final String reqType, final String lat, final String lng, final String range, final String count) {
        new AsyncTask<Void, Void, String>() {
            ProgressDialog progressDialog = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);

            @Override
            protected void onPreExecute() {
                progressDialog.setTitle("Please wait");
                progressDialog.setMessage("Retrieving items...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);
            }

            @Override
            protected String doInBackground(Void... params) {
                URL url;
                HttpURLConnection connection = null;
                String response;

                try {
                    String requestJson = "{\"reqType\":\"" + reqType
                            + "\", \"lat\":\"" + lat + "\", \"lng\":\"" + lng +
                            "\", \"range\":\"" + range + "\", \"count\":\"" + count + "\"}";
                    url = new URL("http://murmuring-depths-93571.herokuapp.com/rest");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(30000);
                    connection.setReadTimeout(30000);
                    connection.setDoOutput(true);

                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(requestJson);
                    outputStream.flush();
                    outputStream.close();

                    response = readResponse(new BufferedInputStream(connection.getInputStream()));
                    return response;
                } catch (Exception e) {
                    Log.d("MyFragment", "Could not retrieve items.");
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                progressDialog.dismiss();
                try {
                    JSONArray resultArray = new JSONArray(s);

                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject jsonObject = resultArray.getJSONObject(i);
                        String itemName = jsonObject.getString("itemName");
                        String itemId = jsonObject.getString("item");
                        al.add(itemName);
                        idArray.add(itemId);
                    }
                    setCardView();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void setCardView() {
        arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.item, R.id.helloText, al);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                al.remove(0);
                idArray.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject

            }

            @Override
            public void onRightCardExit(Object dataObject) {
                sendLikeToServer(idArray.get(0), "56", "90");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                getJSONList("nearby", "56", "90", "20", "5");
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                makeToast(getActivity(), "Clicked!");
            }
        });
    }


    @OnClick(R.id.right)
    public void right() {
        /**
         * Trigger the right event manually.
         */
        flingContainer.getTopCardListener().selectRight();
    }

    @OnClick(R.id.left)
    public void left() {
        flingContainer.getTopCardListener().selectLeft();
    }

    public static String readResponse(InputStream stream) {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            // Store the response as a string.
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } catch (Exception e) {
            Log.v("ServerProblemHelper", "Error reading response.");
            e.printStackTrace();
        }
        return result.toString();
    }

    private void sendLikeToServer(final String itemId, final String lat, final String lng) {
        new AsyncTask<Void, Void, String>() {
            ProgressDialog progressDialog = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);

            @Override
            protected void onPreExecute() {
                progressDialog.setTitle("Please wait");
                progressDialog.setMessage("Sending like...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);
            }

            @Override
            protected String doInBackground(Void... params) {
                URL url;
                HttpURLConnection connection = null;
                String response;

                try {
                    String requestJson = "{\"reqType\":\"addlike\", \"itemId\":\"" + itemId
                            + "\", \"lat\":\"" + lat + "\", \"lng\":\"" + lng + "\"}";
                    url = new URL("http://murmuring-depths-93571.herokuapp.com/rest");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(30000);
                    connection.setReadTimeout(30000);
                    connection.setDoOutput(true);

                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(requestJson);
                    outputStream.flush();
                    outputStream.close();

                    response = readResponse(new BufferedInputStream(connection.getInputStream()));
                    return response;
                } catch (Exception e) {
                    Log.d("MyFragment", "Could not send like to server.");
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String result = jsonObject.getString("result");
                    if ("Ok".equals(result)) {
                        makeToast(getActivity(), "Like registered");
                    } else {
                        makeToast(getActivity(), "An error occurred. Please try again later.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}