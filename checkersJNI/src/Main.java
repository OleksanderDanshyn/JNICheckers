import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Objects;

public class Main {
    static {
        System.loadLibrary("checkers");
    }

    public native boolean selectOrMove(int row, int col);
    public native int[] getValidMoves(int row, int col);
    public native int getPiece(int x, int y);
    public native int getCurrentPlayer();
    public native int getScorePlayer1();
    public native int getScorePlayer2();
    public native int[][] getCheckerboard();
    public native void resetGame();
    public native void setPlayerScore(int player);

    public static final JButton[][] buttons = new JButton[8][8];
    public JLabel player1Label;
    public static Main game;
    public JLabel player2Label;
    public static int selectedRow = -1;
    public static int selectedCol = -1;

    public static void main(String[] args) {
        game = new Main();
        JFrame frame = new JFrame("Checkers Game");
        frame.setLayout(new BorderLayout());
        game.setupBoard(frame);
        frame.setSize(860, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        game.setupFigures();
        game.updateTurnAndScores();

        frame.requestFocusInWindow();

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (selectedRow == -1 || selectedCol == -1) {
                    selectedRow = 0;
                    selectedCol = 0;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        selectedRow = Math.max(selectedRow - 1, 0);
                        break;
                    case KeyEvent.VK_DOWN:
                        selectedRow = Math.min(selectedRow + 1, 7);
                        break;
                    case KeyEvent.VK_LEFT:
                        selectedCol = Math.max(selectedCol - 1, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        selectedCol = Math.min(selectedCol + 1, 7);
                        break;
                    case KeyEvent.VK_ENTER:
                        game.handleSelection(selectedRow, selectedCol);
                        break;
                }
                clearHighlights();
                if (selectedRow >= 0 && selectedCol >= 0) {
                    buttons[selectedRow][selectedCol].setBackground(Color.GREEN);
                }
            }
        });
    }

    public void setupBoard(JFrame frame) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(800, 50));

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new FlowLayout());

        player1Label = new JLabel("Player 1 (White) Score: 0");
        player2Label = new JLabel("Player 2 (Black) Score: 0");

        Font font = new Font("Arial", Font.PLAIN, 20);
        player1Label.setFont(font);
        player2Label.setFont(font);

        player1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        player2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        player1Label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));
        player2Label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        scorePanel.add(player1Label);
        scorePanel.add(player2Label);
        infoPanel.add(scorePanel);
        frame.add(infoPanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(8, 8));

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JButton button = new JButton();
                if ((i + j) % 2 == 0) {
                    button.setBackground(Color.WHITE);
                } else {
                    button.setBackground(Color.BLACK);
                }
                int finalJ = j;
                int finalI = i;
                button.addActionListener(e -> game.handleSelection(finalI, finalJ));
                buttons[i][j] = button;
                boardPanel.add(button);
            }
        }
        frame.add(boardPanel, BorderLayout.CENTER);
    }

    private boolean pieceSelected = false;

    public void handleSelection(int row, int col) {
        if (!pieceSelected) {
            if (selectOrMove(row, col)) {
                clearHighlights();
                int[] validMoves = getValidMoves(row, col);

                if (validMoves.length % 3 != 0) {
                    System.err.println("Invalid moves length: " + validMoves.length);
                    return;
                }

                for (int i = 0; i < validMoves.length; i += 3) {
                    int moveRow = validMoves[i];
                    int moveCol = validMoves[i + 1];
                    boolean isCaptureMove = validMoves[i + 2] == 1;

                    if (isCaptureMove) {
                        buttons[moveRow][moveCol].setBackground(Color.RED);
                    } else {
                        buttons[moveRow][moveCol].setBackground(Color.YELLOW);
                    }
                }

                pieceSelected = true;
                selectedRow = row;
                selectedCol = col;
            }
        } else {
            if (selectOrMove(row, col)) {
                clearHighlights();
                setupFigures();
                updateTurnAndScores();

                if (checkForWin()) {
                    String winningMessage;
                    if (getScorePlayer1() >= 13) {
                        winningMessage = "Game over. Player 1 (White) wins!";
                    } else {
                        winningMessage = "Game over. Player 2 (Black) wins!";
                    }
                    showGameOverDialog(winningMessage);
                }
                pieceSelected = false;
            } else {
                clearHighlights();
                pieceSelected = false;
            }
        }

        SwingUtilities.getWindowAncestor(buttons[row][col]).requestFocusInWindow();
    }

    private boolean checkForWin() {
        return getScorePlayer1() >= 13 || getScorePlayer2() >= 13;
    }

    public void showGameOverDialog(String message) {
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog("Game Over");

        JButton restartButton = new JButton("Restart Game");
        restartButton.setBackground(Color.GREEN);
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.addActionListener(e -> {
            resetGame();
            setupFigures();
            updateTurnAndScores();
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(restartButton);

        optionPane.setMessage(message);
        optionPane.setOptions(new Object[]{});
        optionPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        optionPane.add(buttonPanel);

        dialog.setModal(true);
        dialog.setSize(300, 150);
        dialog.setVisible(true);
    }

    public static void clearHighlights() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    buttons[i][j].setBackground(Color.WHITE);
                } else {
                    buttons[i][j].setBackground(Color.BLACK);
                }
            }
        }
    }

    public void setupFigures() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece = getPiece(i, j);
                JButton button = buttons[i][j];
                if (piece == 1) {
                    button.setIcon(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/images/checkerWhite.png"))));
                } else if (piece == -1) {
                    button.setIcon(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/images/checkerBlack.png"))));
                } else {
                    button.setIcon(null);
                }
            }
        }
    }

    public void updateTurnAndScores() {
        int currentPlayer = getCurrentPlayer();
        int score1 = getScorePlayer1();
        int score2 = getScorePlayer2();

        player1Label.setText("Player 1 (White) Score: " + score1);
        player2Label.setText("Player 2 (Black) Score: " + score2);

        if (currentPlayer == 1) {
            player1Label.setForeground(Color.RED);
            player2Label.setForeground(Color.BLACK);
        } else {
            player1Label.setForeground(Color.BLACK);
            player2Label.setForeground(Color.RED);
        }
    }
}
