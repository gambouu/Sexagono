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
    private boolean timeout = false;
    private final boolean useTimeout;
    
    public Sexagono(int depth, boolean useTimeout) {
        this.MAX_DEPTH = depth;
        this.useTimeout = useTimeout;
    }

    @Override
    public void timeout() {
        System.out.println("He saltao primiko");
        timeout = !timeout;
    }

    @Override
    public PlayerMove move(HexGameStatus s) {
        expandedNodes = 0;
        int bestValue = Integer.MIN_VALUE;
        int prof = 1;
        player = s.getCurrentPlayer();
        List<MoveNode> moves = s.getMoves();
        Point bestMove = null; 
        
        if (!useTimeout) { 
            for (MoveNode move : moves) {
               
                if (bestMove == null) 
                    bestMove = moves.get(0).getPoint();
                
                HexGameStatus copiaTablero = new HexGameStatus(s); 
                copiaTablero.placeStone(move.getPoint()); 
                
                if (copiaTablero.isGameOver())
                    return new PlayerMove(move.getPoint(), expandedNodes, MAX_DEPTH, SearchType.MINIMAX);
                
                int value = minimax(copiaTablero, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                System.out.println(value);

                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move.getPoint();
                }       
            }
        }
        
        else  {
            
            while (!timeout) {      
                for (MoveNode move : moves) {
                    
                    if (bestMove == null) 
                        bestMove = moves.get(0).getPoint();
                    
                    HexGameStatus copiaTablero = new HexGameStatus(s);
                    copiaTablero.placeStone(move.getPoint());
                   
                    if (copiaTablero.isGameOver())
                        return new PlayerMove(move.getPoint(), expandedNodes, prof, SearchType.MINIMAX_IDS);
                                        
                    int value = minimax(copiaTablero, prof, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                    System.out.println(value);
              
                    if (value > bestValue) {
                        bestValue = value;
                        bestMove = move.getPoint();
                    }
                }
                prof++; 
            }         
        }
        
        timeout = false;
        return new PlayerMove(bestMove, expandedNodes, MAX_DEPTH, useTimeout ? SearchType.MINIMAX_IDS: SearchType.MINIMAX);
    }

    /**
     * Minimax con poda Alfa-Beta.
    */
    private int minimax(HexGameStatus s, int depth, int alpha, int beta, boolean isMaximizing) {
        
        expandedNodes++;

        if (timeout || depth == 0) 
            return 0;
        
        List<MoveNode> moves = s.getMoves();

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (MoveNode move : moves) {
               
                if (timeout) break;
               
                HexGameStatus copiaTablero = new HexGameStatus(s);
                copiaTablero.placeStone(move.getPoint());
                
                if (copiaTablero.isGameOver())
                    return Integer.MAX_VALUE;
                           
                maxEval = Math.max(maxEval, minimax(copiaTablero, depth - 1, alpha, beta, false));
                
                if (beta <= maxEval) return maxEval;
                alpha = Math.max(alpha, maxEval);

            }
            return maxEval;
        } 
        
        else {
            int minEval = Integer.MAX_VALUE;
            for (MoveNode move : moves) {
                
                if (timeout) break;
                
                HexGameStatus copiaTablero = new HexGameStatus(s);
                copiaTablero.placeStone(move.getPoint());
                
                if (copiaTablero.isGameOver())
                    return Integer.MIN_VALUE;
                
                minEval = Math.min(minEval, minimax(copiaTablero, depth - 1, alpha, beta, true));

                if (minEval <= alpha) return minEval;
                beta = Math.min(beta, minEval);
            }
            return minEval;
        }
    }

    @Override
    public String getName() {
        return "Sexagono";
    }
}
