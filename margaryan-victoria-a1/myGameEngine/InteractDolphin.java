package myGameEngine;

import a1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class InteractDolphin extends AbstractInputAction {
    private MyGame game;
    private Camera camera;

    public InteractDolphin(MyGame g, Camera c) {
        game = g;
        camera = c;
    }

    public void performAction(float time, Event e) {
        char mode = camera.getMode(); //get the camera's mode, c for global, n for node
        //Node parent = camera.getParentNode();
        SceneNode rootNode = game.getEngine().getSceneManager().getRootSceneNode();
        SceneNode cameraNode = game.getEngine().getSceneManager().getSceneNode("MainCameraNode");
        SceneNode dolphinNode = game.getEngine().getSceneManager().getSceneNode("myDolphinNode");
        //Vector3f worldUp = Vector3f.createFrom(0.0f, 1.0f, 0.0f);

        if (mode == 'c') {
            Vector3f cp = camera.getPo(); //gets our camera's position
            Vector3f dp = (Vector3f) dolphinNode.getWorldPosition(); //gets our dolphin's position
            
            // are we right next to the dolphin
            if(checkBounds(cp.x(), dp.x()) && checkBounds(cp.y(), dp.y()) && checkBounds(cp.z(), dp.z())) {
                //set us on the dolphins back
                camera.setMode('n');
                //dolphinNode.attachObject(camera);
                rootNode.detachChild(cameraNode);
                dolphinNode.attachChild(cameraNode);
                cameraNode.setLocalPosition(0f, .25f, -.25f); //offset character on dolphin's back

                //Vector3f cpDolphin = (Vector3f) Vector3f.createFrom(1.0f*dp.x(), 1.2f*dp.y(), 1.0f*dp.z());
                //camera.setPo(cpDolphin); //set new position
                /* Vector3f v = camera.getFd(); //gets our forward vector
                Vector3f p = camera.getPo(); //gets our camera's position
                Vector3f p1 = (Vector3f) Vector3f.createFrom(0.2f*v.x(), 0.5f*v.y(), 0.2f*v.z());
                Vector3f p2 = (Vector3f) p.add((Vector3)p1);
                camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z())); */

                game.updateCamera(camera); //update camera
            } else {
                //do nothing
                System.out.println("You're too far from the dolphin!");
            }
           /*  Vector3f p = camera.getPo(); //gets our camera's position
            Vector3f p1 = (Vector3f) Vector3f.createFrom(0.2f*v.x(), 0.2f*v.y(), 0.2f*v.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1);
            camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));

            Vector3f u = camera.getRt(); //gets our sideways vector
            Vector3f v = camera.getUp(); //gets our up vector
            Vector3f n = camera.getFd(); //gets our forward vector

            Angle degrees10 = Degreef.createFrom(-10.0f);
            
            Vector3f n1 = (Vector3f) n.rotate(degrees10, u);
            Vector3f v1 = (Vector3f) v.rotate(degrees10, u);

            camera.setFd(n1);
            camera.setUp(v1);

            game.updateCamera(camera); */

        } else if (mode == 'n') {
            //Vector3f cp = camera.getPo(); //gets our camera's position
            //camera.setPo((Vector3f) Vector3f.createFrom(cp.x()-1.0f, cp.y()+2.0f, cp.z()));
            camera.setMode('c');
            Vector3f u = camera.getRt();
            Vector3 p = Vector3f.createFrom(.2f*u.x(), 0.0f*u.y(), 0.0f*u.z());
            camera.setPo((Vector3f)(dolphinNode.getWorldPosition()).add(p));

            /* Vector3f p = camera.getPo(); //gets our camera's position
            Vector3f p1 = (Vector3f) Vector3f.createFrom(p.x(), value*0.1f*v.y(), value*0.1f*v.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1); */

            //cameraNode.attachObject(camera);
            dolphinNode.detachChild(cameraNode);
            rootNode.attachChild(cameraNode);
            //camera.setPo((Vector3f) Vector3f.createFrom(cp.x()-1.0f, cp.y()+2.0f, cp.z()));

            game.updateCamera(camera);
        }
        game.sanityNodeCheck(rootNode, cameraNode, dolphinNode);
    }

    public boolean checkBounds(float value, float target) {
        float difference = target - value;

        if (-1.5f <= difference && difference <= 1.5f) {
            return true;
        }

        return false;
    }
}