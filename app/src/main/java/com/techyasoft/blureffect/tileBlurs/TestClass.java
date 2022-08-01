package com.techyasoft.blureffect.tileBlurs;

import android.util.Log;

public class TestClass extends ParentClass {
    private static final String TAG = "TestClass";

    public TestClass() {
        Log.d(TAG, "TestClass: ");
    }
    
    static{
        Log.d(TAG, "static initializer: ");
    }

    public void f3(){
        Log.d(TAG, "f3: test");
    }

    public void f4(){
        Log.d(TAG, "f4: test");
    }

}
