package servidor.utils;
import java.io.Serializable;

public class Tarea implements Serializable {
    public int nodoEncargado;
    public String encargo;

    public Tarea(int nodoEncargado, String encargo){
        this.nodoEncargado = nodoEncargado;
        this.encargo = encargo;
    }
}
