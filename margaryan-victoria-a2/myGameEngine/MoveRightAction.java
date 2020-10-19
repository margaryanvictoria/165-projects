package myGameEngine;

import a2.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveRightAction extends AbstractInputAction {
    private SceneNode avN;

    public MoveRightAction(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        avN.moveRight(-0.1f);
    }
}