package myGameEngine;

import a1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class PitchUDAction extends AbstractInputAction {
    private MyGame game;
    private Camera camera;

    public PitchUDAction(MyGame g, Camera c) {
        game = g;
        camera = c;
    }

    public void performAction(float time, Event e) {
        char mode = camera.getMode(); //get the camera's mode, c for global, n for node
        Node parent = camera.getParentNode().getParent();

        float value = -(e.getValue()); //negate the value cause for some reason it's being super flippy

        // acount for deadzones
        if(value > .1f || value < -.1f) {
            if (mode == 'c') {
                Vector3f u = camera.getRt(); //gets our sideways vector
                Vector3f v = camera.getUp(); //gets our up vector
                Vector3f n = camera.getFd(); //gets our forward vector

                //Vector3f u = (Vector3f) Vector3f.createFrom(1.0f, 0.0f, 0.0f);

                Angle degrees = Degreef.createFrom(value*1.0f);
                
                Vector3f n1 = (Vector3f) n.rotate(degrees, u).normalize();
                Vector3f v1 = (Vector3f) v.rotate(degrees, u).normalize();

                camera.setFd(n1);
                camera.setUp(v1);

                game.updateCamera(camera);

            } else if (mode == 'n') {
                //parent.moveForward(0.1f);
                Vector3f side = (Vector3f) parent.getLocalRightAxis(); //gets our up vector
                Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(value*-1.0f), side);
                parent.setLocalRotation(matRot.mult(parent.getWorldRotation()));
            }
        }
    }
}