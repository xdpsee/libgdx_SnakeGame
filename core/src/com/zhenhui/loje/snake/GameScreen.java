package com.zhenhui.loje.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {

    private static final int GRID_CELL = 32;

    private static final int MOVEMENT_STEP = 32;

    private static final float MOVE_TIME = 1f;

    private float timer = MOVE_TIME;

    private Viewport viewport;

    private Camera camera;

    private SpriteBatch batch;

    private ShapeRenderer shapeRenderer;

    private BitmapFont bitmapFont;

    private GlyphLayout layout;

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

    private State state = State.PLAYING;

    private boolean drawGrid = true;

    private int score = 0;

    public GameScreen() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(new Vector2(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2), 0);
        camera.update();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        layout = new GlyphLayout();
        bitmapFont = new BitmapFont();
        bitmapFont.setColor(Color.GOLD);
        bitmapFont.getData().setScale(1.2f);

        snakeHead = new Texture(Gdx.files.internal("snake_head.png"));
        snakeBody = new Texture(Gdx.files.internal("snake_body.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
    }

    @Override
    public void render(float delta) {
        switch (state) {
            case PLAYING: {
                handleInput();
                updateSnake(delta);
                checkAppleCollision();
                checkAndPlaceApple();
            }
            break;
            case GAME_OVER: {
                checkForRestart();
            }
            break;
        }
        // Draw
        clearScreen();
        if (drawGrid) {
            drawGrid();
        }
        draw();
    }

    private void updateSnake(float delta) {

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
                state = State.GAME_OVER;
            }
        }
    }

    private void drawGrid() {

        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int x = 0; x < viewport.getWorldWidth(); x += GRID_CELL) {
            for (int y = 0; y < viewport.getWorldHeight(); y += GRID_CELL) {
                shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
            }
        }

        shapeRenderer.end();
    }

    private void draw() {

        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        batch.begin();

        batch.draw(snakeHead, snakeX, snakeY);
        for (BodyPart part : bodyParts) {
            part.draw(batch);
        }
        if (appleAvailable) {
            batch.draw(apple, appleX, appleY);
        }

        drawScore(batch);

        if (state == State.GAME_OVER) {
            layout.setText(bitmapFont, Constants.GAME_OVER_TEXT_TIPS);
            bitmapFont.draw(batch
                    , layout
                    , (viewport.getWorldWidth() - layout.width) / 2
                    , (viewport.getWorldHeight() - layout.height) / 2);
        }


        batch.end();
    }

    private void drawScore(Batch batch) {

        if (state == State.PLAYING) {
            layout.setText(bitmapFont, String.valueOf(score));
            bitmapFont.draw(batch
                    , String.valueOf(score)
                    , (viewport.getWorldWidth() - layout.width) / 2
                    , (viewport.getWorldHeight() * 4f / 5) - layout.height / 2);
        }

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

        if (snakeX >= viewport.getWorldWidth()) {
            snakeX = 0;
        }

        if (snakeX < 0) {
            snakeX = (int) viewport.getWorldWidth();
        }

        if (snakeY >= viewport.getWorldHeight()) {
            snakeY = 0;
        }

        if (snakeY < 0) {
            snakeY = (int) viewport.getWorldHeight();
        }
    }

    private void checkAndPlaceApple() {

        if (!appleAvailable) {
            do {
                appleX = MathUtils.random((int) viewport.getWorldWidth() / MOVEMENT_STEP - 1) * MOVEMENT_STEP;
                appleY = MathUtils.random((int) viewport.getWorldHeight() / MOVEMENT_STEP - 1) * MOVEMENT_STEP;
                appleAvailable = true;
            } while (snakeX == appleX && snakeY == appleY);
        }
    }

    private void checkAppleCollision() {
        if (appleAvailable && (snakeX == appleX) && (snakeY == appleY)) {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updatePos(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            score += Constants.SCORE_PER_APPLE;
            appleAvailable = false;
        }
    }

    private void checkForRestart() {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            restartGame();
        }
    }

    private void restartGame() {
        state = State.PLAYING;
        snakeX = 0;
        snakeY = 0;
        snakeXBeforeUpdate = 0;
        snakeYBeforeUpdate = 0;
        bodyParts.clear();
        appleAvailable = false;
        direction = MoveDirection.RIGHT;
        directionSet = false;
        timer = 0;
        score = 0;
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


