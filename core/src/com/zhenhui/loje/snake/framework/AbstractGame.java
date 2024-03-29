package com.zhenhui.loje.snake.framework;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public abstract class AbstractGame implements ApplicationListener {

    protected Screen screen;

    @Override
    public void resize(int width, int height) {
        if (screen != null) {
            screen.resize(width, height);
        }
    }

    @Override
    public void render() {
        if (screen != null) {
            screen.render(Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void pause() {
        if (screen != null) {
            screen.pause();
        }
    }

    @Override
    public void resume() {
        if (screen != null) {
            screen.resume();
        }
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
    }

    public void setScreen(Screen screen) {
        if (this.screen != null) {
            this.screen.hide();
        }

        this.screen = screen;

        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }
}
