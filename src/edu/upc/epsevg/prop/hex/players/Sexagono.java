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
        
        //heuristica = Conectividad + Bloquear + Centro + Flexibilidad
        
        //Conectividad: Es la ruta mas corta al destino
        int myDistance = dijkstra(s, currentPlayer);
        int opponentDistance = dijkstra(s, opponentPlayer);
        
        System.out.println(opponentDistance - myDistance);
        return (opponentDistance - myDistance);
        
        //Bloquear: Tapar al oponente para que no pueda ganar
        //Centro: Control del centro con pesos
        //Flexibilidad: Cuantas opciones de expansión tiene en el proximo turno
        //return heuristica
        
    }
                
    /*
    En el minimax se añade una pieza al tablero, en funcion de esa pieza colocada en el minimax se ejecuta el dijkstra
    SI la pieza une un bridge la puntuacion del camino baja, es decir será más facil llegar al final
    SI la pieza esta en un sitio random la puntuacion seguira siendo la misma y por tanto la puntuacion será la misma

    La funcion dijkstra siempre debe devolver el valor del camino mas pequeño

    */
    private int dijkstra(HexGameStatus s, PlayerType player) {
        
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
                    distancias[0][i] = 0;
                    pQueue.add(new Node(p, 0));
                }
                else if (s.getPos(p) == 0) {
                    distancias[0][i] = 1;
                    pQueue.add(new Node(p, 1));
                }
            }
        } 
                
        else { // De arriba a abajo
            for (int i = 0; i < s.getSize(); i++) {
                Point p = new Point(i, 0);
                if (s.getPos(p) == s.getCurrentPlayerColor()) {
                    distancias[i][0] = 0;
                    pQueue.add(new Node(p, 0));
                }
                else if (s.getPos(p) == 0) {
                    distancias[i][0] = 1;
                    pQueue.add(new Node(p, 1));
                }
            }
        }
        
        while (!pQueue.isEmpty()) {
            
            Node currentNode = pQueue.poll();
            //System.out.println(currentNode.dist);
            
            if (visitados.contains(currentNode)) 
                continue;
                   
            visitados.add(currentNode);
           
            if (player == PlayerType.PLAYER1 && currentNode.point.x == s.getSize() - 1) { 
                return currentNode.dist;
            }
            
            else if (player == PlayerType.PLAYER2 && currentNode.point.y == s.getSize() - 1) {
                return currentNode.dist;       
            }
            
            ArrayList<Point> vecinos = s.getNeigh(currentNode.point);
            ArrayList<Point> bridges = null;
            //Afegir els ponts a la llista vecinos
            addBridges(s, bridges, currentNode);
            for (Point vecino : vecinos) {
                //System.out.println(vecino);
                int vecinoCost = Integer.MAX_VALUE;
                int cellStatus = s.getPos(vecino);

                if (cellStatus == s.getCurrentPlayerColor()) 
                    vecinoCost = 0; 
             
                else if (cellStatus == 0) 
                    vecinoCost = 1;
                
                else
                    continue;

                int newCost = currentNode.dist + vecinoCost;
                if (newCost < distancias[vecino.x][vecino.y]) {
                    distancias[vecino.x][vecino.y] = newCost;
                    pQueue.add(new Node(vecino, newCost));
                }
            }
        }

        return Integer.MAX_VALUE;
        
    }
    
    private void addBridges(HexGameStatus s, ArrayList<Point> bridges, Node currentNode){
    
        // Posiciones relativas de los bridges
        // [x-2, y+1]
        // [x+2, y-1]
        // [x+1, y+1]
        // [x-1, y-1]
        // [x+1, y-2]
        // [x-1, y+2]
        int x = currentNode.getPoint().x;
        int y = currentNode.getPoint().y;
        int size = s.getSize();
        if((x > 1 && y > 1) && (x < size - 1 && y < size - 1)){
            
            
            
        }
        //CASO PARA X = 0
        else if(x == 0){
            if(y == 0){
            
                Point p1 = new Point(x+1, y+1);
                bridges.add(p1);
            
            } else if (y == 1){
            
                Point p1 = new Point(x+1, y+1);
                Point p2 = new Point(x+2, y-1);
                bridges.add(p1);
                bridges.add(p2);
                
            } else if (y == size){
            
                Point p1 = new Point(x+2, y-1);
                Point p2 = new Point(x+1, y-2);
                bridges.add(p1);
                bridges.add(p2);
            
            } else {
            
                Point p1 = new Point(x+1, y+1);
                Point p2 = new Point(x+2, y-1);
                Point p3 = new Point(x+1, y-2);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
            
            }

        }
        //CASO PARA X = 1
        else if(x == 1){
            if(y == 0){
            
                Point p1 = new Point(x+1, y+1);
                Point p2 = new Point(x-1, y+2);
                bridges.add(p1);
                bridges.add(p2);
            
            } else if (y == 1){
            
                Point p1 = new Point(x+1, y+1);
                Point p2 = new Point(x-1, y+2);
                Point p3 = new Point(x+2, y-1);
                Point p4 = new Point(x+1, y+1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);
                
            } else if (y == size){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x+1, y-2);
                Point p3 = new Point(x+2, y-1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
            
            } else if (y == size - 1){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x+1, y-2);
                Point p3 = new Point(x+2, y-1);
                Point p4 = new Point(x+1, y+1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);
            
            } else {
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x+1, y-2);
                Point p3 = new Point(x+2, y-1);
                Point p4 = new Point(x+1, y+1);
                Point p5 = new Point(x-1, y+2);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);
                bridges.add(p5);
            
            }
        }
        //CASO PARA Y = 0
        else if(y == 0){
            if(x == size){
            
                Point p1 = new Point(x-2, y+1);
                Point p2 = new Point(x-1, y+2);
                bridges.add(p1);
                bridges.add(p2);
            
            } else {
            
                Point p1 = new Point(x+1, y+1);
                Point p2 = new Point(x-1, y+2);
                Point p3 = new Point(x-2, y+1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);

            }
        }
        //CASO PARA Y = 1
        else if(y == 1){
            if(x == size){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x-1, y+2);
                Point p3 = new Point(x-2, y+1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
            
            } else if(x == size - 1){
            
                Point p1 = new Point(x+1, y+1);
                Point p2 = new Point(x-1, y+2);
                Point p3 = new Point(x-2, y+1);
                Point p4 = new Point(x-1, y-1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);

            } else {
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x-2, y+1);
                Point p3 = new Point(x+2, y-1);
                Point p4 = new Point(x+1, y+1);
                Point p5 = new Point(x-1, y+2);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);
                bridges.add(p5);
            
            }
        }
        //CASO PARA X = SIZE
        else if(x == size){
            if(y == size){
            
                Point p1 = new Point(x-1, y-1);
                bridges.add(p1);
            
            } else if (y == size - 1){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x-2, y+1);
                bridges.add(p1);
                bridges.add(p2);
                
            } else {
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x-2, y+1);
                Point p3 = new Point(x-1, y+2);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
            
            }
        }
        //CASO PARA X = SIZE - 1
        else if(x == size - 1){
            if(y == size){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x+1, y-2);
                bridges.add(p1);
                bridges.add(p2);
            
            } else if (y == size - 1){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x+1, y-2);
                Point p3 = new Point(x-2, y+1);
                Point p4 = new Point(x+1, y+1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);
                
            } else {
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x-2, y+1);
                Point p3 = new Point(x-2, y+1);
                Point p4 = new Point(x+1, y+1);
                Point p5 = new Point(x-1, y+2);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);
                bridges.add(p5);
            
            }
        }
        // CASO PARA Y = SIZE
        else if(y == size){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x+1, y-2);
                Point p3 = new Point(x+2, y-1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
            
        }
        // CASO PARA Y = SIZE - 1
        else if(y == size - 1){
            
                Point p1 = new Point(x-1, y-1);
                Point p2 = new Point(x+1, y-2);
                Point p3 = new Point(x+2, y-1);
                Point p4 = new Point(x+1, y+1);
                Point p5 = new Point(x-2, y+1);
                bridges.add(p1);
                bridges.add(p2);
                bridges.add(p3);
                bridges.add(p4);
                bridges.add(p5);
            
        }
        else {
        
            Point p1 = new Point(x-2, y+1);
            Point p2 = new Point(x+2, y-1);
            Point p3 = new Point(x+1, y+1);
            Point p4 = new Point(x-1, y-1);
            Point p5 = new Point(x+1, y-2);
            Point p6 = new Point(x-1, y+2);
            bridges.add(p1);
            bridges.add(p2);
            bridges.add(p3);
            bridges.add(p4);
            bridges.add(p5);
            bridges.add(p6);
        
        }
    }

    @Override
    public String getName() {
        return "Sexagono";
    }
    
    private class Node {
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
