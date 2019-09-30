package com.zhenhui.loje.snake;

import com.zhenhui.loje.snake.framework.AbstractGame;

public class SnakeGame extends AbstractGame {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }

}
