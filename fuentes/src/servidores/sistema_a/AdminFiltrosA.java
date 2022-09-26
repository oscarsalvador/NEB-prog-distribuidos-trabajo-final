import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class AdminFiltrosA {
    //directorios
    private String pathLocal;
    private String pathRemoto;
    //donde enviar
    private int pCentral;
    private String dCentral;



    AdminFiltrosA(String pathDirLocal, String pathDirRemoto, int puertoCentral, String direccionCentral){
        pathLocal = pathDirLocal;
        pathRemoto = pathDirRemoto;
        pCentral = puertoCentral;
        dCentral = direccionCentral;
    }




    private void dialogo(){
        String opcion = "";
        while(!opcion.equals("q")){
            try{
                System.out.println("Filtros disponibles: ");
                printFiltrosDisponibles();
                System.out.println();

                System.out.println("Opciones: ");
                System.out.println("(c) Copiar filtro a carpeta distribuida");
                System.out.println("(b) Borrar filtro de carpeta distribuida");
                System.out.println("(q) Cerrar el administrador de filtros");
                opcion = System.console().readLine();
                
                String filtro = "";
                FileSystem fs = FileSystems.getDefault();
                Path remoto;
                switch(opcion){                
                    case "c":
                        System.out.print("Filtro que copiar a la carpeta: ");
                        filtro = System.console().readLine();
                        Path local = fs.getPath(pathLocal + filtro);
                        remoto = fs.getPath(pathRemoto + filtro);

                        Files.copy(local, remoto, StandardCopyOption.REPLACE_EXISTING);
                        break;

                    case "b":
                        System.out.print("Filtro que borrar de la carpeta distribuida");
                        filtro = System.console().readLine();
                        remoto = fs.getPath(pathRemoto + filtro);
                        Files.delete(remoto);
                        break;

                    default: break;
                }

                notificarCentral();
            }catch(Exception e){e.printStackTrace(System.out);}
        }
    }
    private void printFiltrosDisponibles(){
        try{
            File dirPath = new File(pathLocal);
            String[] disponibles = dirPath.list();
            for(String i : disponibles){
                System.out.println(i);
            }
        }catch(Exception e){e.printStackTrace(System.out);}
    }

    private void notificarCentral(){
        System.out.println("notificarCentral");
        try{
            File dirPath = new File(pathRemoto);
            List<String> filtrosCorriendo = Arrays.asList(dirPath.list());
            Socket datosCentral = new Socket(dCentral, pCentral);
            ObjectOutputStream salidaCentral = new ObjectOutputStream(datosCentral.getOutputStream());
            salidaCentral.writeObject(filtrosCorriendo);
        }catch(Exception e){e.printStackTrace(System.out);}
    }





    public static void main(String args[]){
        AdminFiltrosA aA = new AdminFiltrosA(args[0], args[1], Integer.valueOf(args[2]), args[3]);
        aA.dialogo();
    }
}
//crear y borrar filtros, asignar a anillos
//primero tener que cambiar filtros de todos anillos, antes de poder borrar