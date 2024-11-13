from collections import deque, defaultdict
import math

class Graph:
    def __init__(self, vertices):
        """
        Inicializa una instancia del algoritmo Push-Relabel para un grafo con un número dado de vértices.

        Args:
            vertices (int): El número de vértices en el grafo.

        Atributos:
            V (int): El número de vértices en el grafo.
            capacity (list): Matriz de capacidad del grafo, inicializada con ceros.
            flow (list): Matriz de flujo del grafo, inicializada con ceros.
            excess (list): Lista que almacena el exceso de flujo en cada vértice, inicializada con ceros.
            height (list): Lista que almacena la altura de cada vértice, inicializada con ceros.
            height_count (list): Lista que cuenta el número de vértices a cada altura, inicializada con ceros.
            active_nodes_by_height (defaultdict): Diccionario que almacena deques de nodos activos por altura.
            scaled_capacity (int): Capacidad escalada, inicializada en 1.
        """
        self.V = vertices
        self.capacity = [[0] * vertices for _ in range(vertices)]
        self.flow = [[0] * vertices for _ in range(vertices)]
        self.excess = [0] * vertices
        self.height = [0] * vertices
        self.height_count = [0] * (2 * vertices)
        self.active_nodes_by_height = defaultdict(deque)
        self.scaled_capacity = 1

    def add_edge(self, u, v, capacity):
        """
        Agrega una arista al grafo con una capacidad específica.

        Args:
            u (int): El nodo de origen de la arista.
            v (int): El nodo de destino de la arista.
            capacity (int): La capacidad de la arista entre los nodos u y v.

        """
        self.capacity[u][v] = capacity

    def traducir_a_grafo_push_relabel(grafo_flujo):
        """
        Traduce un grafo de flujo a un grafo compatible con el algoritmo Push-Relabel.
        Args:
            grafo_flujo (GrafoFlujo): Un objeto que representa un grafo de flujo, 
                                      que contiene neuronas y aristas con capacidades.
        Returns:
            Graph: Una instancia de la clase Graph que representa el grafo traducido 
                   para ser utilizado con el algoritmo Push-Relabel.
        El método realiza los siguientes pasos:
        1. Encuentra el número máximo de identificadores únicos de neuronas en el grafo de flujo.
        2. Crea una instancia de la clase Graph con el número de vértices basado en el identificador máximo.
        3. Traduce las aristas y sus capacidades del grafo de flujo al nuevo grafo de Push-Relabel.
        4. Devuelve el grafo traducido.
        """
        # Encuentra el número de vértices (identificadores únicos de neuronas)
        max_id = max(neurona.id for neurona in grafo_flujo.neuronas)
        graph_push_relabel = Graph(max_id + 1)  # Crear instancia de Graph con número de vértices

        # Traducir aristas y capacidades
        for origen, destinos in grafo_flujo.adyacencias.items():
            for destino in destinos:
                # Obtener la capacidad (peso) de la arista del grafo de flujo
                capacidad = grafo_flujo.pesos[(origen, destino)]
                # Agregar arista en el grafo de Push-Relabel
                graph_push_relabel.add_edge(origen, destino, capacidad)

        return graph_push_relabel

def initialize_preflow(graph, source):
    """
    Inicializa el preflujo empujando el flujo máximo posible desde la fuente.

    Args:
        graph (Graph): El grafo en el que se realizará el algoritmo de Push-Relabel.
        source (int): El nodo fuente desde el cual se inicia el preflujo.

    Esta función realiza las siguientes acciones:
    1. Establece la altura del nodo fuente al número total de nodos en el grafo.
    2. Inicializa el contador de alturas con la altura del nodo fuente.
    3. Para cada nodo en el grafo, si hay capacidad desde la fuente hacia ese nodo:
       - Actualiza la capacidad máxima.
       - Empuja el flujo máximo posible desde la fuente hacia ese nodo.
       - Ajusta el flujo de retorno desde el nodo hacia la fuente.
       - Actualiza el exceso de flujo en el nodo destino y en la fuente.
       - Si el nodo destino no es la fuente y tiene exceso de flujo, lo añade a la lista de nodos activos por altura.
    4. Establece la capacidad escalada al valor de la capacidad máxima encontrada.
    """
    graph.height[source] = graph.V
    graph.height_count[graph.V] = 1
    max_capacity = 0
    for v in range(graph.V):
        if graph.capacity[source][v] > 0:
            max_capacity = max(max_capacity, graph.capacity[source][v])
            graph.flow[source][v] = graph.capacity[source][v]
            graph.flow[v][source] = -graph.flow[source][v]
            graph.excess[v] += graph.flow[source][v]
            graph.excess[source] -= graph.flow[source][v]
            if v != source and graph.excess[v] > 0:
                graph.active_nodes_by_height[graph.height[v]].append(v)
    graph.scaled_capacity = max_capacity

def push(graph, u, v):
    """
    Empuja flujo desde el nodo u al nodo v.

    Args:
        graph (Graph): El grafo que contiene la red de flujo.
        u (int): El nodo desde el cual se empuja el flujo.
        v (int): El nodo al cual se empuja el flujo.

    Descripción:
        Esta función empuja el flujo desde el nodo u al nodo v en la red de flujo.
        Calcula el flujo máximo que se puede empujar (delta) basado en el exceso
        de flujo en u y la capacidad residual entre u y v. Luego, actualiza el flujo
        en ambas direcciones (u a v y v a u), ajusta los excesos de flujo en u y v,
        y si v se vuelve activo (tiene exceso de flujo), lo añade a la lista de nodos
        activos por altura si no está ya presente.
    """
    delta = min(graph.excess[u], graph.capacity[u][v] - graph.flow[u][v])
    graph.flow[u][v] += delta
    graph.flow[v][u] -= delta
    graph.excess[u] -= delta
    graph.excess[v] += delta
    if graph.excess[v] == delta and v != 0 and v != graph.V - 1:
        if v not in graph.active_nodes_by_height[graph.height[v]]:
            graph.active_nodes_by_height[graph.height[v]].append(v)

def relabel(graph, u):
    """
    Relabela el nodo u para permitir más empujes.

    Este método ajusta la altura del nodo u en el grafo para asegurar que
    se puedan realizar más operaciones de empuje en el algoritmo Push-Relabel.
    La nueva altura se establece en función de la altura mínima de los nodos
    adyacentes que aún tienen capacidad residual.

    Args:
        graph (Graph): El grafo que contiene la capacidad, flujo y altura de los nodos.
        u (int): El índice del nodo que se va a relabelar.

    Returns:
        None
    """
    min_height = float('inf')
    for v in range(graph.V):
        if graph.capacity[u][v] - graph.flow[u][v] > 0:
            min_height = min(min_height, graph.height[v])
    old_height = graph.height[u]
    if min_height < float('inf'):
        graph.height[u] = min_height + 1
        graph.height_count[old_height] -= 1
        graph.height_count[graph.height[u]] += 1

def gap_heuristic(graph, height):
    """
    Aplica gap_heuristic en el algoritmo Push-Relabel para flujos máximos con el fin de mejorar la eficiencia del algoritmo al identificar y manejar alturas que no son útiles.
    Si se encuentra un nodo con una altura mayor o igual a la altura dada, se incrementa su altura a un valor
    mayor que el número de nodos en el grafo, lo que efectivamente lo elimina de futuras consideraciones.
    Args:
        graph (Graph): El grafo sobre el cual se está ejecutando el algoritmo Push-Relabel.
        height (int): La altura a partir de la cual se aplicará la gap_heuristic.
    Modifica:
        graph.height (list): La lista de alturas de los nodos en el grafo.
        graph.height_count (list): La lista que cuenta el número de nodos en cada altura.
    """
    
    for u in range(graph.V):
        if graph.height[u] >= height:
            old_height = graph.height[u]
            graph.height_count[old_height] -= 1
            graph.height[u] = max(graph.height[u], graph.V + 1)
            graph.height_count[graph.height[u]] += 1

def discharge(graph, u):
    """
    Realiza la operación de descarga en el nodo 'u' del grafo.
    La función intenta empujar el exceso de flujo desde el nodo 'u' a sus vecinos.
    Si no es posible empujar el flujo, se incrementa la altura del nodo 'u' (relabel).
    Si después de relabel no quedan nodos en la altura antigua, se aplica la gap_heuristic.
    Args:
        graph (Graph): El grafo en el cual se realiza la operación de descarga.
        u (int): El nodo desde el cual se intenta descargar el exceso de flujo.
    Returns:
        None
    """
    
    while graph.excess[u] > 0:
        pushed = False
        for v in range(graph.V):
            if graph.capacity[u][v] - graph.flow[u][v] > 0 and graph.height[u] == graph.height[v] + 1:
                push(graph, u, v)
                pushed = True
                if graph.excess[u] == 0:
                    return
        if not pushed:  # If no push was possible, relabel
            old_height = graph.height[u]
            relabel(graph, u)
            # Apply gap heuristic if no nodes remain at the old height
            if graph.height_count[old_height] == 0:
                gap_heuristic(graph, old_height)
                return

def get_max_flow(graph, source, sink):
    """Calcula el flujo máximo desde la fuente hasta el sumidero usando Push-Relabel con escalado."""
    initialize_preflow(graph, source)

    # Comienza con la capacidad escalada más alta y redúcela a la mitad en cada iteración
    while graph.scaled_capacity > 1:  # Asegura que scaled_capacity no se reduzca a cero
        # Lista de nodos activos (con exceso de flujo) excluyendo la fuente y el sumidero
        active_nodes = deque(
            [i for i in range(graph.V) if i != source and i != sink and graph.excess[i] > 0]
        )
        
        while active_nodes:
            u = active_nodes.popleft()  # Toma un nodo activo
            discharge(graph, u)  # Intenta descargar el exceso de flujo del nodo
            if graph.excess[u] > 0:  # Si aún tiene exceso de flujo, vuelve a añadirlo a la lista
                active_nodes.append(u)
        
        # Reduce la capacidad escalada a la mitad para la siguiente iteración
        graph.scaled_capacity //= 2

    # El flujo máximo es el flujo total empujado desde la fuente
    return sum(graph.flow[source][v] for v in range(graph.V) if graph.capacity[source][v] > 0)

#################Push Relabel######################
class Neurona:
    def __init__(self, id, x, y, tipo, peptidos):
        """
        Inicializa una nueva instancia de la clase.

        Parámetros:
        id (int): Identificador único del objeto.
        x (float): Coordenada x del objeto.
        y (float): Coordenada y del objeto.
        tipo (str): Tipo o categoría del objeto.
        peptidos (list): Lista de péptidos asociados al objeto.
        """
        self.id = id
        self.x = x
        self.y = y
        self.tipo = tipo
        self.peptidos = peptidos

class GrafoFlujo:
    def __init__(self):
        """
        Inicializa una nueva instancia de la clase.

        Atributos:
            neuronas (list): Lista que almacenará las neuronas.
            adyacencias (defaultdict): Diccionario que almacenará las listas de adyacencias de cada neurona.
            pesos (dict): Diccionario que almacenará los pesos de las conexiones entre neuronas.
        """
        self.neuronas = []
        self.adyacencias = defaultdict(list)
        self.pesos = {}

    def agregar_neurona(self, neurona):
        """
        Agrega una neurona a la lista de neuronas.

        Args:
            neurona: El objeto neurona que se va a agregar a la lista de neuronas.
        """
        self.neuronas.append(neurona)

    def agregar_arista(self, origen, destino, peso):
        """
        Agrega una arista al grafo con un peso específico.

        Args:
            origen (Nodo): El nodo de origen de la arista.
            destino (Nodo): El nodo de destino de la arista.
            peso (int): El peso de la arista.

        """
        self.adyacencias[origen.id].append(destino.id)
        self.pesos[(origen.id, destino.id)] = peso

    def construir_grafo(self, n, d):
        """
        Construye un grafo basado en las neuronas y sus conexiones.
        Args:
            n (int): Número de neuronas.
            d (float): Distancia máxima para considerar una conexión entre neuronas.
        Este método itera sobre todas las neuronas y calcula la distancia entre cada par de neuronas.
        Si la distancia entre dos neuronas es menor o igual a 'd' y cumplen con las condiciones de conexión,
        se agrega una arista entre ellas con un peso basado en el número de péptidos comunes.
        Las conexiones se realizan según los siguientes tipos de neuronas:
            - Iniciadora (tipo 1) -> Calculadora (tipo 2)
            - Calculadora (tipo 2) -> Calculadora (tipo 2)
            - Calculadora (tipo 2) -> Ejecutora (tipo 3)
            - Iniciadora (tipo 1) -> Ejecutora (tipo 3) (opcional)
        """
        for i in range(n):
            for j in range(i + 1, n):
                neurona1 = self.neuronas[i]
                neurona2 = self.neuronas[j]
                
                # Calcula la distancia entre dos neuronas
                distancia = math.sqrt((neurona1.x - neurona2.x) ** 2 + (neurona1.y - neurona2.y) ** 2)
                
                # Si están dentro de la distancia máxima y cumplen las condiciones de conexión
                if distancia <= d:
                    # Calcula el peso como el número de péptidos comunes
                    peso = len(set(neurona1.peptidos) & set(neurona2.peptidos))
                    
                    # Solo conecta según el tipo: Iniciadora -> Calculadora, Calculadora -> Ejecutora, etc.
                    if neurona1.tipo == 1 and neurona2.tipo == 2:
                        self.agregar_arista(neurona1, neurona2, peso)
                    elif neurona1.tipo == 2 and neurona2.tipo == 2:
                        self.agregar_arista(neurona1, neurona2, peso)
                        self.agregar_arista(neurona2, neurona1, peso)
                    elif neurona1.tipo == 2 and neurona2.tipo == 3:
                        self.agregar_arista(neurona1, neurona2, peso)
                    elif neurona1.tipo == 1 and neurona2.tipo == 3:
                        self.agregar_arista(neurona1, neurona2, peso)  # Esto es opcional, según las reglas

    def verificar_grafo(self):
        """
        Imprime las neuronas y sus conexiones en el grafo.

        Este método recorre todas las neuronas y sus listas de adyacencias,
        imprimiendo cada conexión junto con su peso.

        Esto es útil para verificar visualmente la estructura del grafo y
        asegurarse de que las conexiones y pesos se han establecido correctamente.
        """
        print("Neuronas y sus conexiones:")
        for origen, destinos in self.adyacencias.items():
            for destino in destinos:
                print(f"Neurona {origen} -> Neurona {destino} con peso {self.pesos[(origen, destino)]}")

def cargar_datos():
    """
    Carga los datos de entrada para múltiples casos de prueba y construye grafos de flujo.
    Returns:
        list: Una lista de objetos GrafoFlujo, cada uno representando un caso de prueba.
    El formato de entrada esperado es:
    - La primera línea contiene un entero que indica el número de casos de prueba.
    - Para cada caso de prueba:
        - La primera línea contiene dos enteros n y d, donde n es el número de neuronas y d es una distancia.
        - Las siguientes n líneas contienen información sobre cada neurona en el formato:
          id x y tipo peptido1 peptido2 ... peptidoN
          donde:
            - id es el identificador de la neurona (entero).
            - x e y son las coordenadas de la neurona (enteros).
            - tipo es el tipo de neurona (entero).
            - peptido1, peptido2, ..., peptidoN son los péptidos asociados a la neurona (cadenas de texto).
    La función construye un grafo de flujo para cada caso de prueba utilizando la información de las neuronas y la distancia d, y devuelve una lista de estos grafos.
    """
    grafos = []
    
    # Leer número de casos de prueba
    casos = int(input())
    
    for _ in range(casos):
        grafo = GrafoFlujo()
        
        # Leer n y d
        n, d = map(int, input().split())
        
        # Leer información de cada neurona
        for _ in range(n):
            datos = input().split()
            id = int(datos[0])
            x = int(datos[1])
            y = int(datos[2])
            tipo = int(datos[3])
            peptidos = datos[4:]
            neurona = Neurona(id, x, y, tipo, peptidos)
            grafo.agregar_neurona(neurona)
        
        # Construir el grafo de flujo para el caso actual
        grafo.construir_grafo(n, d)
        
        # Agregar el grafo a la lista de grafos
        grafos.append(grafo)
    
    return grafos

def agregar_super_fuente_sumidero(graph_push_relabel, grafo_flujo):
    """
    Agrega un super fuente y un super sumidero al grafo para el algoritmo Push-Relabel.
    Parámetros:
    graph_push_relabel (GraphPushRelabel): El grafo en el que se ejecutará el algoritmo Push-Relabel.
    grafo_flujo (GrafoFlujo): El grafo de flujo que contiene las neuronas y sus tipos.
    Descripción:
    - Se añade un nodo adicional al grafo original para que actúe como super sumidero.
    - Se ajustan las matrices de capacidad y flujo, así como las listas de exceso y altura para incluir el nuevo nodo.
    - Se conecta el super fuente (nodo 0) a todas las neuronas de tipo iniciadora con una capacidad suficientemente grande.
    - Se conecta todas las neuronas de tipo ejecutora al super sumidero con una capacidad suficientemente grande.
    """
    n = len(graph_push_relabel.capacity)  # Número actual de nodos en el grafo
    nuevo_n = n + 1  # Añadimos un nodo adicional para el super sumidero
    
    # Ajustar las matrices y listas para incluir el nuevo nodo super sumidero
    graph_push_relabel.V = nuevo_n  # Actualizamos el número total de vértices
    for i in range(n):
        graph_push_relabel.capacity[i].append(0)
        graph_push_relabel.flow[i].append(0)
    graph_push_relabel.capacity.append([0] * nuevo_n)
    graph_push_relabel.flow.append([0] * nuevo_n)
    graph_push_relabel.excess.append(0)
    graph_push_relabel.height.append(0)
    graph_push_relabel.height_count.append(0)
    
    # Conectar super fuente (nodo 0) a todas las neuronas de tipo iniciadora
    for neurona in grafo_flujo.neuronas:
        if neurona.tipo == 1:  # Tipo 1 es iniciadora
            graph_push_relabel.add_edge(0, neurona.id, 10**6)  # Capacidad suficientemente grande, NO debe usarse inf para evitar errores con la super fuente

    # Conectar todas las neuronas de tipo ejecutora al super sumidero (nodo n+1)
    super_sumidero = n  # El nodo del super sumidero es el último (n)
    for neurona in grafo_flujo.neuronas:
        if neurona.tipo == 3:  # Tipo 3 es ejecutora
            graph_push_relabel.add_edge(neurona.id, super_sumidero, 10**6)  # Capacidad suficientemente grande, NO debe usarse inf para evitar errores con el super sumidero

# Función para imprimir el grafo y verificar sus aristas y capacidades
def ver_grafo(graph_push_relabel):
    """
    Imprime las aristas y sus capacidades en el grafo proporcionado.

    Parámetros:
    graph_push_relabel (Graph): Un objeto de tipo Graph que contiene la representación del grafo,
                                incluyendo el número de vértices (V) y la matriz de capacidades (capacity).

    El método recorre todos los pares de vértices (u, v) en el grafo y, si existe una capacidad positiva
    entre ellos, imprime la arista y su capacidad.
    """
    print("Aristas y capacidades en el grafo:")
    for u in range(graph_push_relabel.V):
        for v in range(graph_push_relabel.V):
            if graph_push_relabel.capacity[u][v] > 0:
                print(f"{u} -> {v} con capacidad {graph_push_relabel.capacity[u][v]}")


def encontrar_neurona_critica():
    """
    Encuentra la neurona crítica en una serie de grafos de flujo.
    Este método procesa una lista de grafos de flujo, uno por cada caso de prueba, y determina cuál es la neurona crítica
    en cada grafo, donde una neurona crítica es aquella cuya eliminación reduce más significativamente el flujo máximo en el grafo.
    Pasos:
    1. Cargar todos los casos de prueba.
    2. Para cada caso de prueba:
        a. Convertir el grafo al formato adecuado para el algoritmo Push-Relabel.
        b. Agregar una super fuente y un super sumidero al grafo.
        c. Calcular el flujo máximo original sin bloquear ninguna neurona.
        d. Iterar sobre cada neurona calculadora y calcular el flujo máximo con esa neurona bloqueada.
        e. Determinar si la neurona bloqueada es la neurona crítica.
    3. Imprimir y retornar los resultados de todos los casos de prueba.
    Returns:
        list: Una lista de tuplas, cada una conteniendo:
            - neurona_critica (int): El ID de la neurona crítica.
            - flujo_maximo_original (int): El flujo máximo original sin bloquear ninguna neurona.
            - flujo_maximo_con_bloqueo_min (int): El flujo máximo con la neurona crítica bloqueada.
    """
    # Paso 1: Cargar todos los casos de prueba
    casos_prueba = cargar_datos()  # Supongo que cargar_datos ahora devuelve una lista de grafos, uno por cada caso de prueba
    print("casos_prueba_cargados")
    resultados = []
    
    # Procesar cada caso de prueba individualmente
    for i, grafo in enumerate(casos_prueba):
                
        # Convertir el grafo de flujo para Push-Relabel
        grafo_push_relabel = Graph.traducir_a_grafo_push_relabel(grafo)
        agregar_super_fuente_sumidero(grafo_push_relabel, grafo)
        
        source = 0
        sink = grafo_push_relabel.V - 1

        # Calcular el flujo máximo original sin bloquear ninguna neurona
        print("Calculando flujo máximo original...")
        flujo_maximo_original = get_max_flow(grafo_push_relabel, source, sink)
        print(f"Flujo máximo original: {flujo_maximo_original}")
        # Variables para almacenar los resultados del caso actual
        neurona_critica = None
        flujo_maximo_con_bloqueo_min = flujo_maximo_original

        # Iterar sobre cada neurona calculadora y calcular el flujo máximo con esa neurona bloqueada
        for neurona in grafo.neuronas:
            if neurona.tipo == 2:  # Tipo 2 indica que es una neurona calculadora
                # Crear una copia del grafo para bloquear la neurona
                grafo_modificado = Graph.traducir_a_grafo_push_relabel(grafo)
                agregar_super_fuente_sumidero(grafo_modificado, grafo)
                
                # Bloquear la neurona eliminando sus conexiones
                for destino in list(grafo.adyacencias[neurona.id]):
                    grafo_modificado.capacity[neurona.id][destino] = 0
                    grafo_modificado.capacity[destino][neurona.id] = 0
                
                # Calcular el flujo máximo con la neurona bloqueada
                flujo_con_bloqueo = get_max_flow(grafo_modificado, source, sink)

                # Verificar si esta es la neurona crítica
                if flujo_con_bloqueo < flujo_maximo_con_bloqueo_min:
                    flujo_maximo_con_bloqueo_min = flujo_con_bloqueo
                    neurona_critica = neurona.id

        # Almacenar el resultado para este caso de prueba
        resultados.append((neurona_critica, flujo_maximo_original, flujo_maximo_con_bloqueo_min))
    
    # Imprimir los resultados de todos los casos de prueba
    for i, (neurona_critica, flujo_total, flujo_bloqueado) in enumerate(resultados):
        print(f"{neurona_critica} {flujo_total} {flujo_bloqueado}")

    # Retornar los resultados en caso de que se necesiten para otros usos
    return resultados

# Llamada a la función
resultados = encontrar_neurona_critica()



