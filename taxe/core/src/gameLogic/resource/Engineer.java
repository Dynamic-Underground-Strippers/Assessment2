package gameLogic.resource;


import gameLogic.map.Connection;
import gameLogic.map.Station;

public class Engineer extends Resource{
    private Station station1;
    private Station station2;
    private boolean placing;

    public Engineer()
    {
        this.name="Engineer";
        this.station1 = null;
        this.station2 = null;
        this.placing = false;
    }

    public void use(Connection connection)
    {
        connection.setBlocked(0);
    }

    public Station getStation1() {
        return station1;
    }

    public void setStation1(Station station1) {
        this.station1 = station1;
    }

    public Station getStation2() {
        return station2;
    }

    public void setStation2(Station station2) {
        this.station2 = station2;
    }

    public boolean isPlacing() {
        return placing;
    }

    public void setPlacing(boolean isPlacing) {
        this.placing = isPlacing;
    }

    @Override
    public void dispose() {

    }
}
