import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;

import javax.swing.text.ElementIterator;


public class DinicsV {
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
            List<Integer> eliminar = new ArrayList<Integer>();
            for (Celula c : celulas) {
                if (c.tipo == 1) {
                    mensajes.get(0).add(new Tupla(c.id, Integer.MAX_VALUE));
                    
                }
                if (c.tipo == 3) {
                    mensajes.put( c.id, new ArrayList<Tupla>());
                    mensajes.get(c.id).add(new Tupla(n + 1, Integer.MAX_VALUE));
                }
                if (c.tipo == 2) {
                    eliminar.add(c.id);
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
                            }
                        }
                    }
                }
            }

                //Imprimir Flujo Maximo
                long time = System.currentTimeMillis();
                Dinic dinic = new Dinic(mensajes);
                Integer flujoOriginal=  dinic.calcularFlujoMaximo(0, n + 1);
                int[] respuesta = dinic.hallarFlujoMaximoMinimo(0, n + 1, eliminar);
                System.out.println(respuesta[1] + " " + flujoOriginal + " " + respuesta[0]);
                System.err.println("Tiempo de ejecución: " + (System.currentTimeMillis() - time) + " ms");


        }



    
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

class Pair<T, U> {
    T first;
    U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
               Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
}

class Dinic {
    HashMap<Integer, List<Tupla>> mensajes;
    Map<Pair<Integer, Integer>, Integer> capacidades;
    Map<Integer, Integer> niveles;

    public Dinic(HashMap<Integer, List<Tupla>> mensajes) {
        this.mensajes = mensajes;
        this.capacidades = new HashMap<>();
        this.niveles = new HashMap<>();
        inicializarCapacidades();
    }

    private void inicializarCapacidades() {
        for (Map.Entry<Integer, List<Tupla>> entry : mensajes.entrySet()) {
            int nodoOrigen = entry.getKey();
            for (Tupla tupla : entry.getValue()) {
                int nodoDestino = tupla.id;
                int capacidad = tupla.mensajes;
                capacidades.put(new Pair<>(nodoOrigen, nodoDestino), capacidad);
                capacidades.putIfAbsent(new Pair<>(nodoDestino, nodoOrigen), 0); // Asegura reverso
            }
        }
    }

    public void inicializarCapacidadesSinNodo(int nodoIgnorado) {
        capacidades.clear();  
        for (Map.Entry<Integer, List<Tupla>> entry : mensajes.entrySet()) {
            int nodoOrigen = entry.getKey();
            if (nodoOrigen == nodoIgnorado) continue;  // Ignorar este nodo

            for (Tupla tupla : entry.getValue()) {
                int nodoDestino = tupla.id;
                if (nodoDestino == nodoIgnorado) continue;  // Ignorar las conexiones con el nodo

                int capacidad = tupla.mensajes;
                capacidades.put(new Pair<>(nodoOrigen, nodoDestino), capacidad);
                capacidades.putIfAbsent(new Pair<>(nodoDestino, nodoOrigen), 0); 
            }
        }
    }

    public boolean construirNiveles(int fuente, int sumidero) {
        niveles.clear();
        niveles.put(fuente, 0);
        Queue<Integer> cola = new LinkedList<>(List.of(fuente));

        while (!cola.isEmpty()) {
            int nodo = cola.poll();
            int nivelNodo = niveles.get(nodo);

            for (Tupla tupla : mensajes.getOrDefault(nodo, new ArrayList<>())) {
                int vecino = tupla.id;

                if (capacidades.getOrDefault(new Pair<>(nodo, vecino), 0) > 0 && !niveles.containsKey(vecino)) {
                    niveles.put(vecino, nivelNodo + 1);
                    cola.add(vecino);
                }
            }
        }
        return niveles.containsKey(sumidero);
    }

    public int enviarFlujo(int nodo, int flujo, int sumidero, Map<Integer, Integer> punteros) {
        if (nodo == sumidero) return flujo;

        List<Tupla> vecinos = mensajes.getOrDefault(nodo, new ArrayList<>());
        for (int i = punteros.getOrDefault(nodo, 0); i < vecinos.size(); i++) {
            Tupla tupla = vecinos.get(i);
            int vecino = tupla.id;

            if (niveles.getOrDefault(vecino, -1) == niveles.get(nodo) + 1 &&
                capacidades.getOrDefault(new Pair<>(nodo, vecino), 0) > 0) {

                int capacidadResidual = capacidades.get(new Pair<>(nodo, vecino));
                int flujoEnviado = enviarFlujo(vecino, Math.min(flujo, capacidadResidual), sumidero, punteros);

                if (flujoEnviado > 0) {
                    capacidades.put(new Pair<>(nodo, vecino), capacidadResidual - flujoEnviado);
                    capacidades.put(new Pair<>(vecino, nodo), capacidades.getOrDefault(new Pair<>(vecino, nodo), 0) + flujoEnviado);
                    return flujoEnviado;
                }
            }
            punteros.put(nodo, i + 1); // Mueve puntero solo si no hay flujo
        }
        return 0;
    }

    public int calcularFlujoMaximo(int fuente, int sumidero) {
        int flujoMaximo = 0;

        while (construirNiveles(fuente, sumidero)) {
            Map<Integer, Integer> punteros = new HashMap<>();

            int flujo;
            while ((flujo = enviarFlujo(fuente, Integer.MAX_VALUE, sumidero, punteros)) > 0) {
                flujoMaximo += flujo;
            }
        }
        return flujoMaximo;
    }

    public int calcularFlujoMaximoExcluyendoNodo(int fuente, int sumidero, int nodoIgnorado) {
        // Primero inicializamos las capacidades sin el nodo ignorado
        inicializarCapacidadesSinNodo(nodoIgnorado);

        // Luego calculamos el flujo máximo con las nuevas capacidades
        return calcularFlujoMaximo(fuente, sumidero);
    }

    public int[] hallarFlujoMaximoMinimo(int fuente, int sumidero, List<Integer> nodos) {
        int flujoMinimo = Integer.MAX_VALUE;
        int nodoConFlujoMinimo = -1; // Variable para almacenar el nodo con el flujo mínimo
    
        // Recorremos cada nodo para calcular el flujo máximo excluyéndolo
        for (int nodo : nodos) {
            int flujo = calcularFlujoMaximoExcluyendoNodo(fuente, sumidero, nodo);
    
            // Si encontramos un flujo menor, actualizamos la variable flujoMinimo y el nodo correspondiente
            if (flujo < flujoMinimo) {
                flujoMinimo = flujo;
                nodoConFlujoMinimo = nodo;
            }
        }
    
        // Devolvemos tanto el flujo mínimo como el nodo con ese flujo
        return new int[]{flujoMinimo, nodoConFlujoMinimo};
    }
    



    
}




