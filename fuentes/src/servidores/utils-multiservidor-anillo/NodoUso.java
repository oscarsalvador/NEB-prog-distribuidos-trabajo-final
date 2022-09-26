package servidor.utils;

import java.io.Serializable;

import java.io.Serializable;

public class NodoUso implements Serializable {
    public long usoNodo;
    public int numeroNodo;

    public NodoUso(long usoNodo, int numeroNodo){
        this.usoNodo = usoNodo;
        this.numeroNodo = numeroNodo;
    }
}
