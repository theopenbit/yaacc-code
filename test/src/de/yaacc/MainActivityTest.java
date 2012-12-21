package de.yaacc;

import android.annotation.TargetApi;
import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class de.yaacc.MainActivityTest \
 * de.yaacc.tests/android.test.InstrumentationTestRunner
 */
@TargetApi(3)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super("de.yaacc", MainActivity.class);
    }

}
