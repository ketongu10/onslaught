package onslaught.ketongu10.war;

import java.util.function.Consumer;

public abstract class AngryEvent {
    protected Long timer;
    protected Consumer plot;
    public boolean isFinished;
    public void update() {
        this.timer++;
        this.plot.accept(0);
        shouldStopThis();

    }
    protected abstract Consumer<Integer> getPlot();
    protected abstract boolean shouldStopThis();
    public abstract void stopThis();
}
