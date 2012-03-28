import nanoxml.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;

/**
 * @author Nestor Manrique
 * @author Samuel Bartoli
 */

public class clicert{

    /**
     * Realiza el parseo de la entrada proveniente de la consola
     * @param dir: directorio donde guarda los certificados.
     * @param bushost: direccion de buscert.
     * @param busport: puerto por donde escucha buscert.
     */

    static int parser(String args[], StringBuilder dir, StringBuilder bushost, int busport){
        int i;
        if(args.length==1 && args[0].equals("--help")){
            System.out.println("Uso: java clicert -d <directorio de certificados descargados> -h <nombre de dominio, o direccion de ip de buscert>");
            System.out.println("\t\t[-p <puerto de buscCert>]\n");
            System.out.println("\t<directorio de certificados descargados>: contiene el camino absoluto o relativo \n\t\t del directorio donde estarán los certificados digitales descargados por el cliente.");
            System.out.println("\t<nombre de dominio, o direccion de ip de buscert>: maquina donde se encuentra corriendo el buscador");
            System.out.println("\t<puerto de busCert>: puerto de escucha del buscador busCert. 4000 por default.");

            return 0;
        }else if(args.length % 2 == 0 && args.length >3){

            for(i=0; i<args.length; i++){

                if(args[i+1].charAt(0)=='-'){
                    System.err.print("Error en las opciones: Falta el valor algunos argumentos. ");
                    return -1;
                }else if(args[i].equals("-d")){
                    dir.append(args[++i]);
                }else if(args[i].equals("-h")){
                    bushost.append(args[++i]);
                }else if(args[i].equals("-p")){
                    try{
                        busport = Integer.parseInt(args[++i]);
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
     * Lista el contenido de un directorio.
     * @param dir: directorio a saber su contenido.
     * @throws NullPointerException.
     */

    public static void list(File dir) throws NullPointerException{
        File files[] = dir.listFiles();
        if(files == null){
            throw new NullPointerException();
        }
        for(int i = 0; i< files.length; i++ ){
            if(files[i].isFile()){
                Certf.log(files[i].getPath());
            } else if(files[i].isDirectory()){
                list(files[i]);
            }
        }
    }
    
    /**
     * @param args: argumentos recibidos por la linea de comados
     */
    public static void main(String args[]) throws IOException{

        String dir = null;
        String bushost = null;
        int busport = 4000;
        int id=-1;
        StringBuilder d = new StringBuilder();
        StringBuilder host = new StringBuilder();

        busport = parser(args,d,host,busport);

        if(busport>0){
            dir = d.toString();
            bushost = host.toString();
        }else if(busport==0){
            System.exit(0);
        }else{
            System.out.println("Para ver una ayuda ejecute java clicert -help");
            System.exit(1);
        }

        if(dir.charAt(dir.length()-1)!='/') dir = dir+"/";
        System.out.println("dir dond guardar los certificados "+dir
                            +"\nip o nombre del buscador "+bushost
                            +"\npuerto de escucha del buscador "+busport+"\n");

        String direccionBus = "rmi://"+bushost+":"+busport+"/buscert";

        System.out.println(direccionBus);

        OperBus opbus = null;

        try{
            opbus = (OperBus) Naming.lookup(direccionBus);
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

        String chost="";
        try{
            //Sacando el servidor local
            InetAddress addr = InetAddress.getLocalHost();
            chost = addr.getHostName().toString();
        }catch(java.net.UnknownHostException e){
            System.err.println("Error en el host local. "+e.getMessage());
        }

        try{
            id=opbus.incli(chost);
        }catch(Exception e){
            System.err.println("Error al inscribirse al buscador"+e.getMessage());
        }

        boolean listening = true;
        int ln=0;
        BufferedReader prompt = null;
        String comando = "";
        String query = "";

        while(listening){
            System.out.print("clicert>: ");
            prompt = new BufferedReader(new InputStreamReader(System.in));
            try{
                comando = prompt.readLine();
            }catch(Exception e){
                System.err.println("Error leyendo el comando. Intente de nuevo.");
            }

            if(comando.equals("")){
               if(ln==0){
                    ln=ln+1;
                    System.out.println("Si inserta otra linea vacía el programa clicert terminara.");
                }else if(ln==1){
                    opbus.outcli(chost,id);
                    System.exit(0);
                }
            }else if(comando.equals("list")){
                ln=0;
                try{
                    File f = new File(dir);
                    list(f);
                }catch(NullPointerException n){
                    System.err.println("Error listando certificados "+n.getMessage());
                }
            }else{
                ln=0;
                query = Certf.query2xml(comando);
                if(query.equals("")){
                    Certf.log("Solicitud inválida. Inténtelo de nuevo.");
                    continue;
                }
                ArrayList<String> lc = opbus.searchCert(query,id);
                
                System.out.println("Encontrados: ");
                for(String r : lc){
                    Certf.xml2cert(dir,r);
                }
            }
        }

        System.exit(0);

    }
}
