package uy.com.agm.gamethree.screens;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import uy.com.agm.gamethree.assets.Assets;
import uy.com.agm.gamethree.assets.scene2d.AssetGameOver;
import uy.com.agm.gamethree.assets.scene2d.AssetScene2d;
import uy.com.agm.gamethree.screens.util.ScreenEnum;
import uy.com.agm.gamethree.screens.util.UIFactory;
import uy.com.agm.gamethree.tools.AudioManager;
import uy.com.agm.gamethree.widget.AnimatedActor;

/**
 * Created by AGM on 12/23/2017.
 */

public class GameOverScreen extends AbstractScreen {
    private static final String TAG = GameOverScreen.class.getName();

    public GameOverScreen() {
        super();

        // Audio FX
        AudioManager.getInstance().playSound(Assets.getInstance().getSounds().getGameOver());
    }

    @Override
    public void buildStage() {
        defineMainTable();
        defineNavigationTable();
    }

    private void defineMainTable() {
        // UI assets
        AssetScene2d assetScene2d = Assets.getInstance().getScene2d();

        // Set table structure
        Table table = new Table();

        // Design
        table.setBackground(new TextureRegionDrawable(assetScene2d.getTable()));

        // Debug lines
        table.setDebug(PlayScreen.DEBUG_MODE);

        // Center-Align table
        table.center();

        // Make the table fill the entire stage
        table.setFillParent(true);

        // Animation
        AnimatedActor animatedActor = new AnimatedActor();
        animatedActor.setAnimation(assetScene2d.getGameOver().getGameOverAnimation());
        animatedActor.setAlign(Align.center);

        // Add values
        table.add(animatedActor).size(AssetGameOver.WIDTH_PIXELS, AssetGameOver.HEIGHT_PIXELS);

        // Adds table to stage
        addActor(table);
    }

    private void defineNavigationTable() {
        // UI assets
        AssetScene2d assetScene2d = Assets.getInstance().getScene2d();

        // Set table structure
        Table table = new Table();

        // Debug lines
        table.setDebug(PlayScreen.DEBUG_MODE);

        // Bottom-Align table
        table.bottom();

        // Make the table fill the entire stage
        table.setFillParent(true);

        // Define images
        ImageButton back = new ImageButton(new TextureRegionDrawable(assetScene2d.getBack()),
                new TextureRegionDrawable(assetScene2d.getBackPressed()));

        // Add values
        table.add(back).padBottom(AbstractScreen.PAD * 2);

        // Events
        back.addListener(UIFactory.createListener(ScreenEnum.MAIN_MENU));

        // Adds table to stage
        addActor(table);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
