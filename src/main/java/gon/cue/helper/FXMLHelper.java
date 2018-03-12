package gon.cue.helper;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class FXMLHelper {
    
    public Region get_region() {
		return _region;
	}

	protected FXMLLoader _fxmlLoader = null;
	private Region _region = null;

	public FXMLHelper() {

	}

	public void initialize(Object controller) {
		try {
			_fxmlLoader = new FXMLLoader(
					getClass().getResource("/" + controller.getClass().getName().replaceAll("\\.", "/") + ".fxml"));
			_fxmlLoader.setController(controller);
			_region = _fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initializeControls() {

	}

	protected final Stage getStage() {
		return (Stage) _region.getScene().getWindow();
	}

	protected final void close() {
		getStage().close();
	}

	protected final void setTitle(String windowTitle) {
		getStage().setTitle(windowTitle);
	}
    
}