package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import edu.upc.epsevg.prop.hex.PlayerType;
import edu.upc.epsevg.prop.hex.MoveNode;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 * Jugador de Hex con Minimax y Poda Alfa-Beta sin heurística,
 * explorando completamente hasta la profundidad máxima definida.
 */
public class Sexagono implements IPlayer, IAuto {

    private int MAX_DEPTH;
    private long expandedNodes;
    private boolean timeout = false;
    private final boolean useTimeout;
    private PlayerType myPlayer;
    private PlayerType otherPlayer;
    
    public Sexagono(int depth, boolean useTimeout) {
        this.MAX_DEPTH = depth;
        this.useTimeout = useTimeout;
    }

    @Override
    public void timeout() {
        if(useTimeout) timeout = !timeout;
    }

    @Override
    public PlayerMove move(HexGameStatus s) {
        expandedNodes = 0;
        int bestValue = Integer.MIN_VALUE;
        int prof = 1;
        List<MoveNode> moves = s.getMoves();
        Point bestMove = null; 
        
                
        myPlayer = s.getCurrentPlayer();
        if(myPlayer == PlayerType.PLAYER1) otherPlayer = PlayerType.PLAYER2;
        else otherPlayer = PlayerType.PLAYER1;
        
        if (!useTimeout) { 
            for (MoveNode move : moves) {
               
                if (bestMove == null) 
                    bestMove = moves.get(0).getPoint();
                
                HexGameStatus copiaTablero = new HexGameStatus(s); 
                copiaTablero.placeStone(move.getPoint()); 
                
                if (copiaTablero.isGameOver())
                    return new PlayerMove(move.getPoint(), expandedNodes, MAX_DEPTH, SearchType.MINIMAX);
                
                int value = minimax(copiaTablero, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
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
            MAX_DEPTH = prof;
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
            return evaluateHeuristica(s);
        
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
    
    private int evaluateHeuristica(HexGameStatus s) {
        // Jugador al que le toca mover en este estado

        PlayerType currentPlayer = s.getCurrentPlayer();
        PlayerType opponentPlayer;
        if(myPlayer == PlayerType.PLAYER1) opponentPlayer = PlayerType.PLAYER2;
        else opponentPlayer = PlayerType.PLAYER1;
        
        int myDistance = dijkstra(s, currentPlayer);
        int opponentDistance = dijkstra(s, opponentPlayer);
        
        
        return 0;
        
    }
    
    private int dijkstra(HexGameStatus s, PlayerType player){
    
        List<MoveNode> posiblesMoves = s.getMoves();
        ArrayList<Point> vecinos;
        PriorityQueue<MoveNode> prio;
        int puntuación_camino = 0;
        
        if(player == PlayerType.PLAYER1){
        //Va de derecha a izquierda, te da igual cual sea el primer nodo y cual sea el ultimo
        //Es decir primer y ultimo nodo simepre puedes coger el que mas te beneficie;
            
        /*
            ArrayList<MoveNode> inicio = null;
            // Este for es para tratar todos los nodos del principio como uno solo, si encuentra alguno empieza por ahí
            // Si no empieza por el primero disponible
            for(int i = 0; i < s.getSize(); i++){
                Point p = new Point(0, i);
                if(s.getPos(p) == s.getCurrentPlayerColor())
                    inicio.add(p);
            }
            if(inicio == null){
                inicio.add(posiblesMoves.get(0));
            }
        */        
        
            for(MoveNode move : posiblesMoves){

                vecinos = s.getNeigh(move.getPoint());

                for(Point vecino : vecinos){
                    if(s.getPos(vecino) == s.getCurrentPlayerColor()){

                        

                    }

                }

            }
        
        }
        else {
        //Va de arriba a abajo, te da igual cual sea el primer nodo y cual sea el ultimo
        //Es decir primer y ultimo nodo simepre puedes coger el que mas te beneficie;

        
        
        }
        
        
        return 0;
        
        // Si celda ocupada por el jugador acutal puntuacion + 0 camino ya es tuyo
        // Si celda vacia puntuacion + 1
        // Si celda ocupada por el rival puntuacion + infinito, ya que por ahí no se puede pasar
        
        /*
        
        En el minimax se añade una pieza al tablero, en funcion de esa pieza colocada en el minimax se ejecuta el dijkstra
        SI la pieza une un bridge la puntuacion del camino baja, es decir será más facil llegar al final
        SI la pieza esta en un sitio random la puntuacion seguira siendo la misma y por tanto la puntuacion será la misma
        
        La funcion dijkstra siempre debe devolver el valor del camino mas pequeño
        
        
        */
        
        
        
    }

    @Override
    public String getName() {
        return "Sexagono";
    }

}
