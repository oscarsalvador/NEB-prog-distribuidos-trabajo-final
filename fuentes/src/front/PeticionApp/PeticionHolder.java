package PeticionApp;

/**
* PeticionApp/PeticionHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Peticion.idl
* Saturday, May 8, 2021 7:10:24 PM CEST
*/

public final class PeticionHolder implements org.omg.CORBA.portable.Streamable
{
  public PeticionApp.Peticion value = null;

  public PeticionHolder ()
  {
  }

  public PeticionHolder (PeticionApp.Peticion initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = PeticionApp.PeticionHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    PeticionApp.PeticionHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return PeticionApp.PeticionHelper.type ();
  }

}