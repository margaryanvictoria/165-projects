package myGameEngine;

import a1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;

public class IncrementAmountModifierAction extends AbstractInputAction {
    private MyGame game;
    private int incAmt = 1;

    public IncrementAmountModifierAction(MyGame g) {
        game = g;
    }

    public void performAction(float time, Event e) {
        System.out.println("modifier action initiated");
        incAmt++;
        if (incAmt == 5)
            incAmt=1;
    }

    protected int getIncAmt() {
        return incAmt;
    }
}