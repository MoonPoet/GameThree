package uy.com.agm.gamethree.sprites.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import uy.com.agm.gamethree.screens.PlayScreen;
import uy.com.agm.gamethree.tools.Assets;
import uy.com.agm.gamethree.tools.Constants;

/**
 * Created by AGM on 12/19/2017.
 */

public class EnemyBullet extends Weapon {
    private static final String TAG = EnemyBullet.class.getName();

    private float stateTime;
    private Animation enemyBulletAnimation;
    private Vector2 velocity;

    public EnemyBullet(PlayScreen screen, float x, float y) {
        super(screen, x, y);

        // Animation
        enemyBulletAnimation = Assets.instance.enemyBullet.enemyBulletAnimation;

        // Setbounds is the one that determines the size of the EnergyBall's drawing on the screen
        setBounds(getX(), getY(), Constants.ENEMYBULLET_WIDTH_METERS, Constants.ENEMYBULLET_HEIGHT_METERS);

        stateTime = 0;
        currentState = State.SHOT;

        // To go from Enemy to Hero we must subtract their position vectors: Hero - Enemy.
        velocity = new Vector2();
        velocity.x = screen.player.b2body.getPosition().x - b2body.getPosition().x;
        velocity.y = screen.player.b2body.getPosition().y - b2body.getPosition().y;

        // Get the direction of the previous vector (normalization)
        velocity.nor();

        // Apply constant velocity on that direction
        velocity.x = velocity.x * Constants.ENEMYBULLET_LINEAR_VELOCITY;
        velocity.y = velocity.y * Constants.ENEMYBULLET_LINEAR_VELOCITY;
    }

    @Override
    protected void defineWeapon() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY()); // In b2box the origin is at the center of the body
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(Constants.ENEMYBULLET_CIRCLESHAPE_RADIUS_METERS);
        fdef.filter.categoryBits = Constants.ENEMY_WEAPON_BIT; // Depicts what this fixture is
        fdef.filter.maskBits = Constants.BORDERS_BIT |
                Constants.HERO_BIT; // Depicts what can this Fixture collide with (see WorldContactListener)

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

    }

    @Override
    public void update(float dt) {
        switch (currentState) {
            case SHOT:
                stateShot(dt);
                break;
            case ONTARGET:
                stateOnTarget();
                break;
            case FINISHED:
                break;
            default:
                break;
        }
        super.controlBoundaries();
    }

    private void stateShot(float dt) {
        stateTime += dt;
        b2body.setLinearVelocity(velocity);
        /* Update our Sprite to correspond with the position of our Box2D body:
        * Set this Sprite's position on the lower left vertex of a Rectangle determined by its b2body to draw it correctly.
        * At this time, EnergyBall may have collided with sth. and therefore it has a new position after running the physical simulation.
        * In b2box the origin is at the center of the body, so we must recalculate the new lower left vertex of its bounds.
        * GetWidth and getHeight was established in the constructor of this class (see setBounds).
        * Once its position is established correctly, the Sprite can be drawn at the exact point it should be.
         */
        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        setRegion((TextureRegion) enemyBulletAnimation.getKeyFrame(stateTime, true));
    }

    private void stateOnTarget() {
        world.destroyBody(b2body);
        currentState = State.FINISHED;
    }

    @Override
    public void renderDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(getBoundingRectangle().x, getBoundingRectangle().y, getBoundingRectangle().width, getBoundingRectangle().height);
    }

    @Override
    public void onTarget() {
        /*
         * We must remove its b2body to avoid collisions.
         * This can't be done here because this method is called from WorldContactListener that is invoked
         * from PlayScreen.update.world.step(...).
         * No b2body can be removed when the simulation is occurring, we must wait for the next update cycle.
         * Therefore we use a flag (state) in order to point out this behavior and remove it later.
         */
        currentState = State.ONTARGET;
        Gdx.app.debug(TAG, "Hero collision");
    }

    public void draw(Batch batch) {
        if (currentState == State.SHOT) {
            super.draw(batch);
        }
    }
}
