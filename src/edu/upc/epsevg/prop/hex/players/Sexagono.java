package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import edu.upc.epsevg.prop.hex.PlayerType;
import edu.upc.epsevg.prop.hex.MoveNode;
import static edu.upc.epsevg.prop.hex.PlayerType.getColor;
import static edu.upc.epsevg.prop.hex.PlayerType.opposite;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

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
        otherPlayer = opposite(myPlayer);
        
        if (!useTimeout) { 
            for (MoveNode move : moves) {
                               
                HexGameStatus copiaTablero = new HexGameStatus(s); 
                copiaTablero.placeStone(move.getPoint()); 
                
                if (copiaTablero.isGameOver())
                    return new PlayerMove(move.getPoint(), expandedNodes, MAX_DEPTH, SearchType.MINIMAX);
                
                int value = minimax(copiaTablero, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                //System.out.println(value);

                if (value > bestValue || bestMove == null) {
                    bestValue = value;
                    bestMove = move.getPoint();
                }       
            }
        }
        
        else  {
            
            while (!timeout) {      
                for (MoveNode move : moves) {
                    
                    HexGameStatus copiaTablero = new HexGameStatus(s);
                    copiaTablero.placeStone(move.getPoint());
                   
                    if (copiaTablero.isGameOver())
                        return new PlayerMove(move.getPoint(), expandedNodes, prof, SearchType.MINIMAX_IDS);
                                        
                    int value = minimax(copiaTablero, prof, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
              
                    if (value > bestValue || bestMove == null) {
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

    private int minimax(HexGameStatus s, int depth, int alpha, int beta, boolean isMaximizing) {
        
        expandedNodes++;
   
        if (depth == 0 || timeout) 
            return evaluateHeuristica(s); 
            
        List<MoveNode> moves = s.getMoves();

        if (isMaximizing) {
            int mejorValor = Integer.MIN_VALUE;
            for (MoveNode move : moves) {
               
                if (timeout) break;
               
                HexGameStatus copiaTablero = new HexGameStatus(s);
                copiaTablero.placeStone(move.getPoint());
                
                if (copiaTablero.isGameOver())
                    return Integer.MAX_VALUE;
                           
                int valor = minimax(copiaTablero, depth - 1, alpha, beta, false);
                mejorValor = Math.max(mejorValor, valor);
                
                alpha = Math.max(alpha, mejorValor);
                if (alpha >= beta) break;
                    
            }
            return mejorValor;
        } 
        
        else {
            int mejorValor = Integer.MAX_VALUE;
            for (MoveNode move : moves) {
                
                if (timeout) break;
                
                HexGameStatus copiaTablero = new HexGameStatus(s);
                copiaTablero.placeStone(move.getPoint());
                
                if (copiaTablero.isGameOver())
                    return Integer.MIN_VALUE;
                
                int valor = minimax(copiaTablero, depth - 1, alpha, beta, true);
                mejorValor = Math.min(mejorValor, valor);
                
                beta = Math.min(beta, mejorValor);
                if (alpha >= beta) break;
            }
            return mejorValor;
        }
    }
    
    private int evaluateHeuristica(HexGameStatus s) {

        int myDistance = dijkstra(s, myPlayer);
        int opponentDistance = dijkstra(s, otherPlayer);

        int connectivityScore = (opponentDistance - myDistance) * 10; 
        return -myDistance + connectivityScore;


    }    

    public static int dijkstra(HexGameStatus s, PlayerType player) {
        
        int[][] distancias = new int[s.getSize()][s.getSize()];
        PriorityQueue<Node> pQueue = new PriorityQueue<>((a, b) -> Integer.compare(a.dist, b.dist));
        Node[][] visitados = new Node[s.getSize()][s.getSize()];
        
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                distancias[i][j] = Integer.MAX_VALUE;
            }
        }

        if (player == PlayerType.PLAYER1) {
            int pcolor = getColor(player);
            for (int i = 0; i < s.getSize(); i++) {
                Point p = new Point(0, i);
                if (s.getPos(p) == pcolor) {
                    distancias[0][i] = 0;
                    pQueue.add(new Node(p, 0));
                }
                else if (s.getPos(p) == 0) {
                    distancias[0][i] = 2;
                    pQueue.add(new Node(p, 2));
                }
            }
        } 
                
        else {
            int pcolor = getColor(player);
            for (int i = 0; i < s.getSize(); i++) {
                Point p = new Point(i, 0);
                if (s.getPos(p) == pcolor) {
                    distancias[i][0] = 0;
                    pQueue.add(new Node(p, 0));
                }
                else if (s.getPos(p) == 0) {
                    distancias[i][0] = 2;
                    pQueue.add(new Node(p, 2));
                }
            }
        }
        
        while (!pQueue.isEmpty()) {
            
            Node currentNode = pQueue.poll();
            
            if (visitados[currentNode.getPoint().x][currentNode.getPoint().y] != null) 
                continue;
                   
            visitados[currentNode.getPoint().x][currentNode.getPoint().y] = currentNode;
           
            if (player == PlayerType.PLAYER1 && currentNode.point.x == s.getSize() - 1) { 
                //List<Point> path = reconstruirCamino(currentNode);
                //System.out.println("Camino más corto: " + path);
                return currentNode.dist;
            }
            
            else if (player == PlayerType.PLAYER2 && currentNode.point.y == s.getSize() - 1) {
                //List<Point> path = reconstruirCamino(currentNode);
                //System.out.println("Camino más corto: " + path);
                return currentNode.dist;       
            }
            
            ArrayList<Point> vecinos = s.getNeigh(currentNode.point);
            ArrayList<Point> bridges = new ArrayList<>();
            ArrayList<Point> intermediate = new ArrayList<>();
            addBridges(s, bridges, currentNode);
            int cellStatus;
            for (Point bridge : bridges){

                cellStatus = s.getPos(bridge);
                if (cellStatus == s.getCurrentPlayerColor() && s.getPos(currentNode.getPoint()) == s.getCurrentPlayerColor()){
                    addIntermediate(s, intermediate, currentNode.getPoint() ,bridge);
                }
            }
            for (Point vecino : vecinos) {
                
                int vecinoCost = Integer.MAX_VALUE;
                cellStatus = s.getPos(vecino);
                
                if(intermediate.contains(vecino)){
                
                    vecinoCost = 1;
                
                }
                else{
                
                    if (cellStatus == s.getCurrentPlayerColor()) 
                        vecinoCost = 0; 

                    else if (cellStatus == 0) 
                        vecinoCost = 2;

                    else
                        continue;
                
                }
                

                int newCost = currentNode.dist + vecinoCost;
                if (newCost < distancias[vecino.x][vecino.y]) {
                    distancias[vecino.x][vecino.y] = newCost;
                    Node newNode = new Node(vecino, newCost);
                    newNode.parent = currentNode;
                    pQueue.add(newNode);
                } 
            }
        }
        
        return Integer.MAX_VALUE;
        
    }
    
    private static void addBridges(HexGameStatus s, ArrayList<Point> bridges, Node currentNode) {
        int x = currentNode.getPoint().x;
        int y = currentNode.getPoint().y;
        int size = s.getSize();

        // Posiciones relativas de los bridges
        // A = [x-2, y+1]
        // B = [x+2, y-1]
        // C = [x+1, y+1]
        // D = [x-1, y-1]
        // E = [x+1, y-2]
        // F = [x-1, y+2]
        
        //Posibles bloqueos
        // [x+1, y-1] --> E,B
        // [x+1, y] --> B,C
        // [x, y+1] --> C,F
        // [x-1, y+1] --> F,A
        // [x-1, y] --> A,D
        // [x, y-1] --> D,E
        // Si s.getPos(x,y) == -1; no añadir los puntos bloqueados

        // Posiciones relativas de los bridges (A, B, C, D, E, F)
        int[][] offsets = {
            {-2, 1}, {2, -1}, {1, 1}, {-1, -1}, {1, -2}, {-1, 2}
        };

        // Posibles bloqueos y sus puntos asociados
        int[][][] blocks = {
            {{1, -1}},  // E, B
            {{1, 0}},   // B, C
            {{0, 1}},   // C, F
            {{-1, 1}},  // F, A
            {{-1, 0}},  // A, D
            {{0, -1}}   // D, E
        };

        for (int i = 0; i < offsets.length; i++) {
            int newX = x + offsets[i][0];
            int newY = y + offsets[i][1];

            if (newX >= 0 && newX < size && newY >= 0 && newY < size) {
                boolean blocked = false;

                for (int[] block : blocks[i]) {
                    int blockX = x + block[0];
                    int blockY = y + block[1];

                    if (blockX >= 0 && blockX < size && blockY >= 0 && blockY < size) {
                        if (s.getPos(blockX, blockY) == -1 || s.getPos(blockX, blockY) == 1) {
                            blocked = true;
                            break; 
                        }
                    }
                }

                if (!blocked) {
                    bridges.add(new Point(newX, newY));
                }
            }
        }
    }
    
    private static void addIntermediate(HexGameStatus s, ArrayList<Point> intermediates, Point p1, Point p2) {
        // Obtener vecinos de cada punto
        ArrayList<Point> neighborsP1 = s.getNeigh(p1);
        ArrayList<Point> neighborsP2 = s.getNeigh(p2);

        // Buscar vecinos comunes
        int i = 0;
        for (Point n1 : neighborsP1) {
            if (s.getPos(n1) == -1)
                continue;
            if (i == 2) break;
            for (Point n2 : neighborsP2) {
                // Si los puntos vecinos son iguales, añádelos a la lista
                if (n1.equals(n2)) {
                    intermediates.add(n1);
                    ++i;
                }
            }
        }
    }


    private static List<Point> reconstruirCamino(Node endNode) {
        LinkedList<Point> camino = new LinkedList<>();
        Node actual = endNode;
        while (actual != null) {
            camino.addFirst(actual.point);
            actual = actual.parent;
        }
        return camino;
    }

    @Override
    public String getName() {
        return "Sexagono";
    }
    
    public static class Node {
        Point point; 
        int dist;    
        Node parent;

        public Node(Point point, int dist) {
            this.point = point;
            this.dist = dist;
            this.parent = null;
        }

        public Point getPoint() {
            return point;
        }

        public int getDist() {
            return dist;
        }
    }   
}