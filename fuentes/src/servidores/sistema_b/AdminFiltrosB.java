import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class AdminFiltrosB {
    private class Anillo {
        public int numeroAnillo;
        public String filtroAsignado;
        public boolean aplicado = false;
    
        Anillo(int numeroAnillo, String filtroAsignado){
            this.numeroAnillo = numeroAnillo;
            this.filtroAsignado = filtroAsignado;
        }
    }
    private List<Anillo> anillos = new ArrayList<Anillo>();
    private List<String> filtrosDisponibles = new ArrayList<String>();

    private String pathLocal;
    //donde enviar
    private int pCentral;
    private String dCentral;
    //donde escuchar
    private int pAnillos;




    AdminFiltrosB(String pathDirLocal, int puertoEscuchaAnillos, int puertoCentral, String direccionCentral){
        pathLocal = pathDirLocal;
        pAnillos = puertoEscuchaAnillos;
        pCentral = puertoCentral;
        dCentral = direccionCentral;

        cargaDefaultAnillos();
    }




    private void cargaDefaultAnillos(){
        try{
            File dirPath = new File(pathLocal);
            String[] disponibles = dirPath.list();
            
            if(disponibles.length > 1){
                Anillo a1 = new Anillo(1, disponibles[0]);
                Anillo a2 = new Anillo(2, disponibles[1]);
                anillos.add(a1);
                anillos.add(a2);
            }
        }catch(Exception e){e.printStackTrace(System.err);}
    }

    private void dialogo(){
        String opcion = "";
        while(!opcion.equals("q")){
            try{
                System.out.println("Filtros disponibles: ");
                printFiltrosDisponibles();
                System.out.println();

                System.out.println("Anillos y filtros asignados: ");
                printFiltrosAsignados();
                System.out.println();

                System.out.println("Opciones : ");
                System.out.println("(a) Asignar un filtro a un anillo (el anillo no tiene que estar inicializado)");
                System.out.println("(q) Cerrar el administrador de filtros");
                opcion = System.console().readLine();
                
                if(opcion.equals("a")){
                    System.out.println("Numero de anillo: ");
                    int numAnillo = Integer.valueOf(System.console().readLine());
                    String filtro;
                    do{
                        System.out.println("Nombre de filtro: ");
                        filtro = System.console().readLine();
                    }while(!filtrosDisponibles.contains(filtro));

                    asignarFiltro(numAnillo, filtro);
                }

                //notificarCentral(); cuando el anillo lo reciba, corriendo no asignados
            }catch(Exception e){e.printStackTrace(System.err);}
        }
    }
    private void printFiltrosDisponibles(){
        try{
            File dirPath = new File(pathLocal);
            String[] disponibles = dirPath.list();
            filtrosDisponibles = Arrays.asList(disponibles);
            for(String i : disponibles){
                System.out.println(i);
            }
        }catch(Exception e){e.printStackTrace(System.err);}
    }
    private void printFiltrosAsignados(){
        for(Anillo i : anillos){
            System.out.println("Anillo " + i.numeroAnillo + " filtro " + i.filtroAsignado);
        }
    }
    private boolean asignarFiltro(int anillo, String nombreFiltro){
        System.out.println("asginarFiltro " + nombreFiltro + " al anillo " + anillo);
        boolean existe = false;
        int indice = 0;
        for(Anillo i : anillos){
            indice++;
            if(i.numeroAnillo == anillo) existe = true;
        }
        if(!existe) return false;
        if(!anillos.get(indice).filtroAsignado.equals(nombreFiltro)){
            anillos.get(indice).filtroAsignado = nombreFiltro;
        }
        anillos.get(indice).aplicado = false;
        return true;            
    }
    
    private void escuchaAnillos(){
        System.err.println("escuchaAnillos");
        try{
            ServerSocket conexionAnillos = new ServerSocket(pAnillos);
            Socket datosAnillo;

            while( (datosAnillo = conexionAnillos.accept()) != null){
                System.err.println("escuchaAnillos nueva peticion");

                ObjectInputStream entradaAnillo = new ObjectInputStream(datosAnillo.getInputStream());
                
                int numeroAnillo = (int) entradaAnillo.readObject();
                String filtroAnillo = (String) entradaAnillo.readObject();
                
                ObjectOutputStream salidaAnillo = new ObjectOutputStream(datosAnillo.getOutputStream());
                boolean anilloExiste = false;
                for(Anillo i : anillos){
                    if(i.numeroAnillo == numeroAnillo){
                        anilloExiste = true;
                        if(i.filtroAsignado.equals(filtroAnillo)){
                            salidaAnillo.writeObject("ok");;
                            salidaAnillo.close(); //rompera? rompera!, pero asi no se ropme cuando anilloB rompe conexion
                        }else{
                            EnviaCodigo e = this.new EnviaCodigo(this, i, salidaAnillo);
                            Thread hilo = new Thread(e);
                            hilo.start();
                        }
                        continue;
                    }
                }

                if(!anilloExiste)
                    System.out.println("Admin no tiene constancia del anillo " + numeroAnillo);
            }
        }catch(Exception e){e.printStackTrace(System.err);}
    }



    public static void main(String args[]){
        AdminFiltrosB aB = new AdminFiltrosB(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]), args[3]);
        AtiendeAnillos atiende = aB.new AtiendeAnillos(aB);
        Thread hilo = new Thread(atiende);
        hilo.start();

        aB.dialogo();
    }




    private class AtiendeAnillos implements Runnable{
        AdminFiltrosB admin;

        AtiendeAnillos(AdminFiltrosB admin){
            this.admin = admin;
        }

        @Override
        public void run(){
            admin.escuchaAnillos();
        }
    }
    private class EnviaCodigo implements Runnable{
        AdminFiltrosB admin;
        Anillo anillo;
        ObjectOutputStream out;

        EnviaCodigo(AdminFiltrosB admin, Anillo anillo, ObjectOutputStream out){
            this.admin = admin;
            this.anillo = anillo;
            this.out = out;
        }

        @Override
        public void run(){
            try{
                //envia codigo a anillo
                out.writeObject(anillo.filtroAsignado);
                System.err.println("cargando codigo para anillo " + anillo.numeroAnillo);
                File f = new File(admin.pathLocal + anillo.filtroAsignado);
                List<String> codigo = new ArrayList<String>();
                Scanner scan = new Scanner(f);
                while(scan.hasNextLine()){
                    codigo.add(scan.nextLine());
                }
                out.writeObject(codigo);
                
                List<String> filtrosCorriendo = new ArrayList<String>();
                for(Anillo i :admin.anillos){
                    //guarda en memoria 
                    if(i.numeroAnillo == anillo.numeroAnillo){
                        i.aplicado = true;
                        continue;
                    }
                    if(i.aplicado) filtrosCorriendo.add(i.filtroAsignado);
                }

                //notifica central
                Socket datosCentral = new Socket(admin.dCentral, admin.pCentral);
                ObjectOutputStream salidaCentral = new ObjectOutputStream(datosCentral.getOutputStream());
                salidaCentral.writeObject(filtrosCorriendo);
            }catch(Exception e){e.printStackTrace(System.err);}
        }
    }
}
//crear y borrar filtros, asignar a anillos
//primero tener que cambiar filtros de todos anillos, antes de poder borrar