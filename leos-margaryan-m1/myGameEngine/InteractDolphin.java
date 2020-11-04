package myGameEngine;

import m1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class InteractDolphin extends AbstractInputAction {
    private SceneNode avN;

    public InteractDolphin(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        //Vector3f pos = (Vector3f) avN.getLocalPosition();
        //slerp((avN.setLocalPosition(pos.x(), pos.y()+1.0f, pos.z())), 100.0f);
        //slerp((avN.setLocalPosition(pos.x(), pos.y()-1.0f, pos.z())), 100.0f);    
        Vector3 pos = avN.getLocalPosition();
        //avN.setLocalPosition(avN.getLocalPosition().lerp(pos.add(0f,1f,0f), 100.0f));
        //avN.setLocalPosition(avN.getLocalPosition().lerp(pos, 100.0f));
    }

    public boolean checkBounds(float value, float target) {
        float difference = target - value;

        if (-1.5f <= difference && difference <= 1.5f) {
            return true;
        }

        return false;
    }
}