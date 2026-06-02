package ui;

import java.io.File;
import java.net.URL;

import controller.Controller;
import controller.MazeRenderer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mazeLogic.GameContext;
import mazeLogic.Rules;
import persistence.Loader;
import persistence.SavingContainer;

public class ViewManager extends Application{
	
	private Rules rules;
	private GameContext context;
	private MazeRenderer mazeRenderer;
	private File savePath;
	
	

	@Override
	public void init() throws Exception {
		this.savePath = new File("save.json");
		if(savePath.exists()) {
			Loader loader = new Loader();
			SavingContainer dataLoaded = loader.load(this.savePath);
			this.context = new GameContext(dataLoaded.getMazeToSave(),dataLoaded.getPlayerToSave());
			this.rules = new Rules(context);
			this.mazeRenderer = new MazeRenderer(this.context);
			savePath.delete();
		}else {
			this.context = new GameContext();
			this.rules = new Rules(context);
			this.mazeRenderer = new MazeRenderer(this.context);
		}
	}
	

	@Override
	public void start(Stage stage) throws Exception {
		
		URL fxml = getClass().getResource("/views/mainView.fxml");
		FXMLLoader loader = new FXMLLoader(fxml);
		Parent mainView = loader.load();
		
		Controller controller = loader.getController();
		controller.initializeGame(this.rules, this.context, this.mazeRenderer);
		
		Scene scene = new Scene(mainView);
		controller.enableInput(true);
		stage.setScene(scene);
		stage.show();
	}
	
}
