package uy.com.agm.gamethree.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Scaling;

import uy.com.agm.gamethree.actors.player.Hero;
import uy.com.agm.gamethree.assets.Assets;
import uy.com.agm.gamethree.assets.scene2d.AssetScene2d;
import uy.com.agm.gamethree.assets.scene2d.AssetStageCleared;
import uy.com.agm.gamethree.game.DebugConstants;
import uy.com.agm.gamethree.game.GameSettings;
import uy.com.agm.gamethree.game.LevelState;
import uy.com.agm.gamethree.screens.util.ScreenEnum;
import uy.com.agm.gamethree.screens.util.UIFactory;
import uy.com.agm.gamethree.tools.AudioManager;
import uy.com.agm.gamethree.widget.AnimatedImage;
import uy.com.agm.gamethree.widget.TypingLabelWorkaround;

/**
 * Created by AGM on 12/23/2017.
 */

public class LevelCompletedScreen extends AbstractScreen {
    private static final String TAG = LevelCompletedScreen.class.getName();

    // Constants
    private static final int PENALTY_COST = 200;

    private int currentLevel;
    private int nextLevel;
    private int currentScore;
    private int currentPenalties;
    private int finalScore;
    private int finalStars;
    private int gameScore;
    private TextureRegion highScoreImage;
    private boolean showNewHighScoreLabel;
    private boolean showNextLevelLabel;

    public LevelCompletedScreen(Integer currentLevel, Integer currentScore, Integer currentPenalties) {
        super();
        GameSettings prefs = GameSettings.getInstance();

        this.currentLevel = currentLevel;
        this.nextLevel = currentLevel + 1;
        this.currentScore = currentScore;
        this.currentPenalties = currentPenalties;
        this.finalScore = Math.max(this.currentScore - this.currentPenalties * PENALTY_COST, 0);
        this.finalStars = getFinalStars(this.currentPenalties);
        prefs.setLevelStateInfo(this.currentLevel, this.finalScore, this.finalStars);
        this.gameScore = getGameScore();
        this.highScoreImage = Assets.getInstance().getScene2d().getRankingImage(prefs.updateRanking(this.gameScore));
        this.showNewHighScoreLabel = this.highScoreImage != null;
        this.showNextLevelLabel = this.nextLevel <= GameSettings.MAX_LEVEL;
        if (this.showNextLevelLabel) {
            prefs.addNextLevel(this.nextLevel);
        }

        // Saves preferences
        prefs.save();

        // Audio FX
        AudioManager.getInstance().playSound(Assets.getInstance().getSounds().getApplause());
    }

    @Override
    public void buildStage() {
        defineMainTable();
        defineNavigationTable();
    }

    private void defineMainTable() {
        // UI assets
        AssetScene2d assetScene2d = Assets.getInstance().getScene2d();

        // I18n
        I18NBundle i18NGameThreeBundle = Assets.getInstance().getI18NGameThree().getI18NGameThreeBundle();

        // Set table structure
        Table table = new Table();

        // Design
        table.setBackground(new TextureRegionDrawable(assetScene2d.getTable()));

        // Debug lines
        table.setDebug(DebugConstants.DEBUG_LINES);

        // Top-Align table
        table.top();

        // Make the table fill the entire stage
        table.setFillParent(true);

        // Personal fonts
        Label.LabelStyle labelStyleBig = new Label.LabelStyle();
        labelStyleBig.font = Assets.getInstance().getFonts().getDefaultBig();

        Label.LabelStyle labelStyleNormal = new Label.LabelStyle();
        labelStyleNormal.font = Assets.getInstance().getFonts().getDefaultNormal();

        // Animation
        AnimatedImage animatedImage = new AnimatedImage(assetScene2d.getStageCleared().getStageClearedAnimation());
        animatedImage.setAlign(Align.center);

        // Define our labels based on labelStyle
        Label currentScoreLabel = new Label(i18NGameThreeBundle.format("levelCompleted.currentScore", currentScore), labelStyleNormal);
        Label penaltiesLabel = new Label(i18NGameThreeBundle.format("levelCompleted.penalties", currentPenalties), labelStyleNormal);
        TypingLabelWorkaround gameScoreLabel = new TypingLabelWorkaround(i18NGameThreeBundle.format("levelCompleted.gameScore", gameScore), labelStyleNormal);
        Label newHighScoreLabel = new Label(i18NGameThreeBundle.format("levelCompleted.newHighScore"), labelStyleNormal);
        TypingLabelWorkaround nextLevelLabel = new TypingLabelWorkaround(i18NGameThreeBundle.format("levelCompleted.nextLevel"), labelStyleNormal);
        TypingLabelWorkaround grandFinaleLabel = new TypingLabelWorkaround(i18NGameThreeBundle.format("levelCompleted.grandFinale"), labelStyleNormal);

        // Add values
        table.add(animatedImage).size(AssetStageCleared.WIDTH_PIXELS, AssetStageCleared.HEIGHT_PIXELS).padTop(AbstractScreen.PAD);
        table.row();
        table.add(currentScoreLabel).padTop(AbstractScreen.PAD);
        table.row();
        table.add(penaltiesLabel).padTop(AbstractScreen.PAD);
        table.row();
        table.add(gameScoreLabel).padTop(AbstractScreen.PAD);
        table.row();
        table.add(getStarsTable(assetScene2d.getStar(), assetScene2d.getEmptyStar(), finalStars)).padTop(AbstractScreen.PAD);
        table.row();
        float padTop = AbstractScreen.PAD * 2;
        if (showNewHighScoreLabel) {
            padTop = AbstractScreen.PAD;
            table.add(getNewHighScoreTable(highScoreImage, newHighScoreLabel)).padTop(AbstractScreen.PAD);
            table.row();
        }
        if (showNextLevelLabel) {
            table.row();
            table.add(nextLevelLabel).padTop(padTop);

            // Events
            nextLevelLabel.addListener(UIFactory.screenNavigationListener(ScreenEnum.PLAY_GAME, nextLevel));
        } else {
            table.row();
            table.add(grandFinaleLabel).padTop(padTop);

            // Events
            grandFinaleLabel.addListener(UIFactory.screenNavigationListener(ScreenEnum.GRAND_FINALE));
        }
        table.padTop(padTop);

        // Adds table to stage
        addActor(table);
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

    private Table getNewHighScoreTable(TextureRegion highScoreImage, Label highScoreLabel) {
        Image image = new Image();
        image.setDrawable(new TextureRegionDrawable(highScoreImage));
        image.setScaling(Scaling.fit);

        Table table = new Table();
        table.setDebug(DebugConstants.DEBUG_LINES);
        table.add(image);
        table.add(highScoreLabel).padLeft(AbstractScreen.PAD / 2);

        return table;
    }

    private int getGameScore() {
        int gameScore = 0;
        for(LevelState levelState : GameSettings.getInstance().getLevels()) {
            gameScore += levelState.getFinalScore();
        }
        return gameScore;
    }

    private int getFinalStars(int currentPenalties) {
        int averageLevelPenalty = Hero.LIVES_START * Hero.ENDURANCE_START / 2;
        int stars = 0;

        if (currentPenalties == 0) {
            stars = 3;
        } else {
            if (currentPenalties < averageLevelPenalty) {
                stars = 2;
            } else {
                stars = 1;
            }
        }

        return stars;
    }

    private Table getStarsTable(TextureRegion star, TextureRegion emptyStar, int stars) {
        Table table = new Table();
        table.setDebug(DebugConstants.DEBUG_LINES);
        Image image;
        for (int i = 1; i <= 3; i++) {
            image = new Image();
            image.setDrawable(new TextureRegionDrawable(i <= stars ? star : emptyStar));
            image.setScaling(Scaling.fit);
            table.add(image);
        }
        return table;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
