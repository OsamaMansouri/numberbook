package com.example.numberbook;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.*;
import android.content.Intent;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.numberbook.models.Contact;
import com.example.numberbook.network.ApiClient;
import com.example.numberbook.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.android.volley.toolbox.JsonObjectRequest;



public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;

    private ListView lv;
    private Button btnSendContacts;

    private ArrayList<String> contactList = new ArrayList<>();
    private List<Contact> fullContactList = new ArrayList<>(); // Pour l'envoi API
    private ArrayList<String> numbersList = new ArrayList<>(); // Pour clics appel/sms


    private void sendContactsWithVolley(List<Contact> contacts) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/php/numberbook/contacts.php";

        JSONArray contactArray = new JSONArray();
        for (Contact contact : contacts) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", contact.getName());
                obj.put("number", contact.getNumber());
                contactArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject finalBody = new JSONObject();
        try {
            finalBody.put("contacts", contactArray); // correspond à $data['contacts']
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                finalBody,
                response -> Toast.makeText(MainActivity.this, "✅ Envoi réussi : " + response.toString(), Toast.LENGTH_LONG).show(),
                error -> {
                    error.printStackTrace();
                    String errorMessage = "Erreur inconnue";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMessage = new String(error.networkResponse.data);
                    }

                    Toast.makeText(MainActivity.this, "❌ Erreur Volley : " + errorMessage, Toast.LENGTH_LONG).show();
                }
        );

        queue.add(request);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = findViewById(R.id.lv);
        btnSendContacts = findViewById(R.id.btn_send_contacts);

        // Permission + affichage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        } else {
            loadContacts();
        }

        btnSendContacts.setOnClickListener(v -> {
            if (!fullContactList.isEmpty()) {
                sendContactsWithVolley(fullContactList); // ✅ Volley version
            } else {
                Toast.makeText(this, "Aucun contact à envoyer", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadContacts() {
        ContentResolver cr = getContentResolver();
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);

        contactList.clear();
        fullContactList.clear();
        numbersList.clear();

        if (phones != null && phones.getCount() > 0) {
            while (phones.moveToNext()) {
                String name = phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactList.add(name + " : " + number);
                fullContactList.add(new Contact(name, number));
                numbersList.add(number);
            }
            phones.close();
        }

        if (contactList.isEmpty()) {
            contactList.add("Aucun contact trouvé");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                contactList
        );

        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= numbersList.size()) {
                Toast.makeText(this, "Aucun numéro valide", Toast.LENGTH_SHORT).show();
                return;
            }

            String number = numbersList.get(position);
            showOptionsDialog(number);
        });
    }

    private void showOptionsDialog(String phoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Que voulez-vous faire ?");
        builder.setItems(new CharSequence[]{"📞 Appeler", "💬 Envoyer un message"},
                (dialog, which) -> {
                    if (which == 0) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(callIntent);
                    } else if (which == 1) {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setData(Uri.parse("sms:" + phoneNumber));
                        startActivity(smsIntent);
                    }
                });
        builder.show();
    }

    private void sendContactsToServer(List<Contact> contacts) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.sendContacts(contacts);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Contacts envoyés avec succès", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Échec réseau : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (permissions.length == 0 || grantResults.length == 0) {
                Toast.makeText(this, "Demande de permission annulée.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_LONG).show();
            }
        }
    }
}
