package gameLogic.resource;

import Util.Tuple;
import fvs.taxe.actor.TrainActor;
import gameLogic.map.IPositionable;
import gameLogic.map.Station;

import java.util.ArrayList;
import java.util.List;


public class Obstacle extends Resource {

    private int numberTurns;
    private IPositionable position;
    private Station station1; // if station 1 is the same as station 2, the station itself is blocked
    private Station station2; // otherwise, the track between the two stations is blocked
    private boolean active = false;


    public Obstacle(String name, int forTurns, Station station1, Station station2) {

        this.name = name;
        this.numberTurns = forTurns;
        this.station1 = station1;
        this.station2 = station2;

    }

    public void setActive(){
        this.active=true;
    }

    public void setInactive(){
        this.active=false;

    }

    public void setPosition(IPositionable position) {
        this.position = position;
        changed();
    }


    @Override
    public void dispose() {

    }
}