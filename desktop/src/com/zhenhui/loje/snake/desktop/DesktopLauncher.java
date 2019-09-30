package com.zhenhui.loje.snake.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.zhenhui.loje.snake.SnakeGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("");
        config.setWindowedMode(640, 480);
        new Lwjgl3Application(new SnakeGame(), config);
    }
}
