package uy.com.agm.gamethree.screens;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Scaling;

import uy.com.agm.gamethree.assets.Assets;
import uy.com.agm.gamethree.assets.scene2d.AssetScene2d;
import uy.com.agm.gamethree.game.DebugConstants;
import uy.com.agm.gamethree.game.GameSettings;
import uy.com.agm.gamethree.game.LevelState;
import uy.com.agm.gamethree.screens.util.ScreenEnum;
import uy.com.agm.gamethree.screens.util.UIFactory;
import uy.com.agm.gamethree.tools.LevelFactory;
import uy.com.agm.gamethree.widget.TypingLabelWorkaround;

/**
 * Created by AGM on 12/23/2017.
 */

public class SelectLevelScreen extends AbstractScreen {
    private static final String TAG = SelectLevelScreen.class.getName();

    // Constants
    public static final float SCROLL_PANE_MAX_HEIGHT = 650.0f;
    public static final float CONTAINER_WIDTH = 400.0f;
    public static final float STAR_WIDTH = 30.0f;
    public static final float STAR_HEIGHT = 100.0f;
    public static final float STARS_TABLE_WIDTH = 300.0f;

    public SelectLevelScreen() {
        super();
    }

    @Override
    public void buildStage() {
        defineMainTable();
        defineNavigationTable();
    }

    private void defineMainTable() {
        // I18n
        I18NBundle i18NGameThreeBundle = Assets.getInstance().getI18NGameThree().getI18NGameThreeBundle();

        // Set table structure
        Table container = new Table();

        // Design
        container.setBackground(new TextureRegionDrawable(Assets.getInstance().getScene2d().getTable()));

        // Debug lines
        container.setDebug(DebugConstants.DEBUG_LINES);

        // Center-Align table
        container.center().padBottom(AbstractScreen.PAD * 2);

        // Make the table fill the entire stage
        container.setFillParent(true);

        // Personal fonts
        Label.LabelStyle labelStyleBig = new Label.LabelStyle();
        labelStyleBig.font = Assets.getInstance().getFonts().getDefaultBig();

        // Define our labels based on labelStyle
        Label selectLevelLabel = new Label(i18NGameThreeBundle.format("selectLevel.title"), labelStyleBig);

        // Define a ScrollPane with scrollbars
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch vScrollKnobNine = new NinePatch(Assets.getInstance().getScene2d().getvScrollbarKnob9(), 2, 2, 2, 2);
        NinePatch vScrollNine = new NinePatch(Assets.getInstance().getScene2d().getvScrollbar9(), 2, 2, 2, 2);
        NinePatch hScrollKnobNine = new NinePatch(Assets.getInstance().getScene2d().gethScrollbarKnob9(), 2, 2, 2, 2);
        NinePatch hScrollNine = new NinePatch(Assets.getInstance().getScene2d().gethScrollbar9(), 2, 2, 2, 2);

        scrollStyle.vScrollKnob = new NinePatchDrawable(vScrollKnobNine);
        scrollStyle.vScroll = new NinePatchDrawable(vScrollNine);
        scrollStyle.hScrollKnob = new NinePatchDrawable(hScrollKnobNine);
        scrollStyle.hScroll = new NinePatchDrawable(hScrollNine);

        ScrollPane scrollPane = new ScrollPane(defineLevelsTable(), scrollStyle);

        // Add values
        container.add(selectLevelLabel);
        container.row();
        container.add(scrollPane).top().maxHeight(SCROLL_PANE_MAX_HEIGHT).width(CONTAINER_WIDTH);

        // Adds table to stage
        addActor(container);
    }

    private Table defineLevelsTable() {
        // UI assets
        AssetScene2d assetScene2d = Assets.getInstance().getScene2d();

        // I18n
        I18NBundle i18NGameThreeBundle = Assets.getInstance().getI18NGameThree().getI18NGameThreeBundle();

        // Set table structure
        Table table = new Table();

        // Debug lines
        table.setDebug(DebugConstants.DEBUG_LINES);

        // Personal fonts
        Label.LabelStyle labelStyleNormal = new Label.LabelStyle();
        labelStyleNormal.font = Assets.getInstance().getFonts().getDefaultNormal();

        // Add values
        int level;
        Label levelLabel;
        Table levelTable;
        TextureRegion star = assetScene2d.getStar();
        TextureRegion emptyStar = assetScene2d.getEmptyStar();
        GameSettings prefs = GameSettings.getInstance();
        for (LevelState levelState : prefs.getLevels().values()) {
            table.row();
            level = levelState.getLevel();
            levelLabel = new Label(LevelFactory.getLevelName(level), labelStyleNormal);
            levelTable = getStarsTable(levelLabel, star, emptyStar, levelState.getFinalStars());
            table.add(levelTable);

            // Events
            levelTable.addListener(UIFactory.screenNavigationListener(ScreenEnum.PLAY_GAME, level,
                    levelState.getInitialLives(),
                    levelState.getInitialScore()));
        }

        if (prefs.isGameComplete()) {
            table.row();
            TypingLabelWorkaround grandFinaleLabel = new TypingLabelWorkaround(i18NGameThreeBundle.format("selectLevel.grandFinale"), labelStyleNormal);
            table.add(grandFinaleLabel).padTop(AbstractScreen.PAD);

            // Events
            grandFinaleLabel.addListener(UIFactory.screenNavigationListener(ScreenEnum.GRAND_FINALE));
        }

        return table;
    }

    private void defineNavigationTable() {
        // UI assets
        AssetScene2d assetScene2d = Assets.getInstance().getScene2d();

        // Set table structure
        Table table = new Table();

        // Debug lines
        table.setDebug(DebugConstants.DEBUG_LINES);

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
        back.addListener(UIFactory.screenNavigationListener(ScreenEnum.MAIN_MENU));

        // Adds table to stage
        addActor(table);
    }

    private Table getStarsTable(Label levelLabel, TextureRegion star, TextureRegion emptyStar, int stars) {
        Stack stack = new Stack();
        Table starsTable = new Table();
        starsTable.center();
        levelLabel.setAlignment(Align.center);
        Image image;
        if (stars > 0) {
            for (int i = 1; i <= 3; i++) {
                image = new Image();
                image.setDrawable(new TextureRegionDrawable(i <= stars ? star : emptyStar));
                image.setAlign(Align.bottom);
                image.setScaling(Scaling.fit);
                starsTable.add(image).size(STAR_WIDTH, STAR_HEIGHT);
            }
            stack.add(starsTable);
        }
        stack.add(levelLabel);

        Table table = new Table();
        starsTable.setDebug(DebugConstants.DEBUG_LINES);
        table.add(stack).size(STARS_TABLE_WIDTH, STAR_HEIGHT);

        return table;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
