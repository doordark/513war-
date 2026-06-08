package main;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

/**
 * 排行榜面板 —— 展示历史高分记录。
 */
public class LeaderboardOverlay extends StackPane {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;

    private final ScoreManager scoreManager;
    private final Runnable onBack;

    private VBox scoreList;

    public LeaderboardOverlay(ScoreManager scoreManager, Runnable onBack) {
        this.scoreManager = scoreManager;
        this.onBack = onBack;

        setPrefSize(WIDTH, HEIGHT);
        setAlignment(Pos.TOP_CENTER);
        setVisible(false);
        setManaged(false);

        // 半透明背景
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("-fx-background-color: rgba(8, 8, 24, 0.95);");
        backgroundLayer.setPrefSize(WIDTH, HEIGHT);

        // 主内容
        VBox mainContent = new VBox();
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(24, 32, 24, 32));
        mainContent.setSpacing(16);
        mainContent.setPrefSize(WIDTH - 64, HEIGHT - 48);

        // 标题栏
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(16);
        topBar.setPrefWidth(WIDTH - 64);

        Button backBtn = createBackButton("← 返回", e -> {
            setVisible(false);
            setManaged(false);
            if (onBack != null) onBack.run();
        });

        Label titleLabel = new Label("🏆 排行榜");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.4), 8, 0, 0, 0);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label("共 " + scoreManager.getScoreCount() + " 条记录");
        countLabel.setFont(Font.font("Microsoft YaHei", 13));
        countLabel.setTextFill(Color.color(0.5, 0.55, 0.65));

        topBar.getChildren().addAll(backBtn, titleLabel, spacer, countLabel);
        mainContent.getChildren().add(topBar);

        // 分数列表
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(WIDTH - 64);
        scrollPane.setPrefHeight(380);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        scoreList = new VBox();
        scoreList.setSpacing(8);
        scoreList.setAlignment(Pos.TOP_CENTER);
        scoreList.setPadding(new Insets(8));

        refreshScoreList();
        scrollPane.setContent(scoreList);
        mainContent.getChildren().add(scrollPane);

        getChildren().addAll(backgroundLayer, mainContent);
    }

    private void refreshScoreList() {
        scoreList.getChildren().clear();
        List<ScoreEntry> scores = scoreManager.getTopScores();

        if (scores.isEmpty()) {
            Label emptyLabel = new Label("暂无记录，快来挑战吧！");
            emptyLabel.setFont(Font.font("Microsoft YaHei", 16));
            emptyLabel.setTextFill(Color.color(0.5, 0.55, 0.65));
            emptyLabel.setPadding(new Insets(40, 0, 40, 0));
            scoreList.getChildren().add(emptyLabel);
            return;
        }

        for (int i = 0; i < scores.size(); i++) {
            ScoreEntry entry = scores.get(i);
            HBox row = createScoreRow(entry, i + 1);
            scoreList.getChildren().add(row);
        }
    }

    private HBox createScoreRow(ScoreEntry entry, int rank) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(12);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setPrefWidth(WIDTH - 96);

        // 排名
        Label rankLabel = new Label("#" + rank);
        rankLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        rankLabel.setPrefWidth(40);
        if (rank == 1) {
            rankLabel.setTextFill(Color.web("#ffd700"));
        } else if (rank == 2) {
            rankLabel.setTextFill(Color.web("#c0c0c0"));
        } else if (rank == 3) {
            rankLabel.setTextFill(Color.web("#cd7f32"));
        } else {
            rankLabel.setTextFill(Color.color(0.5, 0.55, 0.65));
        }

        // 名字
        Label nameLabel = new Label(entry.getName());
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setPrefWidth(120);

        // 分数
        Label scoreLabel = new Label(String.valueOf(entry.getScore()));
        scoreLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        scoreLabel.setTextFill(Color.web("#ffd700"));
        scoreLabel.setPrefWidth(80);

        // 波次
        Label waveLabel = new Label("波次 " + entry.getWave());
        waveLabel.setFont(Font.font("Microsoft YaHei", 12));
        waveLabel.setTextFill(Color.color(0.6, 0.65, 0.75));
        waveLabel.setPrefWidth(80);

        // 难度
        Label diffLabel = new Label(entry.getDifficulty());
        diffLabel.setFont(Font.font("Microsoft YaHei", 11));
        diffLabel.setTextFill(Color.color(0.55, 0.6, 0.7));
        diffLabel.setPrefWidth(60);

        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel, waveLabel, diffLabel);

        // 卡片背景
        row.setStyle(
            "-fx-background-color: rgba(28, 33, 45, 0.7);" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(80, 100, 130, 0.15);" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;"
        );

        return row;
    }

    private Button createBackButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Microsoft YaHei", 13));
        btn.setTextFill(Color.color(0.7, 0.8, 1));
        btn.setStyle(
            "-fx-background-color: rgba(45, 55, 75, 0.6);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(90, 120, 170, 0.3);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: rgba(65, 80, 105, 0.8);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(120, 160, 220, 0.6);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.5;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: rgba(45, 55, 75, 0.6);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(90, 120, 170, 0.3);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnAction(handler);
        return btn;
    }

    public void show() {
        refreshScoreList();
        setOpacity(0);
        setVisible(true);
        setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}
