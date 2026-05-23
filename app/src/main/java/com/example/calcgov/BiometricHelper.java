package com.example.calcgov;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricHelper {

    private static final String PREFS_NAME = "myPrefs";
    private static final String LOGGED_CPF_KEY = "logged_cpf";
    private static final String BIOMETRIC_SUFFIX = "_biometric_enabled";
    private static final int BIOMETRIC_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG;

    public interface BiometricCallback {
        void onAuthenticationSucceeded();
        void onAuthenticationError(String error);
    }

    public static boolean isBiometricEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String loggedCpf = prefs.getString(LOGGED_CPF_KEY, "");
        return !loggedCpf.isEmpty() && prefs.getBoolean(loggedCpf + BIOMETRIC_SUFFIX, false);
    }

    public static void setBiometricEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String loggedCpf = prefs.getString(LOGGED_CPF_KEY, "");
        if (!loggedCpf.isEmpty()) {
            prefs.edit().putBoolean(loggedCpf + BIOMETRIC_SUFFIX, enabled).apply();
        }
    }

    public static boolean canUseBiometric(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate(BIOMETRIC_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void authenticateIfEnabled(FragmentActivity activity, BiometricCallback callback) {
        if (!isBiometricEnabled(activity)) {
            callback.onAuthenticationSucceeded();
            return;
        }

        if (!canUseBiometric(activity)) {
            setBiometricEnabled(activity, false);
            callback.onAuthenticationSucceeded();
            return;
        }

        showBiometricPrompt(activity, callback);
    }

    public static void showBiometricPrompt(FragmentActivity activity, BiometricCallback callback) {
        BiometricManager biometricManager = BiometricManager.from(activity);

        if (biometricManager.canAuthenticate(BIOMETRIC_AUTHENTICATORS) != BiometricManager.BIOMETRIC_SUCCESS) {
            callback.onAuthenticationSucceeded();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onAuthenticationError(errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticationSucceeded();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticação Digital")
                .setSubtitle("Use sua biometria para acessar o perfil")
                .setAllowedAuthenticators(BIOMETRIC_AUTHENTICATORS)
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
