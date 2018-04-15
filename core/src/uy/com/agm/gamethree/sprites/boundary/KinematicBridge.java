package uy.com.agm.gamethree.sprites.boundary;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import uy.com.agm.gamethree.assets.Assets;
import uy.com.agm.gamethree.assets.sprites.AssetKinematicBridge;
import uy.com.agm.gamethree.screens.PlayScreen;
import uy.com.agm.gamethree.sprites.tileobjects.IBlockingObject;
import uy.com.agm.gamethree.tools.AudioManager;
import uy.com.agm.gamethree.tools.B2WorldCreator;
import uy.com.agm.gamethree.tools.WorldContactListener;

/**
 * Created by AGM on 4/14/2018.
 */

public class KinematicBridge  extends Sprite implements IBlockingObject {
    private static final String TAG = KinematicBridge.class.getName();

    // Constants (meters = pixels * resizeFactor / PPM)
    private static final float VELOCITY_X = -1.0f;
    private static final float VELOCITY_Y = 0.0f;

    private PlayScreen screen;
    private World world;
    private Rectangle boundsMeters;
    private Body b2body;

    private enum State {
        INACTIVE, MOVING, FINISHED
    }
    private State currentState;
    private int tiledMapId;
    private TextureRegion bridgeStand;

    public KinematicBridge(PlayScreen screen, MapObject object) {
        this.tiledMapId = object.getProperties().get(B2WorldCreator.KEY_ID, 0, Integer.class);
        this.screen = screen;
        this.world = screen.getWorld();

        // Get the rectangle drawn in TiledEditor (pixels)
        Rectangle bounds = ((RectangleMapObject) object).getRectangle();

        /* Set this Sprite's bounds on the lower left vertex of a Rectangle.
        * This point will be used by defineKinematicBridge() calling getX(), getY() to center its b2body.
        * SetBounds always receives world coordinates.
        */
        setBounds(bounds.getX() / PlayScreen.PPM, bounds.getY() / PlayScreen.PPM, AssetKinematicBridge.WIDTH_METERS, AssetKinematicBridge.HEIGHT_METERS);
        defineKinematicBridge();

        // The bridge crosses the entire screen
        boundsMeters = new Rectangle(0, getY(), screen.getGameViewPort().getWorldWidth(), getHeight());

        // Constant velocity
        b2body.setLinearVelocity(VELOCITY_X, VELOCITY_Y);

        // By default this KinematicBridge doesn't interact in our world
        b2body.setActive(false);

        // Textures
        bridgeStand = Assets.getInstance().getKinematicBridge().getKinematicBridgeA();

        // Initial state
        currentState = State.INACTIVE;
    }

    private void defineKinematicBridge() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2); // In b2box the origin is at the center of the body
        bdef.type = BodyDef.BodyType.KinematicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(getWidth() / 2, getHeight() / 2);
        fdef.filter.categoryBits = WorldContactListener.NOTHING_BIT; // Depicts what this fixture is

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        float offset = screen.getGameViewPort().getWorldWidth() - getWidth() / 2;
        float polygonShapeWidthMeters = getWidth();
        defineShape(-offset, -polygonShapeWidthMeters);
        defineShape(offset, polygonShapeWidthMeters);
    }

    private void defineShape(float offsetXMeters, float polygonShapeWidthMeters) {
        float polygonShapeHalfHeightMeters = getHeight() / 2;

        PolygonShape shape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(offsetXMeters, polygonShapeHalfHeightMeters);
        vertices[1] = new Vector2(polygonShapeWidthMeters / 2, polygonShapeHalfHeightMeters);
        vertices[2] = new Vector2(offsetXMeters, -polygonShapeHalfHeightMeters);
        vertices[3] = new Vector2(polygonShapeWidthMeters / 2, -polygonShapeHalfHeightMeters);
        shape.set(vertices);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        // The default value is 0xFFFF for maskBits, or in other words this fixture will collide
        // with every other fixture as long as the other fixture has this categoryBit in its maskBits list.
        fdef.filter.categoryBits = WorldContactListener.PATH_BIT;  // Depicts what this fixture is
        b2body.createFixture(fdef).setUserData(this);
    }

    public void update(float dt) {
        checkBoundaries();
        if (currentState != State.INACTIVE) {
            switch (currentState) {
                case MOVING:
                    stateMoving(dt);
                    break;
                case FINISHED:
                    break;
                default:
                    break;
            }
        }
    }

    private void checkBoundaries() {
        /* When a KinematicBridge is on camera, it activates (it can collide).
        * You have to be very careful because if the kinematic bridge is destroyed, its b2body does not exist and gives
        * random errors if you try to active it.
        */
        if (!isDestroyed()) {
            float upperEdge = screen.getGameCam().position.y + screen.getGameViewPort().getWorldHeight() / 2;
            float bottomEdge = screen.getGameCam().position.y - screen.getGameViewPort().getWorldHeight() / 2;
            if (bottomEdge <= getY() + getHeight() && getY() <= upperEdge) {
                b2body.setActive(true);
                currentState = State.MOVING;
            } else {
                if (b2body.isActive()) { // Was on camera...
                    // It's outside bottom edge
                    if (bottomEdge > getY() + getHeight()) {
                        if(!world.isLocked()) {
                            world.destroyBody(b2body);
                        }
                        currentState = State.FINISHED;
                    }
                }
            }
        }
    }

    protected void stateMoving(float dt) {
        /* Update our Sprite to correspond with the position of our Box2D body:
        * Set this Sprite's position on the lower left vertex of a Rectangle determined by its b2body to draw it correctly.
        * At this time, the KinematicBridge has a new position after running the physical simulation.
        * In b2box the origin is at the center of the body, so we must recalculate the new lower left vertex of its bounds.
        * GetWidth and getHeight was established in the constructor of this class (see setBounds).
        * Once its position is established correctly, the Sprite can be drawn at the exact point it should be.
         */
        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        setRegion(bridgeStand);

        if (getX() <= 0 || getX() + getWidth() >= screen.getGameViewPort().getWorldWidth()) {
            b2body.setLinearVelocity(b2body.getLinearVelocity().x * -1, VELOCITY_Y);
        }
    }

    // This KinematicBridge doesn't have any b2body inside these states
    public boolean isDestroyed() {
        return currentState == State.FINISHED;
    }

    // This KinematicBridge can be removed from our game
    public boolean isDisposable() {
        return currentState == State.FINISHED;
    }

    public void draw(Batch batch) {
        if (currentState != State.INACTIVE && currentState != State.FINISHED) {
            super.draw(batch);
        }
    }

    public String whoAmI() {
        return this.getClass().getName();
    }

    public String getTiledMapId() {
        return String.valueOf(tiledMapId);
    }

    public String getCurrentState() {
        return currentState.toString();
    }

    public void onBump() {
        AudioManager.getInstance().play(Assets.getInstance().getSounds().getBump());
    }

    @Override
    public Rectangle getBoundsMeters() {
        return boundsMeters;
    }
}