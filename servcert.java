import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
//import nanoxml.*;

/**
 * @author Nestor Manrique
 * @author Samuel Bartoli
 */

public class servcert extends java.rmi.server.UnicastRemoteObject implements OperServ{

    static OperBus operbus;
    static String host;
    static int port;
    private static List<Certf> lc = Collections.synchronizedList(new ArrayList<Certf>());
    
    /**
      * Constructor vacio 
      *
      */
    public servcert() throws java.rmi.RemoteException{
        super();
    }
   
    /**
    * Constructor de la clase servcert recibe
    * @param opbus: 
    * @param servhost: direccion de servcert.
    * @param servport: puerto de escucha de servcert.
    * @throws RemoteException
    */  
    public servcert(OperBus opbus, String servhost, int servport) throws java.rmi.RemoteException{
        super();
        operbus=opbus;
        host = servhost;
        port = servport;
    }


    /**
    * Parser para interpretar correctamente la entrada por consola
    * @param dir: directorio donde se encuentran los certificados
    * @param host: direccion del servidor
    * @param busport: puerto de escucha de buscert
    * @param servport: puerto de escucha del servidor
    */
    static int parser(String args[], StringBuilder dir, StringBuilder host, int busport, StringBuilder servport){
        int i;
        if(args.length==1 && args[0].equals("--help")){
            System.out.println("Uso: java servcert [-p <puerto>] -d <directorio que almacena los certificados> -h <nombre de dominio de buscert>");
            System.out.println("\t\t[-b <puerto de escucha de buscCert>]\n");
            System.out.println("\t<puerto>: puerto de escucha de peticiones de sercert. 5000 por default.");
            System.out.println("\t<directorio que almacena los certificados>: contiene el camino absoluto o relativo \n\t\t del directorio donde estarán los certificados del repositorio.");
            System.out.println("\t<nombre de dominio de buscert>: ip o nombre de dominio de buscert");
            System.out.println("\t<puerto de escucha de buscert>: puerto de escucha del buscador. 4000 por default.");

            return 0;
        }else if(args.length % 2 == 0 && args.length >3){

            for(i=0; i<args.length; i++){

                if(args[i+1].charAt(0)=='-'){
                    System.err.print("Error en las opciones: Falta valor de algunos argumentos. ");
                    return -1;
                }else if(args[i].equals("-d")){
                    dir.append(args[++i]);
                }else if(args[i].equals("-h")){
                    host.append(args[++i]);
                }else if(args[i].equals("-b")){
                    try{
                        busport = Integer.parseInt(args[++i]);
                    }catch(NumberFormatException n){
                        System.err.print("Error en las opciones: "+n.getMessage()+" ");
                        return -1;
                    }
                }else if(args[i].equals("-p")){
                    try{
                        servport.append(Integer.parseInt(args[++i]));
                    }catch(NumberFormatException n){
                        System.err.print("Error en las opciones: "+n.getMessage()+" ");
                        return -1;
                    }
                }else{
                    System.err.print("Error en las opciones: Opcion invalida. ");
                    return -1;
                }
            }

            return busport;

        }else{
            System.err.print("Error en las opciones: Faltan argumentos. ");
            return -1;
        }

    }

    /**
    * Establece conexion con buscert
    * @param bushost: direccion de buscert
    * @param busport: puerto de escucha de buscert
    * @param puertoclientes: puerto de escucha de los clientes
    */
    public static void conectar(String bushost, int busport, int puertoclientes){

        //Creacion del objeto remoto del buscador para inscribir el servidor
        String direccionBus = "rmi://"+bushost+":"+busport+"/buscert";
        System.out.println("Servidor registrandose a: "+direccionBus);

        OperBus opbus = null;

        try{
            opbus = (OperBus)Naming.lookup(direccionBus);
        }catch(MalformedURLException murle) {
            System.out.println();
            System.out.println(
              "MalformedURLException");
            System.out.println(murle);
        }
        catch(RemoteException re) {
            System.out.println();
            System.out.println(
                        "RemoteException");
            System.out.println(re);
        }
        catch(NotBoundException nbe) {
            System.out.println();
            System.out.println(
                       "NotBoundException");
            System.out.println(nbe);
        }

        String servhost="";
        try{
            //Sacando el servidor local
            InetAddress addr = InetAddress.getLocalHost();
            servhost = addr.getHostName();
        }catch(java.net.UnknownHostException e){
            System.err.println("Error en el host local. "+e.getMessage());
        }

        //Creacion del objeto remoto y registro para los clientes
        try{
            java.rmi.registry.LocateRegistry.createRegistry(puertoclientes);
        }catch (RemoteException ex){
            System.err.println("Error creando el registry, el puerto ya esta en uso.");
            System.exit(-1);
        }

        OperServ operserv = null;
        try{
            operserv = new servcert(opbus,servhost,puertoclientes);
        }catch(RemoteException ex){
            System.out.println("Error en la creacion del objeto remoto.");
        }

        String dirserv = "rmi://localhost:"+puertoclientes+"/servcert";
        System.out.println("Direccion del servidor: "+dirserv);
        try{
            Naming.rebind(dirserv,operserv);
        }catch(Exception e){
            System.err.println("Error binding. "+e);
            System.exit(-1);
        }

        //Registrandose al buscador
        try{
            //Inscribiendo el servidor
            int resp = opbus.signin(servhost,puertoclientes);
        }catch(RemoteException r){
            System.err.println("Error signing up: "+r.getMessage());
            System.exit(-1);
        }

    }

    /**
    * Busca el certificado  de acuerdo a los parametros especificados con sus
    * valores
    * @param query: parametros que determinan la busqueda del certificado
    * @throws RemoteException
    */
    public ArrayList<String> searchCert(String query) throws java.rmi.RemoteException{

        ArrayList<String []> q = new ArrayList<String[]>();
        ArrayList<String> encontrados = new ArrayList<String>();
        Certf.xml2query(query,q);
        Certf.searchCert(q,lc,encontrados);
        int i;
        for(i=0; i<encontrados.size(); i++){
            encontrados.set(i,Certf.cert2xml(encontrados.get(i)));
        }

        return encontrados;
    }
 
    public static void main(String args[]){
        int servport = 5000;
        int busport = 4000;
        String dir = null;
        String bushost = null;
        StringBuilder d = new StringBuilder();
        StringBuilder host = new StringBuilder();
        StringBuilder serv = new StringBuilder();

        busport = parser(args,d,host,busport,serv);

        if(busport > 0){
            dir = d.toString();
            bushost = host.toString();
            if(!serv.toString().equals(""))
                servport = Integer.parseInt(serv.toString());
        }else if(busport == 0){
            System.exit(0);
        }else{
            System.out.println("Para ver una ayuda ejecute java clicert -help");
            System.exit(1);
        }

        if(dir.charAt(dir.length()-1)!='/') dir = dir+"/";
        System.out.println("dir dond guardar los certificados "+dir
                            +"\nip o nombre del buscador "+bushost
                            +"\npuerto de escucha del buscador "+busport
                            +"\npuerto de escucha del servidor "+servport+"\n");

        //LLeno la lista de certificados
        Certf c = new Certf();
        File f = new File(dir);
        c.abrirdir(f,lc);
        //Certf.printList(lc);

        //Inscribo el servidor en el buscador
        conectar(bushost, busport, servport);

        //Manejo de Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    //Sacando el servidor local
                    InetAddress addr = InetAddress.getLocalHost();
                    String servhost = addr.getHostName();
                    //Logging out
                    operbus.signout(servhost,port);
                }catch (Throwable t) {
                    System.out.println("Error reportando el cierre del "
                    + "programa.");
                }
            }
        });

        boolean listening = true;
        BufferedReader prompt = null;
        String comando = "";

        while(listening){
            System.out.print("servcert>: ");
            prompt = new BufferedReader(new InputStreamReader(System.in));
            try{
                comando = prompt.readLine();
            }catch(Exception e){
                System.err.println("Error leyendo el comando. Intente de nuevo.");
            }

            if(comando.equals("quit")){
                listening = false;
                try{
                    operbus.signout(host.toString(),port);
                }catch(RemoteException r){
                    System.err.println("Error cerrando el servidor.");
                }
            }else if(comando.equals("list")){
                Certf.printList(lc);
            }
        }

        System.exit(0);
    }
}
