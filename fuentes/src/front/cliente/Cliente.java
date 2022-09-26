//import FiltroApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.naming.NameNotFoundException;

import org.omg.CORBA.*;

import PeticionApp.Peticion;


public class Cliente {
    private String correo;
    private String hash;
    private String nombreImg;
    private String filtrosCSV = "";

    private NamingContextExt mNcRef;

    Cliente(){}

    public boolean empezarConexion(String argumentos[]){
        try{
            ORB orb = ORB.init(argumentos, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            mNcRef = NamingContextExtHelper.narrow(objRef);

        }catch (Exception e) {
            e.printStackTrace(System.out);
            return false;
        }
        return true;
    }


    private boolean peticion(){
        boolean todoBien = false;
        try{
            Peticion peticionInst = PeticionApp.PeticionHelper.narrow(mNcRef.resolve_str("Peticion"));
            System.out.println("Enviando credenciales, imagen y filtro(s)");
            todoBien = peticionInst.getFiltrado(correo, hash, nombreImg, filtrosCSV);
        }catch(Exception e){e.printStackTrace(System.out);}
        return todoBien;
    }

    public void dialogo(){
        boolean fExists = false;
        do{
            System.out.print("Nombre de la imagen: ");
            nombreImg = System.console().readLine();
            File f = new File(nombreImg);
            fExists = f.exists();
        }while(!fExists);

        do{
            System.out.print("Correo: ");
            correo = System.console().readLine();
            System.out.print("Contraseña: ");
            hash = passwd4hash(System.console().readLine());
            System.out.print("Filtro(s) que aplicar, separados por comas y sin espacios: ");
            filtrosCSV = System.console().readLine();
        }while(!peticion());

        System.out.println("Imagen filtrada");
    }
    private String passwd4hash(String passwd){
        System.out.println("Convirtiendo contraseña");
        String hash = passwd;
        try {
            MessageDigest md=null;
			md = MessageDigest.getInstance("MD5");
            md.update(passwd.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            hash = String.format("%032x", new BigInteger(1, digest));
            System.out.println(hash);
            System.out.println("Contraseña convertida sin problemas");
		}catch(Exception e){e.printStackTrace(System.out);}
        return hash;
    }

    public static void main(String args[]) {
        //pasar por args los detalles de orb
        Cliente c = new Cliente();
        c.empezarConexion(args);
        c.dialogo();
    }
}
