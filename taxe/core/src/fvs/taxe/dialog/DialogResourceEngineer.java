package fvs.taxe.dialog;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import fvs.taxe.Button;
import gameLogic.resource.Engineer;
import gameLogic.resource.Obstacle;

import java.util.ArrayList;
import java.util.List;

public class DialogResourceEngineer extends Dialog{
    private List<ResourceDialogClickListener> clickListeners = new ArrayList<ResourceDialogClickListener>();

    public DialogResourceEngineer(Engineer engineer, Skin skin) {
        super(engineer.toString(), skin);

        text("What do you want to do with this engineer?");

        button("Cancel", "CLOSE");
        button("Drop", "DROP");
        button("Repair a blocked connection", "PLACE");
    }

    @Override
    public Dialog show (Stage stage) {
        show(stage, null);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    @Override
    public void hide () {
        hide(null);
    }

    private void clicked(Button button) {
        for(ResourceDialogClickListener listener : clickListeners) {
            listener.clicked(button);
        }
    }

    public void subscribeClick(ResourceDialogClickListener listener) {
        clickListeners.add(listener);
    }

    @Override
    protected void result(Object obj) {
        if (obj == "CLOSE") {
            this.remove();
        } else if (obj == "DROP") {
            clicked(Button.ENGINEER_DROP);
        } else if(obj == "PLACE") {
            clicked(Button.ENGINEER_USE);
        }
    }
}
