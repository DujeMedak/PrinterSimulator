package cmov.feup.printersimulator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cmov.feup.printersimulator.model.Order;
import cmov.feup.printersimulator.model.Product;

public class MainActivity extends AppCompatActivity {

        static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
        private static ListViewAdapter adapter;
        final String CURRENCY = " €";
    TextView tv, nameTxt, surnameTxt, totalPriceTxt;
    ListView listView;
    ArrayList<Order> arrayOrder = new ArrayList<>();

    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                act.startActivity(intent);
            }
        });
        downloadDialog.setNegativeButton(buttonNo, null);
        return downloadDialog.show();
    }


        private void displayOrder(String token){


            ListView view_instance = (ListView)findViewById(R.id.listView);
            ViewGroup.LayoutParams params=view_instance.getLayoutParams();
            if (arrayOrder.size() > 10) {
                params.height=600;
            }
            else{
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }

            view_instance.setLayoutParams(params);

            adapter = new ListViewAdapter(arrayOrder, getApplicationContext());
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();


            //nameTxt.setText("name: ");
            //surnameTxt.setText("surname: ");
            //totalPriceTxt.setText(String.format("%.2f",totalprice) + CURRENCY);
            LinearLayout l = (LinearLayout)findViewById(R.id.list);
            l.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);

        }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView1);
        nameTxt = (TextView) findViewById(R.id.nameTxt);
        totalPriceTxt = (TextView) findViewById(R.id.orderPriceTotalTxt);
        surnameTxt = (TextView) findViewById(R.id.surnnameTxt);
        listView = (ListView) findViewById(R.id.listView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan(true);
            }
        });

    }

    public void scan(boolean qrcode) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", qrcode ? "QR_CODE_MODE" : "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        }
        catch (ActivityNotFoundException anfe) {
            showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                displayOrder(contents);
                GetOrder getOrder = new GetOrder(contents);
                Thread thr = new Thread(getOrder);
                thr.start();
            }
        }
    }



    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            //tv.setText(new String(message.getRecords()[0].getPayload()));
            //displayOrder(new String(message.getRecords()[0].getPayload()));
        }

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


    private void updateUI(String response) {
        final Context c = this;
        final String r = response;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Order> arrayOrder = new ArrayList<>();
                    JSONObject jsonOrder = new JSONObject(r);
                    String orderId = jsonOrder.getString("_id");
                    String date = jsonOrder.getString("date");

                    String name = jsonOrder.getString("name");
                    String surname = jsonOrder.getString("surname");
                    JSONArray jsonArrayProducts = jsonOrder.getJSONArray("products");
                    for (int j = 0; j < jsonArrayProducts.length(); j++) {
                        JSONObject jsonProduct = jsonArrayProducts.getJSONObject(j);
                        JSONObject jsonProductDetails = jsonProduct.getJSONObject("product");
                        String ref = jsonProductDetails.getString("_id");
                        String pName = jsonProductDetails.getString("name");
                        String desc = jsonProductDetails.getString("desc");
                        double cost = jsonProductDetails.getDouble("cost");
                        int quantity = jsonProduct.getInt("quantity");
                        Product product = new Product(ref, pName, desc, cost);
                        Order order = new Order(product, quantity);
                        arrayOrder.add(order);
                    }

                    double totalprice = 0;
                    for (Order o : arrayOrder) {
                        totalprice += (o.getQuantity() * o.getProduct().getPrice());
                    }

                    ListView view_instance = (ListView) findViewById(R.id.listView);
                    ViewGroup.LayoutParams params = view_instance.getLayoutParams();
                    if (arrayOrder.size() > 10) {
                        params.height = 600;
                    } else {
                        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    }

                    view_instance.setLayoutParams(params);

                    adapter = new ListViewAdapter(arrayOrder, getApplicationContext());
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();


                    nameTxt.setText("Name: " + name + " " + surname);
                    surnameTxt.setText("Date: " + date);
                    totalPriceTxt.setText(String.format("%.2f", totalprice) + CURRENCY);
                    LinearLayout l = (LinearLayout) findViewById(R.id.list);
                    l.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.GONE);

                } catch (Exception e) {
                    Log.d("updateUI", e.getMessage());
                }
            }
        });

    }

    private class GetOrder implements Runnable {
        String tkn = null;

        GetOrder(String token) {
            tkn = token;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                return e.getMessage();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        return e.getMessage();
                    }
                }
            }
            return response.toString();
        }

        @Override
        public void run() {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://" + EshopServer.address + ":8181/sale");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestMethod("POST");

                DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                outputStream.writeBytes("token=" + tkn);
                outputStream.flush();
                outputStream.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    String response = readStream(urlConnection.getInputStream());
                    updateUI(response);
                }
            } catch (Exception e) {
                Log.d(EshopServer.address, "order", e);
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
        }
    }
}
