package me.p3074098.gotcha.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import me.p3074098.gotcha.main.Contestant;

import java.io.InputStream;

public class ContestantController {
    @FXML
    private Label nameLabel;
    @FXML
    private Label killsLabel;
    @FXML
    private Label roundLabel;
    @FXML
    private Label idLabel;
    @FXML
    private ImageView image;
    
    public void setData(Contestant contestant, Image image) {
        nameLabel.setText(contestant.getName());
        killsLabel.setText(String.valueOf(contestant.getKills()));
        roundLabel.setText(contestant.getRoundOut() < 0 ? "N/A" : String.valueOf(contestant.getRoundOut()));
        idLabel.setText(String.valueOf(contestant.getId()));
        this.image.setImage(image);
    }
}
