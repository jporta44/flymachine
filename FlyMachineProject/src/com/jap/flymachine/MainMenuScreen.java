package com.jap.flymachine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MainMenuScreen implements Screen {
    
    final Fly game;

    OrthographicCamera camera;
    
    public MainMenuScreen(final Fly game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        Gdx.input.setCatchBackKey(false);
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
//
//        camera.update();
//        game.batch.setProjectionMatrix(camera.combined);
//
//        game.batch.begin();
//        game.font.draw(game.batch, "Bienvenido a Fly Machine! ", 100, 150);
//        game.font.draw(game.batch, "Toque la pantalla para comenzar!", 100, 100);
//        game.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new FlyGameScreen(game));
            dispose();
        }
    }


    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void hide() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void pause() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void resize(int arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void resume() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void show() {
        // TODO Auto-generated method stub
        
    }


}
