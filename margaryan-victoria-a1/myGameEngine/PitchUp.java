package myGameEngine;

import a1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class PitchUp extends AbstractInputAction {
    private MyGame game;
    private Camera camera;

    public PitchUp(MyGame g, Camera c) {
        game = g;
        camera = c;
    }

    public void performAction(float time, Event e) {
        char mode = camera.getMode(); //get the camera's mode, c for global, n for node
        Node parent = camera.getParentNode().getParent();
        //Vector3f worldUp = Vector3f.createFrom(0.0f, 1.0f, 0.0f);

        if (mode == 'c') {
            Vector3f u = camera.getRt(); //gets our sideways vector
            Vector3f v = camera.getUp(); //gets our up vector
            Vector3f n = camera.getFd(); //gets our forward vector

            //Vector3f u = (Vector3f) Vector3f.createFrom(1.0f, 0.0f, 0.0f);

            Angle degrees = Degreef.createFrom(1.0f);
            
            Vector3f n1 = (Vector3f) n.rotate(degrees, u).normalize();
            Vector3f v1 = (Vector3f) v.rotate(degrees, u).normalize();

            camera.setFd(n1);
            camera.setUp(v1);

            game.updateCamera(camera);

        } else if (mode == 'n') {
            //parent.moveForward(0.1f);
            //Vector3 worldUp = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
            Vector3f side = (Vector3f) parent.getLocalRightAxis(); //gets our up vector
            Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), side);
            parent.setLocalRotation(matRot.mult(parent.getWorldRotation()));
        }
    }
}