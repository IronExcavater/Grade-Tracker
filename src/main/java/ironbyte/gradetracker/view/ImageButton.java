package ironbyte.gradetracker.view;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.image.*;

public class ImageButton extends Button {
    public final ImageView imageView = new ImageView();
    private ReadOnlyBooleanProperty focusedProperty = focusedProperty();
    private ReadOnlyBooleanProperty hoverProperty = hoverProperty();
    private final ChangeListener<Boolean> changeListener = (_, _, _) -> updateImage();

    private Image focusedImage;
    private Image unfocusedImage;
    private Image hoverImage;
    private Image pressImage;

    public ImageButton() {
        setFocusedProperty(focusedProperty());
        setHoverProperty(hoverProperty());
        initialize();
    }
    public ImageButton(ReadOnlyBooleanProperty focusedProperty, ReadOnlyBooleanProperty hoverProperty) {
        setFocusedProperty(focusedProperty);
        setHoverProperty(hoverProperty);
        initialize();
    }

    private void initialize() {
        setGraphic(imageView);
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(prefWidthProperty());
        pressedProperty().addListener(changeListener);
    }

    private void updateImage() {
        if (isPressed() && pressImage != null) {
            imageView.setImage(pressImage);
        } else if (hoverProperty.get() && hoverImage != null) {
            imageView.setImage(hoverImage);
        } else if (focusedProperty.get() && focusedImage != null) {
            imageView.setImage(focusedImage);
        } else if (unfocusedImage != null) {
            imageView.setImage(unfocusedImage);
        }
    }

    public void setFocusedProperty(ReadOnlyBooleanProperty focusedProperty) {
        this.focusedProperty.removeListener(changeListener);
        this.focusedProperty = focusedProperty;
        focusedProperty.addListener(changeListener);
    }
    public void setHoverProperty(ReadOnlyBooleanProperty hoverProperty) {
        this.hoverProperty.removeListener(changeListener);
        this.hoverProperty = hoverProperty;
        hoverProperty.addListener(changeListener);
    }
    public void setProperties(ReadOnlyBooleanProperty focusedProperty, ReadOnlyBooleanProperty hoverProperty) {
        setFocusedProperty(focusedProperty);
        setHoverProperty(hoverProperty);
    }

    public void setImages(Image focusedImage, Image unfocusedImage, Image hoverImage, Image pressImage) {
        this.focusedImage = focusedImage;
        this.unfocusedImage = unfocusedImage;
        this.hoverImage = hoverImage;
        this.pressImage = pressImage;
        updateImage();
    }
}
