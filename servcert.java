import java.util.*;
import java.io.*;
import java.net.*;
//import nanoxml.*;

public class servcert{

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
        }else if(args.length % 2 == 0 && args.length >5){

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

/*
        Socket busqsocket = null;
        ServerSocket listsocket = null;
        PrintWriter out = null;
        BufferedReader in = null; 
        String inputLine, outputLine;
        Certf p = new Certf();

        //LLeno la lista de certificados
        List<Certf> lc = Collections.synchronizedList(new ArrayList<Certf>());
        Certf c = new Certf();
        File f = new File(dir);

        c.abrirdir(f,lc);

        //c.printList(lc);
        
        try{
            //Inscribo el servidor en el buscador
            busqsocket = new Socket(bushost, busport);
            out = new PrintWriter(busqsocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(busqsocket.getInputStream()));
            out.println("SERVER");
            out.println("SIGNIN");
            out.println(servport+"");
            out.close();
            in.close();
            busqsocket.close();
        }catch(UnknownHostException e){
            System.err.println("Error al conctarse al host: "+bushost);
            System.exit(1);
        }catch(IOException e){
            System.err.println("Couldn't get I/O for the connection to: "+bushost);
            System.exit(1);
        }

        while(true){

            //Abro un puerto para escuchar las peticiones del buscador
            try{
                listsocket = new ServerSocket(servport);
            }catch (IOException e){
                System.err.println("No se puede escuchar en el puerto: " + servport);
                System.exit(1);
            }

            try{
                //Certf.log("recibi un cliente");
                Thread t = new Thread(new servThread(listsocket.accept(),lc));
                t.start();
            }catch(IOException e){
                System.err.println("Error aceptando la conexion");
                continue;
            }
            
            try{
                listsocket.close();
            }catch(IOException e){
                System.err.println("Error cerrando la conexion");
                continue;
            }
        }*/
    }
}
