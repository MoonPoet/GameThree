package uy.com.agm.gamethree.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import uy.com.agm.gamethree.sprites.enemies.Enemy;
import uy.com.agm.gamethree.sprites.enemies.EnemyOne;
import uy.com.agm.gamethree.sprites.player.Hero;
import uy.com.agm.gamethree.sprites.powerup.Items.Item;
import uy.com.agm.gamethree.sprites.powerup.boxes.PowerBox;
import uy.com.agm.gamethree.sprites.tileObjects.InteractiveTileObject;
import uy.com.agm.gamethree.sprites.weapons.Weapon;

/**
 * Created by AGM on 12/8/2017.
 */

public class WorldContactListener implements ContactListener {
    private static final String TAG = WorldContactListener.class.getName();

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        Fixture fixC;

        int collisionDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
// TODO HABRIA QUE OPTIMIAR ESTO USANDO CASES SIN BRAKES ADEMAS NO ME INTERESA REGISTRAR LOS CHOQUES CONTRA LOS BORDES Y ESO COMO POR EJEMPLO EL PRIMER CASE
        switch (collisionDef) {
            case Constants.HERO_BIT | Constants.BORDERS_BIT: // bordes
                fixC = fixA.getFilterData().categoryBits == Constants.BORDERS_BIT ? fixA : fixB;
                ((InteractiveTileObject) fixC.getUserData()).onHit();
                break;
            case Constants.HERO_BIT | Constants.OBSTACLE_BIT: // arboles
                fixC = fixA.getFilterData().categoryBits == Constants.OBSTACLE_BIT ? fixA : fixB;
                ((InteractiveTileObject) fixC.getUserData()).onHit();
                break;
            case Constants.HERO_BIT | Constants.POWERBOX_BIT: // regalos
                fixC = fixA.getFilterData().categoryBits == Constants.POWERBOX_BIT ? fixA : fixB;
                ((PowerBox) fixC.getUserData()).onHit();
                break;
            case Constants.HERO_BIT | Constants.ENEMY_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.ENEMY_BIT ? fixA : fixB;
                ((Enemy) fixC.getUserData()).onHit();
                Gdx.app.debug(TAG, "Hero muere!");
                break;
            case Constants.ENEMY_BIT | Constants.BORDERS_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.ENEMY_BIT ? fixA : fixB;
                ((EnemyOne) fixC.getUserData()).reverseVelocity(true, false);
                break;
            case Constants.ENEMY_BIT | Constants.OBSTACLE_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.ENEMY_BIT ? fixA : fixB;
                ((EnemyOne) fixC.getUserData()).reverseVelocity(true, false);
                break;
            case Constants.ENEMY_BIT | Constants.ENEMY_BIT:
                ((EnemyOne) fixA.getUserData()).reverseVelocity(true, false);
                ((EnemyOne) fixB.getUserData()).reverseVelocity(true, false);
                break;
            case Constants.ITEM_BIT | Constants.BORDERS_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.ITEM_BIT ? fixA : fixB;
                ((Item) fixC.getUserData()).reverseVelocity(true, false);
                break;
            case Constants.ITEM_BIT | Constants.HERO_BIT:
                if (fixA.getFilterData().categoryBits == Constants.ITEM_BIT) {
                    ((Item) fixA.getUserData()).use((Hero) fixB.getUserData());
                } else {
                    ((Item) fixB.getUserData()).use((Hero) fixA.getUserData());
                }
                break;
            case Constants.ITEM_BIT | Constants.OBSTACLE_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.ITEM_BIT ? fixA : fixB;
                ((Item) fixC.getUserData()).reverseVelocity(true, false);
                break;
            case Constants.ITEM_BIT | Constants.ENEMY_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.ITEM_BIT ? fixA : fixB;
                ((Item) fixC.getUserData()).reverseVelocity(true, false);
                break;
            case Constants.ITEM_BIT | Constants.POWERBOX_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.ITEM_BIT ? fixA : fixB;
                ((Item) fixC.getUserData()).reverseVelocity(true, false);
                break;
            case Constants.ITEM_BIT | Constants.ITEM_BIT:
                ((Item) fixA.getUserData()).reverseVelocity(true, false);
                ((Item) fixB.getUserData()).reverseVelocity(true, false);
                break;


            // EnergyBall
            case Constants.WEAPON_BIT | Constants.BORDERS_BIT:
            case Constants.WEAPON_BIT | Constants.OBSTACLE_BIT:
            case Constants.WEAPON_BIT | Constants.POWERBOX_BIT:
            case Constants.WEAPON_BIT | Constants.ITEM_BIT:
            case Constants.WEAPON_BIT | Constants.ENEMY_BIT:
                fixC = fixA.getFilterData().categoryBits == Constants.WEAPON_BIT ? fixA : fixB;
                ((Weapon) fixC.getUserData()).onTarget();
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
