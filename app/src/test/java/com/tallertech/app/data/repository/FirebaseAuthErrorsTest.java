package com.tallertech.app.data.repository;

import org.junit.Test;
import static org.junit.Assert.*;

public class FirebaseAuthErrorsTest {
    @Test public void credentialsDoNotDiscloseWhetherAccountExists() {
        String message = FirebaseAuthErrors.message("ERROR_INVALID_CREDENTIAL");
        assertEquals(message, FirebaseAuthErrors.message("ERROR_USER_NOT_FOUND"));
        assertEquals(message, FirebaseAuthErrors.message("ERROR_WRONG_PASSWORD"));
        assertEquals(message, FirebaseAuthErrors.message("ERROR_INVALID_LOGIN_CREDENTIALS"));
    }
    @Test public void networkErrorSuggestsCheckingConnection() {
        assertTrue(FirebaseAuthErrors.message("ERROR_NETWORK_REQUEST_FAILED").contains("conexión"));
    }
    @Test public void disabledAccountHasSpecificMessage() {
        assertTrue(FirebaseAuthErrors.message("ERROR_USER_DISABLED").contains("deshabilitada"));
    }
    @Test public void throttledRequestSuggestsWaiting() {
        assertTrue(FirebaseAuthErrors.message("ERROR_TOO_MANY_REQUESTS").contains("más tarde"));
    }
    @Test public void unknownErrorNeverExposesInternalDetails() {
        assertEquals(FirebaseAuthErrors.message(null), FirebaseAuthErrors.message("INTERNAL_SECRET_DETAILS"));
    }
}
