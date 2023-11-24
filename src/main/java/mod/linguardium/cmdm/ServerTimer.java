package mod.linguardium.cmdm;

import java.util.ArrayList;

public class ServerTimer {
    public static ArrayList<Timer> timers = new ArrayList<>();
    public static void tick() {
        timers.removeIf(Timer::tick);
    }
    public static class Timer {
        Runnable runner;
        int timeLeft;
        String identifier;
        Timer(String id, Runnable runner, int timeLeft) {
            this.identifier = id;
            this.runner=runner;
            this.timeLeft=timeLeft;
        }
        public boolean tick() {
            timeLeft--;
            if (timeLeft == 0) {
                runner.run();
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Timer timer && timer.identifier.equals(this.identifier)) || super.equals(obj);
        }
    };
}
