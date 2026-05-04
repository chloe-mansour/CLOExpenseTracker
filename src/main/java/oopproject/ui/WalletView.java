package oopproject.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import oopproject.model.User;
import oopproject.service.FinanceService;
import oopproject.util.CurrencyManager;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;

// Interactive stacked wallet used inside the dashboard
public class WalletView {

    private final VBox root;
    private final FinanceService financeService = new FinanceService();

    private static final double PEEK_HEIGHT = 62;
    private static final double OPEN_HEIGHT = 210;
    private static final double CARD_WIDTH = 520;
    private static final double ANIMATION_TIME = 320;

    private int openIndex = -1;

    private final StackPane[] cardWrappers;
    private final VBox[] cardBodies;
    private final double[] currentHeights;

    public WalletView() {
        User currentUser = SessionManager.getCurrentUser();
        int userId = currentUser != null ? currentUser.getId() : 0;

        double essentials = financeService.getCategoryTotal(userId, "Essentials");
        double education = financeService.getCategoryTotal(userId, "Education & Work");
        double savings = financeService.getCategoryTotal(userId, "Savings & Investments");
        double lifestyle = financeService.getCategoryTotal(userId, "Lifestyle & Fun");
        double health = financeService.getCategoryTotal(userId, "Health & Emergencies");

        WalletCardData[] cards = {
                new WalletCardData(
                        "Essentials",
                        "Rent, bills, groceries",
                        essentials,
                        "wallet-red",
                        "Your necessary daily and monthly spending."
                ),
                new WalletCardData(
                        "Education & Work",
                        "Tuition, books, tools",
                        education,
                        "wallet-yellow",
                        "Expenses related to learning, work, and productivity."
                ),
                new WalletCardData(
                        "Savings & Investments",
                        "Reserve, future, investments",
                        savings,
                        "wallet-green",
                        "Money set aside for future plans and financial safety."
                ),
                new WalletCardData(
                        "Lifestyle & Fun",
                        "Shopping, outings, hobbies",
                        lifestyle,
                        "wallet-orange",
                        "Flexible spending for enjoyment and personal activities."
                ),
                new WalletCardData(
                        "Health & Emergencies",
                        "Medicine, doctor, urgent needs",
                        health,
                        "wallet-blue",
                        "Health-related expenses and unexpected situations."
                )
        };

        cardWrappers = new StackPane[cards.length];
        cardBodies = new VBox[cards.length];
        currentHeights = new double[cards.length];

        Label title = new Label("Financial Overview");
        title.getStyleClass().add("wallet-main-title");

        Label subtitle = new Label("Tap a category to expand your wallet.");
        subtitle.getStyleClass().add("wallet-main-subtitle");

        Label currencyBadge = new Label("Viewing in " + CurrencyManager.getCurrencyLabel());
        currencyBadge.getStyleClass().add("currency-pill");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        VBox titleBlock = new VBox(4, title, subtitle);
        titleBlock.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(12, titleBlock, headerSpacer, currencyBadge);
        header.setAlignment(Pos.CENTER_LEFT);

        Pane stackPane = new Pane();
        stackPane.getStyleClass().add("wallet-stack-pane");
        stackPane.setPrefWidth(CARD_WIDTH);
        stackPane.setMinWidth(CARD_WIDTH);
        stackPane.setMaxWidth(CARD_WIDTH);
        stackPane.setPrefHeight(cards.length * PEEK_HEIGHT);

        for (int i = 0; i < cards.length; i++) {
            WalletCardData cardData = cards[i];
            final int index = i;

            Label cardTitle = new Label(cardData.title);
            cardTitle.getStyleClass().add("wallet-card-title");

            Label cardAmount = new Label(CurrencyManager.formatAmount(cardData.total));
            cardAmount.getStyleClass().add("wallet-card-amount");

            Label chevron = new Label("›");
            chevron.getStyleClass().add("wallet-chevron");

            Region rowSpacer = new Region();
            HBox.setHgrow(rowSpacer, Priority.ALWAYS);

            HBox peekRow = new HBox(12, cardTitle, rowSpacer, cardAmount, chevron);
            peekRow.setAlignment(Pos.CENTER_LEFT);
            peekRow.setPadding(new Insets(0, 20, 0, 20));
            peekRow.setPrefHeight(PEEK_HEIGHT);
            peekRow.setMinHeight(PEEK_HEIGHT);
            peekRow.setMaxHeight(PEEK_HEIGHT);

            Label categoryInfo = new Label(cardData.subtitle);
            categoryInfo.getStyleClass().add("wallet-expanded-subtitle");

            Label categoryAdvice = new Label(cardData.description);
            categoryAdvice.getStyleClass().add("wallet-expanded-text");
            categoryAdvice.setWrapText(true);

            Button openButton = new Button("Open Details");
            openButton.getStyleClass().add("wallet-details-button");
            openButton.setOnAction(e ->
                    SceneManager.switchTo(new CategoryDetailsView(cardData.title).getScene(), "CLO - " + cardData.title)
            );

            cardBodies[i] = new VBox(10, categoryInfo, categoryAdvice, openButton);
            cardBodies[i].setPadding(new Insets(4, 20, 18, 20));
            cardBodies[i].setOpacity(0);

            VBox cardContent = new VBox(0, peekRow, cardBodies[i]);

            Rectangle clip = new Rectangle();
            clip.setWidth(CARD_WIDTH);
            clip.setHeight(PEEK_HEIGHT);
            cardContent.setClip(clip);

            currentHeights[i] = PEEK_HEIGHT;

            cardWrappers[i] = new StackPane(cardContent);
            cardWrappers[i].getStyleClass().addAll("wallet-interactive-card", cardData.colorClass);
            cardWrappers[i].setPrefWidth(CARD_WIDTH);
            cardWrappers[i].setMinWidth(CARD_WIDTH);
            cardWrappers[i].setMaxWidth(CARD_WIDTH);
            cardWrappers[i].setPrefHeight(PEEK_HEIGHT);
            cardWrappers[i].setLayoutY(i * PEEK_HEIGHT);

            peekRow.setOnMouseClicked(e -> toggleCard(index, stackPane, cards.length));

            peekRow.setOnMouseEntered(e ->
                    cardWrappers[index].getStyleClass().add("wallet-card-hover")
            );

            peekRow.setOnMouseExited(e ->
                    cardWrappers[index].getStyleClass().remove("wallet-card-hover")
            );

            stackPane.getChildren().add(cardWrappers[i]);
        }

        root = new VBox(18, header, stackPane);
        root.getStyleClass().add("wallet-wrapper");
        root.setPadding(new Insets(26));
        root.setPrefWidth(590);
        root.setMaxWidth(590);
        root.setAlignment(Pos.TOP_CENTER);

        playEntranceAnimation(cards.length);
    }

    private void toggleCard(int index, Pane stackPane, int cardCount) {
        boolean clickedCardWasOpen = openIndex == index;
        int previouslyOpen = openIndex;

        openIndex = clickedCardWasOpen ? -1 : index;

        if (previouslyOpen != -1 && previouslyOpen != index) {
            animateCard(previouslyOpen, PEEK_HEIGHT, false, stackPane, cardCount);
        }

        animateCard(index, clickedCardWasOpen ? PEEK_HEIGHT : OPEN_HEIGHT, !clickedCardWasOpen, stackPane, cardCount);
    }

    private void animateCard(int index, double targetHeight, boolean opening, Pane stackPane, int cardCount) {
        StackPane wrapper = cardWrappers[index];
        VBox body = cardBodies[index];
        VBox cardContent = (VBox) wrapper.getChildren().get(0);
        Rectangle clip = (Rectangle) cardContent.getClip();

        FadeTransition fade = new FadeTransition(Duration.millis(opening ? 180 : 100), body);
        fade.setToValue(opening ? 1 : 0);
        fade.setDelay(Duration.millis(opening ? 110 : 0));
        fade.play();

        Timeline heightAnimation = new Timeline(
                new KeyFrame(
                        Duration.millis(ANIMATION_TIME),
                        new KeyValue(clip.heightProperty(), targetHeight, Interpolator.EASE_BOTH),
                        new KeyValue(wrapper.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH)
                )
        );

        currentHeights[index] = targetHeight;

        heightAnimation.setOnFinished(e -> repositionCards(stackPane, cardCount));
        heightAnimation.play();

        wrapper.getStyleClass().remove("wallet-card-open");

        if (opening) {
            wrapper.getStyleClass().add("wallet-card-open");
        }
    }

    private void repositionCards(Pane stackPane, int cardCount) {
        double yPosition = 0;

        for (int i = 0; i < cardCount; i++) {
            double finalY = yPosition;

            Timeline moveAnimation = new Timeline(
                    new KeyFrame(
                            Duration.millis(ANIMATION_TIME),
                            new KeyValue(cardWrappers[i].layoutYProperty(), finalY, Interpolator.EASE_BOTH)
                    )
            );

            moveAnimation.play();
            yPosition += currentHeights[i];
        }

        stackPane.setPrefHeight(yPosition);
    }

    private void playEntranceAnimation(int cardCount) {
        for (int i = 0; i < cardCount; i++) {
            StackPane card = cardWrappers[i];

            card.setOpacity(0);
            card.setTranslateY(18);

            Timeline entrance = new Timeline(
                    new KeyFrame(
                            Duration.millis(i * 70),
                            new KeyValue(card.opacityProperty(), 0),
                            new KeyValue(card.translateYProperty(), 18)
                    ),
                    new KeyFrame(
                            Duration.millis(i * 70 + 280),
                            new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_OUT)
                    )
            );

            entrance.play();
        }
    }

    public VBox getRoot() {
        return root;
    }

    private static class WalletCardData {
        private final String title;
        private final String subtitle;
        private final double total;
        private final String colorClass;
        private final String description;

        private WalletCardData(String title, String subtitle, double total, String colorClass, String description) {
            this.title = title;
            this.subtitle = subtitle;
            this.total = total;
            this.colorClass = colorClass;
            this.description = description;
        }
    }
}