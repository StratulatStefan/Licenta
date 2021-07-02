package com.safestorage.controller;

import com.safestorage.model.UploadPendingQueue;
import com.safestorage.services.FileService;
import com.safestorage.services.ResponseHandlerService;
import jwt.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <ul>
 * 	<li>Componenta de tip controller a interactiunii cu clientul.</li>
 * 	<li> Se vor expune toate metodele necesare prelucrarii si accesarii fisierelor si a datelor despre nodurile interne.</li>
 * 	<li>Se configureaza si <strong>CrossOrigin</strong> doar cu adresa aplicatiei client.</li>
 * 	<li>Se va adauga adnotarea <strong>Controller</strong> pentru a informa SpringBoot ca aceasta este componenta de tip controller.</li>
 * 	<li>Help : https://stackabuse.com/uploading-files-with-spring-boot/</li>
 * </ul>
 */
@CrossOrigin(origins = "http://localhost:5000")
@Controller
public class FileController {
    /**
     * <ul>
     * 	<li>Obiectul care va facilita asigurarea mecanismelor de autorizare a clientului care va trimitere cererile.</li>
     * 	<li> Scopul principal va fi prelucrarea <strong>JWT</strong>-ului.</li>
     * </ul>
     */
    private AuthorizationService authorizationService = new AuthorizationService();

    /**
     * <ul>
     * 	<li>Obiectul de tip coada de asteptare, ce va fi folosit in mecanismul de incarcare a unui fisier.</li>
     * 	<li> Procesul de receptionare a unui feedback de incarcare cu succes a fisierului este asincron.</li>
     * 	<li> Confirmarea va fi trimisa catre client atunci cand confirmarea nodurilor interne va fi in coada.</li>
     * </ul>
     */
    public static UploadPendingQueue uploadPendingQueue = new UploadPendingQueue();

    /**
     * <ul>
     * 	<li>Serviciul care va facilita prelucrarea de fisiere.</li>
     * 	<li> Acesta va trimite cereri catre nodul general al aplicatiei, prin care va solicita efectuarea unei anumite operatiuni, ceruta de client.</li>
     * 	<li>Definirea si instantierea obiectului se va face prin injectarea dependintelor efectuata de SpringBoot (<strong>@Autowired</strong>).</li>
     * </ul>
     */
    @Autowired
    FileService fileService;

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de incarcare a unui fisier in sistem.</li>
     * 	<li> Operatia este disponibila doar pentru utilizatorii obisnuiti.</li>
     * </ul>
     * @param file Fisierul ce se doreste a fi incarcat.
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     *                           Se va folosi la autorizarea clientului si la extragerea identificatorului.
     * @param descriptionValue Descrierea primei versiuni a fisierului.
     * @param availableStorage Cantitatea de memorie disponibila pentru utilizator.
     * @param userType Tipul utilizatorului, necesar pentru a decide factorul de replicare.
     */
    @RequestMapping(path = "/proxy/upload", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                          @RequestHeader("Authorization")       String authorizationValue,
                                                          @RequestHeader("version_description") String descriptionValue,
                                                          @RequestHeader("available_storage")   long availableStorage,
                                                          @RequestHeader("user_type")           String userType){

        int userId = -1;
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.STANDARD, AuthorizationService.UserTypes.PREMIUM};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            String filepath = fileService.uploadFile(file, userId, descriptionValue, userType, availableStorage);
            while(!uploadPendingQueue.containsRegister(String.format("%d", userId), filepath));
            uploadPendingQueue.popFromQueue();
            Map<String, String> successResponse =
                    ResponseHandlerService.buildSuccessStatus("You successfully uploaded " + file.getOriginalFilename() + "!");
            return new ResponseEntity<Map<String, String>>(successResponse, HttpStatus.OK);

        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity<Map<String, String>>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de extragere a tuturor fisierelor unui utilizator.</li>
     * 	<li> Operatia este disponibila pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     *                           Se va folosi la autorizarea clientului si la extragerea identificatorului.
     */
    @RequestMapping(path = "/proxy/files", method = RequestMethod.GET)
    public ResponseEntity<List<HashMap<String, Object>>> getUserFiles(@RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(fileService.getUserFiles(userId).getResponse(), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de extragerii a tuturor versiunilor unui fisier.</li>
     * 	<li> Operatia este disponibila pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param filename Numele fisierului ale carui versiuni sunt solicitate.
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     *                           Se va folosi la autorizarea clientului si la extragerea identificatorului.
     */
    @RequestMapping(path = "/proxy/history", method = RequestMethod.GET)
    public ResponseEntity<List<HashMap<String, Object>>> getUserFileHistory(@RequestParam(name="filename", required = false, defaultValue = "") String filename,
                                                                            @RequestHeader("Authorization")                                     String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(fileService.getUserFileHistory(userId, filename).getResponse(), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de extragere a tuturor versiunilor unui fisier al unui anumit utilizator.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului, in procesul de monitorizare.</li>
     * </ul>
     * @param filename Numele fisierului.
     * @param userId Identificatorul utilizatorului ce detine fisierul.
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     */
    @RequestMapping(path = "/proxy/versions", method = RequestMethod.GET)
    public ResponseEntity<List<HashMap<String, Object>>> getUserFileHistory(@RequestParam(name="filename", required = false, defaultValue = "") String filename,
                                                                            @RequestParam(name="userid",   required = false, defaultValue = "") int userId,
                                                                            @RequestHeader("Authorization")                                     String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(fileService.getUserFileHistory(userId, filename).getResponse(), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de eliminare a unui fisier din sistem.</li>
     * 	<li> Operatia este disponibila pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param filename Numele fisierului.
     * @param description Descrierea evenimentului de eliminare a unui fisier.
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     *                           Se va folosi la autorizarea clientului si la extragerea identificatorului.
     */
    @RequestMapping(path = "/proxy/{filename}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteFile(@PathVariable String                    filename,
                                             @RequestBody HashMap<String, String>   description,
                                             @RequestHeader("Authorization") String  authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            String status = fileService.deleteFile(userId, filename, description.get("description")).getResponse();
            return new ResponseEntity(ResponseHandlerService.buildSuccessStatus(status), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de redenumire a unui fisier.</li>
     * 	<li> Operatia este disponibila pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param filename Numele fisierului
     * @param data Noul nume al fisierului
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     *                           Se va folosi la autorizarea clientului si la extragerea identificatorului.
     */
    @RequestMapping(path = "/proxy/{filename}", method = RequestMethod.PUT)
    public ResponseEntity<String> renameFile(@PathVariable String                  filename,
                                             @RequestBody HashMap<String, String>  data,
                                             @RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            String status = fileService.renameFile(userId, filename, data.get("newname"), data.get("description")).getResponse();
            return new ResponseEntity(ResponseHandlerService.buildSuccessStatus(status), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de descarcare a unui fisier.</li>
     * 	<li> Operatia este disponibila pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param filename Numele fisierului
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     *                           Se va folosi la autorizarea clientului si la extragerea identificatorului.
     */
    @RequestMapping(path = "/proxy/{filename}", method = RequestMethod.GET)
    public ResponseEntity<String> downloadFile(@PathVariable String filename,
                                               @RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(ResponseHandlerService.buildSuccessStatus(fileService.downloadFile(userId, filename)), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de extragere a tuturor nodurilor care stocheaza o replica a unui fisier.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului, in procesul de monitorizare.</li>
     * </ul>
     * @param userId Id-ul utilizatorul care detine fisierul.
     * @param filename Fisierul ale carui replici sunt distribuite pe nodurile cautate.
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     */
    @RequestMapping(path = "/proxy/nodesforfile", method = RequestMethod.GET)
    public ResponseEntity<List<Object>> getNodesOfFile(@RequestParam(name="user",     required = true, defaultValue = "1") String userId,
                                                       @RequestParam(name="filename", required = true, defaultValue = "")  String filename,
                                                       @RequestHeader("Authorization")                                     String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(fileService.getNodesStoringUserFile(userId, filename).getResponse(), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de eliminare a unei replici a unui fisier de pe unul dintre nodurile interne.</li>
     * 	<li>Cererea este relevanta in procesul de verificare a bunei functionari a mecanismului de replicare.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului, in procesul de monitorizare.</li>
     * </ul>
     * @param userId Id-ul utilizatorului care detine fisierul.
     * @param filename Numele fisierului a carui replica trebuie eliminata.
     * @param address Adresa nodului ce contine replica ce trebuie eliminata.
     * @param authorizationValue Valoarea de autorizare, avand structura <strong>BEARER token</strong>.
     */
    @RequestMapping(path = "/proxy/internalnodefile", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteFileFromInternalNode(@RequestParam(name="user",     required = true, defaultValue = "") String userId,
                                                             @RequestParam(name="filename", required = true, defaultValue = "") String filename,
                                                             @RequestParam(name="address",  required = true, defaultValue = "") String address,
                                                             @RequestHeader("Authorization")                                    String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            String status = fileService.deleteFileFromInternalNode(userId, filename, address).getResponse();
            return new ResponseEntity(ResponseHandlerService.buildSuccessStatus(status), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


