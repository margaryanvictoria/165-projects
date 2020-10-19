package myGameEngine;

import a2.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class YawRight extends AbstractInputAction {
    private SceneNode avN;

    public YawRight(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        Vector3f up = (Vector3f) avN.getLocalUpAxis(); //gets our up vector
        Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), up);
        avN.setLocalRotation(matRot.mult(avN.getWorldRotation()));
    }
}