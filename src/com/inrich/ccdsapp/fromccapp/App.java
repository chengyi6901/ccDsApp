package com.inrich.ccdsapp.fromccapp;


import java.net.URL;

import com.inrich.ccdsapp.fromccapp.ui.main.MainView;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application  {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    	
//    	URL url  = getClass().getResource("a/main.fxml");
//    	System.out.println(url);
    	
//    	Parent root = FXMLLoader.load(getClass().getResource("/com/inrich/ccdsapp/fromccapp/main/main.fxml"));
//        
//        
//    	Scene scene = new Scene(root);
//        
//    	stage.setScene(scene);
//    	stage.show();
        
        MainView mainView = new MainView();
        
        Scene scene = new Scene(mainView.getView());
        stage.setScene(scene);
        stage.show();

        
    }

}
