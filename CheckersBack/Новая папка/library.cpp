#include <jni.h>
#include <vector>
#include <cmath>
#include <iostream>

int checkerboard[8][8] = {
        {0, 1, 0, 1, 0, 1, 0, 1},
        {1, 0, 1, 0, 1, 0, 1, 0},
        {0, 1, 0, 1, 0, 1, 0, 1},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {-1, 0, -1, 0, -1, 0, -1, 0},
        {0, -1, 0, -1, 0, -1, 0, -1},
        {-1, 0, -1, 0, -1, 0, -1, 0}
};

int currentPlayer = 1;
int startX = -1, startY = -1;
int scorePlayer1 = 0;
int scorePlayer2 = 0;

extern "C" {

JNIEXPORT jint JNICALL Java_Main_getPiece(JNIEnv *, jclass, jint x, jint y) {
    return (x >= 0 && x < 8 && y >= 0 && y < 8) ? checkerboard[x][y] : 0;
}

JNIEXPORT jint JNICALL Java_Main_getCurrentPlayer(JNIEnv *, jclass) {
    return currentPlayer;
}

JNIEXPORT jint JNICALL Java_Main_getScorePlayer1(JNIEnv *, jclass) {
    return scorePlayer1;
}

JNIEXPORT jint JNICALL Java_Main_getScorePlayer2(JNIEnv *, jclass) {
    return scorePlayer2;
}

bool isGameOver() {
    return scorePlayer1 >= 13 || scorePlayer2 >= 13;
}

JNIEXPORT jboolean JNICALL Java_Main_selectOrMove(JNIEnv *, jclass, jint row, jint col) {
    if (isGameOver()) {
        return false;
    }

    if (startX == -1 && startY == -1) {
        if (checkerboard[row][col] * currentPlayer > 0) {
            startX = row;
            startY = col;
            return true;
        }
        return false;
    }

    int piece = checkerboard[startX][startY];
    int targetPiece = checkerboard[row][col];

    if (targetPiece != 0 || abs(row - startX) > 2 || abs(col - startY) > 2 || abs(row - startX) != abs(col - startY)) {
        startX = startY = -1;
        return false;
    }

    if ((piece == 1 && row < startX) || (piece == -1 && row > startX)) {
        startX = startY = -1;
        return false;
    }

    if (abs(row - startX) == 2) {
        int midX = (startX + row) / 2;
        int midY = (startY + col) / 2;
        if (checkerboard[midX][midY] * currentPlayer < 0) {
            checkerboard[midX][midY] = 0;
            if (currentPlayer == 1) scorePlayer1++;
            else scorePlayer2++;
        } else {
            startX = startY = -1;
            return false;
        }
    }
    checkerboard[row][col] = piece;
    checkerboard[startX][startY] = 0;

    if ((row == 0 && piece == -1) || (row == 7 && piece == 1)) {
        checkerboard[row][col] = 0;
        if (piece == -1) scorePlayer2++;
        else scorePlayer1++;
        std::cout << "Player " << (piece == 1 ? "2 (Black)" : "1 (White)") << " reached the end and gains a point!" << std::endl;
    }

    startX = startY = -1;
    currentPlayer = -currentPlayer;
    return true;
}

JNIEXPORT jintArray JNICALL Java_Main_getValidMoves(JNIEnv *env, jclass, jint x, jint y) {
    std::vector<int> moves;
    int piece = checkerboard[x][y];
    int direction = (piece > 0) ? 1 : -1;

    for (int dy : {-1, 1}) {
        int nx = x + direction;
        int ny = y + dy;

        if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8 && checkerboard[nx][ny] == 0) {
            moves.push_back(nx);
            moves.push_back(ny);
            moves.push_back(0);
        }

        int jumpX = x + 2 * direction;
        int jumpY = y + 2 * dy;
        if (jumpX >= 0 && jumpX < 8 && jumpY >= 0 && jumpY < 8 && checkerboard[nx][ny] * currentPlayer < 0 && checkerboard[jumpX][jumpY] == 0) {
            moves.push_back(jumpX);
            moves.push_back(jumpY);
            moves.push_back(1);
        }
    }

    jintArray result = env->NewIntArray(moves.size());
    env->SetIntArrayRegion(result, 0, moves.size(), moves.data());
    return result;
}

//https://stackoverflow.com/questions/43865462/return-multidimensional-array-in-jni
JNIEXPORT jobjectArray JNICALL Java_Main_getCheckerboard(JNIEnv *env, jclass) {
    jclass intArrayClass = env->FindClass("[I");
    jobjectArray result = env->NewObjectArray(8, intArrayClass, nullptr);

    for (int i = 0; i < 8; i++) {
        jintArray row = env->NewIntArray(8);
        if (row == nullptr) {
            return nullptr;
        }
        env->SetIntArrayRegion(row, 0, 8, reinterpret_cast<jint*>(checkerboard[i]));
        env->SetObjectArrayElement(result, i, row);
        env->DeleteLocalRef(row);
    }

    return result;
}

JNIEXPORT void JNICALL Java_Main_resetGame(JNIEnv *, jclass) {
    int initialBoard[8][8] = {
            {0, 1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1, 0, 1},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {-1, 0, -1, 0, -1, 0, -1, 0},
            {0, -1, 0, -1, 0, -1, 0, -1},
            {-1, 0, -1, 0, -1, 0, -1, 0}
    };

    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
            checkerboard[i][j] = initialBoard[i][j];
        }
    }

    scorePlayer1 = 0;
    scorePlayer2 = 0;
    currentPlayer = 1;
}

JNIEXPORT void JNICALL Java_Main_setPlayerScore(JNIEnv *, jclass, jint player) {
    if (player == 1) {
        scorePlayer1 = 14;
    } else if (player == 2) {
        scorePlayer2 = 14;
    } else {
        std::cerr << "Invalid player number. Use 1 or 2." << std::endl;
    }
}
}
