package myGameEngine;

import a2.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class YawLRAction extends AbstractInputAction {
    private SceneNode avN;

    public YawLRAction(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        float value = e.getValue();

        /* if(value > .1f || value < -.1f) {
            Vector3f up = (Vector3f) avN.getLocalUpAxis();
            Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(value*-1.0f), up);
            avN.setLocalRotation(matRot.mult(avN.getWorldRotation()));
        } */
        //System.out.println(e.getComponent().toString());
        if (e.getComponent().toString().compareTo("Button 4") == 0) {
            //
        } else if (e.getComponent().toString().compareTo("Button 5") == 0) {
            value = -value;
        }
        Vector3f up = (Vector3f) avN.getLocalUpAxis();
        Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(value*1.0f), up);
        avN.setLocalRotation(matRot.mult(avN.getWorldRotation()));
    }
}