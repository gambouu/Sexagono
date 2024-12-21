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

    private int MAX_DEPTH;
    private long expandedNodes;
    private boolean timeout = false;
    private final boolean useTimeout;
    
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
        PlayerType currentPlayer = s.getCurrentPlayer();
        PlayerType opponentPlayer = (currentPlayer == PlayerType.PLAYER1)
                ? PlayerType.PLAYER2
                : PlayerType.PLAYER1;

        // Distancia mínima de cada jugador para ganar (tipo BFS/Dijkstra).
        int playerDistance = distanciaDjikstra(s, currentPlayer);
        int oppDistance = distanciaDjikstra(s, opponentPlayer);

        // Si no hay camino (bloqueado), penalizamos fuertemente.
        if (playerDistance == Integer.MAX_VALUE) playerDistance = 999999;
        if (oppDistance == Integer.MAX_VALUE)   oppDistance   = 999999;

        // Contamos puentes "favorables" para cada jugador.
        int playerBridges = countDirectionalBridges(s, currentPlayer);
        int oppBridges    = countDirectionalBridges(s, opponentPlayer);

        // Control del centro (por ejemplo, fomenta ocupar casillas centrales).
        double playerCenterControl = measureCenterControl(s, currentPlayer);
        double oppCenterControl    = measureCenterControl(s, opponentPlayer);

        // ------------------------------------------------------------------------
        // HEURÍSTICA: SUMA DE FACTORES
        // ------------------------------------------------------------------------
        double heuristicValue = 0.0;

        // 1) Diferencia de distancias: si el rival está “lejos” y yo “cerca” => mejor.
        //    (oppDistance - playerDistance): más positivo es mejor para mí.
        heuristicValue += (oppDistance - playerDistance);

        // 2) Diferencia en puentes. Si tengo muchos más puentes que el rival => mejor.
        //    Así fomentamos crear puentes y, a la vez, se penaliza que el rival tenga más.
        heuristicValue += 5.0 * (playerBridges - oppBridges);

        // 3) Control de centro. Da un “boost” a quien controle más el centro.
        heuristicValue += (playerCenterControl - oppCenterControl);

        // 4) Bonificación si estoy muy cerca de ganar (por ejemplo, BFS <= 2).
        //    Ajusta este umbral según el tamaño del tablero.
        if (playerDistance <= 2) {
            heuristicValue += 200; // Bosteamos fuerte si estamos a 1 o 2 “saltos” de la victoria
        }

        // 5) Penalización si el rival está muy cerca de ganar.
        if (oppDistance <= 2) {
            heuristicValue -= 250; // Penalización fuerte si el rival puede ganar muy pronto
        }

        // 6) Bonificación extra si tenemos un número de puentes muy alto,
        //    interpretándolo como "en cualquier momento puedo conectar".
        //    Este umbral puede depender del tamaño del tablero, por ejemplo n/2.
        //    Ajusta 'bridgeThreshold' según tu criterio.
        int bridgeThreshold = (s.getSize() / 2) + 1;
        if (playerBridges >= bridgeThreshold) {
            heuristicValue += 150;
        }

        // 7) Penalización extra si el rival tiene muchos puentes, 
        //    pues significa que, si no ponemos atención, podría cerrar la partida pronto.
        if (oppBridges >= bridgeThreshold) {
            heuristicValue -= 150;
        }

        // 8) (Opcional) Si deseas un factor adicional para “tapado” al rival,
        //    podrías estimar cuántos movimientos clave hay que colocar para
        //    interrumpir la trayectoria rival y premiar esas posiciones. Esto se hace
        //    generalmente con métodos de “análisis de cortes de red” en Hex (más avanzado).
        //    Como aproximación: penaliza fuertemente que la distancia del rival sea muy pequeña
        //    y no existan suficientes celdas vacías cerca para bloquearlo.

        // 9) Devuelve la heurística “redondeada” a int.
        return (int) heuristicValue;
    }


    /**
     * Calcula la distancia mínima tipo BFS (Dijkstra "sin pesos") 
     * desde un lado al otro para el jugador (horizontal = Player1, vertical = Player2).
     */
    private int distanciaDjikstra(HexGameStatus s, PlayerType player) {
        int n = s.getSize(); 
        int playerColor = (player == PlayerType.PLAYER1) ? 1 : 2;
        boolean horizontal = (player == PlayerType.PLAYER1);

        // 1. Generar nodos de salida y llegada
        java.util.ArrayList<Point> startNodes = new java.util.ArrayList<>();
        java.util.ArrayList<Point> endNodes   = new java.util.ArrayList<>();

        if (horizontal) {
            // Salida: columna 0
            for (int row = 0; row < n; row++) {
                Point p = new Point(0, row);
                if (!isBlocked(s, p, playerColor)) {
                    startNodes.add(p);
                }
            }
            // Llegada: columna n-1
            for (int row = 0; row < n; row++) {
                Point p = new Point(n - 1, row);
                if (!isBlocked(s, p, playerColor)) {
                    endNodes.add(p);
                }
            }
        } else {
            // Salida: fila 0
            for (int col = 0; col < n; col++) {
                Point p = new Point(col, 0);
                if (!isBlocked(s, p, playerColor)) {
                    startNodes.add(p);
                }
            }
            // Llegada: fila n-1
            for (int col = 0; col < n; col++) {
                Point p = new Point(col, n - 1);
                if (!isBlocked(s, p, playerColor)) {
                    endNodes.add(p);
                }
            }
        }

        if (startNodes.isEmpty() || endNodes.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        // 2. Matriz de distancias
        int[][] dist = new int[n][n];
        for (int[] row : dist) {
            java.util.Arrays.fill(row, Integer.MAX_VALUE);
        }

        // 3. Cola BFS
        java.util.Queue<Point> queue = new java.util.LinkedList<>();

        // Inicializamos distancias de startNodes a 0
        for (Point start : startNodes) {
            dist[start.y][start.x] = 0;
            queue.offer(start);
        }

        // Bucle BFS
        while (!queue.isEmpty()) {
            Point u = queue.poll();
            int ux = u.x;
            int uy = u.y;
            int currentDist = dist[uy][ux];

            // Si llegamos a un endNode devolvemos la distancia
            if (endNodes.contains(u)) {
                return currentDist;
            }

            // Examina vecinos
            for (Point v : s.getNeigh(u)) {
                int vx = v.x;
                int vy = v.y;
                if (!isBlocked(s, v, playerColor) && dist[vy][vx] == Integer.MAX_VALUE) {
                    dist[vy][vx] = currentDist + 1;
                    queue.offer(v);
                }
            }
        }
        return Integer.MAX_VALUE;
    }
    
    /**
     * Cuenta la cantidad de "puentes" que el jugador puede formar,
     * premiando solo los que van en la dirección correcta de victoria.
     */
    private int countDirectionalBridges(HexGameStatus s, PlayerType player) {
        int n = s.getSize();
        int color = (player == PlayerType.PLAYER1) ? 1 : 2;
        boolean isHorizontal = (player == PlayerType.PLAYER1); 
        int bridgeCount = 0;

        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (s.getPos(x, y) == 0) {
                    Point p = new Point(x, y);

                    // Contar vecinos del mismo color
                    int numMyNeighbors = 0;
                    boolean hasAlternative = false;

                    // Para almacenar las diferencias de posición de los vecinos del mismo color
                    java.util.ArrayList<Point> myNeighborsDiff = new java.util.ArrayList<>();

                    for (Point nb : s.getNeigh(p)) {
                        int nbColor = s.getPos(nb);
                        if (nbColor == color) {
                            numMyNeighbors++;
                            // Guardar la diferencia de posición
                            myNeighborsDiff.add(new Point(nb.x - p.x, nb.y - p.y));
                        }
                        // Si el vecino es vacio o del mismo color, hay posibilidad de extender
                        if (nbColor == 0 || nbColor == color) {
                            hasAlternative = true;
                        }
                    }

                    // Si tenemos al menos 2 vecinos del color
                    if (numMyNeighbors >= 2 && hasAlternative) {
                        // Calculamos peso de puente basado en su dirección
                        double directionWeight = calculateDirectionWeight(myNeighborsDiff, isHorizontal);
                        if (directionWeight > 0.0) {
                            bridgeCount += (int)(directionWeight);
                        }
                    }

                }
            }
        }
        return bridgeCount;
    }


    /**
     * Devuelve un valor > 0 si existe al menos un par de vecinos del mismo color
     * que estén en lados opuestos en la dirección de victoria (horizontal o vertical).
    */
    private double calculateDirectionWeight(List<Point> neighborsDiff, boolean isHorizontal) {
        double weight = 0.0;

        for (int i = 0; i < neighborsDiff.size(); i++) {
            for (int j = i + 1; j < neighborsDiff.size(); j++) {
                Point diff1 = neighborsDiff.get(i);
                Point diff2 = neighborsDiff.get(j);

                if (isHorizontal) {
                    // Queremos puentes "izquierda-derecha"
                    // Chequeamos que los x-diff tengan signos opuestos
                    boolean oppositeSidesInX = (diff1.x < 0 && diff2.x > 0) || (diff1.x > 0 && diff2.x < 0);

                    // Además, que estén “más” en horizontal que en vertical
                    boolean mostlyHorizontal1 = (Math.abs(diff1.x) >= Math.abs(diff1.y));
                    boolean mostlyHorizontal2 = (Math.abs(diff2.x) >= Math.abs(diff2.y));

                    if (oppositeSidesInX && mostlyHorizontal1 && mostlyHorizontal2) {
                        // Hemos encontrado un par que “cruza” horizontalmente la celda.
                        weight = 2.0; // o el valor que tú quieras
                        return weight;
                    }

                } else {
                    // Queremos puentes "arriba-abajo"
                    boolean oppositeSidesInY = (diff1.y < 0 && diff2.y > 0) || (diff1.y > 0 && diff2.y < 0);

                    boolean mostlyVertical1 = (Math.abs(diff1.y) >= Math.abs(diff1.x));
                    boolean mostlyVertical2 = (Math.abs(diff2.y) >= Math.abs(diff2.x));

                    if (oppositeSidesInY && mostlyVertical1 && mostlyVertical2) {
                        weight = 2.0; 
                        return weight;
                    }
                }
            }
        }

        return weight; // Si no se encontró ningún par que cumpla, retorna 0.
    }


    /**
     * Mide cuánto "control" tiene un jugador en el centro del tablero.
     * Suma 1/(1+dist) por cada celda ocupada por el jugador, donde dist
     * es la distancia Euclídea al centro.
     */
    private double measureCenterControl(HexGameStatus s, PlayerType player) {
        int n = s.getSize();
        double cx = (n - 1) / 2.0;
        double cy = (n - 1) / 2.0;
        int color = (player == PlayerType.PLAYER1) ? 1 : 2;

        double sum = 0.0;
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (s.getPos(x, y) == color) {
                    double dx = x - cx;
                    double dy = y - cy;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    // Más cerca del centro => mayor aportación
                    sum += 1.0 / (1.0 + dist);
                }
            }
        }
        return sum;
    }

    /**
     * Devuelve true si la casilla 'p' está bloqueada para 'playerColor'.
     */
    private boolean isBlocked(HexGameStatus s, Point p, int playerColor) {
        int cellColor = s.getPos(p);
        // Vacía (0) o misma ficha => no está bloqueada.
        if (cellColor == 0 || cellColor == playerColor) {
            return false;
        }
        // Cualquier otro color (oponente) bloquea.
        return true;
    }
    
    @Override
    public String getName() {
        return "Sexagono";
    }

}
