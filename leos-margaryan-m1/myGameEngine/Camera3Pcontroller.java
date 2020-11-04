package myGameEngine;

import m1.MyGame; //import the game
import ray.rage.scene.*;
import ray.rml.*;
import ray.input.*;
import java.util.ArrayList;
import net.java.games.input.Controller;
import ray.input.action.*;

//import net.java.games.input.Controller;

public class Camera3Pcontroller {
    private Camera camera; //the camera being controlled
    private SceneNode cameraN; //the node the camera is attached to
    private SceneNode target; //the target the camera looks at
    private float cameraAzimuth; //rotation of camera around Y axis
    private float cameraElevation; //elevation of camera above target
    private float radias; //distance between camera and target
    private Vector3 targetPos; //target's position in the world
    private Vector3 worldUpVec;

    public Camera3Pcontroller(Camera cam, SceneNode camN, SceneNode targ, String controllerType, InputManager im) {
        camera = cam;
        cameraN = camN;
        target = targ;
        //cameraAzimuth = 225.0f; // start from BEHIND and ABOVE the target
        cameraAzimuth = 0.0f;
        cameraElevation = 20.0f; // elevation is in degrees
        radias = 2.0f;
        worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
        setupInput(im, controllerType);
        updateCameraPosition();
    }

    // Updates camera position: computes azimuth, elevation, and distance
    // relative to the target in spherical coordinates, then converts those
    // to world Cartesian coordinates and setting the camera position
    public void updateCameraPosition() {
        double theta = Math.toRadians(cameraAzimuth); // rot around target
        double phi = Math.toRadians(cameraElevation); // altitude angle
        double x = radias * Math.cos(phi) * Math.sin(theta);
        double y = radias * Math.sin(phi);
        double z = radias * Math.cos(phi) * Math.cos(theta);
        cameraN.setLocalPosition(Vector3f.createFrom((float)x, (float)y, (float)z).add(target.getWorldPosition()));
        cameraN.lookAt(target, worldUpVec);
    }

    private void setupInput(InputManager im, String cT) {
        //for now? ignoring cT
        ArrayList<Controller> controllers = im.getControllers(); //get all our controllers

        //camera.setMode('n');
        //Controller.Type controllerType;
        //if (cT.equals("keyboard")) {}
        //boolean mouseFlag = (cT.equals("mouse") ? true : false);
        //boolean gamepadFlag = (cT.equals("gamepad") ? true : false);
        
        Action orbitAAction = new OrbitAroundAction();
        Action orbitRadiasAction = new OrbitRadiasAction();
        Action orbitElevationAction = new OrbitElevationAction();

        boolean keyboard = ((cT.compareTo("keyboard") == 0) ? true : false);

        for (Controller c : controllers) {
            if(keyboard) {
                if (c.getType() == Controller.Type.KEYBOARD) {
                    im.associateAction(c, net.java.games.input.Component.Identifier.Key.LEFT, orbitAAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Key.RIGHT, orbitAAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Key.UP, orbitElevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Key.DOWN, orbitElevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Key.Z, orbitRadiasAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Key.X, orbitRadiasAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                } /* else if (c.getType() == Controller.Type.MOUSE) {
                    im.associateAction(c, net.java.games.input.Component.Identifier.Axis.SLIDER, orbitRadiasAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Axis.SLIDER_VELOCITY, orbitRadiasAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Axis.SLIDER_ACCELERATION, orbitRadiasAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
                } */
            } else {
                if(c.getType() == Controller.Type.GAMEPAD) {
                    im.associateAction(c, net.java.games.input.Component.Identifier.Axis.RX, orbitAAction,
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Axis.Z, orbitRadiasAction,
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    im.associateAction(c, net.java.games.input.Component.Identifier.Axis.RY, orbitElevationAction,
                        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 
                }
            } 
        }
    }

    public class OrbitRadiasAction extends AbstractInputAction {
        public void performAction(float time, net.java.games.input.Event evt) {
            float rotAmount;
            //System.out.println(evt.getComponent().toString());
            
           // System.out.println(evt.getComponent().toString());
            if (evt.getComponent().toString().compareTo("Z Axis") == 0) {
                if (evt.getValue() < -0.2) {
                    rotAmount=0.2f;
                } else if (evt.getValue() > 0.2) {
                    rotAmount=-0.2f;
                } else {
                    rotAmount=0.0f;
                }
            } else {
                if (evt.getComponent().toString().compareTo("Z") == 0) {
                    rotAmount=-.2f;
                } else if (evt.getComponent().toString().compareTo("X") == 0) {
                    rotAmount=.2f;
                } else {
                    rotAmount=0f;
                }
            }

            //System.out.println("radias amt: " + evt.getValue());
            //System.out.println("camRadias: " + radias);
            //System.out.println("radius: " + radias + " rotAmount: " + rotAmount);
            if(radias+rotAmount > .5f && radias+rotAmount < 10.0f) {
                radias += rotAmount;
                radias = radias % 360;
                updateCameraPosition();
            }
        }
    }

    public class OrbitElevationAction extends AbstractInputAction {
        public void performAction(float time, net.java.games.input.Event evt) {
            float rotAmount;

            if(evt.getComponent().toString().compareTo("Y Rotation") == 0) {
                if (evt.getValue() < -0.2) {
                    rotAmount=-0.2f;
                } else if (evt.getValue() > 0.2) {
                    rotAmount=0.2f;
                } else {
                    rotAmount=0.0f;
                }
            } else {
                if (evt.getComponent().toString().compareTo("Up") == 0) {
                    rotAmount=-.2f;
                } else if (evt.getComponent().toString().compareTo("Down") == 0) {
                    rotAmount=.2f;
                } else {
                    rotAmount=0f;
                }
            }

            //don't go below ground plane or too high
            //System.out.println("elevation: " + evt.getValue());
            //System.out.println("camElevation: " + cameraElevation);
            if(cameraElevation+rotAmount > 1.0f && cameraElevation+rotAmount < 50.0f) {
                cameraElevation += rotAmount;
                cameraElevation = cameraElevation % 360;
                updateCameraPosition();
            }
        }
    }

    public class OrbitAroundAction extends AbstractInputAction {
        public void performAction(float time, net.java.games.input.Event evt) {
            float rotAmount;

            //System.out.println(evt.getValue());
            if(evt.getComponent().toString().compareTo("X Rotation") == 0) {
                if (evt.getValue() < -0.2) {
                    rotAmount=0.5f;
                } else if (evt.getValue() > 0.2) {
                    rotAmount=-0.5f;
                } else {
                    rotAmount=0.0f;
                }
            } else {
                //System.out.println(evt.getComponent().toString());
                if (evt.getComponent().toString().compareTo("Right") == 0) {
                    rotAmount=-.5f;
                } else if (evt.getComponent().toString().compareTo("Left") == 0) {
                    rotAmount=.5f;
                } else {
                    rotAmount=0f;
                }
            }
            //System.out.println(evt.getValue());
            //System.out.println("around: " + evt.getValue());
            //System.out.println("camAzimuth: " + cameraAzimuth);
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }
}

