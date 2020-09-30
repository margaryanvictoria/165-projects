package myGameEngine;

import a1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class YawRight extends AbstractInputAction {
    private MyGame game;
    private Camera camera;

    public YawRight(MyGame g, Camera c) {
        game = g;
        camera = c;
    }

    public void performAction(float time, Event e) {
        char mode = camera.getMode(); //get the camera's mode, c for global, n for node
        Node parent = camera.getParentNode().getParent();
        //Vector3f worldUp = Vector3f.createFrom(0.0f, 1.0f, 0.0f);

        if (mode == 'c') {
            Vector3f v = camera.getUp(); //gets our up vector
            Vector3f n = camera.getFd(); //gets our forward vector
            Vector3f u = camera.getRt(); //gets our sideways vector

            Angle degrees = Degreef.createFrom(-1.0f);

            //Vector3f v = (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f);
            
            Vector3f n1 = (Vector3f) n.rotate(degrees, v).normalize();
            Vector3f u1 = (Vector3f) u.rotate(degrees, v).normalize();

            camera.setFd(n1);
            camera.setRt(u1);
            
            game.updateCamera(camera);

        } else if (mode == 'n') {
            //parent.moveForward(0.1f);
            //Vector3 worldUp = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
            Vector3f up = (Vector3f) parent.getLocalUpAxis(); //gets our up vector
            Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), up);
            parent.setLocalRotation(matRot.mult(parent.getWorldRotation()));
        }
    }
}