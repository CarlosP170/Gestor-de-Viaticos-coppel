package com.example.gestorviaticoscoppel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.gestorviaticoscoppel.models.User;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROL = "user_rol";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUserSession(User user) {
        editor.putString(KEY_USER_ID, user.getIdUsuario());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROL, user.getRol());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setIdUsuario(pref.getString(KEY_USER_ID, ""));
        user.setName(pref.getString(KEY_USER_NAME, ""));
        user.setEmail(pref.getString(KEY_USER_EMAIL, ""));
        user.setRol(pref.getString(KEY_USER_ROL, ""));
        return user;
    }

    public String getCurrentUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getCurrentUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getCurrentUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getCurrentUserRol() {
        return pref.getString(KEY_USER_ROL, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }
}