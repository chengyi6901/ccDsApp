package com.inrich.ccdsapp.fromccapp.ui.main;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.airhacks.afterburner.views.FXMLView;
import com.inrich.ccdsapp.fromccapp.buss.SwType;
import com.inrich.ccdsapp.fromccapp.subui.JsPerSalaryQueryView;
import com.inrich.ccdsapp.fromccapp.subui.QueryPresenter;
import com.inrich.ccdsapp.fromccapp.subui.QueryView;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;

/**
 * 主控制器
 * @author chengyi
 *
 */
public class MainPresenter implements Initializable {
	
	//查询条件区域，装载查询子视图
    @FXML
    private AnchorPane queryArea;

    //查询结果显示域。
    @FXML
    private TextFlow outArea;
	
	/**
	 * 个人基本信息类别下拉框
	 */
    @FXML
    private ChoiceBox<SwType> personBasicInfoCategory = new ChoiceBox<SwType>();;

	/**
	 * 个人其他信息类别下拉框
	 */
    @FXML
    private ChoiceBox<SwType> personOtherInfoCategory = new ChoiceBox<SwType>();;

    //查询子视图
    private FXMLView queryView = null;
    
    //private FXMLView jsPerSalaryQueryView = null;
    
    //private Object queryView = null;
    
    /**
     * 
     */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final MainPresenter mainPresenter = this;
		
		//从SwType中获取个人基本信息类别，然后装载到下拉框
		List<SwType> list = SwType.getPersonBasicInfoCategory();
		personBasicInfoCategory.setItems(FXCollections.observableList(list));
		
		//从SwType中获取个人其他信息类别，然后装载到下拉框
		List<SwType> list2 = SwType.getPersonOtherInfoCategory();
		personOtherInfoCategory.setItems(FXCollections.observableList(list2));
		
//		ChangeListener<? super SwType> listener = 
//			(ObservableValue<? extends SwType> observable,
//				SwType oldValue,
//				SwType newValue) -> {
//					
//					//清空主界面中的queryArea区域后，把QueryView视图在其中显示
//					queryArea.getChildren().clear();
//					QueryView queryView = new QueryView();
//					queryView.getView(queryArea.getChildren()::add);
//				
//				
//				//把下拉框中选中项（SwType）的symbol传递给QueryPresenter
//				QueryPresenter queryPresenter = (QueryPresenter)queryView.getPresenter();
//				queryPresenter.swTypeSymbol = newValue.symbol;
//				
//				//把主控制器传递给QueryPresenter
//				queryPresenter.mainPresenter = mainPresenter;
//				
//				//重新设置个人其他信息类别下拉框
//				personOtherInfoCategory.setItems(FXCollections.observableList(list2));
//			};
//		
//		personBasicInfoCategory.getSelectionModel().selectedItemProperty().addListener(listener);
			
		//个人基本信息类别下拉框中某个项被选中时触发
		personBasicInfoCategory.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<? extends SwType> observable,
						SwType oldValue,
						SwType newValue) -> {
					if(oldValue == null && newValue != null) //表示第一次选择
					{
						//重新设置个人其他信息类别下拉框
						personOtherInfoCategory.setItems(FXCollections.observableList(list2));
					}
					
					
					if(oldValue == null && newValue != null) //表示第一次选择
					{
						//清空主界面中的queryArea区域后，把QueryView视图在其中显示
						queryArea.getChildren().clear();

						if(newValue.symbol.equals("JsPerSalary"))
						{
							queryView = new JsPerSalaryQueryView();
							queryView.getView(queryArea.getChildren()::add);
						}
						else
						{
							queryView = new QueryView();
							queryView.getView(queryArea.getChildren()::add);
						}
						
						//把下拉框中选中项（SwType）的symbol传递给QueryPresenter
						QueryPresenter queryPresenter = (QueryPresenter)queryView.getPresenter();
						queryPresenter.swTypeSymbol = newValue.symbol;
						
						//把主控制器传递给QueryPresenter
						queryPresenter.mainPresenter = mainPresenter;

					}
					else if(oldValue != null && newValue != null) //表示第二次及以后的选择
					{
						QueryPresenter queryPresenter = (QueryPresenter)queryView.getPresenter();
						queryPresenter.swTypeSymbol = newValue.symbol;
					}
		});
		
		//个人其他信息类别下拉框中某个项被选中时触发
		personOtherInfoCategory.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<? extends SwType> observable,
						SwType oldValue,
						SwType newValue) -> {
					if(oldValue == null && newValue != null) //表示第一次选择
					{
						//重新设置个人基本信息类别下拉框
						personBasicInfoCategory.setItems(FXCollections.observableList(list));
					}

//					if(oldValue == null && newValue != null) //表示第一次选择
//					{
//						//清空主界面中的queryArea区域后，把QueryView视图在其中显示
//						queryArea.getChildren().clear();
//						queryView = new QueryView();
//						queryView.getView(queryArea.getChildren()::add);
//						
//						//把下拉框中选中项（SwType）的symbol传递给QueryPresenter
//						QueryPresenter queryPresenter = (QueryPresenter)queryView.getPresenter();
//						queryPresenter.swTypeSymbol = newValue.symbol;
//						
//						//把主控制器传递给QueryPresenter
//						queryPresenter.mainPresenter = mainPresenter;
//						
//					}
//					else if(oldValue != null && newValue != null) //表示第二次及以后的选择
//					{
//						QueryPresenter queryPresenter = (QueryPresenter)queryView.getPresenter();
//						queryPresenter.swTypeSymbol = newValue.symbol;
//					}
		});

	}
	
	private void showView(SwType oldValue, SwType newValue)
	{
		if(oldValue == null && newValue != null) //表示第一次选择
		{
			//清空主界面中的queryArea区域后，把QueryView视图在其中显示
			queryArea.getChildren().clear();
			queryView = new QueryView();
			queryView.getView(queryArea.getChildren()::add);
			
			//把下拉框中选中项（SwType）的symbol传递给QueryPresenter
			QueryPresenter queryPresenter = (QueryPresenter)queryView.getPresenter();
			queryPresenter.swTypeSymbol = newValue.symbol;
			
			//把主控制器传递给QueryPresenter
			queryPresenter.mainPresenter = this;
			
		}
		else if(oldValue != null && newValue != null) //表示第二次及以后的选择
		{
			QueryPresenter queryPresenter = (QueryPresenter)queryView.getPresenter();
			queryPresenter.swTypeSymbol = newValue.symbol;
		}

		Class klass = null;
		
		try
		{
			klass = Class.forName(newValue.queryView);
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
		
		
		
		if(queryView == null  || !klass.isInstance(queryView))
		{
			
		}
		else
		{
			
		}
	}

	/**
	 * 
	 * @return
	 */
	public TextFlow getOutArea()
	{
		return outArea;
	}
}
