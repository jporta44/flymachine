package com.jap.flymachine;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.jap.flymachine.FlyGameScreen;


public class AndroidGame extends AndroidApplication  {

    public void onCreate (android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useAccelerometer = true;
        cfg.useCompass = false;
        initialize(new Fly(), cfg);
    }

}
