import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class GeneradorCasosPrueba {

    private static final Random random = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el número de casos de prueba: ");
        int numCasos = scanner.nextInt();
        
        System.out.print("Ingrese el número de células por caso (entre 3 y 100000): ");
        int numCelulas = scanner.nextInt();
        
        System.out.print("Ingrese la distancia máxima (d): ");
        int distanciaMaxima = scanner.nextInt();

        try {
            generarCasosPrueba(numCasos, numCelulas, distanciaMaxima, "p0.in");
            System.out.println("Archivo p0.in generado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al generar el archivo: " + e.getMessage());
        }
    }

    public static void generarCasosPrueba(int numCasos, int numCelulas, int distanciaMaxima, String nombreArchivo) throws IOException {
        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write(numCasos + "\n");
            
            for (int i = 0; i < numCasos; i++) {
                writer.write(numCelulas + " " + distanciaMaxima + "\n");

                for (int j = 1; j <= numCelulas; j++) {
                    int x = random.nextInt(100);
                    int y = random.nextInt(100);
                    int tipoCelula = 1 + random.nextInt(3);  // 1: iniciadora, 2: calculadora, 3: ejecutora
                    int numPeptidos = 3 + random.nextInt(1000);  // Entre 3 y 5 péptidos

                    StringBuilder celula = new StringBuilder();
                    celula.append(j).append(" ").append(x).append(" ").append(y).append(" ").append(tipoCelula);

                    // Generar péptidos únicos
                    Set<String> peptidos = new HashSet<>();
                    while (peptidos.size() < numPeptidos) {
                        peptidos.add(generarPeptido());
                    }

                    for (String peptido : peptidos) {
                        celula.append(" ").append(peptido);
                    }

                    writer.write(celula.toString() + "\n");
                }
            }
        }
    }

    // Genera un péptido aleatorio de 5 caracteres
    public static String generarPeptido() {
        StringBuilder peptido = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            char letra = (char) ('A' + random.nextInt(26));  // Genera una letra aleatoria entre A y Z
            peptido.append(letra);
        }
        return peptido.toString();
    }
}
