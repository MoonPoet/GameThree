package uy.com.agm.gamethree.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import uy.com.agm.gamethree.screens.PlayScreen;
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

    protected enum State {ALIVE, INJURED, EXPLODING, DEAD}

    ;
    protected State currentState;
    protected MapObject object;

    public Enemy(PlayScreen screen, MapObject object) {
        this.object = object;
        this.world = screen.getWorld();
        this.screen = screen;

        Rectangle rect = ((RectangleMapObject) object).getRectangle();
        setPosition(rect.getX() / Constants.PPM, rect.getY() / Constants.PPM);
        defineEnemy();
        b2body.setActive(false);
    }

    public boolean isDestroyed() {
        return currentState == State.DEAD || currentState == State.EXPLODING;
    }

    protected void getItemOnHit() {
        float MARGEN = 32; // TODO ARREGLAR ESTO
        if (object.getProperties().containsKey("powerOne")) {
            screen.creator.createGameThreeActor(new GameThreeActorDef(new Vector2(b2body.getPosition().x, b2body.getPosition().y + MARGEN / Constants.PPM), PowerOne.class));
        }
    }

    protected abstract void defineEnemy();

    public abstract void update(float dt);

    public abstract void renderDebug(ShapeRenderer shapeRenderer);

    public abstract void onHit();
}
