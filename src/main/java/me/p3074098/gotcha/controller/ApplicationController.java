package me.p3074098.gotcha.controller;

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
import me.p3074098.gotcha.main.Contestant;

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
    
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private GridPane searchGrid;
    @FXML
    private VBox selectedVBox;
    @FXML
    private Label selectedContestantLabel;
    @FXML
    private Button killButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button reviveButton;
    @FXML
    private Label optionRing;
    @FXML
    private Label optionGraveyard;
    @FXML
    private Label optionAll;
    @FXML
    private GridPane displayGrid;
    @FXML
    private Label consoleLabel;
    @FXML
    private Button addPlayerButton;
    @FXML
    private TextField addTextField;
    @FXML
    private Button resetButton;
    
    private Contestant selectedContestant;
    private Label selectedOption;
    private Label[] options;
    private LinkedList<Contestant> ring;
    private LinkedList<Contestant> graveyard;
    private Console console;
    
    private int round;
    private int id;
    
    private Image aliveImage;
    private Image deadImage;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        options = new Label[]{optionAll, optionRing, optionGraveyard};
        selectedOption = optionAll;
        console = new Console();
//        selectedVBox.getStyleClass().add("hidden");
        selectedVBox.setVisible(false);
        selectedVBox.setManaged(false);
        round = 1;
        id = 0;
        
        console.out(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm());
        
        deserialize();
        fillContestantGrid();
        fillSearchGrid("");
    }
    
    @FXML
    public void onResetButtonPressed(ActionEvent event) {
        console.out("Resetting game...");
        initialize(null, null);
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
            console.out(selectedContestant + " could not be killed because they are already dead.");
            return;
        }
        
        Contestant previous = getPreviousInList(selectedContestant);
        previous.addKill();
        
        selectedContestant.setDead(true);
        selectedContestant.setRoundOut(round++);
        
        ring.remove(selectedContestant);
        graveyard.add(selectedContestant);
        
        console.out(previous.getName() + " killed " + selectedContestant.getName());
        
        fillContestantGrid();
        fillSearchGrid(searchField.getText());
        
        Contestant c = ring.get(0);
        if (ring.size() == 1)
            console.out(c.getName() + " won the game with " + c.getKills() + " kills!");
    }
    
    @FXML
    public void onRemoveButtonPress(ActionEvent event) {
        console.out("Removed " + selectedContestant.getName() + " from the game permanently.");
        
        ring.remove(selectedContestant);
        graveyard.remove(selectedContestant);
        
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
        
        console.out("Revived " + selectedContestant.getName() + " from the graveyard.");
        
        selectedContestant.setDead(false);
        selectedContestant.setRoundOut(-1);
        
        graveyard.remove(selectedContestant);
        ring.add(selectedContestant);
        
        fillContestantGrid();
        fillSearchGrid(searchField.getText());
    }
    
    public int fillSearchGrid(String input) {
        searchGrid.getChildren().clear();
        
        List<Contestant> filtered = new ArrayList<>(graveyard);
        filtered.addAll(ring);
        
        filtered = filtered.stream().filter(name -> name.getName().startsWith(input)).collect(Collectors.toList());
        
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
        List<Contestant> filtered = new ArrayList<>();
        
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
    
    public void deserialize() {
        aliveImage = new Image(getClass().getResourceAsStream("alive.png"));
        deadImage = new Image(getClass().getResourceAsStream("dead.png"));
        
        ring = new LinkedList<>();
        graveyard = new LinkedList<>();
        
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
        
        console.out("Switched display category to " + selectedOption.getText());
        
        fillContestantGrid();
    }
    
    private Contestant getPreviousInList(Contestant who) {
        int index = ring.indexOf(who);
        
        return index == 0 ? ring.get(ring.size() - 1) : ring.get(index - 1);
    }
    
    private class Console {
        
        public void out(String message) {
            consoleLabel.setText(message);
        }
    }
}
