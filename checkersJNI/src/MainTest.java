import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MainTest {
    private static Main game;

    @BeforeAll
    public static void setupOnce() {
        System.loadLibrary("checkers");
        game = new Main();
    }

    @Test
    public void testBoardInitialization() {
        game.resetGame();
        int[][] initialBoard = game.getCheckerboard();
        int[][] expectedBoard = {
                {0, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0},
                {0, 1, 0, 1, 0, 1, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {-1, 0, -1, 0, -1, 0, -1, 0},
                {0, -1, 0, -1, 0, -1, 0, -1},
                {-1, 0, -1, 0, -1, 0, -1, 0}
        };
        assertArrayEquals(expectedBoard, initialBoard);
    }

    @Test
    public void testSwitchPlayerTurn() {
        game.resetGame();
        assertEquals(1, game.getCurrentPlayer());
        game.selectOrMove(2, 3);
        game.selectOrMove(3, 4);
        assertEquals(-1, game.getCurrentPlayer());
    }

    @Test
    public void testValidMove() {
        game.resetGame();
        assertTrue(game.selectOrMove(2, 3));
        assertTrue(game.selectOrMove(3, 4));
        assertEquals(1, game.getPiece(3, 4));
        assertEquals(0, game.getPiece(2, 3));
    }

    @Test
    public void testInvalidMove() {
        assertFalse(game.selectOrMove(3, 0));
        game.selectOrMove(2, 3);
        assertFalse(game.selectOrMove(3, 3));
    }

    @Test
    public void testScoreInitialization() {
        assertEquals(0, game.getScorePlayer1());
        assertEquals(0, game.getScorePlayer2());
    }

    @Test
    public void testWin() {
        game.resetGame();
        game.setPlayerScore(1);
        assertEquals(14, game.getScorePlayer1());
        boolean moveResult = game.selectOrMove(2, 3);
        assertFalse(moveResult);
    }
    @Test
    public void testCapturePiece() {
        game.resetGame();
        game.selectOrMove(2, 1);
        game.selectOrMove(3, 2);
        game.selectOrMove(5, 4);
        game.selectOrMove(4, 3);
        game.selectOrMove(3, 2);
        game.selectOrMove(5, 4);

        assertEquals(1, game.getScorePlayer1());
        assertEquals(1, game.getPiece(5, 4));
        assertEquals(0, game.getPiece(4, 3));
    }
}


