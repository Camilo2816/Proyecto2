import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

public class Proyectov2 {
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
                HashSet<String> peptidos = new HashSet<>();
                for (int j = 4; j < datosCelula.length; j++) {
                    peptidos.add(datosCelula[j]);
                }
                celulas.add(new Celula(id, x, y, tipo, peptidos));
            }

            //int[][] matrizMensajes = new int[n + 2][n + 2];
            HashMap<Integer, List<Tupla>> mensajes = new HashMap<Integer, List<Tupla>>();
            mensajes.put(0, new ArrayList<Tupla>());
            mensajes.put(n + 1, new ArrayList<Tupla>());
    
            for (Celula c : celulas) {
                if (c.tipo == 1) {
                    //matrizMensajes[0][c.id] = Integer.MAX_VALUE;
                    mensajes.get(0).add(new Tupla(c.id, Integer.MAX_VALUE));
                    
                }
                if (c.tipo == 3) {
                    //matrizMensajes[c.id][n + 1] = Integer.MAX_VALUE;
                    mensajes.put( c.id, new ArrayList<Tupla>());
                    mensajes.get(c.id).add(new Tupla(n + 1, Integer.MAX_VALUE));
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
                            if (mensajes.containsKey(c2.id))
                            {
                                mensajes.get(c2.id).add(new Tupla(c1.id, peptidosComunes));
                            }
                            else
                            {
                                mensajes.put(c2.id, new ArrayList<Tupla>());
                                mensajes.get(c2.id).add(new Tupla(c1.id, peptidosComunes));
                            }              
                            //matrizMensajes[c2.id][c1.id] = peptidosComunes;
                                               
                        }
                    }

                    if ((c1.tipo != 3 && c2.tipo != 1) && ((c1.tipo == 1 && c2.tipo == 2) || c1.tipo == 2)) {
                        double distancia = c1.distancia(c2);
                        if (distancia <= d) {
                            HashSet<String> peptidosC1 = c1.peptidos;
                            HashSet<String> peptidosC2 = c2.peptidos;
            
                            int peptidosComunes = Celula.contarPeptidosComunes(peptidosC1, peptidosC2);
                            if(mensajes.containsKey(c1.id))
                            {
                                mensajes.get(c1.id).add(new Tupla(c2.id, peptidosComunes));
                            }
                            else
                            {
                                mensajes.put(c1.id, new ArrayList<Tupla>());
                                mensajes.get(c1.id).add(new Tupla(c2.id, peptidosComunes));
                            }
                            //matrizMensajes[c1.id][c2.id] = peptidosComunes;
            
                            if (c1.tipo == 2 && c2.tipo == 2) {
                                if(mensajes.containsKey(c2.id))
                                {
                                    mensajes.get(c2.id).add(new Tupla(c1.id, peptidosComunes));
                                }
                                else
                                {
                                    mensajes.put(c2.id, new ArrayList<Tupla>());
                                    mensajes.get(c2.id).add(new Tupla(c1.id, peptidosComunes));
                                }
                                //matrizMensajes[c2.id][c1.id] = peptidosComunes;
                            }
                        }
                    }
                }
            }

                //Imprimir Celulas relaciones
                //Imprimir Flujo Maximo
                System.err.println("FIn de la contruccion del grafo");
                long time = System.currentTimeMillis(); 
                Integer flujoOriginal=edmondsKarp(mensajes, 0, n + 1);
                int[] menorFlujo = menorFlujoMaximo(mensajes, 0, n + 1, celulas);
                System.err.println(menorFlujo[0] + " " + flujoOriginal + " " + menorFlujo[1]);
                System.err.println("Tiempo de ejecución: " + (System.currentTimeMillis() - time) + " ms");
        }



    
    }


    
    public static Map<Integer, List<Tupla>> copiarGrafo(Map<Integer, List<Tupla>> grafo) {
        Map<Integer, List<Tupla>> copia = new HashMap<>();
        
        for (Map.Entry<Integer, List<Tupla>> entry : grafo.entrySet()) {
            List<Tupla> copiaVecinos = new ArrayList<>();
            for (Tupla t : entry.getValue()) {
                // Crear una copia de cada tupla
                copiaVecinos.add(new Tupla(t.id, t.mensajes));
            }
            copia.put(entry.getKey(), copiaVecinos);
        }

        return copia;
    }



    public static boolean bfs(Map<Integer, List<Tupla>> capacidad, int fuente, int destino, Map<Integer, Integer> padre) {
        Set<Integer> visitado = new HashSet<>();
        Queue<Integer> cola = new LinkedList<>();
        cola.add(fuente);
        visitado.add(fuente);
        padre.put(fuente, -1);
    
        while (!cola.isEmpty()) {
            int u = cola.poll();
    
            // Revisar cada vecino `v` de `u`
            for (Tupla tupla : capacidad.getOrDefault(u, new ArrayList<>())) {
                int v = tupla.id;
                int capacidadResidual = tupla.mensajes;
    
                // Si `v` no ha sido visitado y hay capacidad residual, lo añadimos a la cola
                if (!visitado.contains(v) && capacidadResidual > 0) {
                    padre.put(v, u);
                    if (v == destino) {
                        return true;
                    }
                    cola.add(v);
                    visitado.add(v);
                }
            }
        }
        return false;
    }
    

    public static int edmondsKarp(Map<Integer, List<Tupla>> capacidad, int fuente, int destino) {
        int flujoMaximo = 0;
        Map<Integer, Integer> padre = new HashMap<>();
    
        // Crear una copia del grafo para no modificar el original
        Map<Integer, List<Tupla>> grafoCopia = copiarGrafo(capacidad);
    
        // Mientras haya un camino aumentante en el grafo residual
        while (bfs(grafoCopia, fuente, destino, padre)) {
            int flujoCamino = Integer.MAX_VALUE;
    
            // Encontrar el flujo mínimo a lo largo del camino aumentante
            for (int v = destino; v != fuente; v = padre.get(v)) {
                int u = padre.get(v);
                final int vFinal = v;
    
                // Buscar la tupla correspondiente en la copia del grafo
                Tupla tupla = grafoCopia.get(u).stream()
                    .filter(t -> t.id == vFinal)
                    .findFirst()
                    .orElse(null); // Si no se encuentra, retorna null
    
                if (tupla != null) {
                    flujoCamino = Math.min(flujoCamino, tupla.mensajes);
                }
            }
    
            // Actualizar las capacidades residuales en ambas direcciones del camino
            for (int v = destino; v != fuente; v = padre.get(v)) {
                int u = padre.get(v);
    
                // Reducir capacidad en la dirección `u -> v`
                for (Tupla t : grafoCopia.get(u)) {
                    if (t.id == v) {
                        t.mensajes -= flujoCamino;
                        break;
                    }
                }
    
                // Aumentar capacidad en la dirección inversa `v -> u`
                boolean tieneAristaInversa = false;
                for (Tupla t : grafoCopia.get(v)) {
                    if (t.id == u) {
                        t.mensajes += flujoCamino;
                        tieneAristaInversa = true;
                        break;
                    }
                }
                // Si no existe arista inversa, la creamos
                if (!tieneAristaInversa) {
                    grafoCopia.get(v).add(new Tupla(u, flujoCamino));
                }
            }
    
            flujoMaximo += flujoCamino;
        }
    
        return flujoMaximo;
    }

    //Eliminar aristas

    public static int edmondsKarpSN(Map<Integer, List<Tupla>> capacidad, int fuente, int destino, int nodoIgnorado) {
        int flujoMaximo = 0;
    
        // Crear una copia del grafo para la matriz residual
        Map<Integer, List<Tupla>> capacidadResidual = copiarGrafo(capacidad);
    
        Map<Integer, Integer> padre = new HashMap<>();
    
        // Mientras haya un camino aumentante en el grafo residual
        while (bfsSN(capacidadResidual, fuente, destino, padre, nodoIgnorado)) {
            int flujoCamino = Integer.MAX_VALUE;
    
            // Encontrar el flujo mínimo a lo largo del camino aumentante
            for (int v = destino; v != fuente; v = padre.get(v)) {
                int u = padre.get(v);
                int vFinal = v;
                // Buscar la tupla correspondiente en la copia del grafo
                Tupla tupla = capacidadResidual.get(u).stream()
                    .filter(t -> t.id == vFinal)
                    .findFirst()
                    .orElse(null);
    
                if (tupla != null) {
                    flujoCamino = Math.min(flujoCamino, tupla.mensajes);
                }
            }
    
            // Actualizar las capacidades residuales, excluyendo el nodo ignorado
            for (int v = destino; v != fuente; v = padre.get(v)) {
                int u = padre.get(v);
                int vFinal = v;
    
                // Excluir el nodo ignorado de la actualización de capacidades
                if (u == nodoIgnorado || v == nodoIgnorado) {
                    continue;
                }
    
                // Buscar la tupla en la dirección u -> v
                Tupla tupla = capacidadResidual.get(u).stream()
                    .filter(t -> t.id == vFinal)
                    .findFirst()
                    .orElse(null);
    
                // Reducir la capacidad en la dirección u -> v
                if (tupla != null) {
                    tupla.mensajes -= flujoCamino;
                }
    
                // Buscar o crear la tupla en la dirección inversa v -> u
                Tupla tuplaInversa = capacidadResidual.get(v).stream()
                    .filter(t -> t.id == u)
                    .findFirst()
                    .orElse(null);
    
                if (tuplaInversa != null) {
                    tuplaInversa.mensajes += flujoCamino;
                } else {
                    // Si no existe, la creamos
                    capacidadResidual.get(v).add(new Tupla(u, flujoCamino));
                }
            }
    
            flujoMaximo += flujoCamino;
        }
    
        return flujoMaximo;
    }
    
    // Función BFS modificada para excluir el nodo ignorado
    private static boolean bfsSN(Map<Integer, List<Tupla>> capacidad, int fuente, int destino, Map<Integer, Integer> padre, int nodoIgnorado) {
        Set<Integer> visitado = new HashSet<>();
        Queue<Integer> cola = new LinkedList<>();
        cola.add(fuente);
        visitado.add(fuente);
        padre.put(fuente, -1);
    
        while (!cola.isEmpty()) {
            int u = cola.poll();
    
            // Excluir el nodo ignorado
            if (u == nodoIgnorado) continue;
    
            // Explorar los vecinos
            for (Tupla tupla : capacidad.getOrDefault(u, new ArrayList<>())) {
                int v = tupla.id;
                int capacidadResidual = tupla.mensajes;
    
                // No visitar el nodo ignorado y asegurarse de que haya capacidad residual
                if (!visitado.contains(v) && capacidadResidual > 0 && v != nodoIgnorado) {
                    cola.add(v);
                    visitado.add(v);
                    padre.put(v, u);
    
                    // Si llegamos al destino, terminamos la búsqueda
                    if (v == destino) {
                        return true;
                    }
                }
            }
        }
    
        return false;  // No se encontró un camino
    }
    

    // Encontrar el nodo con el menor flujo máximo

    public static int[] menorFlujoMaximo(Map<Integer, List<Tupla>> capacidad, int fuente, int destino, List<Celula> celulas) {
        int menorFlujo = Integer.MAX_VALUE;
        int[] respuesta = new int[2];
    
        // Iterar sobre las celdas para encontrar el nodo a excluir
        for (int i = 1; i < capacidad.size() - 1; i++) {
            // Verificar si la celda debe ser ignorada según su tipo
            if (celulas.get(i - 1).tipo == 2) {
                // Ejecutar el algoritmo de Edmonds-Karp con el nodo a ignorar
                int flujo = edmondsKarpSN(capacidad, fuente, destino, i);
    
                // Si encontramos un flujo menor, actualizamos la respuesta
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

class Tupla{

    int id;
    int mensajes;

    public Tupla(int id, int mensajes) {
        this.id = id;
        this.mensajes = mensajes;
    }


}
