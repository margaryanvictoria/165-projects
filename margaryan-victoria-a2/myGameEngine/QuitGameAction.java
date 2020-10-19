package myGameEngine;

import a2.MyGame; //import the game

// It assumes availability of a method "shutdown" in the game
// (this is always true for classes that extend BaseGame).
import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;

public class QuitGameAction extends AbstractInputAction {
    private MyGame game;

    //Our main class will call this method and pass in our game.
    public QuitGameAction(MyGame g) {
        game = g;
    }

    public void performAction(float time, Event event) {
        System.out.println("shutdown requested");
        game.setState(Game.State.STOPPING);
        //game.incrementCounter();
    }
}