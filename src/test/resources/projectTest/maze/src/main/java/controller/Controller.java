package controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import mazeLogic.GameContext;
import mazeLogic.Rules;
import model.MoveSet;
import persistence.JsonSaver;
import persistence.SavingContainer;
public class Controller {
	
	
	private GameContext context;
	private Rules rules;
	private MazeRenderer mazeRenderer;
	private EventHandler<KeyEvent> inputHandler;
	
	@FXML
	GridPane mazeLayout;
	
	@FXML
	BorderPane generalLayout;
	
	@FXML
	Label victoryLabel;
	
	
	@FXML
	public void onClickReset() {
		enableInput(false);
		this.context.reset();
		this.mazeLayout.getChildren().clear();
		this.mazeRenderer.renderMaze(mazeLayout);
		this.mazeRenderer.storePreviousPlayerPosition();
		victoryLabel.setOpacity(0);
		enableInput(true);
	}
	
	@FXML
	public void onClickSaveAndQuit() {
		SavingContainer saveGame = new SavingContainer(this.context.getMaze(),this.context.getPlayer());
		JsonSaver jsonSaver = new JsonSaver(saveGame, "save.json");
		jsonSaver.save();
		Platform.exit();
	}

	@FXML
	public void initialize() {
		
	}
	
	public void initializeGame(Rules rules, GameContext context, MazeRenderer renderer){
		
		this.rules = rules;
		this.mazeRenderer = renderer;
		this.context = context;
		this.inputHandler = e->{
			switch(e.getCode()) {
			case DOWN ->{movePlayer(MoveSet.DOWN);e.consume();}
			case UP ->{movePlayer(MoveSet.UP);e.consume();}
			case LEFT ->{movePlayer(MoveSet.LEFT);e.consume();}
			case RIGHT ->{movePlayer(MoveSet.RIGHT);e.consume();}
			default -> {}
			};
		};
		this.context.getStateProperty().addListener(e->{
			switch(this.context.getGlobalState()){
				case VICTORY -> drawVictory();
				case IN_GAME ->{
					victoryLabel.setOpacity(0);
					enableInput(true);
				}
				default ->{}
			};
		});
		renderer.storePreviousPlayerPosition();
		renderer.renderMaze(this.mazeLayout);
	}
	
	public void enableInput(boolean state) {
		if(state) {
			this.generalLayout.getScene().addEventHandler(KeyEvent.KEY_PRESSED,this.inputHandler);			
		}else {
			this.generalLayout.getScene().removeEventHandler(KeyEvent.KEY_PRESSED, this.inputHandler);		
		}
	}
	
	private void drawVictory() {
		victoryLabel.setOpacity(1);
		enableInput(false);
	}

	private void movePlayer(MoveSet move) {
		if(this.rules.applyMove(move)) {
			mazeRenderer.renderPlayerMove();
			mazeRenderer.storePreviousPlayerPosition();
		}
	}
}
