package myGameEngine;

import a1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import net.java.games.input.Rumbler; //for Rumbler

public class GamepadController implements Rumbler {
    private MyGame game;
    private Camera camera;

    public GamepadController(MyGame g, Camera c) {
        game = g;
        camera = c;
    }
}