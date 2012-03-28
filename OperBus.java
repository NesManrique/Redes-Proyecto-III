import java.util.*;

/**
* @author Nestor Marique 07-41150
* @author Samuel Bartoli 07-40632
*/

public interface OperBus extends java.rmi.Remote{
 
     /**
    *  inscribe nuevos servidores
    *  @param servhost: direccion del nuevo servidor
    *  @param servport: puerto del nuevo servidor
    *
    */   
    public int signin(String servhost, int servport) throws java.rmi.RemoteException;
    /**
    *  Elimina un servidor que ya este inscrito
    *  @param servhost: direccion del nuevo servidor
    *  @param servport: puerto del nuevo servidor
    *  @throws RemoteException
    *
    */
    public void signout(String servhost, int servport) throws java.rmi.RemoteException;

     /**
    * Inscribe un nuevo cliente
    * @param host: direccion del nuevo cliente
    * @throws RemoteException
    */
    public int incli(String host) throws java.rmi.RemoteException;

    /**
    * Elimina un cliente que ya haya sido registrado
    * @param host: direccion del cliente a eliminar
    * @param id: identificador del cliente
    * @throws RemoteException
    */
    public void outcli(String host, int id) throws java.rmi.RemoteException;

    /**
    * busca un certificado del cliente ????????
    * @param query: solicitud con sus campos y valores 
    * @param id:  
    * @throws RemoteException
    */
    public ArrayList<String> searchCert(String query,int id) throws java.rmi.RemoteException; 

}
