package jwt;

import java.util.Map;

/**
 * <ul>
 * 	<li>Clasa folosita pentru a asigura mecanismul de autorizare a clientului.</li>
 * 	<li> Va avea ca principal obiect de lucru <strong>JWT</strong>-ul.</li>
 * 	<li> Va realiza decodificarea acestuia pentru a verifica daca rolul utilizatorului este potrivit pentru a putea realiza o anumita actiune.</li>
 * 	<li> Se va verifica si valabilitatea token-ului.</li>
 * </ul>
 */
public class AuthorizationService {
    /**
     * <ul>
     * 	<li>Rolurile posibile ale utilizatorilor.</li>
     * 	<li> primul membru <strong>ALL</strong> va autoriza cu succes toti clientii, indiferent de rol.</li>
     * </ul>
     */
    public enum UserTypes {ALL, STANDARD, PREMIUM, ADMIN};

    /**
     * <ul>
     * 	<li>Schema de autorizare.</li>
     * 	<li> Header-ul de autorizare va avea formatul <strong>BEARER <JWT></strong>.</li>
     * </ul>
     */
    private static final String authorizationModel = "BEARER";

    /**
     * <ul>
     * 	<li>Valabilitatea unei sesiuni.</li>
     * </ul>
     */
    private static final long sessionTime          = 1000 * 3600;

    /**
     * <ul>
     * 	<li>Functia de creare a unui nou JWT, pe baza datelor utilizatorilor.</li>
     * 	<li> Se va returna sirul de caractere care va reprezenta <strong>token</strong>-ul.</li>
     * </ul>
     * @param id Identificatorul unic al fisierului
     * @param username Numele utilizatorului
     * @param userRole Rolul utilizatorului, folosit la autorizare.
     */
    public static String generateUserIdentity(int id, String username, String userRole){
        return new JWT(id, username, userRole, sessionTime).getJWT();
    }

    /**
     * <ul>
     * 	<li>Functie de decodificare a jwt-ului.</li>
     * 	<li> Se va parsa header-ul de autorizare, se va extrage token-ul, se va verifica daca respecta formatul <strong>Base64</strong>.</li>
     * </ul>
     * @param authorizationMessage Header-ul de autorizare
     * @return Claim-urile token-ului.
     */
    private Map<String, Object> decodeToken(String authorizationMessage) throws Exception{
        if(!authorizationMessage.toUpperCase().contains(authorizationModel)){
            throw new Exception("Invalid Authorization model. You should use " + authorizationModel);
        }
        String[] authorizationItems = authorizationMessage.split("\\s");
        String jwtToken = authorizationItems[1];

        if(!jwtToken.contains(".")){
            throw new Exception("Your token does not respect the Base64 JWT format! Include the delimitation between header and payload");
        }

        JWT jwt = new JWT(jwtToken);
        try {
            return jwt.decodeJWT();
        }
        catch (Exception exception){
            throw new Exception(exception.getMessage());
        }
    }

    /**
     * <ul>
     * 	<li>Functie care verifica daca un JWT a expirat.</li>
     * 	<li> Se va verifica daca se genereaza o exceptie, a carei mesaj contine indicii despre expirare.</li>
     * </ul>
     * @param exceptionMessage Mesajul de exceptie, cauzat de imposibilitatea parsarii token-ului.
     */
    private boolean checkJWTExpiration(String exceptionMessage){
        return exceptionMessage.contains("expired");
    }

    /**
     * <ul>
     * 	<li>Functie care incearca autorizarea clientului.</li>
     * 	<li> Se va decodifica token-ul.</li>
     * 	<li>  Se va verifica daca rolul extras din token coincide cu rolul corespunzator operatiei.</li>
     * </ul>
     * @param authorizationValue Header-ul de autorizare.
     * @param requiredUserType Tipurile de utilizatori carora le este permis sa execute operatie.
     * @return Campurile din jwt-ul decodificat.
     */
    public Map<String, Object> userAuthorization(String authorizationValue, UserTypes[] requiredUserType) throws Exception{
        Map<String, Object> jwtData = null;
        try {
            jwtData = decodeToken(authorizationValue);
        }
        catch (Exception exception){
            if(checkJWTExpiration(exception.getMessage())){
                String exceptionMessage = "JWT Token expired!";
                System.out.println(exceptionMessage);
                throw new Exception(exceptionMessage);
            }
        }

        for(UserTypes userType : requiredUserType){
            if(userType == UserTypes.ALL || UserTypes.valueOf((String)jwtData.get("role")) == userType){
                System.out.println("User successfully authorized!");
                return jwtData;
            }
        }
        throw new Exception("Role exception! Your user type is not allowed to take this action!");
    }
}
