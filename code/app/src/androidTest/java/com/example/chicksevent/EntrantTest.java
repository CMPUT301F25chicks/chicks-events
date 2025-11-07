package com.example.chicksevent;

import static androidx.test.InstrumentationRegistry.getContext;

import android.provider.Settings;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.User;

import org.junit.Test;

public class EntrantTest {
    @Test
    public void testEnterWaitingList() {
        String androidId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        System.out.println("Android ID: " + androidId);
//        return androidId;

        User u = new User(androidId);
        u.listEvents();

        Entrant e = new Entrant(androidId, "8923y98fhwiuoer");
        e.joinWaitingList();
        e.leaveWaitingList();

        e.joinWaitingList();
        e.swapStatus(EntrantStatus.INVITED);
    }
}
