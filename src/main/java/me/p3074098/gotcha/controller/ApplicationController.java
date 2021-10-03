package me.p3074098.gotcha.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import me.p3074098.gotcha.main.ApplicationEntry;
import me.p3074098.gotcha.main.Contestant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ApplicationController implements Initializable {
    
    private static final int seconds = 30;
    private static ApplicationController instance;
    
    @FXML
    private transient TextField searchField;
    @FXML
    private transient Button searchButton;
    @FXML
    private transient GridPane searchGrid;
    @FXML
    private transient VBox selectedVBox;
    @FXML
    private transient Label selectedContestantLabel;
    @FXML
    private transient Button killButton;
    @FXML
    private transient Button removeButton;
    @FXML
    private transient Button reviveButton;
    @FXML
    private transient Label optionRing;
    @FXML
    private transient Label optionGraveyard;
    @FXML
    private transient Label optionAll;
    @FXML
    private transient GridPane displayGrid;
    @FXML
    private transient Label consoleLabel;
    @FXML
    private transient Button addPlayerButton;
    @FXML
    private transient TextField addTextField;
    @FXML
    private transient Button resetButton;
    
    private transient Contestant selectedContestant;
    private transient Label selectedOption;
    private transient Label[] options;
    private transient Console console;
    private transient Image aliveImage;
    private transient Image deadImage;
    private transient File file;
    
    //Serialized values
    private LinkedList<Contestant> ring;
    private LinkedList<Contestant> graveyard;
    private int round;
    private int id;
    
    public static ApplicationController getInstance() {
        return instance;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        
        options = new Label[]{optionAll, optionRing, optionGraveyard};
        selectedOption = optionAll;
        console = new Console();
        selectedVBox.setVisible(false);
        selectedVBox.setManaged(false);
        file = new File("./app/data/database.txt");
        
        deserialize();
        fillContestantGrid();
        fillSearchGrid("");
        
        console.out("Game successfully started.");
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(seconds), e -> serialize()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
    private void createFiles() {
        try {
            File runtimeDirectory = new File("./app/data");
            
            if (!runtimeDirectory.exists())
                runtimeDirectory.mkdirs();
            
            if (!file.exists()) {
                file.setWritable(true, false);
                file.setReadable(true, false);
                file.setExecutable(true, false);
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void onResetButtonPressed(ActionEvent event) {
        
        round = 1;
        id = 0;
        ring.clear();
        graveyard.clear();
        
        serialize();
        
        initialize(null, null);
        
        console.out("Game reset.");
    }
    
    @FXML
    public void onSearchFieldKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER))
            onSearchButtonPress(null);
    }
    
    @FXML
    public void onAddFieldKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER))
            addPlayerButtonPress(null);
    }
    
    @FXML
    public void addPlayerButtonPress(ActionEvent event) {
        if (addTextField.getText().isEmpty())
            return;
        
        ApplicationEntry.mainStage.setTitle("*Gotcha!");
        
        Contestant c = new Contestant(addTextField.getText(), id++);
        ring.add(c);
        addTextField.clear();
        
        console.out("Added " + c.getName() + " as a new contestant.");
        
        fillContestantGrid();
        fillSearchGrid(searchField.getText());
    }
    
    @FXML
    public void onSearchButtonPress(ActionEvent event) {
        console.out("Searching..." + fillSearchGrid(searchField.getText()) + " results found.");
    }
    
    @FXML
    public void onKillButtonPress(ActionEvent event) {
        if (selectedContestant.isDead()) {
            console.out(selectedContestant.getName() + " could not be killed because they are already dead.");
            return;
        }
        
        if (ring.size() == 1) {
            console.out(selectedContestant.getName() + " could not be killed because they are the last contestant alive.");
        }
        
        ApplicationEntry.mainStage.setTitle("*Gotcha!");
        
        Contestant previous = getPreviousInList(selectedContestant);
        previous.addKill();
        
        selectedContestant.setDead(true);
        selectedContestant.setRoundOut(round++);
        
        ring.remove(selectedContestant);
        graveyard.add(selectedContestant);
        
        console.out(previous.getName() + " killed " + selectedContestant.getName() + ".");
        
        fillContestantGrid();
        fillSearchGrid(searchField.getText());
        
        Contestant c = ring.get(0);
        if (ring.size() == 1)
            console.out(c.getName() + " won the game with " + c.getKills() + " kills!");
    }
    
    @FXML
    public void onRemoveButtonPress(ActionEvent event) {
        console.out("Removed " + selectedContestant.getName() + " from the game permanently.");
        
        ApplicationEntry.mainStage.setTitle("*Gotcha!");
        
        ring.remove(selectedContestant);
        graveyard.remove(selectedContestant);
        
        selectedContestant = null;
        selectedVBox.setVisible(false);
        selectedVBox.setManaged(false);
        
        fillContestantGrid();
        
        searchField.clear();
        fillSearchGrid("");
    }
    
    @FXML
    public void onReviveButtonPress(ActionEvent event) {
        if (!graveyard.contains(selectedContestant)) {
            console.out(selectedContestant.getName() + " is not dead, so they couldn't be revived.");
            return;
        }
        
        ApplicationEntry.mainStage.setTitle("*Gotcha!");
        
        console.out("Revived " + selectedContestant.getName() + " from the graveyard.");
        
        selectedContestant.setDead(false);
        selectedContestant.setRoundOut(-1);
        
        graveyard.remove(selectedContestant);
        ring.add(selectedContestant);
        
        fillContestantGrid();
        fillSearchGrid(searchField.getText());
    }
    
    public int fillSearchGrid(String input) {
        final String uppercaseInput = input.toUpperCase();
        
        searchGrid.getChildren().clear();
        
        List<Contestant> filtered = new ArrayList<>(graveyard);
        filtered.addAll(ring);
        
        filtered = filtered.stream().filter(name -> name.getName().toUpperCase().startsWith(uppercaseInput)).collect(Collectors.toList());
        
        int col = 0, row = 1;
        for (int i = 0; i < filtered.size(); i++) {
            Contestant contestant = filtered.get(i);
            
            if (col == 2) {
                col = 0;
                row++;
            }
            
            AnchorPane anchorPane = getContestantCard(contestant);
            searchGrid.add(anchorPane, col++, row);
            GridPane.setMargin(anchorPane, new Insets(5));
        }
        
        return filtered.size();
    }
    
    public int fillContestantGrid() {
        List<Contestant> filtered;
        
        if (selectedOption.equals(optionRing))
            filtered = new ArrayList<>(ring);
        else if (selectedOption.equals(optionAll)) {
            filtered = new ArrayList<>(ring);
            filtered.addAll(graveyard);
        } else
            filtered = new ArrayList<>(graveyard);
        
        displayGrid.getChildren().clear();
        
        int col = 0, row = 1;
        for (int i = 0; i < filtered.size(); i++) {
            Contestant contestant = filtered.get(i);
            
            if (col == 2) {
                col = 0;
                row++;
            }
            
            AnchorPane anchorPane = getContestantCard(contestant);
            displayGrid.add(anchorPane, col++, row);
            GridPane.setMargin(anchorPane, new Insets(6));
        }
        
        return filtered.size();
    }
    
    private AnchorPane getContestantCard(Contestant contestant) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contestant-card.fxml"));
        AnchorPane anchorPane = null;
        try {
            anchorPane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ContestantController contestantController = fxmlLoader.getController();
        contestantController.setData(contestant, contestant.isDead() ? deadImage : aliveImage);
        
        anchorPane.setCursor(Cursor.HAND);
        anchorPane.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {
                    selectedContestant = (selectedContestant != null && selectedContestant.equals(contestant)) ? null : contestant;
                    
                    if (selectedContestant == null) {
//                        selectedVBox.getStyleClass().add("hidden");
                        selectedVBox.setVisible(false);
                        selectedVBox.setManaged(false);
                    } else {
                        selectedContestantLabel.setText("Selected: " + contestant.getName());
//                        selectedVBox.getStyleClass().remove("hidden");
                        selectedVBox.setVisible(true);
                        selectedVBox.setManaged(true);
                    }
                });
        
        return anchorPane;
    }
    
    public void resetDefaults() {
        ring = new LinkedList<>();
        graveyard = new LinkedList<>();
        id = 0;
        round = 1;
    }
    
    public void deserialize() {
        aliveImage = new Image(getClass().getResourceAsStream("alive.png"));
        deadImage = new Image(getClass().getResourceAsStream("dead.png"));
        
        resetDefaults();
        
        List<String> names = new ArrayList<>();
        Scanner scanner = new Scanner(getClass().getResourceAsStream("gotcha.txt"));
        while (scanner.hasNext())
            names.add(scanner.nextLine());
        scanner.close();
        
        for (String name : names)
            ring.add(new Contestant(name, id++));
        
        Collections.shuffle(ring);
    }
    
    @FXML
    public void optionButtonClick(MouseEvent event) {
        selectedOption = (Label) event.getSource();
        
        selectedOption.getStyleClass().add("option-label-selected");
        
        for (Label l : options)
            if (!l.equals(selectedOption))
                l.getStyleClass().remove("option-label-selected");
        
        console.out("Switched display category to " + selectedOption.getText() + ".");
        
        fillContestantGrid();
    }
    
    private Contestant getPreviousInList(Contestant who) {
        int index = ring.indexOf(who);
        
        return index == 0 ? ring.get(ring.size() - 1) : ring.get(index - 1);
    }
    
    public void serialize() {
//        try {
//
//            if (!file.exists())
//                createFiles();
//
//            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
//
//            stream.writeInt(id);
//            stream.writeInt(round);
//            stream.writeObject(ring);
//            stream.writeObject(graveyard);
//
//            stream.flush();
//            console.out("Automatically saving...");
//            ApplicationEntry.mainStage.setTitle("Gotcha!");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    
    private class Console {
        
        public void out(String message) {
            consoleLabel.setText(message);
        }
    }
}
