package com.example.chicksevent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
//import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.chicksevent.Organizer;
import com.example.chicksevent.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

@RunWith(MockitoJUnitRunner.class)
public class Test_02_07_02 {

    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseDatabase> firebaseDbMock;

    @Before
    public void setUp() {  // Now runs before each test
        // Mock FirebaseApp.getInstance() to return a relaxed mock app
        firebaseAppMock = mockStatic(FirebaseApp.class);
        FirebaseApp mockApp = mock(FirebaseApp.class, withSettings().lenient());  // Lenient: Ignores unmocked calls
        when(FirebaseApp.getInstance()).thenReturn(mockApp);

        // Mock FirebaseDatabase.getInstance() to return a relaxed mock DB
        firebaseDbMock = mockStatic(FirebaseDatabase.class);
        FirebaseDatabase mockDb = mock(FirebaseDatabase.class, withSettings().lenient());
        when(FirebaseDatabase.getInstance()).thenReturn(mockDb);
    }

    @After
    public void tearDown() {  // Now runs after each test
        // Clean up mocks to avoid leaks
        if (firebaseAppMock != null) {
            firebaseAppMock.close();
        }
        if (firebaseDbMock != null) {
            firebaseDbMock.close();
        }
    }

    @Test
    public void addition_isCorrect() {
        // Now instantiate classes without real Firebase init (mocks are active)
        Organizer organizer = new Organizer();  // Chains to User, FirebaseService—mocks prevent errors
        User user = new User();

        // Your actual test logic, e.g.:
        assertEquals(2, 1 + 1);  // Placeholder; replace with real assertions (e.g., on organizer/user methods)
    }
}