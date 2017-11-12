package cmov.feup.printersimulator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cmov.feup.printersimulator.model.Order;
import cmov.feup.printersimulator.model.Product;

public class MainActivity extends AppCompatActivity {

        TextView tv, nameTxt, surnameTxt,totalPriceTxt;
        static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";


        ListView listView;
        private static ListViewAdapter adapter;
        final String CURRENCY = " €";

        @Override
        public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView1);
        nameTxt = (TextView)findViewById(R.id.nameTxt);
        totalPriceTxt = (TextView)findViewById(R.id.orderPriceTotalTxt);
        surnameTxt = (TextView)findViewById(R.id.surnnameTxt);
        listView = (ListView)findViewById(R.id.listView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scan(true);
                }
            });

        }


        private void displayOrder(String token){
            ArrayList<Order> order = new ArrayList<>();
            String name= "John",surname = "Doe";


            tv.setText("Please wait");
            //TODO remove mock data after implementing rest connection
            order.add(new Order(new Product("Product 1","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",1d), 1));
            order.add(new Order(new Product("Product 2","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",2.21d), 4));
            order.add(new Order(new Product("Product 3","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",3.12d), 5));
            order.add(new Order(new Product("Product 1","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",1d), 1));
            /*order.add(new Order(new Product("Product 2","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",2.21d), 4));
            order.add(new Order(new Product("Product 3","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",3.12d), 5));
            order.add(new Order(new Product("Product 1","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",1d), 1));
            order.add(new Order(new Product("Product 2","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",2.21d), 4));
            order.add(new Order(new Product("Product 3","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",3.12d), 5));
            order.add(new Order(new Product("Product 4","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",24.99d), 11));
            order.add(new Order(new Product("Product 4","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",24.99d), 11));
            order.add(new Order(new Product("Product 4","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",24.99d), 11));
            order.add(new Order(new Product("Product 4","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",24.99d), 11));
            order.add(new Order(new Product("Product 4","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",24.99d), 11));
            order.add(new Order(new Product("Product 4","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",24.99d), 11));
            */
            //--------------------------------------------------------

            /*TODO use token to get order details (nameTxt and surnameTxt of the customer , all products and quantities + price for each)
            order = get order list from server
            name = ...
            surname = ...
            */

            double totalprice = 0;
            for (Order o:order){
                totalprice += (o.getQuantity()*o.getProduct().getPrice());
            }

            ListView view_instance = (ListView)findViewById(R.id.listView);
            ViewGroup.LayoutParams params=view_instance.getLayoutParams();
            if(order.size() > 10){
                params.height=600;
            }
            else{
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }

            view_instance.setLayoutParams(params);

            adapter= new ListViewAdapter(order,getApplicationContext());
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();


            nameTxt.setText("name: " + name);
            surnameTxt.setText("surname: " + surname);
            totalPriceTxt.setText(String.format("%.2f",totalprice) + CURRENCY);
            LinearLayout l = (LinearLayout)findViewById(R.id.list);
            l.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);

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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                displayOrder(contents);
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
            displayOrder(new String(message.getRecords()[0].getPayload()));
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
}
