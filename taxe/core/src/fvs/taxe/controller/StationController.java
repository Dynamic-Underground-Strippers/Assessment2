package fvs.taxe.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fvs.taxe.StationClickListener;
import fvs.taxe.TaxeGame;
import fvs.taxe.Tooltip;
import fvs.taxe.actor.CollisionStationActor;
import fvs.taxe.actor.StationActor;
import fvs.taxe.dialog.DialogStationMultitrain;
import fvs.taxe.dialog.TrainClicked;
import gameLogic.Game;
import gameLogic.GameState;
import gameLogic.Player;
import gameLogic.goal.Goal;
import gameLogic.map.CollisionStation;
import gameLogic.map.Connection;
import gameLogic.map.IPositionable;
import gameLogic.map.Station;
import gameLogic.resource.Resource;
import gameLogic.resource.Train;

public class StationController {
	public final static int CONNECTION_LINE_WIDTH = 5;

	private Context context;
	private Tooltip tooltip;
	/*
	have to use CopyOnWriteArrayList because when we iterate through our listeners and execute
	their handler's method, one case unsubscribes from the event removing itself from this list
	and this list implementation supports removing elements whilst iterating through it
	*/
	private static List<StationClickListener> stationClickListeners = new CopyOnWriteArrayList<StationClickListener>();
	private Color translucentBlack = new Color(0, 0, 0, 0.8f);
	private static final Texture[] blockageTextures = new Texture[5];

	public StationController(Context context, Tooltip tooltip) {
		this.context = context;
		this.tooltip = tooltip;
		for (int i = 0; i < 5; i++) {
			blockageTextures[i] = new Texture(Gdx.files.internal("blockage" + (i + 1) + ".png"));
		}
	}

	public static void subscribeStationClick(StationClickListener listener) {
		stationClickListeners.add(listener);
	}

	public static void unsubscribeStationClick(StationClickListener listener) {
		stationClickListeners.remove(listener);
	}

	private static void stationClicked(Station station) {
		for (StationClickListener listener : stationClickListeners) {
			listener.clicked(station);
		}
	}


	private void renderStation(final Station station) {
		final StationActor stationActor = new StationActor(station.getLocation(), station);

		stationActor.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (Game.getInstance().getState() == GameState.NORMAL) {
					ArrayList<Train> trains = new ArrayList<Train>();
					for (Player player : context.getGameLogic().getPlayerManager()
												.getAllPlayers()) {
						for (Resource resource : player.getResources()) {
							if (resource instanceof Train) {
								if (((Train) resource).getPosition() == station.getLocation()) {
									trains.add((Train) resource);
								}
							}
						}
					}
					if (trains.size() == 1) {
						TrainClicked clicker = new TrainClicked(context, trains.get(0));
						clicker.clicked(null, -1, 0);
					} else if (trains.size() > 0) {
						DialogStationMultitrain dia = new DialogStationMultitrain(trains,
								context.getSkin(), context);
						dia.show(context.getStage());
					}
				}
				stationClicked(station);
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				tooltip.setPosition(stationActor.getX() + 20, stationActor.getY() + 20);
				tooltip.show(station.getName());
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				tooltip.hide();
			}
		});

		station.setActor(stationActor);

		context.getStage().addActor(stationActor);
	}

	private void renderCollisionStation(final Station collisionStation) {
		final CollisionStationActor collisionStationActor = new CollisionStationActor(
				collisionStation.getLocation());

		collisionStationActor.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				stationClicked(collisionStation);
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				tooltip.setPosition(collisionStationActor.getX() + 10,
						collisionStationActor.getY() + 10);
				tooltip.show("Junction");
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				tooltip.hide();
			}
		});

		context.getStage().addActor(collisionStationActor);
	}

	public static Color[] colours = {Color.ORANGE, Color.GREEN, Color.PURPLE};

	public void renderStationGoalHighlights() {
		List<Station> stations = context.getGameLogic().getMap().getStations();
		ArrayList<StationHighlight> list = new ArrayList<StationHighlight>();
		for (Station station : stations) {
			if (Game.getInstance().getState() == GameState.PLACING_TRAIN ||
					Game.getInstance().getState() == GameState.ROUTING) {
				int index = 0;
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				for (Goal goal : Game.getInstance().getPlayerManager().getCurrentPlayer()
									 .getGoals()) {
					if (!goal.getComplete()) {
						if (goal.getOrigin().equals(station) ||
								goal.getDestination().equals(station) ||
								goal.getIntermediary().equals(station)) {
							int radius;
							if (map.containsKey(station.getName())) {
								radius = map.get(station.getName()) + 5;
							} else {
								radius = 15;
							}
							map.put(station.getName(), radius);
							list.add(new StationHighlight(station, radius, colours[index]));
						}
						index++;
					}
				}
			}
		}
		Collections.sort(list);
		Collections.reverse(list);
		TaxeGame game = context.getTaxeGame();
		for (StationHighlight sh : list) {
			game.shapeRenderer.begin(ShapeType.Filled);
			game.shapeRenderer.setColor(sh.getColour());
			game.shapeRenderer.circle(sh.getStation().getLocation().getX(),
					sh.getStation().getLocation().getY(), sh.getRadius());
			game.shapeRenderer.end();
		}
	}

	class StationHighlight implements Comparable<StationHighlight> {
		private final Station station;
		private final int radius;
		private final Color colour;

		StationHighlight(Station station, int radius, Color colour) {
			this.station = station;
			this.radius = radius;
			this.colour = colour;
		}

		@Override
		public int compareTo(StationHighlight o) {
			return radius - o.radius;
		}

		public Color getColour() {
			return colour;
		}

		public int getRadius() {
			return radius;
		}

		public Station getStation() {
			return station;
		}
	}

	public void renderStations() {
		List<Station> stations = context.getGameLogic().getMap().getStations();

		for (Station station : stations) {
			if (station instanceof CollisionStation) {
				renderCollisionStation(station);
			} else {
				renderStation(station);
			}
		}
		renderStationGoalHighlights();
	}

	public void renderConnections(List<Connection> connections, Color color) {
		TaxeGame game = context.getTaxeGame();

		game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		for (Connection connection : connections) {
			IPositionable start = connection.getStation1().getLocation();
			IPositionable end = connection.getStation2().getLocation();
			game.shapeRenderer.setColor(color);
			game.shapeRenderer.rectLine(start.getX(), start.getY(), end.getX(), end.getY(),
					CONNECTION_LINE_WIDTH);
		}
		game.shapeRenderer.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		game.shapeRenderer.setColor(translucentBlack);

		// draw an icon on connections that are blocked, showing how many turns remain until they
		// become unblocked
		for (Connection connection : connections) {
			IPositionable midpoint = connection.getMidpoint();
			if (connection.isBlocked()) {
				game.batch.begin();
				game.batch.draw(blockageTextures[connection.getTurnsBlocked() - 1],
						midpoint.getX() - 16, midpoint.getY() - 16, 32, 32);
				game.batch.end();
			}
		}
		game.shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);

		// draw a number on each blocked connection, indicating how many turns remain until they
		// become unblocked
		// if the game is in routing mode, then the length of the connection is displayed
		for (Connection connection : connections) {
			if (connection.isBlocked()) {
				IPositionable midpoint = connection.getMidpoint();
				game.batch.begin();
				game.fontSmall.setColor(Color.WHITE);
				game.fontSmall.draw(game.batch, String.valueOf(connection.getTurnsBlocked()),
						midpoint.getX() - 5, midpoint.getY() + 7);
				game.batch.end();
			} else if (Game.getInstance().getState() == GameState.ROUTING) {
				IPositionable midpoint = connection.getMidpoint();
				game.batch.begin();
				game.fontTiny.setColor(Color.BLACK);
				String text = String.valueOf(Math.round(
						Vector2.dst(connection.getStation1().getLocation().getX(),
								connection.getStation1().getLocation().getY(),
								connection.getStation2().getLocation().getX(),
								connection.getStation2().getLocation().getY())));
				game.fontTiny.draw(game.batch, text,
						midpoint.getX() - game.fontTiny.getBounds(text).width / 2f,
						midpoint.getY() + game.fontTiny.getBounds(text).height / 2f);
				game.batch.end();
			}
		}
	}

	public void displayNumberOfTrainsAtStations() {
		TaxeGame game = context.getTaxeGame();
		game.batch.begin();
		game.fontSmall.setColor(Color.BLACK);

		for (Station station : context.getGameLogic().getMap().getStations()) {
			if (trainsAtStation(station) > 0) {
				game.fontSmall.draw(game.batch, trainsAtStation(station) + "",
						(float) station.getLocation().getX() - 6,
						(float) station.getLocation().getY() + 26);
			}
		}

		game.batch.end();
	}

	private int trainsAtStation(Station station) {
		int count = 0;

		for (Player player : context.getGameLogic().getPlayerManager().getAllPlayers()) {
			for (Resource resource : player.getResources()) {
				if (resource instanceof Train) {
					if (((Train) resource).getActor() != null) {
						if (((Train) resource).getPosition().equals(station.getLocation())) {
							count++;
						}
					}
				}
			}
		}
		return count;
	}
}
