package com.inrich.ccdsapp.fromccapp.subui;

import java.net.URL;
import java.util.ResourceBundle;

import com.inrich.ccdsapp.fromccapp.ui.main.MainPresenter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class QueryPresenter implements Initializable{

	//交换接口符号，表示从哪个接口查询
	public String swTypeSymbol;
	
	//主控制器
	public MainPresenter mainPresenter;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}

	//查询按钮
    @FXML
    private Button query;
    
    //身份证
    @FXML
    private TextField certno;

    //姓名
    @FXML
    private TextField name;

    @FXML
    private DatePicker startDate;
    
    @FXML
    private DatePicker endDate;

    @FXML
    void query(ActionEvent event) {
    	//System.out.println(swTypeSymbol);
    	//System.out.println(name.getText());
    	//System.out.println(certno.getText());
    	
    	TextFlow textFlow = mainPresenter.getOutArea();
    	textFlow.getChildren().add(new Text(swTypeSymbol + "\n"));
    	textFlow.getChildren().add(new Text(name.getText() + "\n"));
    	textFlow.getChildren().add(new Text(certno.getText() + "\n"));
    }

}
