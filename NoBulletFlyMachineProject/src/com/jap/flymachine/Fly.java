package com.jap.flymachine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Fly extends Game {


    
    @Override
    public void create() {

        this.setScreen(new MainMenuScreen(this));

    }
    public void render() {
        super.render(); //important!
    }
    
    public void dispose() {
    }
}
