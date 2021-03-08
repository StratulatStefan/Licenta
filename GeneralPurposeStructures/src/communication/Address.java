package communication;


public class Address {
    /**
     * Adresa IP
     */
    private String ipAddress;

    /**
     * Portul
     */
    private int port;

    /**
     * Constructorul care, pe baza adresei ip si a portului specificate, va crea obiectul adresa.
     * Crearea este conditionata de formatul corect al acestor date.
     * @param ipAddress Adresa IP
     * @param port Portul
     * @throws Exception genarata daca ip-ul sau port-ul nu respecta formatul dorit.
     */
    public Address(String ipAddress, int port) throws Exception {
        if(this.validateIpAddress(ipAddress))
            this.ipAddress = ipAddress;
        if(this.validatePort(port))
            this.port = port;
    }

    /**
     * Getter pentru port
     * @return numarul intreg ce reprezinta portul
     */
    public int getPort() {
        return port;
    }

    /**
     * Getter pentru adresa IP
     * @return sir de caractere ce reprezinta adresa ip
     */
    public String getIpAddress() { return ipAddress; }

    /**
     * Functie care valideaza daca un string furnizat respecta formatul unei adrese IP
     * Trebuie sa contina patru numere pozitive reprezentate pe maxim 8 biti, delimitate prin "."
     * @param ipAddress String-ul asupra caruia se face verificarea
     * @return daca se respecta formatul adresei ip
     * @throws Exception generata daca nu se respecta formatul adresei IP
     */
    public boolean validateIpAddress(String ipAddress) throws Exception{
        String[] items = ipAddress.split("\\.");
        if(items.length != 4){
            throw new Exception("Invalid length for the ip address");
        }
        for(String item : items){
            int parsedValue;
            try{
                parsedValue = Integer.parseInt(item);
            }
            catch (NumberFormatException exception){
                throw new Exception("Ip address should contain only numbers!");
            }
            if(parsedValue < 0 || parsedValue > 255){
                throw new Exception("Ip address should contain only positive numbers, represented on maximum 8 bits!");
            }
        }
        return true;
    }

    /**
     * Functie care valideaza daca un numar furnizat respecta formatul unui port
     * Trebuie sa fie un numar pozitiv din intervalul [1000, 9999]
     * @param port Numarul asupra caruia se face verificarea
     * @return daca se respecta formatul portului
     * @throws Exception generata daca nu se respecta formatul portului
     */
    public boolean validatePort(int port) throws Exception{
        if(port < 0)
            throw new Exception("Port number should be positive");
        if(port < 1000)
            throw new Exception("It is recommended to use a number greater than 1000 for the port number.");
        if(port > 9999)
            throw new Exception("It is recommended to use a number smaller than 10000 for the port number.");
        return true;
    }

    /**
     * Functie care transforma un string intr-o adresa, avand in vedere formatul ip : port (+- un spatiu).
     * Orice alta abatere de la format este respinsa.
     * @param address String-ul care contine adresa in format text.
     * @return Adresa creata daca string-ul este valid.
     */
    public static Address parseAddress(String address){
        String[] elements = address.replace(" ","").split(":");
        try{
            return new Address(elements[0], Integer.parseInt(elements[1]));
        }
        catch (NumberFormatException exception){
            System.out.println("Invalid address format!");
            return null;
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
            return null;
        }
    }

    /**
     * Functie care verifica daca doua adrese sunt egale.
     * Egalitatea este determinata de egalitatea membrilor (ip si port)
     * @param object Adresa cu care se face comparatia
     * @return egalitatea dintre cele doua adrese
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Address address = (Address) object;
        return port == address.port && this.ipAddress.equals(address.ipAddress);
    }

    /**
     * @return Formatul de afisare al obiectului.
     */
    @Override
    public String toString() {
        return String.format("%s : %d", this.ipAddress, this.port);
    }
}
