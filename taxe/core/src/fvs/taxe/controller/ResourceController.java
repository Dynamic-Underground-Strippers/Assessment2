package fvs.taxe.controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import fvs.taxe.TaxeGame;
import fvs.taxe.dialog.EngineerClicked;
import fvs.taxe.dialog.SkipClicked;
import fvs.taxe.dialog.ObstacleClicked;
import fvs.taxe.dialog.TrainClicked;
import gameLogic.Player;
import gameLogic.PlayerChangedListener;
import gameLogic.resource.*;

import javax.swing.*;
import java.util.ArrayList;

public class ResourceController {
    private Context context;
    private Group resourceButtons = new Group();

    public ResourceController(final Context context) {
        this.context = context;

        context.getGameLogic().getPlayerManager().subscribePlayerChanged(new PlayerChangedListener() {
            @Override
            public void changed() {
                drawPlayerResources(context.getGameLogic().getPlayerManager().getCurrentPlayer());
            }
        });
    }

    public void drawHeaderText() {
        TaxeGame game = context.getTaxeGame();

        game.batch.begin();
        game.fontSmall.setColor(Color.BLACK);
        game.fontSmall.draw(game.batch, "Unplaced Resources:", 10.0f, (float) TaxeGame.HEIGHT - 250.0f);
        game.batch.end();
    }

    public void drawPlayerResources(Player player) {

        float top = (float) TaxeGame.HEIGHT;
        float x = 10.0f;
        float y = top - 250.0f;
        y -= 50;

        resourceButtons.remove();
        resourceButtons.clear();

        for (final Resource resource : player.getResources()) {
            if (resource instanceof Train) {
                Train train = (Train) resource;

                // don't show a button for trains that have been placed, trains placed are still part of the 7 total upgrades
                if (train.getPosition() != null) {
                    continue;
                }

                TrainClicked listener = new TrainClicked(context, train);

                TextButton button = new TextButton(resource.toString(), context.getSkin());
                button.setPosition(x, y);
                button.addListener(listener);
                resourceButtons.addActor(button);

                y -= 30;
            }else

            if(resource instanceof Obstacle) {
                Obstacle obstacle = (Obstacle) resource;
                ObstacleClicked listener = new ObstacleClicked(context,obstacle);
                TextButton button = new TextButton("Obstacle", context.getSkin());
                button.setPosition(x, y);
                button.addListener(listener);
                resourceButtons.addActor(button);

                y -= 30;

            } else if(resource instanceof Skip) {
                Skip skip = (Skip) resource;
                SkipClicked listener = new SkipClicked(context, skip);
                TextButton button = new TextButton("Skip", context.getSkin());
                button.setPosition(x, y);
                button.addListener(listener);
                resourceButtons.addActor(button);

                y -= 30;

            } else if(resource instanceof Engineer){
                Engineer engineer = (Engineer) resource;
                EngineerClicked listener = new EngineerClicked(context,engineer);
                TextButton button = new TextButton("Engineer", context.getSkin());
                button.setPosition(x, y);
                button.addListener(listener);
                resourceButtons.addActor(button);

                y -= 30;
            }

        }

        context.getStage().addActor(resourceButtons);
    }

}
