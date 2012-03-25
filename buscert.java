import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;
//import nanoxml.*;

public class buscert extends java.rmi.server.UnicastRemoteObject implements OperBus{

    public buscert() throws java.rmi.RemoteException{
        super();
    }

    public List<String> OperPrueba(String asd){

        List<String> lc = Collections.synchronizedList(new ArrayList<String>());

        lc.add(asd);

        return lc;
    }

    public static void conectar(int puertoclientes){

        try{
            java.rmi.registry.LocateRegistry.createRegistry(puertoclientes);
        }catch (RemoteException ex){
            System.err.println("Error creando el registry, el puerto ya esta en uso.");
            System.exit(-1);
        }

        OperBus operbus = null;
        try{
            operbus = new buscert();
            if(operbus!=null)
                System.out.println("no es null");
            else
                System.out.println("si es que ladilla");
        }catch(RemoteException ex){
            System.out.println("Error en la creacion del objeto remoto.");
        }

        String dirbus = "rmi://localhost:"+puertoclientes+"/buscert";
        System.out.println(dirbus);
        try{
            Naming.rebind(dirbus,operbus);
        }catch(Exception e){
            System.err.println("Error binding. "+e);
            System.exit(-1);
        }

    }

    public static void main(String args[]){
        int port = 4000;
        ServerSocket busqsocket = null;

        if(args.length==2 && args[0].equals("-p")){
            try{
                port = Integer.parseInt(args[1]);
            }catch(NumberFormatException n){
                System.err.println("Error en las opciones: "+n.getMessage());
                System.exit(1);
            }
        }else if(args.length==1 && args[0].equals("-help")){
            System.out.println("Uso: java buscert [-p <puerto>]");
            System.out.println("\t-p <puerto>: numero del puerto de escucha de buscert, por defecto, 4000.");
            System.exit(0);
        }else if(args.length==0){
            
        }else{
            System.out.println("Error en las opciones. Para ver una ayuda ejecute java buscert -help");
            System.exit(1);
        }

        boolean listening=true;
        List<String []> servidores = Collections.synchronizedList(new ArrayList<String []>()); 
        List<String []> clientes = Collections.synchronizedList(new ArrayList<String []>()); 
        List<String []> certificados = Collections.synchronizedList(new ArrayList<String []>());

        conectar(port);

        /*Thread est = new Thread(new busThread(servidores,clientes,certificados,1));
        Thread check = new Thread(new busThread(servidores,0));
        est.start(); 
        check.start(); 

        while(listening){

            //Abrimos Puerto para conexion con clicert y los servcert
            try{
                busqsocket = new ServerSocket(port);

            }catch (IOException e){
                System.err.println("No se puede escuchar en el puerto: " + port);
                System.exit(1);
            }

            try{
                //Certf.log("recibi un cliente");
                Thread t = new Thread(new busThread(busqsocket.accept(),servidores,clientes,certificados,2));
                t.start();
            }catch(IOException e){
                System.err.println("Error aceptando la conexion");
                continue;
            }
            
            try{
                busqsocket.close();
            }catch(IOException e){
                System.err.println("Error cerrando la conexion");
                continue;
            }
        }*/

    }
} 
