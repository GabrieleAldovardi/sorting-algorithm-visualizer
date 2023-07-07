package com.example.sortingalgorithmvisualizator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class MainController {

    @FXML
    private BorderPane pane;
    @FXML
    private Slider arraySizeSlider, arrayRangeSlider;
    @FXML
    private ComboBox<String> sortingAlgorithmChoice;
    @FXML
    private Label arraySizeValueLabel, arrayRangeValueLabel, timeElapsedLabel, timeElapsedValueLabel, infoLabel;
    @FXML
    private Button sortButton, resetButton;
    @FXML
    private Spinner<Integer> delayPicker;

    private static final int DEFAULT_ARRAY_SIZE = 50;
    private static final int DEFAULT_ARRAY_RANGE = 100;
    private static final int MIN_DELAY = 0;
    private static final int DEFAULT_DELAY = 200;
    private static final int MAX_DELAY = 1000;

    private BarChart<String, Number> barChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private static int barsNumber, valueRange;
    private static long currentDelay;
    private String sortingAlgorithm;

    @FXML
    public void initialize() {
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();

        arraySizeSlider.setValue(DEFAULT_ARRAY_SIZE);
        arraySizeValueLabel.setText(Integer.toString(DEFAULT_ARRAY_SIZE));

        arrayRangeSlider.setValue(DEFAULT_ARRAY_RANGE);
        arrayRangeValueLabel.setText(Integer.toString(DEFAULT_ARRAY_RANGE));

        SpinnerValueFactory<Integer> delaySpinner = new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_DELAY, MAX_DELAY);
        delaySpinner.setValue(DEFAULT_DELAY);
        delayPicker.setValueFactory(delaySpinner);

        timeElapsedLabel.setVisible(false);
        timeElapsedValueLabel.setVisible(false);

        barsNumber = DEFAULT_ARRAY_SIZE;
        valueRange = DEFAULT_ARRAY_RANGE;
        currentDelay = DEFAULT_DELAY;

        fillArray();

        arraySizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            barsNumber = (int) arraySizeSlider.getValue();
            arraySizeValueLabel.setText(Integer.toString(barsNumber));
        });

        arrayRangeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            valueRange = (int) arrayRangeSlider.getValue();
            arrayRangeValueLabel.setText(Integer.toString(valueRange));
        });

        delayPicker.valueProperty().addListener((observable, oldValue, newValue) -> currentDelay = delayPicker.getValue());

        sortingAlgorithmChoice.setItems(FXCollections.observableArrayList("Selection sort", "Bubble sort", "Insertion sort", "Quick sort", "Merge sort"));
    }

    public void initializeBarChart() {
        barChart.setLegendVisible(false);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setHorizontalZeroLineVisible(false);
        barChart.setVerticalZeroLineVisible(false);
        barChart.getXAxis().setTickLabelsVisible(false);
        barChart.getXAxis().setOpacity(0);
        barChart.setBarGap(0);
        barChart.setAnimated(false);
    }

    public void fillArray() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        barChart = new BarChart<>(xAxis, yAxis);
        initializeBarChart();
        for (int i = 0; i < barsNumber; i++) {
            series.getData().add(new XYChart.Data<>(Integer.toString(i),
                    new Random().nextInt(valueRange) + 1));
        }
        barChart.getData().add(series);
        pane.setCenter(barChart);

        for (int i = 0; i < barsNumber; i++)
            series.getData().get(i).getNode().setStyle("-fx-background-color:#CC0066");
    }

    public static void delay() {
        try {
            Thread.sleep(currentDelay);
        } catch (InterruptedException ignored) {

        }
    }

    @FXML
    public void handleReset() {
        barChart.getData().clear();
        fillArray();
        sortButton.setDisable(false);
        timeElapsedLabel.setVisible(false);
        timeElapsedValueLabel.setVisible(false);
    }

    @FXML
    public void handleSort() {
        sortButton.setDisable(true);
        sortingAlgorithm = sortingAlgorithmChoice.getSelectionModel().getSelectedItem();
        if(sortingAlgorithm == null){
            showNoSelectedAlgorithmAlert();
        }
        Task task = new Task() {
            @Override
            protected Object call() {
                try {
                    long startTime = System.nanoTime();
                    switch (sortingAlgorithm) {
                        case "Selection sort" -> SortingAlgorithms.selectionSort(barChart.getData().get(0).getData());
                        case "Bubble sort" -> SortingAlgorithms.bubbleSort(barChart.getData().get(0).getData());
                        case "Insertion sort" -> SortingAlgorithms.insertionSort(barChart.getData().get(0).getData());
                        case "Quick sort" -> SortingAlgorithms.quickSort(barChart.getData().get(0).getData());
                        case "Merge sort" -> SortingAlgorithms.mergeSort(barChart.getData().get(0).getData());
                        default -> throw new Exception();
                    }
                    Platform.runLater(() -> {
                        timeElapsedValueLabel.setText(Long.toString(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime)) + " s");
                        timeElapsedLabel.setVisible(true);
                        timeElapsedValueLabel.setVisible(true);
                    });

                } catch (NullPointerException ignored) {

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    resetButton.setDisable(false);
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    public void showNoSelectedAlgorithmAlert() {
        sortButton.setDisable(false);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Selection");
        alert.setHeaderText("No algorithm selected");
        alert.setContentText("Please select an algorithm in the check box.");
        alert.getDialogPane().setGraphic(new ImageView(new Image(String.valueOf(this.getClass().getResource("icons/warning_icon.png")))));
        alert.showAndWait();
    }

    public static class SortingAlgorithms {
        private static void swap(ObservableList<XYChart.Data<String, Number>> list, int index1, int index2) {
            list.get(index1).getNode().setStyle("-fx-background-color: #E1AB0A");
            list.get(index2).getNode().setStyle("-fx-background-color: #E1AB0A");
            delay();
            Number tmp = list.get(index1).getYValue();
            list.get(index1).setYValue(list.get(index2).getYValue());
            list.get(index1).getNode().setStyle("-fx-background-color: #00D8FA");
            list.get(index2).setYValue(tmp);
            list.get(index2).getNode().setStyle("-fx-background-color: #00D8FA");
        }

        private static int findMax(ObservableList<XYChart.Data<String, Number>> list, int range) {
            int maxIndex = 0; //Hp: first element is the max
            for (int i = 1; i < range; ++i) {
                list.get(i).getNode().setStyle("-fx-background-color: #E1AB0A");
                if (list.get(i).getYValue().intValue() > list.get(maxIndex).getYValue().intValue()) {
                    delay();
                    maxIndex = i;
                }
            }
            return maxIndex;
        }

        public static void selectionSort(ObservableList<XYChart.Data<String, Number>> list) {
            int maxIndex;
            for (int listSize = list.size(); listSize > 1; listSize--) {
                maxIndex = findMax(list, listSize);
                if (maxIndex < listSize - 1) {
                    delay();
                    swap(list, maxIndex, listSize - 1);
                }
            }
        }

        public static void bubbleSort(ObservableList<XYChart.Data<String, Number>> list) {
            boolean ordered = false;
            for (int listSize = list.size(); listSize > 1 && !ordered; listSize--) {
                ordered = true; //Hp: the list is ordered
                for (int i = 0; i < listSize - 1; i++) {
                    if (list.get(i).getYValue().intValue() >= list.get(i + 1).getYValue().intValue()) {
                        delay();
                        swap(list, i, i + 1);
                        ordered = false;
                    }
                }
            }
        }

        private static void insertMin(ObservableList<XYChart.Data<String, Number>> list, int lastPos) {
            int i, lastValue = list.get(lastPos).getYValue().intValue();
            for (i = lastPos - 1; i >= 0 && lastValue < list.get(i).getYValue().intValue(); i--) {
                delay();
                swap(list, i + 1, i);
            }
            list.get(i + 1).setYValue(lastValue);
        }

        public static void insertionSort(ObservableList<XYChart.Data<String, Number>> list) {
            for (int i = 1; i < list.size(); i++) {
                insertMin(list, i);
            }
        }

        private static void quickSortRec(ObservableList<XYChart.Data<String, Number>> list, int first, int last) {
            int i, j, pivot;
            if (first < last) {
                i = first;
                j = last;
                pivot = list.get((first + last) / 2).getYValue().intValue();

                do {
                    for (; list.get(i).getYValue().intValue() < pivot; i++);
                    for (; list.get(j).getYValue().intValue() > pivot; j--);

                    if (i <= j) {
                        delay();
                        swap(list, i, j);
                        i++;
                        j--;
                    }
                } while (i <= j);
                quickSortRec(list, first, j);
                quickSortRec(list, i, last);
            }
        }

        public static void quickSort(ObservableList<XYChart.Data<String, Number>> list) {
            quickSortRec(list, 0, list.size() - 1);
        }

        private static void mergeSort(ObservableList<XYChart.Data<String, Number>> list) {
            mergeSortRec(list, 0, barsNumber - 1);
        }

        private static void mergeSortRec(ObservableList<XYChart.Data<String, Number>> list, int start, int end) {
            int mid;
            if (start < end) {
                mid = (start + end) / 2;

                mergeSortRec(list, start, mid);
                mergeSortRec(list, mid + 1, end);

                mergeOperation(list, start, mid, end);
            }
        }
        private static void mergeOperation(ObservableList<XYChart.Data<String, Number>> list, int start, int mid,
                                           int end) {
            int i = start, j = mid + 1, k = start;

            List<Number> tmp = new ArrayList<>();

            for(XYChart.Data<String, Number> data : list){
                tmp.add(data.getYValue());
            }

            while (i <= mid && j <= end) {
                if ((int) tmp.get(i) < (int) tmp.get(j)) {
                    delay();
                    (list.get(k)).setYValue(tmp.get(i));
                    i++;
                    k++;
                } else {
                    delay();
                    (list.get(k)).setYValue(tmp.get(j));
                    j++;
                    k++;
                }
            }
            for (; i <= mid; i++) {
                delay();
                (list.get(k)).setYValue(tmp.get(i));
                k++;
            }
            for (; j <= end; j++) {
                delay();
                (list.get(k)).setYValue( tmp.get(j));
                k++;
            }

        }
    }

    @FXML
    public void handleAbout() {

        Hyperlink linkGA = new Hyperlink("Gabriele Aldovardi -> GitHub");
        linkGA.setOnAction(e -> openWebPage("https://github.com/GabrieleAldovardi"));

        Hyperlink linkFC = new Hyperlink("Filippo Cavalieri -> GitHub");
        linkFC.setOnAction(e -> openWebPage("https://github.com/FilippoCavalieri"));

        VBox vbox = new VBox();
        Label description = new Label("Here some references:");
        vbox.getChildren().addAll(description , linkGA, linkFC);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About us");
        alert.setHeaderText("Hi, we're two Computer Engineers students at UNIMORE, University of Modena and Reggio Emilia");
        alert.getDialogPane().setGraphic(new ImageView(new Image(String.valueOf(this.getClass().getResource("icons/info_icon.png")))));
        alert.getDialogPane().setContent(vbox);
        alert.showAndWait();
    }

    private void openWebPage(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSelection(){
        sortingAlgorithm = sortingAlgorithmChoice.getSelectionModel().getSelectedItem();
        if(sortingAlgorithm != null){
            infoLabel.setVisible(true);
        }
    }
    @FXML
    public void handleInfo(){

    }
}
