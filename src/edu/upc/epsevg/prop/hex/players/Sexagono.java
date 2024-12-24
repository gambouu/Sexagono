package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import edu.upc.epsevg.prop.hex.PlayerType;
import edu.upc.epsevg.prop.hex.MoveNode;
import static edu.upc.epsevg.prop.hex.PlayerType.getColor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
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
        if (myPlayer == PlayerType.PLAYER1) otherPlayer = PlayerType.PLAYER2;
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
                //System.out.println(value);

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
                    //System.out.println(value);
              
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
                
    /*
    En el minimax se añade una pieza al tablero, en funcion de esa pieza colocada en el minimax se ejecuta el dijkstra
    SI la pieza une un bridge la puntuacion del camino baja, es decir será más facil llegar al final
    SI la pieza esta en un sitio random la puntuacion seguira siendo la misma y por tanto la puntuacion será la misma

    La funcion dijkstra siempre debe devolver el valor del camino mas pequeño

    */
    public static int dijkstra(HexGameStatus s, PlayerType player) {
        
        int[][] distancias = new int[s.getSize()][s.getSize()];
        PriorityQueue<Node> pQueue = new PriorityQueue<>((a, b) -> Integer.compare(a.dist, b.dist));
        Set<Node> visitados = new HashSet<>();
        
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                distancias[i][j] = Integer.MAX_VALUE; // Inicializar con valor infinito
            }
        }

        if (player == PlayerType.PLAYER1) { // De izquierda a derecha
            int pcolor = getColor(player);
            for (int i = 0; i < s.getSize(); i++) {
                Point p = new Point(0, i);
                if (s.getPos(p) == pcolor) {
                    System.out.println("PIEZA MIA");
                    distancias[0][i] = 0;
                    pQueue.add(new Node(p, 0));
                }
                else if (s.getPos(p) == -1) {
                    System.out.println("PIEZA VACIA");
                    distancias[0][i] = 1;
                    pQueue.add(new Node(p, 1));
                }
            }
        } 
                
        else { // De arriba a abajo
            for (int i = 0; i < s.getSize(); i++) {
                Point p = new Point(i, 0);
                if (s.getPos(p) == s.getCurrentPlayerColor() || s.getPos(p) == -1) {
                    distancias[i][0] = 0;
                    pQueue.add(new Node(p, 0));
                }
                else if (s.getPos(p) == -1) {
                    distancias[i][0] = 1;
                    pQueue.add(new Node(p, 1));
                }
            }
        }
        
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                System.out.println(distancias[i][j]);
            }
        }

        while (!pQueue.isEmpty()) {
            
            Node currentNode = pQueue.poll();
            
            if (visitados.contains(currentNode)) 
                continue;
                   
            visitados.add(currentNode);
           
            if (player == PlayerType.PLAYER1 && currentNode.point.x == s.getSize() - 1) { 
                System.out.println(currentNode.dist);
                return currentNode.dist;
            }
            
            else if (player == PlayerType.PLAYER2 && currentNode.point.y == s.getSize() - 1) {
                    System.out.println(currentNode.dist);
                    return currentNode.dist;       
            }
            
            ArrayList<Point> vecinos = s.getNeigh(currentNode.point);
            for (Point vecino : vecinos) {
                System.out.println(vecino);
                int vecinoCost = Integer.MAX_VALUE;
                int cellStatus = s.getPos(vecino);

                if (cellStatus == s.getCurrentPlayerColor()) 
                    vecinoCost = 0; 
             
                else if (cellStatus == -1) 
                    vecinoCost = 1;

                int newCost = currentNode.dist + vecinoCost;
                if (newCost < distancias[vecino.x][vecino.y]) {
                    distancias[vecino.x][vecino.y] = newCost;
                    pQueue.add(new Node(vecino, newCost));
                }
            }
        }

        return Integer.MAX_VALUE;
        
    }

    @Override
    public String getName() {
        return "Sexagono";
    }
    
    public static class Node {
        Point point; 
        int dist;    

        public Node(Point point, int dist) {
            this.point = point;
            this.dist = dist;
        }

        public Point getPoint() {
            return point;
        }

        public int getDist() {
            return dist;
        }
    }
    
}
