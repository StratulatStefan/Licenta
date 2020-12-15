package model;

/**
 * Clasa folosita in comunicarea de date cu nodurile adiacente. Inglobeaza toate
 * datele caracteristice header-ului fisierului ce se va trimite. Prin header intelegem
 * token-ul, numele fisierului, dimensiunea fisierului si alte date caracteristice utilizatorului.
 */
public class FileHeader {
    /**
     * Token-ul ce defineste chain-ul de comunicare.
     */
    private String token;
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * Dimensiunea fisierului. Aparent, nu avem nevoie de ea.
     */
    private long filesize;

    /**
     * Constructor vid pentru clasa.
     */
    public FileHeader(){}

    /** Constructorul clasei, care incearca sa parseze mesajul primis si
     * sa il transforme in termenii individuali necesari.
     * @param message Header-ul, in formatul primit de la nodul adiacent.
     * @throws Exception Exceptia este aruncata de metoda de parsare si sugereaza
     * o eroare de conversie
     */
    public FileHeader(String message) throws Exception{
        this.ParseMessage(message);
        System.out.println(this);
    }

    /**
     * Getter pentru token
     * @return token-ul.
     */
    public String getToken(){
        return this.token;
    }

    /**
     * Setter pentru token
     * @param token token-ul
     */
    public void setToken(String token){
        this.token = token;
    }

    /**
     * Getter pentru numele fisierului
     * @return numele fisierului
     */
    public String getFilename(){
        return this.filename;
    }

    /**
     * Setter pentru numele fisierului.
     * Nu pastreaza calea absoluta, ci doar numele efectiv al fisierului.
     * @param filename numele fisierului (poate contine si calea)
     */
    public void setFilename(String filename){
        String[] filePathItems = filename.split("\\/");
        this.filename = filePathItems[filePathItems.length - 1];
    }

    /**
     * Getter pentru dimensiunea fisierului
     * @return dimensiunea fisierului
     */
    public long getFilesize(){
        return this.filesize;
    }

    /**
     * Setter pentru dimensiunea fisierului. Se face parsare de la String la long,
     * deci se poate genera o exceptie;
     * @param filesize
     */
    public void setFilesize(String filesize) throws Exception{
        this.filesize = Long.parseLong(filesize);
    }

    /**
     * Setter pentru dimensiunea fisierului, ce primeste direct dimensiunea in formatul necesar
     * @param filesize dimensiunea fisierului in format long
     */
    public void setFilesize(long filesize){
        this.filesize = filesize;
    }

    /**
     * Functia care realizeaza parsarea mesajului; se extrag elementele individuale
     * si se asociaza campului corespunzator
     * @param message Header-ul, in formatul primit de la nodul adiacent.
     * @throws Exception Exceptia este generata atunci cand header-ul nu este valid,
     * adica nu contine delimitatorul necesar "|", sau in cazul unei erori de converie
     * (de ex, determinarea dimensiunii [int] din string)
     */
    private void ParseMessage(String message) throws Exception{
        if(!message.contains("|")){
            throw new Exception("Header not found yet");
        }
        message = message.split("\\|")[0];
        message = message.replace("<","").replace(" ","");
        String[] items = message.split(">");
        for(String item : items){
            String[] entry = item.split(":", 2);
            String key = entry[0].toLowerCase();
            String value = entry[1];
            if(key.equals("token"))
                this.setToken(value);
            if(key.equals("filename"))
                this.setFilename(value);
            if(key.equals("size"))
                this.setFilesize(value);
        }
    }

    /**
     * Functia de transformare a obiectului FileHeader in formatul necesar afisarii la consola.
     * Mai mult, va fi folosit si la obtinerea string-ului ce se va trimite catre celalalt nod.
     * @return formatul text al obiectului
     */
    @Override
    public String toString() {
        return String.format("<token : %s><filename : %s><size : %d>|", this.token, this.filename, this.filesize);
    }
}
