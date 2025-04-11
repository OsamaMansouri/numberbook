package com.example.numberbook.network;

import com.example.numberbook.models.Contact;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("contacts") // Modifie si l’endpoint est différent
    Call<Void> sendContacts(@Body List<Contact> contacts);
}

