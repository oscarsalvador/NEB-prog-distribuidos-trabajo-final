import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.LinkedList;

import com.ctc.wstx.shaded.msv_core.datatype.xsd.IntegerValueType;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import PeticionApp.*;


class PeticionImpl extends PeticionPOA {
    public int puertoAuth;
    public String direccAuth;
    public int puertoCentral;
    public String direccCentral;

    private ORB orb;
    
    private SyncAuth sync = new SyncAuth();
    private long correccion = sync.getCorreccion();

    public void setORB(ORB orb_val) {
        orb = orb_val; 
    }

    public boolean getFiltrado(String correo, String hash, String nombreImg, String filtrosCSV){
        System.out.println((System.nanoTime() - correccion) + " nueva peticion getFiltrado, usuario: " + correo);
        /*
        AtiendeCliente atiende = this.new AtiendeCliente();
        Thread hilo = new Thread(atiende);
        hilo.start();
        */
        try{
            System.out.println("Peticion de "+correo+" imagen "+nombreImg+" filtros "+filtrosCSV);
            List<String> credenciales = new ArrayList<String>();
            credenciales.add(correo);
            credenciales.add(hash);
            Socket datosAuth = new Socket(direccAuth, puertoAuth);
            ObjectOutputStream salidaAuth = new ObjectOutputStream(datosAuth.getOutputStream());
            salidaAuth.writeObject(credenciales);
    
            ObjectInputStream entradaAuth = new ObjectInputStream(datosAuth.getInputStream());
            boolean userok = (boolean) entradaAuth.readObject();
            if(!userok) return false;

            System.out.println("Propagar peticion a central");
            //List<String> filtros = Arrays.asList(filtrosCSV.split(","));

            Socket datosCentral = new Socket(direccCentral, puertoCentral);
            ObjectOutputStream salidaCentral = new ObjectOutputStream(datosCentral.getOutputStream());
            salidaCentral.writeObject(nombreImg);
            List<String> filtros = new LinkedList<String>(Arrays.asList(filtrosCSV.split(",")));
            salidaCentral.writeObject(filtros);

            ObjectInputStream entradaCentral = new ObjectInputStream(datosCentral.getInputStream());
            String resultado= (String) entradaCentral.readObject();
            return resultado.startsWith("true");
        }catch(Exception e){e.printStackTrace(System.out);}
        return false;
    }

    /*
    private class AtiendeCliente implements Runnable{
        AtiendeAdmin(){}

        @Override
        public void run(){

            return true;
        }
    }
    */
}


public class Proxy {
    public static void main(String args[]){
        SyncAuth s = new SyncAuth(Integer.valueOf(args[2]), args[3]);

        try{
            ORB orb = ORB.init(args, null);
    
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
    
            PeticionImpl peticionImpl = new PeticionImpl();
            peticionImpl.setORB(orb);
            //conexion a autenticacion
            peticionImpl.puertoAuth = Integer.valueOf(args[0]);
            peticionImpl.direccAuth = args[1];
            peticionImpl.puertoCentral = Integer.valueOf(args[4]);
            peticionImpl.direccCentral = args[5];
    
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(peticionImpl);
            Peticion href = PeticionHelper.narrow(ref);
                
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent path[] = ncRef.to_name("Peticion");
            ncRef.rebind(path, href);
    
            System.out.println("Servidor corriendo");
    
            orb.run();
        }catch(Exception e){e.printStackTrace(System.out);}

        System.out.println("Proxy cerrando ...");
    }
}
