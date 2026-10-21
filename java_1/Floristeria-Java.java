import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Floristeria {

    // Interés aplicado cuando se paga con tarjeta de crédito en más de 1 cuota (5% sobre el total)
    static final double INTERES_CREDITO = 0.05;
    static final int[] CUOTAS_PERMITIDAS = {1, 3, 6, 12, 24, 48};

    static final Scanner sc = new Scanner(System.in);

    /** Inventario de la tienda: listas paralelas de tipo, cantidad y precio. */
    static class Inventario {
        List<String> tipo = new ArrayList<>();
        List<Integer> cant = new ArrayList<>();
        List<Integer> precios = new ArrayList<>();
    }

    /** Carrito del usuario: listas paralelas de tipo, cantidad y precio. */
    static class Carrito {
        List<String> tipo = new ArrayList<>();
        List<Integer> cantidad = new ArrayList<>();
        List<Integer> precio = new ArrayList<>();
    }

    /** Tarjeta registrada (en crédito, el saldo es el cupo disponible). */
    static class Tarjeta {
        String titular;
        String tipo;
        double saldo;

        Tarjeta(String titular, String tipo, double saldo) {
            this.titular = titular;
            this.tipo = tipo;
            this.saldo = saldo;
        }
    }

    // ---------- Utilidades de entrada / formato ----------

    static String leer(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine();
    }

    static int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leer(mensaje).trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    static double leerDecimal(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leer(mensaje).trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    /** Equivalente a str.capitalize() de Python: primera letra en mayúscula, el resto en minúscula. */
    static String capitalizar(String s) {
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // ---------- Funciones del programa ----------

    /** Muestra en pantalla el menú principal de la floristería. */
    static void menu() {
        System.out.println("\nQué desea hacer?");
        System.out.println("1. Comprar flores");
        System.out.println("2. Pagar");
        System.out.println("3. Generar factura");
        System.out.println("4. Salir");
    }

    /** Calcula el total a pagar: suma de cantidad * precio unitario de cada flor del carrito. */
    static double calcularTotal(Carrito carrito) {
        double total = 0;
        for (int i = 0; i < carrito.tipo.size(); i++) {
            total += (double) carrito.cantidad.get(i) * carrito.precio.get(i);
        }
        return total;
    }

    /** Imprime las flores que el usuario tiene actualmente en el carrito. */
    static void mostrarCarrito(Carrito carrito) {
        for (int i = 0; i < carrito.tipo.size(); i++) {
            System.out.println((i + 1) + ". " + capitalizar(carrito.tipo.get(i))
                    + " - cantidad: " + carrito.cantidad.get(i)
                    + " - precio: $" + carrito.precio.get(i));
        }
    }

    /**
     * Opción 1 del menú. Muestra las flores disponibles, permite quitar flores del carrito
     * (devolviéndolas al inventario) y agrega al carrito la flor y la cantidad que el usuario elija,
     * descontándola del inventario.
     */
    static void comprarFlores(Inventario inventario, Carrito carrito) {
        System.out.println("Flores disponibles: \n");
        for (int i = 0; i < inventario.tipo.size(); i++) {
            System.out.println((i + 1) + ". " + capitalizar(inventario.tipo.get(i))
                    + " - cantidad: " + inventario.cant.get(i)
                    + " - precio: $" + inventario.precios.get(i));
        }

        // Qué pasa si el usuario entra y ya no hay flores disponibles
        if (inventario.tipo.isEmpty()) {
            System.out.println("No hay flores en stock, sorry :(");
        }

        // Quitar del carrito si compré lo que no quería
        if (!carrito.tipo.isEmpty()) {
            System.out.println("Flores en el carrito: \n");
            mostrarCarrito(carrito);

            System.out.println("Desea quitar o retirar alguna flor del carrito? (si/no)");
            String quitarOpc = sc.nextLine().toLowerCase().trim();
            if (quitarOpc.equals("si")) {
                System.out.println("Flores en el carrito: \n");
                mostrarCarrito(carrito);
                int quitarIdx = leerEntero("Coloque el numero de la flor que deseas quitar del carrito: ");
                if (quitarIdx > 0 && quitarIdx <= carrito.tipo.size()) {
                    String florADevolver = carrito.tipo.remove(quitarIdx - 1);
                    int cantidadADevolver = carrito.cantidad.remove(quitarIdx - 1);
                    int precioADevolver = carrito.precio.remove(quitarIdx - 1);

                    // Devolvemos la flor al inventario (si se había agotado, la volvemos a crear)
                    int idxInventario = inventario.tipo.indexOf(florADevolver);
                    if (idxInventario >= 0) {
                        inventario.cant.set(idxInventario, inventario.cant.get(idxInventario) + cantidadADevolver);
                    } else {
                        inventario.tipo.add(florADevolver);
                        inventario.cant.add(cantidadADevolver);
                        inventario.precios.add(precioADevolver);
                    }
                }
            }
        }

        System.out.println("Ha seleccionado: Comprar flores\n");
        String florOpc = leer("Seleccione la flor que desea comprar: ").toLowerCase().trim();
        int idx = inventario.tipo.indexOf(florOpc);
        if (idx >= 0) {
            int florCant = leerEntero("Ingrese la cantidad que desea comprar: ");

            if (florCant <= inventario.cant.get(idx)) {
                // Agregamos los valores a las listas del carrito
                carrito.tipo.add(florOpc);
                carrito.cantidad.add(florCant);
                carrito.precio.add(inventario.precios.get(idx));

                // Restamos del inventario
                inventario.cant.set(idx, inventario.cant.get(idx) - florCant);
                System.out.println("¡Se han añadido " + florCant + " " + florOpc + "(s) al carrito!");

                // Removemos del inventario las flores si llegan a 0
                if (inventario.cant.get(idx) == 0) {
                    inventario.tipo.remove(idx);
                    inventario.cant.remove(idx);
                    inventario.precios.remove(idx);
                }
            } else {
                System.out.println("Cantidad no disponible");
            }
        } else {
            System.out.println("La flor " + florOpc + " no está disponible");
        }
    }

    /**
     * Busca la tarjeta en el registro y verifica que el titular y el tipo (credito o debito) coincidan.
     * Devuelve la tarjeta si es válida, o null si no lo es.
     */
    static Tarjeta validarTarjeta(Map<String, Tarjeta> registro, String numero, String titular, String tipo) {
        Tarjeta tarjeta = registro.get(numero);
        if (tarjeta == null) {
            return null;
        }
        if (!tarjeta.titular.equals(titular.toLowerCase().trim()) || !tarjeta.tipo.equals(tipo)) {
            return null;
        }
        return tarjeta;
    }

    static boolean cuotaPermitida(int cuotas) {
        for (int c : CUOTAS_PERMITIDAS) {
            if (c == cuotas) return true;
        }
        return false;
    }

    /** Pide el número de cuotas hasta que el usuario ingrese una opción válida (1, 3, 6, 12, 24 o 48). */
    static int pedirCuotas() {
        while (true) {
            int cuotas = leerEntero("a cuantas cuotas se va a endeudar? (1,3,6,12,24,48): ");
            if (cuotaPermitida(cuotas)) {
                return cuotas;
            }
            System.out.println("Número de cuotas no permitido.");
        }
    }

    /** Con 1 cuota no se cobra interés; con más de 1 cuota se suma el porcentaje de interés al total. */
    static double calcularTotalConInteres(double total, int cuotas, double interes) {
        if (cuotas == 1) {
            return total;
        }
        return total * (1 + interes);
    }

    /**
     * Opción 2 del menú. Pide el medio de pago (efectivo, tarjeta de crédito, tarjeta de débito u otro),
     * realiza los cálculos correspondientes y muestra el total a pagar por la compra.
     */
    static void pagar(Carrito carrito, Map<String, Tarjeta> registroTarjetas) {
        if (carrito.tipo.isEmpty()) {
            System.out.println("El carrito de compras está vacío. ");
            return;
        }

        System.out.println("Seleccione el medio de pago: \n");
        System.out.println("1. Efectivo");
        System.out.println("2. Tarjeta de crédito");
        System.out.println("3. Tarjeta de débito");
        System.out.println("4. Otro medio de pago");
        String pagoOpc = leer("Seleccione su opción: \n").trim();

        double totalPagar = calcularTotal(carrito);

        // Si es efectivo hay que dar vuelto
        if (pagoOpc.equals("1")) {
            double efectivoRecibido = leerDecimal("Ingrese la cantidad o monto del dinero registrado porfi: ");
            if (efectivoRecibido >= totalPagar) {
                System.out.println("Su vuelto es: $" + (efectivoRecibido - totalPagar));
            } else {
                System.out.println("No tiene suficiente dinero para realizar la compra.");
                return;
            }

        // Si es con tarjeta hay que registrar número y titular, y revisar que haya saldo suficiente
        } else if (pagoOpc.equals("2") || pagoOpc.equals("3")) {
            String tipo = pagoOpc.equals("2") ? "credito" : "debito";
            String tarjetaNumero = leer("Ingrese el número de su tarjeta: ").trim();
            String tarjetaTitular = leer("Ingrese el nombre del titular de la tarjeta porfi: ");

            Tarjeta tarjeta = validarTarjeta(registroTarjetas, tarjetaNumero, tarjetaTitular, tipo);
            if (tarjeta == null) {
                System.out.println("Tarjeta no registrada, titular incorrecto o tipo de tarjeta equivocado. No se puede realizar la compra.");
                return;
            }

            if (pagoOpc.equals("2")) {
                System.out.println("Ha seleccionado: Tarjeta de crédito");
                int cuotas = pedirCuotas();
                double totalConInteres = calcularTotalConInteres(totalPagar, cuotas, INTERES_CREDITO);

                if (totalConInteres > tarjeta.saldo) {
                    System.out.println(String.format(Locale.US,
                            "Cupo insuficiente. Total con interés: $%.2f - Cupo disponible: $%s",
                            totalConInteres, formatearNumero(tarjeta.saldo)));
                    return;
                }

                tarjeta.saldo -= totalConInteres;
                System.out.println(String.format(Locale.US, "Interés aplicado: $%.2f", totalConInteres - totalPagar));
                System.out.println(String.format(Locale.US, "Pagará %d cuota(s) de $%.2f", cuotas, totalConInteres / cuotas));
                totalPagar = totalConInteres;
            } else {
                System.out.println("Ha seleccionado: Tarjeta de débito");
                if (totalPagar > tarjeta.saldo) {
                    System.out.println("Saldo insuficiente. Total: $" + formatearNumero(totalPagar)
                            + " - Saldo disponible: $" + formatearNumero(tarjeta.saldo));
                    return;
                }
                tarjeta.saldo -= totalPagar;
            }
        }

        System.out.println("\nUsted ha seleccionado la opción de pago " + pagoOpc + ".");
        System.out.println(String.format(Locale.US, "El total a pagar por su compra es: $%.2f", totalPagar));
    }

    /** Imprime un número sin decimales si es entero (ej. 500000), o con decimales si no lo es. */
    static String formatearNumero(double valor) {
        if (valor == Math.floor(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    /** Opción 3 del menú. Imprime la factura con cada flor, su cantidad, precio unitario y subtotal. */
    static void generarFactura(Carrito carrito) {
        System.out.println("Ha seleccionado: Generar factura\n");
        System.out.println(String.format("%-15s | %-10s | %-15s | %-15s", "Flores", "Cantidad", "Precio Unitario", "Subtotal"));
        System.out.println("-".repeat(64));
        for (int i = 0; i < carrito.tipo.size(); i++) {
            int cantidad = carrito.cantidad.get(i);
            int precio = carrito.precio.get(i);
            System.out.println(String.format("%-15s | %-10d | $%-14d | $%-14d",
                    capitalizar(carrito.tipo.get(i)), cantidad, precio, precio * cantidad));
        }
    }

    /** Opción 4 del menú. Muestra el mensaje de que el usuario ha seleccionado salir. */
    static void salir() {
        System.out.println("Ha seleccionado: Salir\n");
    }

    /** Caso por defecto del menú. */
    static void opcionInvalida() {
        System.out.println("Número incorrecto, por favor seleccione uno correcto");
    }

    // ---------- Programa principal ----------

    public static void main(String[] args) {
        // Inventario inicial
        Inventario flores = new Inventario();
        String[] tipos = {"margarita", "azucena", "rosa"};
        int[] cantidades = {10, 20, 30};
        int[] precios = {1200, 3000, 1200};
        for (int i = 0; i < tipos.length; i++) {
            flores.tipo.add(tipos[i]);
            flores.cant.add(cantidades[i]);
            flores.precios.add(precios[i]);
        }

        System.out.println("Bienvenido a mi floristería");
        System.out.println("Se ofrecen los siguientes productos: \n");
        System.out.println(String.format("%-15s | %-10s | %-15s", "Flores", "Cantidad", "Precio Unitario"));
        System.out.println("-".repeat(46));
        for (int i = 0; i < flores.tipo.size(); i++) {
            System.out.println(String.format("%-15s | %-10d | $%-14d",
                    capitalizar(flores.tipo.get(i)), flores.cant.get(i), flores.precios.get(i)));
        }

        // Carrito de compras (fuera del ciclo para que no se borre al repetir el menú)
        Carrito compraUser = new Carrito();

        // Registro simulado de tarjetas: número -> titular, tipo y saldo
        Map<String, Tarjeta> tarjetas = new HashMap<>();
        tarjetas.put("1111222233334444", new Tarjeta("maria lopez", "credito", 500000));
        tarjetas.put("5555666677778888", new Tarjeta("carlos gomez", "debito", 100000));

        boolean continuar = true;
        while (continuar) {
            menu();
            String opc = leer("Seleccione su opción: \n").trim();
            switch (opc) {
                case "1" -> comprarFlores(flores, compraUser);
                case "2" -> pagar(compraUser, tarjetas);
                case "3" -> {
                    generarFactura(compraUser);
                    continuar = false;
                }
                case "4" -> {
                    salir();
                    continuar = false;
                }
                default -> opcionInvalida();
            }
        }

        System.out.println("Gracias por emplear nuestros servicios. Feliz día");
    }
}
