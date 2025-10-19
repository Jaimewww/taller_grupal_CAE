import dominio.EstadoTicket;
import dominio.Nota;
import dominio.Ticket;
import estructuras.Node;
import estructuras.Queue;
import estructuras.SimpleList;
import estructuras.Stack;

import java.util.Scanner;

public class Main {
    static Stack<Ticket> ticketStack = new Stack<>();
    static Queue<Ticket> ticketQueue = new Queue<>();
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        handleMenuOption(1);
        handleMenuOption(2);
        /*
            *************************************************
            ***     Sistema de Gestión de Casos - CAE     ***
            *************************************************
            --- MENÚ PRINCIPAL ---
            1. Recibir nuevo caso
            2. Atender siguiente caso
            3. Ver casos en espera
            4. Consultar historial de caso (por ID)
            0. Salir
            Seleccione una opción: 1
            Nombre del estudiante: Pedro Torres
            Tipo de trámite: Retiro de Asignatura
            >> Caso 4 recibido y encolado. <<

            --- MENÚ PRINCIPAL ---
            ...
            Seleccione una opción: 3
            --- Casos en Espera (4) ---
            Frente -> [ID 1] -> [ID 2] -> [ID 3] -> [ID 4] -> Fin
         */
    }

    public static void handleMenuOption(int option) {
        switch (option) {
            case 1:
                recibirNuevoCaso();
                break;
            case 2:
                atenderSiguienteCaso();
                break;
            case 3:
                // Lógica para ver casos en espera
                break;
            case 4:
                // Lógica para consultar historial de caso por ID
                break;
            case 0:
                System.out.println("Saliendo del sistema. ¡Hasta luego!");
                break;
            default:
                System.out.println("Opción inválida. Por favor, intente de nuevo.");
        }
    }

    public static void recibirNuevoCaso(){
        System.out.println("Ingrese el nombre del estudiante:");
        String nombreEstudiante = sc.nextLine();
        System.out.println("Ingrese el tipo de trámite:");
        String tipoTramite = sc.nextLine();
        Ticket nuevoTicket = new Ticket(nombreEstudiante, tipoTramite);
        int nuevoId = ticketQueue.size() + ticketStack.size() + 1;
        nuevoTicket.setId(nuevoId);
        ticketQueue.enqueue(nuevoTicket);
        System.out.println(">> Caso " + nuevoTicket.getId() + " del estudiante " + nuevoTicket.getEstudiante() + " recibido y encolado. <<");
    }

    public static Ticket atenderSiguienteCaso(){
        if(ticketQueue.isEmpty()){
            System.out.println("No hay casos en espera.");
            return null;
        }
        Ticket siguienteTicket = ticketQueue.peek();
        siguienteTicket.setEstado(EstadoTicket.EN_ATENCION);
        System.out.println(">> Atendiendo caso " + siguienteTicket.getId() + " del estudiante " + siguienteTicket.getEstudiante() + " <<");
        return siguienteTicket;
        //enviar a otro menu de atencion
    }

    /*public static void verCasosEnEspera(){
        System.out.println("--- Casos en Espera (" + ticketQueue.size() + ") ---");
        if(ticketQueue.isEmpty()){
            System.out.println("No hay casos en espera.");
            return;
        }
        Ticket current = ticketQueue.peek();
        System.out.print("Frente -> ");
        while(current != null){
            System.out.print("[ID " + current.getId() + "] -> ");
            current = ;
        }
        System.out.println("Fin");
    }*/
}