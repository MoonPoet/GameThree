package uy.com.agm.gamethree.sprites.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import uy.com.agm.gamethree.screens.PlayScreen;
import uy.com.agm.gamethree.sprites.weapons.EnemyBullet;
import uy.com.agm.gamethree.tools.GameThreeActorDef;
import uy.com.agm.gamethree.sprites.powerup.Items.PowerOne;
import uy.com.agm.gamethree.tools.Constants;

/**
 * Created by AGM on 12/9/2017.
 */

public abstract class Enemy extends Sprite {
    private static final String TAG = Enemy.class.getName();

    protected World world;
    protected PlayScreen screen;
    public Body b2body;

    protected enum State {
        ALIVE, INJURED, EXPLODING, DEAD
    }

    protected State currentState;
    protected MapObject object;

    public Enemy(PlayScreen screen, MapObject object) {
        this.object = object;
        this.world = screen.getWorld();
        this.screen = screen;

        // Get the rectangle drawn in TiledEditor (pixels)
        Rectangle rect = ((RectangleMapObject) object).getRectangle();

        /* Set this Sprite's position on the lower left vertex of a Rectangle determined by TiledEditor.
        * At this moment we don't have Enemy.width and Enemy.height because this is an abstract class.
        * Width and height will be determined in classes that inherit from this one.
        * This point will be used by defineEnemy() calling getX(), getY() to center its b2body.
        * SetPosition always receives world coordinates.
        */
        setPosition(rect.getX() / Constants.PPM, rect.getY() / Constants.PPM);
        defineEnemy();

        // By default this Enemy doesn't interact in our world
        b2body.setActive(false);
    }

    public boolean isDestroyed() {
        return currentState == State.DEAD || currentState == State.EXPLODING;
    }

    protected void controlBoundaries() {
        /* When an Enemy is on camera, it activates (it moves and can collide).
        * You have to be very careful because if the enemy is destroyed, its b2body does not exist and gives
        * random errors if you try to active it.
        */
        if (!isDestroyed()) {
            float upperEdge = screen.gameCam.position.y + screen.gameViewPort.getWorldHeight() / 2;
            float bottomEdge = screen.gameCam.position.y - screen.gameViewPort.getWorldHeight() / 2;

            if (bottomEdge <= getY() && getY() <= upperEdge) {
                b2body.setActive(true);
            } else {
                b2body.setActive(false);
            }
        }
    }

    // Determine whether or not a power should be released reading a property set in TiledEditor.
    protected void getItemOnHit() {
        if (object.getProperties().containsKey("powerOne")) {
            Vector2 position = new Vector2(b2body.getPosition().x, b2body.getPosition().y + Constants.ITEM_OFFSET_METERS);
            screen.creator.createGameThreeActor(new GameThreeActorDef(position, PowerOne.class));
        }
    }

    protected void openFire() {
        if (!isDestroyed()) {
            if (b2body.isActive()) {
                if (object.getProperties().containsKey("enemyBullet")) {
                    Vector2 position = new Vector2(b2body.getPosition().x, b2body.getPosition().y - Constants.ENEMYBULLET_OFFSET_METERS * 2);
                    screen.creator.createGameThreeActor(new GameThreeActorDef(position, EnemyBullet.class));
                    Gdx.app.debug(TAG, "PUM!");
                }
            }
        }
    }

    protected abstract void defineEnemy();
    public abstract void update(float dt);
    public abstract void renderDebug(ShapeRenderer shapeRenderer);
    public abstract void onHit();
    public abstract void reverseVelocity(boolean x, boolean y);
}