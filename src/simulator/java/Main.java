/**
 * Created by mikhail on 27.04.2017.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Start");
        StreamState state = new StreamState(System.currentTimeMillis());
        state.start();
        do {
            if (!state.schedule.step(state)) break;
            System.out.println("Step: " + state.schedule.getSteps() + "; Time: " + state.schedule.getTime());
        }
        while (true);
        state.finish();
        System.exit(0);
    }
}
