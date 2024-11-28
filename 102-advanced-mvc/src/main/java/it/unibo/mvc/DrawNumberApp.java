package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    /*private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;*/
    private static final InputStream PATH = ClassLoader.getSystemResourceAsStream("config.yml");

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        //this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
        final Configuration.Builder configurationBuilder = new Configuration.Builder();
        try (
            final BufferedReader r = new BufferedReader(new InputStreamReader(PATH));
        ) {
            String line = r.readLine();
            while (Objects.nonNull(line)) {
                StringTokenizer configLine = new StringTokenizer(line, ": ");
                String option;
                String value;
                if (configLine.countTokens() == 2) {
                    option = configLine.nextToken();
                    value = configLine.nextToken();
                } else {
                    throw new IllegalStateException("configuration file layout is not the expected one");
                }
                switch (option) {
                    case "maximum":
                        configurationBuilder.setMax(Integer.parseInt(value));
                        break;
                    
                    case "minimum":
                        configurationBuilder.setMin(Integer.parseInt(value));
                        break;
                    
                    case "attempts":
                        configurationBuilder.setAttempts(Integer.parseInt(value));
                        break;
                    default:
                        throw new IllegalArgumentException("not recognized valure has been passed on the config file");
                }
                line = r.readLine();
            }
        } catch (IOException e) {
            displayErrorViews(e.getMessage() + ": using default configuration");
        }
        final Configuration config = configurationBuilder.build();
        if (config.isConsistent()) {
            this.model = new DrawNumberImpl(config);
        } else {
            displayErrorViews("configuation not valid, using default configuration");
            this.model = new DrawNumberImpl(new Configuration.Builder().build());
        }
    }
    
    private void displayErrorViews(String error) {
        for (DrawNumberView drawNumberView : views) {
            drawNumberView.displayError(error);
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
