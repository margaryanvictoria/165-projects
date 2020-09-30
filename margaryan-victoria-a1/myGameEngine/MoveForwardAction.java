package myGameEngine;

import a1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveForwardAction extends AbstractInputAction {
    private MyGame game;
    private Camera camera;

    public MoveForwardAction(MyGame g, Camera c) {
        game = g;
        camera = c;
    }

    public void performAction(float time, Event e) {
        char mode = camera.getMode(); //get the camera's mode, c for global, n for node
        Node parent = camera.getParentNode().getParent();

        if (mode == 'c') {
            Vector3f v = camera.getFd(); //gets our forward vector
            Vector3f p = camera.getPo(); //gets our camera's position
            Vector3f p1 = (Vector3f) Vector3f.createFrom(0.1f*v.x(), 0.1f*v.y(), 0.1f*v.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1);
            /* if (game.dolphinTetherCheck()) {
                camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
                //game.updateCamera(camera);
            } else {
                //use clamp to lower it
                camera.setPo(p);
            } */
            //camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
            //game.dolphinTetherCheck();
            camera.setPo(game.dolphinTetherCheck((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z())));
        } else if (mode == 'n') {
            parent.moveForward(0.1f);
        }
    }
}