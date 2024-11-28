package iron.gradetracker;

import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class AnimationManager {

    private static final Set<Animation> animations = new HashSet<>();

    public static boolean isAnimating() { return !animations.isEmpty(); }

    public static void startAnimation(Runnable newAnimation) {
        List<Animation> copy = new ArrayList<>(animations);
        for (Animation animation : copy)
            animation.stop();
        newAnimation.run();
    }

    public static void stageTransition(Stage stage, Utils.Point startOrigin, Utils.Point startSize, Utils.Point endOrigin, Utils.Point endSize) {
        new Transition() {
            {
                setCycleDuration(Duration.millis(5));
                setRate(0.01);
            }
            @Override
            protected void interpolate(double v) {
                stage.setX(startOrigin.x + (endOrigin.x - startOrigin.x) * v);
                stage.setY(startOrigin.y + (endOrigin.y - startOrigin.y) * v);
                stage.setWidth(startSize.x + (endSize.x - startSize.x) * v);
                stage.setHeight(startSize.y + (endSize.y - startSize.y) * v);
            }
        }.play();
    }

    public static NodeAnimation byYTranslation(Node node, double byY, double duration) { return byYTranslation(node, byY, duration, null); }
    public static NodeAnimation byYTranslation(Node node, double byY, double duration, Runnable onFinished) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(duration), node);
        transition.setByY(byY);
        return new NodeAnimation(transition, node, onFinished);
    }

    public static NodeAnimation toOpacityFade(Node node, double fromFade, double toFade, double duration) { return toOpacityFade(node, fromFade, toFade, duration, null); }
    public static NodeAnimation toOpacityFade(Node node, double fromFade, double toFade, double duration, Runnable onFinished) {
        FadeTransition transition = new FadeTransition(Duration.millis(duration), node);
        transition.setFromValue(fromFade);
        transition.setToValue(toFade);
        return new NodeAnimation(transition, node, onFinished);
    }

    public static SequenceAnimation sequenceTransition(Runnable onFinished, Animation... animations) { return new SequenceAnimation(new SequentialTransition(), onFinished, animations); }
    public static SequenceAnimation parallelTransition(Runnable onFinished, Animation... animations) { return new SequenceAnimation(new ParallelTransition(), onFinished, animations); }

    public static class Animation {
        protected Transition transition;
        protected Runnable onFinished;
        protected boolean finished;
        protected Animation parent;

        private Animation(Transition transition, Runnable onFinished) {
            this.transition = transition;
            this.finished = false;

            this.onFinished = onFinished;
            transition.setOnFinished(_ -> {
                finish();
            });
        }

        public void play() {
            if (parent == null) transition.play();
            lock();
        }

        public void stop() {
            if (finished) return;

            if (parent == null) {
                transition.stop();
                finish();
            }
            else {
                parent.stop();
            }
        }

        public void finish() {
            if (onFinished != null) onFinished.run();
            unlock();
            finished = true;
        }

        public void lock() {
            animations.add(this);
        }

        public void unlock() {
            animations.remove(this);
        }

        public Transition getTransition() { return transition; }
    }

    public static class NodeAnimation extends Animation {
        private final Node node;

        public NodeAnimation(Transition transition, Node node, Runnable onFinished) {
            super(transition, onFinished);
            this.node = node;
        }

        public Node getNode() { return node; }
    }

    public static class SequenceAnimation extends Animation {
        private final List<Animation> sequence = new ArrayList<>();

        private SequenceAnimation(Transition transition, Runnable onFinished, Animation... animations) {
            super(transition, onFinished);
            sequence.addAll(Arrays.asList(animations));

            getSequence().addAll(sequence.stream().map(Animation::getTransition).toList());
            sequence.forEach(animation -> animation.parent = this);
        }

        public ObservableList<javafx.animation.Animation> getSequence() {
            return switch (transition) {
                case SequentialTransition trans -> trans.getChildren();
                case ParallelTransition trans -> trans.getChildren();
                default -> throw new IllegalStateException("Unexpected value: " + transition);
            };
        }

        public void add(Animation animation) { add(getSequence().size(), animation); }
        public void add(int index, Animation animation) {
            sequence.add(animation);
            animation.parent = this;
            getSequence().add(index, animation.getTransition());
        }

        @Override
        public void finish() {
            super.finish();
            for (Animation animation : sequence) animation.finish();
        }

        @Override
        public void lock() {
            super.lock();
            for (Animation animation : sequence) animation.lock();
        }

        @Override
        public void unlock() {
            super.unlock();
            for (Animation animation : sequence) animation.unlock();
        }
    }
}
