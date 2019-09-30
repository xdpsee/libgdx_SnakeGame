package com.zhenhui.loje.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends ScreenAdapter {

    private static final int GRID_CELL = 32;

    private static final int MOVEMENT_STEP = 32;

    private static final float MOVE_TIME = 1f;

    private float timer = MOVE_TIME;

    private SpriteBatch batch;

    private ShapeRenderer shapeRenderer;

    // snake
    private Texture snakeHead;

    private Texture snakeBody;

    private int snakeX = 0;

    private int snakeY = 0;

    private int snakeXBeforeUpdate = 0;

    private int snakeYBeforeUpdate = 0;

    private Array<BodyPart> bodyParts = new Array<>();

    // apple
    private Texture apple;

    private boolean appleAvailable = false;

    private int appleX;

    private int appleY;

    private MoveDirection direction = MoveDirection.RIGHT;

    private boolean directionSet = false;

    private boolean isHit = false;

    private State state = State.PLAYING;

    public GameScreen() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        snakeHead = new Texture(Gdx.files.internal("snake_head.png"));
        snakeBody = new Texture(Gdx.files.internal("snake_body.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
    }

    @Override
    public void render(float delta) {

        // Input
        handleInput();

        updateSnake(delta);

        checkAppleCollision();

        checkAndPlaceApple();

        // Draw
        clearScreen();
        drawGrid();
        draw();

    }

    private void updateSnake(float delta) {

        if (!isHit) {
            timer -= 2 * delta;
            if (timer <= 0) {
                timer = MOVE_TIME;
                moveSnake();
                checkForOutBounds();
                updateBodyPartsPosition();
                checkSnakeBodyCollision();
                directionSet = false;
            }
        }
    }

    private void updateDirection(MoveDirection newDirection) {
        if (!directionSet && direction != newDirection) {
            directionSet = true;
            switch (newDirection) {
                case LEFT:
                    updateIfNotOppositeDirection(newDirection, MoveDirection.RIGHT);
                    break;
                case UP:
                    updateIfNotOppositeDirection(newDirection, MoveDirection.DOWN);
                    break;
                case RIGHT:
                    updateIfNotOppositeDirection(newDirection, MoveDirection.LEFT);
                    break;
                case DOWN:
                    updateIfNotOppositeDirection(newDirection, MoveDirection.UP);
                    break;
            }
        }
    }

    private void updateIfNotOppositeDirection(MoveDirection newSnakeDirection, MoveDirection
            oppositeDirection) {
        if (direction != oppositeDirection || bodyParts.size == 0)
            direction = newSnakeDirection;
    }

    private void updateBodyPartsPosition() {
        if (!bodyParts.isEmpty()) {
            BodyPart part = bodyParts.removeIndex(0);
            part.updatePos(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(part);
        }
    }

    private void checkSnakeBodyCollision() {

        for (BodyPart part : bodyParts) {
            if (part.x == snakeX && part.y == snakeY) {
                isHit = true;
            }
        }
    }

    private void drawGrid() {

        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int x = 0; x < Gdx.graphics.getWidth(); x += GRID_CELL) {
            for (int y = 0; y < Gdx.graphics.getHeight(); y += GRID_CELL) {
                shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
            }
        }

        shapeRenderer.end();
    }

    private void draw() {

        batch.begin();

        batch.draw(snakeHead, snakeX, snakeY);
        for (BodyPart part : bodyParts) {
            part.draw(batch);
        }
        if (appleAvailable) {
            batch.draw(apple, appleX, appleY);
        }

        batch.end();
    }

    private void clearScreen() {
        Gdx.gl20.glClearColor(0, 0, 0, 0);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    private void handleInput() {

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            updateDirection(MoveDirection.LEFT);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            updateDirection(MoveDirection.UP);

        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            updateDirection(MoveDirection.RIGHT);

        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            updateDirection(MoveDirection.DOWN);

        }
    }


    private void moveSnake() {

        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;

        switch (direction) {
            case LEFT:
                snakeX -= MOVEMENT_STEP;
                break;
            case UP:
                snakeY += MOVEMENT_STEP;
                break;
            case RIGHT:
                snakeX += MOVEMENT_STEP;
                break;
            case DOWN:
                snakeY -= MOVEMENT_STEP;
                break;
        }
    }

    private void checkForOutBounds() {

        if (snakeX >= Gdx.graphics.getWidth()) {
            snakeX = 0;
        }

        if (snakeX < 0) {
            snakeX = Gdx.graphics.getWidth();
        }

        if (snakeY >= Gdx.graphics.getHeight()) {
            snakeY = 0;
        }

        if (snakeY < 0) {
            snakeY = Gdx.graphics.getHeight();
        }
    }

    private void checkAndPlaceApple() {

        if (!appleAvailable) {
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / MOVEMENT_STEP - 1) * MOVEMENT_STEP;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / MOVEMENT_STEP - 1) * MOVEMENT_STEP;
                appleAvailable = true;
            } while (snakeX == appleX && snakeY == appleY);
        }
    }

    private void checkAppleCollision() {
        if (appleAvailable && (snakeX == appleX) && (snakeY == appleY)) {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updatePos(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            appleAvailable = false;
            state = State.GAME_OVER;
        }
    }

    private class BodyPart {

        private int x;

        private int y;

        private Texture texture;

        BodyPart(Texture texture) {
            this.texture = texture;
        }

        void updatePos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void draw(Batch batch) {
            if (!(x == snakeX && y == snakeY)) {
                batch.draw(texture, x, y);
            }
        }
    }
}


