package hr.tvz.njakopcic.zavrsnijakopcic.engine;

import lombok.Getter;

public class Timer {

    @Getter private double lastLoopTime;

    public void init() {
        lastLoopTime = getTime();
    }

    public double getTime() {
        return System.nanoTime() / 1000_000_000.0;
    }

    public float getElapsedTime() {
        double time = getTime();
        float elapsedTime = (float) (time - lastLoopTime); // TODO: could this be a double?
        lastLoopTime = time;
        return elapsedTime;
    }
}
