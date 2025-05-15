package taskmanagement;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class ViewLoaderUtil {

    public static Node loadView(Pane container, String fxmlPath) {
        try {
            java.net.URL resourceUrl = ViewLoaderUtil.class.getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }
            System.out.println("Loading view: " + fxmlPath + " -> " + resourceUrl);
            Node view = FXMLLoader.load(resourceUrl);
            container.getChildren().setAll(view);
            return view;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load view: " + fxmlPath);
            return null;
        }
    }
}