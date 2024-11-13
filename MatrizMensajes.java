
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

class Celula {
    int id;
    int x;
    int y;
    int tipo;
    HashSet<String> peptidos;

    public Celula(int id, int x, int y, int tipo, HashSet<String> peptidos) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.peptidos = peptidos;
    }

    public double distancia(Celula otra) {
        return Math.sqrt(Math.pow(this.x - otra.x, 2) + Math.pow(this.y - otra.y, 2));
    }

    public static int contarPeptidosComunes(HashSet<String> peptidosC1, HashSet<String> peptidosC2) {
        // Aseguramos que siempre iteremos sobre el conjunto más pequeño
        if (peptidosC1.size() > peptidosC2.size()) {
            return contarInterseccion(peptidosC2, peptidosC1);
        } else {
            return contarInterseccion(peptidosC1, peptidosC2);
        }
    }
    
    private static int contarInterseccion(HashSet<String> pequeño, HashSet<String> grande) {
        int contador = 0;
        for (String peptido : pequeño) {
            if (grande.contains(peptido)) {
                contador++;
            }
        }
        return contador;
    }
    
}

public class MatrizMensajes {
      public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int numCasos = Integer.parseInt(scanner.nextLine());

        for (int caso = 0; caso < numCasos; caso++) {
             // Inicializar el tiempo de ejecución

            String[] primeraLinea = scanner.nextLine().split(" ");
            int n = Integer.parseInt(primeraLinea[0]);
            int d = Integer.parseInt(primeraLinea[1]);

            List<Celula> celulas = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String[] datosCelula = scanner.nextLine().split(" ");
                int id = Integer.parseInt(datosCelula[0]);
                int x = Integer.parseInt(datosCelula[1]);
                int y = Integer.parseInt(datosCelula[2]);
                int tipo = Integer.parseInt(datosCelula[3]);
                HashSet<String> peptidos = new HashSet<>();
                for (int j = 4; j < datosCelula.length; j++) {
                    peptidos.add(datosCelula[j]);
                }
                celulas.add(new Celula(id, x, y, tipo, peptidos));
            }
            int[][] matrizMensajes = new int[n + 2][n + 2];

            // Configuramos los valores extremos de las células de tipo iniciadora y ejecutora una vez
            for (Celula c : celulas) {
                if (c.tipo == 1) {
                    matrizMensajes[0][c.id] = Integer.MAX_VALUE;
                }
                if (c.tipo == 3) {
                    matrizMensajes[c.id][n + 1] = Integer.MAX_VALUE;
                }
            }
            
            // Evitamos duplicados iterando sobre combinaciones únicas (j > i)
            for (int i = 0; i < n; i++) {
                Celula c1 = celulas.get(i);
                for (int j = i + 1; j < n; j++) {
                    Celula c2 = celulas.get(j);
                    if((c1.tipo == 3 && c2.tipo == 2) || (c1.tipo == 2 && c2.tipo == 1) ) {
                        double distancia = c1.distancia(c2);
                        if (distancia <= d) {
                            HashSet<String> peptidosC1 = c1.peptidos;
                            HashSet<String> peptidosC2 = c2.peptidos;
            
                            int peptidosComunes = Celula.contarPeptidosComunes(peptidosC1, peptidosC2);                
                            matrizMensajes[c2.id][c1.id] = peptidosComunes;                    
                        }
                    }

                    if ((c1.tipo != 3 && c2.tipo != 1) && ((c1.tipo == 1 && c2.tipo == 2) || c1.tipo == 2)) {
                        double distancia = c1.distancia(c2);
                        if (distancia <= d) {
                            HashSet<String> peptidosC1 = c1.peptidos;
                            HashSet<String> peptidosC2 = c2.peptidos;
            
                            int peptidosComunes = Celula.contarPeptidosComunes(peptidosC1, peptidosC2);
                            
                            matrizMensajes[c1.id][c2.id] = peptidosComunes;
            
                            if (c1.tipo == 2 && c2.tipo == 2) {
                                matrizMensajes[c2.id][c1.id] = peptidosComunes;
                            }
                        }
                    }
                }
            }
                
            System.out.println("Fin de la creación de la matriz de mensajes");
            // Calcular el flujo máximo y la respuesta
            long time = System.currentTimeMillis();
            int flujoMaximo = edmondsKarp(matrizMensajes, 0, n + 1);
            long timeEjecucion = System.currentTimeMillis() - time;
            int[] respuesta = menorFlujoMaximo(matrizMensajes, 0, n + 1, celulas);
            
            // Imprimir solo el resultado final para este caso
            System.out.println(respuesta[0] + " " + flujoMaximo + " " + respuesta[1]);
            System.out.println("Tiempo de ejecución: " + timeEjecucion + " ms");
        }

        scanner.close();
    }


    public static boolean bfs(int[][] capacidad, int fuente, int destino, int[] padre) {
        boolean[] visitado = new boolean[capacidad.length];
        Queue<Integer> cola = new LinkedList<>();
        cola.add(fuente);
        visitado[fuente] = true;
        padre[fuente] = -1;

        while (!cola.isEmpty()) {
            int u = cola.poll();

            for (int v = 0; v < capacidad.length; v++) {
                if (!visitado[v] && capacidad[u][v] > 0) {
                    padre[v] = u;
                    if (v == destino) {
                        return true;
                    }
                    cola.add(v);
                    visitado[v] = true;
                }
            }
        }
        return false;
    }

    public static int edmondsKarp(int[][] capacidad, int fuente, int destino) {
        int flujoMaximo = 0;
        int n = capacidad.length;
    
        // Crear una copia de la matriz de capacidad para la matriz residual
        int[][] capacidadResidual = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                capacidadResidual[i][j] = capacidad[i][j];
            }
        }
    
        int[] padre = new int[n];
    
        // Mientras haya un camino aumentante en el grafo residual
        while (bfs(capacidadResidual, fuente, destino, padre)) {
            int flujoCamino = Integer.MAX_VALUE;
    
            // Encontrar el flujo mínimo a lo largo del camino aumentante
            for (int v = destino; v != fuente; v = padre[v]) {
                int u = padre[v];
                flujoCamino = Math.min(flujoCamino, capacidadResidual[u][v]);
            }
    
            // Actualizar las capacidades residuales
            for (int v = destino; v != fuente; v = padre[v]) {
                int u = padre[v];
                capacidadResidual[u][v] -= flujoCamino;
                capacidadResidual[v][u] += flujoCamino;
            }
    
            flujoMaximo += flujoCamino;
        }
    
        return flujoMaximo;
    }


    // Edmonds-Karp algorithm

    public static int edmondsKarpSN(int[][] capacidad, int fuente, int destino, int nodoIgnorado) {
        int flujoMaximo = 0;
        int n = capacidad.length;
    
        // Crear una copia de la matriz de capacidad para la matriz residual
        int[][] capacidadResidual = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                capacidadResidual[i][j] = capacidad[i][j];
            }
        }
    
        int[] padre = new int[n];
    
        // Mientras haya un camino aumentante en el grafo residual
        while (bfsSN(capacidadResidual, fuente, destino, padre, nodoIgnorado)) {
            int flujoCamino = Integer.MAX_VALUE;
            
            // Encontrar el flujo mínimo a lo largo del camino aumentante
            for (int v = destino; v != fuente; v = padre[v]) {
                int u = padre[v];
                flujoCamino = Math.min(flujoCamino, capacidadResidual[u][v]);
            }
    
            // Actualizar las capacidades residuales, excluyendo el nodo ignorado
            for (int v = destino; v != fuente; v = padre[v]) {
                int u = padre[v];
    
                // Excluir el nodo ignorado de la actualización de capacidades
                if (u == nodoIgnorado || v == nodoIgnorado) {
                    continue;
                }
    
                capacidadResidual[u][v] -= flujoCamino;
                capacidadResidual[v][u] += flujoCamino;
            }
    
            flujoMaximo += flujoCamino;
        }
    
        return flujoMaximo;
    }
    
    // Función BFS modificada para excluir el nodo ignorado
    private static boolean bfsSN(int[][] capacidad, int fuente, int destino, int[] padre, int nodoIgnorado) {
        int n = capacidad.length;
        boolean[] visitado = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(fuente);
        visitado[fuente] = true;
        padre[fuente] = -1;
    
        while (!queue.isEmpty()) {
            int u = queue.poll();
    
            // Excluir el nodo ignorado
            if (u == nodoIgnorado) continue;
    
            // Explorar los vecinos
            for (int v = 0; v < n; v++) {
                // No visitar el nodo ignorado y asegurarse de que haya capacidad residual
                if (!visitado[v] && capacidad[u][v] > 0 && v != nodoIgnorado) {
                    queue.offer(v);
                    visitado[v] = true;
                    padre[v] = u;
    
                    // Si llegamos al destino, terminamos la búsqueda
                    if (v == destino) {
                        return true;
                    }
                }
            }
        }
    
        return false;  // No se encontró un camino
    }

    public static int[] menorFlujoMaximo(int[][] capacidad, int fuente, int destino, List<Celula> celulas) {
        int menorFlujo = Integer.MAX_VALUE;
        int [] respuesta = new int[2];
        for (int i = 1; i < capacidad.length - 1; i++) {
            if (celulas.get(i-1).tipo == 2) {
                int flujo = edmondsKarpSN(capacidad, fuente, destino, i);
                if (flujo < menorFlujo) {
                    menorFlujo = flujo;
                    respuesta[0] = i;
                    respuesta[1] = menorFlujo;
                }
            }
        }
        return respuesta;
    }
    

    
}

