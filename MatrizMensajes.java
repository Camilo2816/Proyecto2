
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
    List<String> peptidos;

    public Celula(int id, int x, int y, int tipo, List<String> peptidos) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.peptidos = peptidos;
    }

    public double distancia(Celula otra) {
        return Math.sqrt(Math.pow(this.x - otra.x, 2) + Math.pow(this.y - otra.y, 2));
    }

    public static int contarPeptidosComunes(List<String> peptidosC1, List<String> peptidosC2) {
        // Convertir listas a conjuntos
        Set<String> setC1 = new HashSet<>(peptidosC1);
        Set<String> setC2 = new HashSet<>(peptidosC2);

        setC1.retainAll(setC2); 

        
        return setC1.size();
    }
}

public class MatrizMensajes {
      public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int numCasos = Integer.parseInt(scanner.nextLine());

        for (int caso = 0; caso < numCasos; caso++) {

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
                List<String> peptidos = new ArrayList<>();
                for (int j = 4; j < datosCelula.length; j++) {
                    peptidos.add(datosCelula[j]);
                }
                celulas.add(new Celula(id, x, y, tipo, peptidos));
            }

            int[][] matrizMensajes = new int[n + 2][n + 2];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        Celula c1 = celulas.get(i);
                        Celula c2 = celulas.get(j);
                        if (c1.tipo == 1) {
                            matrizMensajes[0][c1.id] = Integer.MAX_VALUE;
                        }
                        if (c1.tipo == 3) {
                            matrizMensajes[c1.id][n + 1] = Integer.MAX_VALUE;
                        }

                        if ((c1.tipo != 3 && c2.tipo != 1)) {
                            if ((c1.tipo == 1 && c2.tipo == 2) || c1.tipo == 2) {
                                double distancia = c1.distancia(c2);
                                if (distancia <= d) {
                                    List<String> peptidosC1 = c1.peptidos;
                                    List<String> peptidosC2 = c2.peptidos;
                                    int peptidosComunes = Celula.contarPeptidosComunes(peptidosC1, peptidosC2);
                                    matrizMensajes[c1.id][c2.id] = peptidosComunes;
                                }
                            }
                        }
                    }
                }
            }

            // Calcular el flujo máximo y la respuesta
            int flujoMaximo = edmondsKarp(matrizMensajes, 0, n + 1);
            int[] respuesta = menorFlujoMaximo(matrizMensajes, 0, n + 1, celulas);
            
            // Imprimir solo el resultado final para este caso
            System.out.println(respuesta[0] + " " + flujoMaximo + " " + respuesta[1]);
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

