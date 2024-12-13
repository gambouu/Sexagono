package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import edu.upc.epsevg.prop.hex.PlayerType;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Sexagono implements IPlayer, IAuto {

    private String name;

    public Sexagono(String name) {
        this.name = name;
    }

    @Override
    public PlayerMove move(HexGameStatus hgs) {
        PlayerType currentPlayer = hgs.getCurrentPlayer(); 
        int size = hgs.getSize();

        if (!hayMovimientosDisponibles(hgs)) {
            return null;
        }

        int bestDistance = Integer.MAX_VALUE;
        Point bestMove = null;
        int currentPlayerInt = playerToInt(currentPlayer);

        // Copiamos el estado del tablero a un array local
        int[][] board = copyBoard(hgs);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) {
                    // Simulamos el movimiento poniendo la pieza del jugador actual
                    board[i][j] = currentPlayerInt;

                    int dist = computeShortestPath(board, currentPlayer);

                    // Deshacemos el movimiento
                    board[i][j] = 0;

                    if (dist < bestDistance) {
                        bestDistance = dist;
                        bestMove = new Point(i, j);
                    }
                }
            }
        }

        return new PlayerMove(bestMove, 0L, 0, SearchType.RANDOM); 
        // Usa SearchType.RANDOM u otro que exista en tu enumeración
    }

    /**
     * Convierte un PlayerType a su valor entero correspondiente.
     * PLAYER1 -> 1
     * PLAYER2 -> 2
     */
    private int playerToInt(PlayerType p) {
        return (p == PlayerType.PLAYER1) ? 1 : 2;
    }

    private boolean hayMovimientosDisponibles(HexGameStatus hgs) {
        int size = hgs.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (hgs.getPos(i, j) == 0) return true;
            }
        }
        return false;
    }

    /**
     * Copia el estado del tablero de hgs a un array bidimensional.
     */
    private int[][] copyBoard(HexGameStatus hgs) {
        int size = hgs.getSize();
        int[][] board = new int[size][size];
        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                board[i][j] = hgs.getPos(i, j);
            }
        }
        return board;
    }

    /**
     * Calcula la distancia más corta entre los lados que el jugador actual necesita conectar.
     */
    private int computeShortestPath(int[][] board, PlayerType player) {
        return shortestPathWithDistances(board, player);
    }

    /**
     * Hace un BFS con distancias para encontrar la distancia mínima de conexión.
     */
    private int shortestPathWithDistances(int[][] board, PlayerType player) {
        int size = board.length;
        boolean[][] visited = new boolean[size][size];
        int[][] dist = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                dist[i][j] = Integer.MAX_VALUE;
            }
        }

        Queue<Point> queue = new ArrayDeque<>();
        int pInt = playerToInt(player);

        if (player == PlayerType.PLAYER1) {
            // PLAYER1 conecta de top a bottom
            for (int j = 0; j < size; j++) {
                if (esTransitable(board, 0, j, pInt)) {
                    visited[0][j] = true;
                    dist[0][j] = 0;
                    queue.add(new Point(0, j));
                }
            }
        } else {
            // PLAYER2 conecta de left a right
            for (int i = 0; i < size; i++) {
                if (esTransitable(board, i, 0, pInt)) {
                    visited[i][0] = true;
                    dist[i][0] = 0;
                    queue.add(new Point(i, 0));
                }
            }
        }

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            int x = p.x;
            int y = p.y;
            int d = dist[x][y];

            if ((player == PlayerType.PLAYER1 && x == size - 1) || (player == PlayerType.PLAYER2 && y == size - 1)) {
                return d; // Conexión encontrada
            }

            for (Point neigh : getNeighbors(size, x, y)) {
                int nx = neigh.x;
                int ny = neigh.y;
                if (!visited[nx][ny] && esTransitable(board, nx, ny, pInt)) {
                    visited[nx][ny] = true;
                    dist[nx][ny] = d + 1;
                    queue.add(new Point(nx, ny));
                }
            }
        }

        return Integer.MAX_VALUE; // No hay conexión
    }

    /**
     * Determina si la celda es transitable para el jugador dado.
     */
    private boolean esTransitable(int[][] board, int x, int y, int playerInt) {
        int v = board[x][y];
        return (v == 0 || v == playerInt);
    }

    /**
     * Retorna las celdas adyacentes a (x, y) en el tablero Hex.
     */
    private List<Point> getNeighbors(int size, int x, int y) {
        List<Point> neighbors = new ArrayList<>();
        int[][] dirs = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 },
            { -1, 1 },
            { 1, -1 }
        };
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 0 && nx < size && ny >= 0 && ny < size) {
                neighbors.add(new Point(nx, ny));
            }
        }
        return neighbors;
    }

    @Override
    public void timeout() {
        // No implementamos nada
    }

    @Override
    public String getName() {
        return "Sexagono(" + name + ")";
    }
}
