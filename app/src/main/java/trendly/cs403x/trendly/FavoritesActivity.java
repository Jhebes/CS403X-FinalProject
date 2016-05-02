package trendly.cs403x.trendly;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class FavoritesActivity extends AppCompatActivity {
    private ArrayList<String> favorites;
    private FavoritesAdapter favoritesAdapter;
    private ListView favoritesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favorites = new ArrayList<>();
        favoritesList = (ListView) findViewById(R.id.favorites_list);

        getSupportActionBar().setTitle("Favorites");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFavoritesFromServer("josh");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFavoritesFromServer(final String userId) {
        new AsyncTask<Void, Void, String>() {
            ProgressDialog progressDialog = new ProgressDialog(FavoritesActivity.this, ProgressDialog.STYLE_SPINNER);

            @Override
            protected void onPreExecute() {
                progressDialog.setTitle("Please wait");
                progressDialog.setMessage("Retrieving favorites...");
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
                    String requestJson = "{\"reqType\":\"favorites\", \"userId\":\"" + userId + "\"}";
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
                    JSONArray jsonArray = new JSONArray(s);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        String itemName = jsonArray.getJSONObject(i).getString("itemName");
                        favorites.add(itemName);
                    }
                    setFavoritesList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
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

    private void setFavoritesList() {
        String[] favoritesArray = new String[favorites.size()];
        favoritesAdapter = new FavoritesAdapter(FavoritesActivity.this, favorites.toArray(favoritesArray));
        favoritesList.setAdapter(favoritesAdapter);
    }
}
