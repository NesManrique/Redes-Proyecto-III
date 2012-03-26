//import nanoxml.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;

public class clicert{

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
                //Certf.log(lc);
                for(String r : lc){
                    Certf.xml2cert(dir,r);
                }
            }
        }

        System.exit(0);

/*
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        String userInput="";
        String query="";
        String fromBus="";
        String certific="";
        Certf cc = new Certf();
        int ln=0;

        while((userInput = stdIn.readLine())!=null){
            if(userInput.equals("")){
                if(ln==0){
                    ln=ln+1;
                    System.out.println("Si inserta otra linea vacía el programa clicert terminara.");
                }else if(ln==1){
                    out.println("SALIDA");
                    //System.out.println("Clicer terminara ahora.");
                    System.exit(0);
                }
            }else{
                ln=0;

                //Transformar la entrada en XML
                query = cc.query2xml(userInput);
                //Certf.log("cliente"+query);

                if(query.equals("")){
                    Certf.log("Solicitud inválida. Inténtelo de nuevo.");
                    continue;
                }
            
                //Enviarla al buscert
                out.println("CLIENTE");
                out.println(query);
                out.println("ENDCLIENTE");
                
                //Recibir el Certificado
                while(!(fromBus=in.readLine()).equals("ENDBUSCADOR")){ 
                    if(fromBus.equals("BUSCADOR")){
                        fromBus = in.readLine();
                        certific = certific+fromBus;
                    }else if(fromBus.equals("ENDCERT")){
                        //Imprimir el certificado
                        Certf.log("Certificado guardado: "+ cc.xmlName(certific) + "\n");
                        //Guardar el certificado
                        cc.xml2cert(dir,certific);
                        certific="";
                    }else if(fromBus.equals("NOCERT")){
                        Certf.log("No se encontraron certificados con la búsqueda especificada");
                    }else{
                        fromBus = "";
                    }
                }

            }
        }
        stdIn.close();*/
    }
}