package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.Buffer;
import java.util.*;

@Component
public class ControllerSelectedAnalytic implements Initializable
{
    private final String selectionTutti = "Tutti";
    @FXML
    private Button closeButton;
    @FXML
    private Label lblSelectedQuery;
    @FXML
    private ComboBox cboxLimit;
    @FXML
    private BarChart barChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    //********* Autowireds *********
    @Autowired
    ModelBean modelBean;
    @Autowired
    private UserDBMongo userDBMongo;
    @Autowired
    private UserService serviceUser;
    private StageManager stageManager;

    private ObservableList<String> whatStatisticToShow = FXCollections.observableArrayList(
            "Average age of users per country",
            "Countries with the highest user number"
    );
    @Autowired
    @Lazy
    public ControllerSelectedAnalytic(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        this.cboxLimit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                int selectedLimit = (newValue).equals(selectionTutti) ? -1 : Integer.parseInt((String)newValue);
                this.initializeChart(selectedLimit);
            }
        });
        initializeCbox();
        initializeChart(Constants.LIMIT_ANALYTIC);
    }

    private void initializeCbox()
    {
        ObservableList<String> limitCountrieChoices = FXCollections.observableArrayList("3", "5", "10", "15", "20", selectionTutti);
        this.cboxLimit.setItems(limitCountrieChoices);
    }

    private void initializeChart(int limitCountries)
    {
        ControllerViewStatisticsPage.statisticsToShow selectedAnalytic =
                (ControllerViewStatisticsPage.statisticsToShow)modelBean.getBean(Constants.SELECTED_ANALYTICS);
        this.barChart.setAnimated(true);
        this.xAxis = new CategoryAxis();
        this.yAxis = new NumberAxis();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        switch (selectedAnalytic)
        {
            case AVG_AGE_BY_COUNTRY ->
            {
                this.lblSelectedQuery.setText(this.whatStatisticToShow.get(0));
                series.setName("Average Age");
                HashMap<String, Double> avgAgeMap = this.serviceUser.getAvgAgeByNationality(limitCountries);
                for (Map.Entry<String, Double> pair : avgAgeMap.entrySet())
                {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(pair.getKey(), pair.getValue());
                    data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                        if (newNode != null) { // Quando il nodo è creato
                            Tooltip tooltip = new Tooltip(String.valueOf(pair.getValue()));
                            tooltip.setShowDelay(Duration.millis(100));
                            Tooltip.install(newNode, tooltip); // Associa il tooltip al nodo
                        }
                    });
                    series.getData().add(data);
                }
                xAxis.setLabel("Countries");
                yAxis.setLabel("Average Age");
                barChart.getData().clear();
                barChart.getData().add(series);
                ObservableList<String> dataList = FXCollections.observableArrayList(avgAgeMap.keySet());
                xAxis.setCategories(dataList);
            }
            case COUNTRIES_WITH_HIGHEST_USER_NUMBER ->
            {
                this.lblSelectedQuery.setText(this.whatStatisticToShow.get(1));
                series.setName("Users Number");
                LinkedHashMap<String, Integer> usersNumber = this.serviceUser.getCountriesWithMostUsers(0, limitCountries);
                for (Map.Entry<String, Integer> pair : usersNumber.entrySet())
                {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(pair.getKey(), pair.getValue());
                    data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                        if (newNode != null) { // Quando il nodo è creato
                            Tooltip tooltip = new Tooltip(String.valueOf(pair.getValue()));
                            tooltip.setShowDelay(Duration.millis(100));
                            Tooltip.install(newNode, tooltip); // Associa il tooltip al nodo
                        }
                    });
                    series.getData().add(data);
                }
                xAxis.setLabel("Countries");
                yAxis.setLabel("Users Number");
                barChart.getData().clear();
                barChart.getData().add(series);
                ObservableList<String> dataList = FXCollections.observableArrayList(usersNumber.keySet());
                xAxis.setCategories(dataList);
            }
        }

        this.barChart.setAnimated(false);
    }

    public void onClickCloseButton() {
        stageManager.closeStageButton(this.closeButton);
    }
}
