package main;

import javafx.animation.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sound.SoundManager;
import javafx.util.Duration;

/**
 * 游戏设置面板 — JavaFX 组件，匹配主菜单风格
 */
public class SettingsOverlay extends StackPane {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 600;

    private final GamePanel gamePanel;

    // 背景层
    private Pane backgroundLayer;

    // UI 层
    private VBox uiLayer;

    // 控件
    private Slider bgmSlider;
    private Slider sfxSlider;
    private Button backBtn;

    public SettingsOverlay(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        setPrefSize(WIDTH, HEIGHT);
        setAlignment(Pos.TOP_LEFT);
        setVisible(false);
        setManaged(false);

        // 加载 CSS
        String cssPath = getClass().getResource("/styles/game.css").toExternalForm();
        getStylesheets().add(cssPath);

        // ===== 背景层 =====
        backgroundLayer = new Pane();
        backgroundLayer.setPrefSize(WIDTH, HEIGHT);
        backgroundLayer.setMinSize(WIDTH, HEIGHT);
        backgroundLayer.setMaxSize(WIDTH, HEIGHT);
        backgroundLayer.setStyle("-fx-background-color: #0a0a1e;");

        // ===== UI 层 =====
        uiLayer = new VBox();
        uiLayer.setPrefSize(WIDTH, HEIGHT);
        uiLayer.setAlignment(Pos.TOP_CENTER);
        uiLayer.setPadding(new Insets(0));
        uiLayer.setSpacing(0);

        // 标题
        VBox titleArea = new VBox();
        titleArea.setAlignment(Pos.CENTER);
        titleArea.setPadding(new Insets(50, 0, 20, 0));

        Label titleLabel = new Label("游戏设置");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(4, 0, 2, Color.color(0, 0, 0, 0.35)));

        setupTitleAnimation(titleLabel);
        titleArea.getChildren().add(titleLabel);

        // 设置项列表
        VBox settingsBox = new VBox();
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setSpacing(20);
        settingsBox.setPadding(new Insets(10, 0, 20, 0));

        // BGM 音量
        HBox bgmRow = createSettingRow("背景音乐", createVolumeSlider(gamePanel.getSettings().getBgmVolume(), (obs, oldVal, newVal) -> {
            gamePanel.getSettings().setBgmVolume(newVal.doubleValue());
            SoundManager.getInstance().setBgmVolume(newVal.doubleValue());
            System.out.println("[设置] BGM音量=" + (int)(newVal.doubleValue() * 100) + "%");
        }));

        // 音效音量
        HBox sfxRow = createSettingRow("音效", createVolumeSlider(gamePanel.getSettings().getSfxVolume(), (obs, oldVal, newVal) -> {
            gamePanel.getSettings().setSfxVolume(newVal.doubleValue());
            SoundManager.getInstance().setSfxVolume(newVal.doubleValue());
            System.out.println("[设置] 音效音量=" + (int)(newVal.doubleValue() * 100) + "%");
        }));

        settingsBox.getChildren().addAll(bgmRow, sfxRow);

        // 底部提示
        Label hintLabel = new Label("设置会自动保存");
        hintLabel.setFont(Font.font("Microsoft YaHei", 12));
        hintLabel.setTextFill(Color.color(0.5, 0.5, 0.6, 0.5));
        hintLabel.setPadding(new Insets(10, 0, 10, 0));

        // 返回按钮
        backBtn = createBackButton("← 返回", e -> {
            setVisible(false);
            setManaged(false);
            gamePanel.resumeFromOverlay();
        });

        uiLayer.getChildren().addAll(titleArea, settingsBox, hintLabel, backBtn);

        getChildren().addAll(backgroundLayer, uiLayer);
    }

    /** 标题呼吸发光特效 */
    private void setupTitleAnimation(Label titleLabel) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0.1, 0.3, 0.6, 0.5));
        shadow.setRadius(8);
        shadow.setOffsetY(2);

        Timeline anim = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(shadow.radiusProperty(), 6),
                new KeyValue(shadow.colorProperty(), Color.color(0.1, 0.3, 0.6, 0.3))),
            new KeyFrame(Duration.seconds(2.5),
                new KeyValue(shadow.radiusProperty(), 14),
                new KeyValue(shadow.colorProperty(), Color.color(0.2, 0.5, 0.9, 0.6)))
        );
        anim.setCycleCount(Timeline.INDEFINITE);
        anim.setAutoReverse(true);

        titleLabel.setEffect(shadow);
        anim.play();
    }

    /** 创建设置行 */
    private HBox createSettingRow(String label, Slider slider) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setSpacing(30);
        row.setPadding(new Insets(5, 0, 5, 0));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.color(0.8, 0.85, 1, 0.9));
        lbl.setPrefWidth(100);
        lbl.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(lbl, slider);
        return row;
    }

    /** 创建音量滑块 */
    private Slider createVolumeSlider(double initialValue, javafx.beans.value.ChangeListener<Number> listener) {
        Slider slider = new Slider(0, 1, initialValue);
        slider.setPrefWidth(300);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);

        // 自定义滑块样式
        slider.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-pref-height: 24px;");

        slider.valueProperty().addListener(listener);
        return slider;
    }

    /** 创建返回按钮 */
    private Button createBackButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setPrefSize(120, 42);
        btn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 15));
        btn.setTextFill(Color.WHITE);
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4a5568, #2d3748); " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: rgba(255,255,255,0.15); " +
            "-fx-border-radius: 10; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        btn.setOpacity(0);
        btn.setTranslateY(15);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), btn);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), btn);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            scaleDown.stop();
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #5a6578, #4a5568); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(255,255,255,0.3); " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);");
            scaleUp.playFromStart();
        });

        btn.setOnMouseExited(e -> {
            scaleUp.stop();
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4a5568, #2d3748); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(255,255,255,0.15); " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");
            scaleDown.playFromStart();
        });

        btn.setOnAction(handler);
        return btn;
    }

    /** 显示面板并播放入场动画 */
    public void show() {
        setVisible(true);
        setManaged(true);

        SequentialTransition anim = new SequentialTransition();

        // 设置行淡入
        VBox settingsBox = (VBox) uiLayer.getChildren().get(1);
        for (int i = 0; i < settingsBox.getChildren().size(); i++) {
            Node node = settingsBox.getChildren().get(i);
            node.setOpacity(0);
            node.setTranslateY(20);

            ParallelTransition pt = new ParallelTransition();
            FadeTransition fade = new FadeTransition(Duration.millis(400), node);
            fade.setFromValue(0);
            fade.setToValue(1);
            TranslateTransition trans = new TranslateTransition(Duration.millis(400), node);
            trans.setFromY(20);
            trans.setToY(0);
            pt.getChildren().addAll(fade, trans);
            anim.getChildren().add(pt);
        }

        // 返回按钮淡入
        backBtn.setOpacity(0);
        backBtn.setTranslateY(20);
        ParallelTransition backPt = new ParallelTransition();
        FadeTransition backFade = new FadeTransition(Duration.millis(400), backBtn);
        backFade.setFromValue(0);
        backFade.setToValue(1);
        TranslateTransition backTrans = new TranslateTransition(Duration.millis(400), backBtn);
        backTrans.setFromY(20);
        backTrans.setToY(0);
        backPt.getChildren().addAll(backFade, backTrans);
        anim.getChildren().add(backPt);

        anim.play();
    }
}
