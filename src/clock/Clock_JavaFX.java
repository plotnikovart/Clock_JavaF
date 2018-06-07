package clock;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.Calendar;

/**
 * JavaFX класс для отрисовки приложения
 * @author Плотников Артем
 */
public class Clock_JavaFX extends Application
{
    public static void main(String[] args)
    {
        Application.launch();
    }

    private int WIDTH = 600;              // ширина и высота панели с часами
    private int TOOL_WIDTH = WIDTH / 5;   // ширина панели с элементами

    private Timeline timeLine;
    private boolean pause = false;

    private double[] arrowsLength = {0.8, 0.65, 0.45};  // коэффиценты длин стрелок
    private Line[] arrows = new Line[3];                // стрелки: секундная, минутная часовая
    private Circle clock;                               // часы
    private Arc sector;                                 // сектор
    private double[] time;                              // массив с текущим временем
    private boolean open = true;                        // флаг, показывающий открывает или закрывает циферблат стрелка

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Clock_JavaFX");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.setResizable(false);

        // Отрисовка панели с часами
        Pane pane = new Pane();
        drawClock(pane);

        // Отрисовка панели с элементами управления
        VBox toolPanel = new VBox();
        drawToolPanel(toolPanel);

        HBox rootNode = new HBox();
        rootNode.getChildren().addAll(pane, toolPanel);

        Scene scene = new Scene(rootNode, WIDTH + TOOL_WIDTH, WIDTH);
        primaryStage.setScene(scene);

        // Инициализация периода обновления таймера
        timeLine = new Timeline(new KeyFrame(Duration.seconds(1), event -> drawArrows()));
        timeLine.setCycleCount(Timeline.INDEFINITE);

        timeInit();     // инициализация текущего времени
        drawArrows();   // установка стрелок в начальное положение

        Thread.sleep(1000);
        timeLine.play();    // запуск таймера
        primaryStage.show();
    }

    /**
     * Прорисовка панели с элементами управления
     * <p>Кнопки: пуск/пауза, синхронизация времени</p>
     * <p>Слайдер: изменение скорости вращения стрелок</p>
     * @param toolPanel VBox панель для добавления
     */
    private void drawToolPanel(VBox toolPanel)
    {
        // Настройка панели
        toolPanel.setAlignment(Pos.TOP_CENTER);
        toolPanel.setPadding(new Insets(WIDTH * 0.1, TOOL_WIDTH / 3, 0, TOOL_WIDTH / 2));
        toolPanel.setSpacing(WIDTH * 0.05);

        // Настройка внешнего вида кнопок
        String style = "-fx-background-color: \n" +
                "linear-gradient(#686868 0%, #232723 25%, #373837 75%, #757575 100%),\n" +
                "linear-gradient(#020b02, #3a3a3a),\n" +
                "linear-gradient(#b9b9b9 0%, #c2c2c2 20%, #afafaf 80%, #c8c8c8 100%),\n" +
                "linear-gradient(#f5f5f5 0%, #dbdbdb 50%, #cacaca 51%, #d7d7d7 100%);\n" +
                "-fx-background-insets: 0,1,4,5;\n" +
                "-fx-background-radius: 9,8,5,4;\n" +
                "-fx-font-family: \"Helvetica\";\n" +
                "-fx-font-size: 18px;\n" +
                "-fx-font-weight: bold;\n" +
                " -fx-text-fill: #333333;\n" +
                "-fx-effect: dropshadow( three-pass-box , rgba(255,255,255,0.2) , 1, 0.0 , 0 , 1);";

        // Кнопка пуска/паузы
        ImageView im1 = new ImageView(new Image(getClass().getResourceAsStream("/play_pause.png")));
        im1.setSmooth(true);
        im1.setFitHeight(TOOL_WIDTH * 0.3);
        im1.setFitWidth(TOOL_WIDTH * 0.3);
        Button playPause = new Button("", im1);
        playPause.setStyle(style);

        // Кнопка перезагрузки времени
        ImageView im2 = new ImageView(new Image(getClass().getResourceAsStream("/refresh.png")));
        im2.setSmooth(true);
        im2.setFitHeight(TOOL_WIDTH * 0.3);
        im2.setFitWidth(TOOL_WIDTH * 0.3);
        Button refresh = new Button("", im2);
        refresh.setStyle(style);

        // Слайдер, регулирующий скорость поворота стрелки
        Slider slider = new Slider();
        slider.setMin(1);
        slider.setMax(10);
        slider.setValue(1);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(1);
        slider.setBlockIncrement(0.1);
        slider.setShowTickMarks(true);
        slider.setMinHeight(WIDTH / 2);

        // Привязка событий к элементам управления
        playPause.setOnAction(event ->
        {
            if (pause) timeLine.play();
            else timeLine.pause();

            pause = !pause;
        });

        refresh.setOnAction(event ->
        {
            slider.setValue(1);
            timeInit();
            timeLine.play();
        });

        slider.valueProperty().addListener((v1, v2, vNew) ->
        {
            timeLine.stop();
            timeLine = new Timeline(new KeyFrame(Duration.seconds(1 / vNew.doubleValue()), event -> drawArrows()));
            timeLine.setCycleCount(Timeline.INDEFINITE);
            timeLine.play();
        });

        toolPanel.getChildren().addAll(playPause, refresh, slider);
    }

    /**
     * Прорисовка циферблата с делениями и стрелками
     * @param pane Pane панель для рисования
     */
    private void drawClock(Pane pane)
    {
        // Добавление циферблата
        clock = new Circle(WIDTH / 2, WIDTH / 2, WIDTH * 0.45, Color.TRANSPARENT);
        clock.setStroke(Color.GRAY);
        clock.setStrokeWidth(clock.getRadius() * 0.1);

        // Добавлние фоторафии в циферблат
        Image photo = new Image(getClass().getResourceAsStream("/photo.jpg"));
        clock.setFill(new ImagePattern(photo));
        pane.getChildren().add(clock);

        double currentRadius = clock.getRadius() - clock.getStrokeWidth() / 2;

        // Добавление сектора
        sector = new Arc();
        sector.setCenterX(clock.getCenterX());
        sector.setCenterY(clock.getCenterY());
        sector.setRadiusX(currentRadius);
        sector.setRadiusY(currentRadius);
        sector.setType(ArcType.ROUND);
        sector.setStartAngle(90);
        pane.getChildren().add(sector);

        // Добавление делений
        for (int i = 0; i < 60; i++)
        {
            double alpha = i * Math.PI / 30;
            Line line = new Line();
            line.setStrokeWidth(currentRadius * 0.015);

            line.setStartX(clock.getCenterX() + (currentRadius - line.getStrokeWidth() / 2) * Math.sin(alpha));
            line.setStartY(clock.getCenterY() - (currentRadius - line.getStrokeWidth() / 2) * Math.cos(alpha));
            line.setEndX(clock.getCenterX() + (i % 5 == 0 ? 0.85 : 0.95) * (currentRadius - line.getStrokeWidth() / 2) * Math.sin(alpha));
            line.setEndY(clock.getCenterY() - (i % 5 == 0 ? 0.85 : 0.95) * (currentRadius - line.getStrokeWidth() / 2) * Math.cos(alpha));

            line.setStroke(Color.WHITE);
            pane.getChildren().add(line);
        }

        // Добавление стрелок
        double[] width = {0.01, 0.025, 0.030};
        for (int i = 0; i < 3; i++)
        {
            Line line = new Line();
            line.setStrokeWidth(currentRadius * width[i]);
            line.setStroke(Color.WHITE);

            line.setStartX(clock.getCenterX());
            line.setStartY(clock.getCenterY());

            arrowsLength[i] *= currentRadius;   // заполнение массива длин стрелок
            arrows[i] = line;   // добывление стрелок в массив стрелок

            pane.getChildren().add(line);
        }

        // Добавление центральной оси
        Circle axis = new Circle(WIDTH / 2, WIDTH / 2, currentRadius * 0.03, Color.WHITE);
        pane.getChildren().add(axis);
    }

    /**
     * Перерировка стрелок и сектора
     */
    private void drawArrows()
    {
        // Обновление времени
        time[0] = (time[0] + 1) % 60;
        time[1] = (time[1] + 1.0 / 60) % 60;
        time[2] = (time[2] + 1.0 / 3600) % 60;

        // Отрисовка стрелок
        int[] ang = {6, 6, 30};
        for (int i = 0; i < 3; i++)
        {
            arrows[i].setEndX(clock.getCenterX() + Math.sin(ang[i] * time[i] * Math.PI / 180) * arrowsLength[i]);
            arrows[i].setEndY(clock.getCenterY() - Math.cos(ang[i] * time[i] * Math.PI / 180) * arrowsLength[i]);
        }

        // Отрисовка сектора
        if (open)
            sector.setLength(360 - 6 * time[0]);
        else
        {
            sector.setStartAngle(90 - time[0] * 6);
            sector.setLength(time[0] * 6);
        }

        // Оборот пройден
        if (time[0] == 0)
        {
            open = !open;
            sector.setStartAngle(90);
            if (open) sector.setLength(360);
            else sector.setLength(0);
        }
    }

    /**
     * Установка системного времени
     */
    private void timeInit()
    {
        time = new double[3];
        time[0] = Calendar.getInstance().get(Calendar.SECOND);  // seconds
        time[1] = Calendar.getInstance().get(Calendar.MINUTE) + time[0] / 60;   // minutes
        time[2] = (Calendar.getInstance().get(Calendar.HOUR) + time[1] / 60) % 12;   // hours
    }
}