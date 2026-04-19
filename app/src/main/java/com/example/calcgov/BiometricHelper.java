package com.example.calcgov;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricHelper {

    public interface BiometricCallback {
        void onAuthenticationSucceeded();
        void onAuthenticationError(String error);
    }

    public static void showBiometricPrompt(FragmentActivity activity, BiometricCallback callback) {
        BiometricManager biometricManager = BiometricManager.from(activity);
        
        int authenticityType = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

        if (biometricManager.canAuthenticate(authenticityType) != BiometricManager.BIOMETRIC_SUCCESS) {
            // Se não tiver biometria, pula direto para o sucesso (ou pode pedir senha customizada)
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
                .setAllowedAuthenticators(authenticityType)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
