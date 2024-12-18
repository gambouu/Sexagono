package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import edu.upc.epsevg.prop.hex.PlayerType;
import edu.upc.epsevg.prop.hex.MoveNode;

import java.awt.Point;
import java.util.List;

/**
 * Jugador de Hex con Minimax y Poda Alfa-Beta sin heurística,
 * explorando completamente hasta la profundidad máxima definida.
 */
public class Sexagono implements IPlayer, IAuto {

    private final int MAX_DEPTH;
    private long expandedNodes;
    private PlayerType player;

    public Sexagono(int depth) {
        this.MAX_DEPTH = depth;
    }

    @Override
    public void timeout() {
        // Sin manejo de timeout
    }

    @Override
    public PlayerMove move(HexGameStatus s) {
        expandedNodes = 0;
        Point bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        this.player = s.getCurrentPlayer();

        // Obtener movimientos válidos (casillas libres)
        List<MoveNode> moves = s.getMoves();

        for (MoveNode move : moves) {
            
            Point currentPoint = move.getPoint();

            // Verificar si la posición está ocupada antes de colocar
            if (s.getPos(currentPoint) != 0) {
                continue; // Si la posición no está vacía, la ignoramos
            }
            
            HexGameStatus copiaTablero = new HexGameStatus(s); // Guarda el valor original
            copiaTablero.placeStone(currentPoint); // Simula el movimiento
            expandedNodes++;

            int value = minimax(copiaTablero, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            System.out.println(value);
            if (value >= bestValue) {
                bestValue = value;
                bestMove = currentPoint;
            }       
        }
        return new PlayerMove(bestMove, expandedNodes, MAX_DEPTH, SearchType.MINIMAX);
    }

    /**
     * Minimax con poda Alfa-Beta.
     */
    private int minimax(HexGameStatus s, int depth, int alpha, int beta, boolean isMaximizing) {
        // Condición de parada: juego terminado o profundidad máxima alcanzada
        if (s.isGameOver() || depth == 0) {
            return evaluateBoard(s);
        }

        // Obtener movimientos válidos
        List<MoveNode> moves = s.getMoves();

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (MoveNode move : moves) {
                Point currentPoint = move.getPoint();

                // Simular movimiento
                HexGameStatus copiaTablero = new HexGameStatus(s);
                copiaTablero.placeStone(currentPoint);
                expandedNodes++;

                // Llamada recursiva
                int eval = minimax(copiaTablero, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);

                // Poda Alfa-Beta
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (MoveNode move : moves) {
                Point currentPoint = move.getPoint();

                // Simular movimiento
                HexGameStatus copiaTablero = new HexGameStatus(s);
                copiaTablero.placeStone(currentPoint);
                expandedNodes++;

                // Llamada recursiva
                int eval = minimax(copiaTablero, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);

                // Poda Alfa-Beta
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }


    /**
     * Evalúa el tablero sin heurística, solo busca si hay ganador.
     */
    private int evaluateBoard(HexGameStatus s) {
        if (s.isGameOver()) {
            if (s.GetWinner() != player) {
                return Integer.MIN_VALUE;
            } else {
                return Integer.MAX_VALUE;
            }
        }
        return 0; // Tablero no terminado
    }

    @Override
    public String getName() {
        return "Sexagono-Minimax-AlphaBeta";
    }
}
