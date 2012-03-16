import nanoxml.*; 
import java.io.*;
import java.util.*;

public class Certf{

    String Fname;
    ArrayList<String> Fields;

    public Certf(){};

    public Certf(String fname, ArrayList<String> fields){
        Fname=fname;
        Fields=fields;
    }

    public void printcert(){
        log(this.Fname);

        ListIterator<String> it = this.Fields.listIterator();
        while(it.hasNext()){
            log(it.next());
        }

    }

    public void printList(ArrayList<Certf> l){
        ListIterator<Certf> it = l.listIterator();

        while(it.hasNext()){
            it.next().printcert();
            log("\n");
        }
    }

    public Certf descifrado(String fname){
        String[] s = new String[6];
        ArrayList<String> ss = new ArrayList<String>();

        try{
            String[] command = {"openssl x509 -noout -in " + fname + " -issuer",
                "openssl x509 -noout -in " + fname + " -subject",
                "openssl x509 -noout -in " + fname + " -hash",
                "openssl x509 -noout -in " + fname + " -fingerprint",
                "openssl x509 -noout -in " + fname + " -dates",
            } ;

            for(int i=0; i<command.length; i++){
                Process child = Runtime.getRuntime().exec(command[i]);

                BufferedReader stdInput = new BufferedReader(new 
                        InputStreamReader(child.getInputStream()));

                if(i==4){
                    if((s[i]=stdInput.readLine())==null){
                        return null;
                    }else{
                        s[i]=s[i].replaceFirst("[\\w ]+=\\s*","");
                    }

                    if((s[i+1]=stdInput.readLine())==null){
                        return null;
                    }else{
                        s[i+1]=s[i+1].replaceFirst("[\\w ]+=\\s*","");
                    }
                }else{
                    if((s[i]=stdInput.readLine())==null){
                        return null;
                    }else{
                        s[i]=s[i].replaceFirst("[\\w ]+=\\s*","");
                    }
                }
                
                stdInput.close();
            }

            ss.add(s[0]);
            ss.add(s[1]);
            ss.add(s[4]);
            ss.add(s[5]);
            ss.add(s[2]);
            ss.add(s[3]);

            Certf c = new Certf(fname,ss);

            return c;

        } catch(IOException e){
            System.out.println("Error");
            return null;
        }

    }


    public void abrirdir(File dir,List<Certf> L) throws NullPointerException{
        Certf cer;
        File files[] = dir.listFiles();
        if(files == null){
            throw new NullPointerException();
        }
        for(int i = 0; i< files.length; i++ ){
            if(files[i].isFile()){
                if((cer=descifrado(files[i].getPath()))!=null){
                    L.add(cer);
                }else{
                    continue;
                }
            } else if(files[i].isDirectory()){
                abrirdir(files[i],L);
            }
        }
    }

    public static void log(Object aMessage){
        System.out.println(aMessage);
    }

    public String cert2xml(String filecert){
        String all = "";
        try{
            BufferedReader input = new BufferedReader(new FileReader(filecert));
            String line = null;
            while((line = input.readLine())!=null){
                all = all + line + "\n"; 
            }
            input.close();
        }catch(IOException ex){
            log("Error abriendo el archivo: "+filecert);
        }
        XMLElement certf = new XMLElement();
        XMLElement certname = new XMLElement();
        certf.setName("Certificate");
        //log("bla "+filecert.replaceAll("([\\w-]*/)+",""));
        certname.setName(filecert.replaceAll("([\\w-]*/)+",""));
        certname.setContent(all);
        certf.addChild(certname);
        all = certf.toString();
        if(all.contains("&#xa;")){
            all = all.replace("&#xa;","\n");
        }
        //log("certificate "+all);
        return all;
    }

    public void xml2cert(String dir, String certxml){
        String fname="";
        String content="";
        String aux="";
        int i=0;
        Vector<XMLElement> v = new Vector<XMLElement>();

        XMLElement certf = new XMLElement();
        //try{
            certf.parseString(certxml);
        //}catch(XMLParseException e){
            //log(e.getMessage());
        //}

        v = (Vector<XMLElement>)certf.getChildren();

        fname = ((XMLElement)v.get(0)).getName();
        content = ((XMLElement)v.get(0)).getContent();

        content = content.substring(27);

        for(i=0; i<content.length(); i=i+64){
            if(i+64<content.length())
                aux=aux+content.substring(i,i+64)+"\n";
            else
                break;
        }
                
        for(;content.charAt(i)!='-';i++){
            aux=aux+content.charAt(i);
        }

        aux="-----BEGIN CERTIFICATE-----\n"+aux+"\n-----END CERTIFICATE-----";

        //log(aux);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(dir+fname));
            out.write(aux);
            out.close();
        } catch (IOException e) {
            log("Problema Escribiendo el archivo: "+fname);
        }
    }

    public String xmlName(String certxml){
        Vector<XMLElement> v = new Vector<XMLElement>();

        XMLElement certf = new XMLElement();
        //try{
            certf.parseString(certxml);
        //}catch(XMLParseException e){
        //    log(e.getMessage());
        //}

        v = (Vector<XMLElement>)certf.getChildren();

        return ((XMLElement)v.get(0)).getName();

    }

    public boolean hasChildren(XMLElement x, String a){
        Vector<XMLElement> v = (Vector<XMLElement>)x.getChildren();
        if(v!=null){
            for(int i=0; i<v.size(); i++){
                if(a.equals(((XMLElement)v.get(i)).getName())){
                    return true;
                }
            }
        }
        
        return false;
    }

    public String query2xml(String query){
		String[] subs;
        if(query.trim().equals("")){
			return "";
		}else
			subs = query.trim().split("\\s+");

        XMLElement qy = new XMLElement();
        XMLElement iss = new XMLElement();
        XMLElement sub = new XMLElement();
        XMLElement vala = new XMLElement();
        XMLElement valb = new XMLElement();
        XMLElement hash = new XMLElement();
        XMLElement fing = new XMLElement();
        qy.setName("Query");
        iss.setName("Issuer");
        sub.setName("Subject");
        vala.setName("DatesA");
        valb.setName("DatesB");
        hash.setName("Hash");
        fing.setName("Fingerprint");

        for(int i=0; i<subs.length; i++){

            if(i+1<subs.length && subs[i+1].endsWith(":")) continue;
            if(i==subs.length-1 && subs[i].endsWith(":")) continue; 
 
            if(subs[i].equalsIgnoreCase("Issuer:")){
                if(iss.getContent()!=""){
                    iss.setContent((iss.getContent())+" "+subs[++i]);
                }else{
                    iss.setContent(subs[++i]);
                }
 
            }else if(subs[i].equalsIgnoreCase("Subject:")){
                if(sub.getContent()!=""){
                    sub.setContent((sub.getContent())+" "+subs[++i]);
                }else{
                    sub.setContent(subs[++i]);
                }
            }else if(subs[i].equalsIgnoreCase("Dates:")){
                i=i+1;
                int flag=0;
                //Mientras no termine con :
                while(i<subs.length && !subs[i].endsWith(":")){
                    //Si empieza con notBefore
                    if(subs[i].startsWith("notBefore=")){
                        //Si el nodo valb esta vacio
                        valb.setContent(subs[i++].substring(10));
                        flag=1;
                    }else if(subs[i].startsWith("notAfter=")){
                        //Si el nodo valb esta vacio
                        vala.setContent(subs[i++].substring(9));
                        flag=2;
                    }else{
                        if(flag==1){
                            valb.setContent((valb.getContent())+" "+subs[i++]);
                        }else if(flag==2){
                            vala.setContent((vala.getContent())+" "+subs[i++]);
                        }else{
                            if(valb.getContent()!=""){
                                valb.setContent((valb.getContent())+" "+subs[i]);
                            }else{
                                valb.setContent(subs[i]);
                            }

                            if(vala.getContent()!=""){
                                vala.setContent((vala.getContent())+" "+subs[i]);
                            }else{
                                vala.setContent(subs[i]);
                            }
                            i=i+1;
                        }
                    }
                }
                i=i-1;
            }else if(subs[i].equalsIgnoreCase("Hash:")){
                if(hash.getContent()!=""){
                    hash.setContent((hash.getContent())+" "+subs[++i]);
                }else{
                    hash.setContent(subs[++i]);
                }
            }else if(subs[i].equalsIgnoreCase("Fingerprint:")){
                if(fing.getContent()!=""){
                    fing.setContent((fing.getContent())+" "+subs[++i]);
                }else{
                    fing.setContent(subs[++i]);
                }
            }else{
                //log("No se de que campo es "+subs[i]);
            }
        }

        qy.addChild(iss);
        qy.addChild(sub);
        qy.addChild(valb);
        qy.addChild(vala);
        qy.addChild(hash);
        qy.addChild(fing);

        Vector<XMLElement> v = new Vector<XMLElement>();
        v = (Vector<XMLElement>)qy.getChildren();

        for(XMLElement e : v){
           if(!e.getContent().equals("")){
             String res = qy.toString();
             return res;
            }
        }

        return "";
    }

    public void xml2query(String xml, ArrayList<String[]> query){
        XMLElement q = new XMLElement();
        q.parseString(xml); //Transformo el query en un arbol
        Vector childs = q.getChildren(); //Saco los hijos de la raiz
        String content;
        String scont[];
        
        if(childs!=null){
            //Recorro los campos y guardo los valores de los campos
            //como un arraylist de arreglos de string
            for(int i=0; i<childs.size(); i++){
                content = ((XMLElement)childs.get(i)).getContent();
                //if(content!=""){
                    scont = content.split("\\s+");
                    query.add(scont);
                //}
            }
        }

        
    }

    public void searchCert(List<String[]> query, List<Certf> cert, 
                                List<String> find){
        String a[];
        Certf c;

        ListIterator<Certf> cer = cert.listIterator();
        ListIterator<String[]> str = query.listIterator();

        //Recorro la lista de certf en el servidor
        int k=0;
        while(cer.hasNext()){
            c = cer.next();
            int i=0;
            boolean aflag=false, vflag=false;
            //int aflag=0;
            //Recorro la lista de atributos
            while(str.hasNext()){
                a = str.next();
                //Recorro la lista de valores para cada atributo
                for(int j=0; j<a.length; j++){

                    if(a[j]=="") continue;

                    //log("cert "+k+" atributo "+i+" valor "+a[j]+" "+(c.Fields.get(i)).contains(a[j]));
                    if(c.Fields.get(i)!="" && !(c.Fields.get(i)).contains(a[j])){
                        vflag=true;
                        break;
                    }
                }

                if(vflag){
                    aflag=true;
                    vflag=false;
                    break;
                }
                i++;
            }

            if(aflag){
                aflag=false;
            }else{
                find.add(c.Fname);
            }
            
            str = query.listIterator();
            k++;
            i=0;
        }

    }

    /*public static void main(String[] args){

            xmlutils p = new xmlutils();
            String a = p.cert2xml("server1",args[0]);
            //p.log(a);
            p.xml2cert(a);

            File dir = new File(args[0]);
            Certf c = new Certf();
            ArrayList<Certf> ac = new ArrayList<Certf>();
            try{
                c.abrirdir(dir,ac);
            }catch(NullPointerException e){
                log("Error leyendo directorios");
            }
            
            c.printList(ac);
            //c.query2xml("Issuer: L=Cape Validity: notBefore=Feb 25 23:30:40");
            String asd = "";
            String asd1 = "    \t   \t\t";
            String asd2 = "   Issuer:   \tccs   Hash: 123456\t";
            String asd3 = "   Habia una \t\tVEz un ISsuer derp  ";
            String asd5 = "   Issuer: Dates: Hash:  ";
            String asd4 = "Issuer: ccs Dates: notAfter=Feb 27 Dates: notAfter=Feb 26 Issuer: sam";
            String algo = c.query2xml("Issuer: vln");
			if(algo==""){ 
				log("query vacio");
				System.exit(0);
			}
            String a[];
            //log(c.query2xml(asd));
            ArrayList<String[]> query = new ArrayList<String[]>();
            c.xml2query(algo,query);
            for(int i=0; i<query.size(); i++){
                a=query.get(i);
                log(a.length);
                for(int j=0; j<a.length; j++){
                    log(a[j]);
                }
            }

            ArrayList<String> encontrados = new ArrayList<String>();

            c.searchCert(query,ac,encontrados);
            
            if(encontrados.size()<=0) log("no hay certificados");
            for(int i=0; i<encontrados.size(); i++){
                asd=encontrados.get(i);
                log(asd);
            }
    }*/

}
