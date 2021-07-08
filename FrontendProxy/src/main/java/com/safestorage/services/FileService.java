package com.safestorage.services;

import client_manager.ManagerComplexeResponse;
import client_manager.ManagerTextResponse;
import client_manager.data.*;
import com.safestorage.proxy.FileSender;
import com.safestorage.proxy.FrontendManager;
import config.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import os.FileSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * <ul>
 * 	<li>Serviciu va expune toate functionalitatile specifice prelucrarii de fisiere.</li>
 * 	<li> Se vor crea cererile corespunzatoare, se vor trimite catre nodul generaliar raspunsul va fi returnat catre controller, spre a fi trimis catre client.</li>
 * </ul>
 */
@Service
public class FileService {
    /**
     * Calea catre directorul unde vor fi stocate fisierele incarcate, de la client.
     */
    private String uploadDir = AppConfig.getParam("uploadDir");

    /**
     Functie care va salva stoca in memoria locala fisierul primit de la client.
     * @param file Fisierul in formatul primit de la client.
     * @return Calea catre locatia fisierului.
     * @throws Exception Nu se poate stoca fisierul.
     */
    private String persistFileToBuffer(MultipartFile file) throws Exception{
        try{
            Path copyLocation = Paths.get(
                uploadDir + File.separator + file.getOriginalFilename()
            );
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
            return uploadDir + "\\" + file.getOriginalFilename();
        }
        catch (Exception e){
            throw new Exception("Could not store file " + file.getOriginalFilename() + ". Please try again!");
        }
    }

    /**
     * <ul>
     * 	<li>Functie de incarcare a unui fisier in sistem.</li>
     * 	<li> Initial, fisierul va fi stocat in memoria locala.</li>
     * 	<li> Ulterior, se va crea cererea ce va fi trimisa catre nodul general,prin care se solicita adresele nodurilor
     *       ce vor stoca replici ale fisierului.</li>
     * 	<li> Apoi, se va trimite fisierul catre nodurile interne.</li>
     * 	<li>Se va returna catre client calea unde a fost stocat fisierul.</li>
     * </ul>
     * @param file Fisierul in formatul primit de la client.
     * @param userId identificatorul unic al utilizatorului
     * @param description Descrierea versiunii fisierului.
     * @param userType Tipul utilizatorului, pentru a decide factorul de replicare
     * @param availableStorage Cantitatea de memorie disponibila, pentru a decide daca fisierul poate fi stocat.
     */
    public String uploadFile(MultipartFile file, int userId, String description, String userType, long availableStorage) throws Exception {
        long filesize = file.getSize();
        if(filesize > availableStorage){
            throw new Exception("Not enough memory storage!");
        }
        String filePath = persistFileToBuffer(file);

        NewFileRequest newFileRequest = new NewFileRequest();
        newFileRequest.setUserId(String.format("%d", userId));
        newFileRequest.setFilename(filePath);
        newFileRequest.setFilesize(filesize);
        newFileRequest.setCrc(FileSystem.calculateCRC(filePath));
        newFileRequest.setUserType(userType);
        newFileRequest.setDescription(description);

        FrontendManager.mainActivity(newFileRequest);

        return filePath;
    }

    /**
     * <ul>
     * 	<li>Functie de eliminare a unui fisier din sistem.</li>
     * 	<li> Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param userId identificatorul unic al utilizatorului
     * @param filename Numele fisierului.
     * @param description Descrierea cererii de eliminare.
     * @return Raspunsul nodului general.
     */
    public ManagerTextResponse deleteFile(int userId, String filename, String description) throws IOException, ClassNotFoundException {
        DeleteFileRequest deleteFileRequest = new DeleteFileRequest();
        deleteFileRequest.setUserId(String.format("%d", userId));
        deleteFileRequest.setFilename(filename);
        deleteFileRequest.setDescription(description);

        return (ManagerTextResponse) FrontendManager.managerOperationRequest(deleteFileRequest);
    }

    /**
     * <ul>
     * 	<li>Functie de eliminare a unei replici a unui fisier, de pe unul dintre nodurile interne.</li>
     * 	<li> Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param userId identificatorul unic al utilizatorului
     * @param filename Numele fisierului.
     * @param address Adresa nodului de pe care se va elimina replica.
     * @return Raspunsul nodului general.
     */
    public ManagerTextResponse deleteFileFromInternalNode(String userId, String filename, String address) throws IOException, ClassNotFoundException {
        DeleteFileFromNodeRequest deleteFileFromNodeRequest = new DeleteFileFromNodeRequest();
        deleteFileFromNodeRequest.setUserId(userId);
        deleteFileFromNodeRequest.setFilename(filename);
        deleteFileFromNodeRequest.setAddress(address);

        return (ManagerTextResponse) FrontendManager.managerOperationRequest(deleteFileFromNodeRequest);
    }

    /**
     * <ul>
     * 	<li>Functie de redenumire a unui fisier.</li>
     * 	<li> Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param userId identificatorul unic al utilizatorului
     * @param filename Numele fisierului.
     * @param newname Noul nume al fisierului.
     * @param description Descrierea noii versiuni.
     * @return Raspunsul nodului general.
     */
    public ManagerTextResponse renameFile(int userId, String filename, String newname, String description) throws IOException, ClassNotFoundException {
        RenameFileRequest renameFileRequest = new RenameFileRequest();
        renameFileRequest.setUserId(String.format("%d", userId));
        renameFileRequest.setFilename(filename);
        renameFileRequest.setNewName(newname);
        renameFileRequest.setDescription(description);

        return (ManagerTextResponse) FrontendManager.managerOperationRequest(renameFileRequest);
    }

    /**
     * <ul>
     * 	<li>Functie pentru solicitarea tuturor fisierelor unui utilizator.</li>
     * 	<li>Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param userId identificatorul unic al utilizatorului
     * @return Raspunsul nodului general.
     */
    public ManagerComplexeResponse getUserFiles(int userId) throws IOException, ClassNotFoundException {
        GetUserFiles getUserFilesRequest = new GetUserFiles();
        getUserFilesRequest.setUserId(String.format("%d", userId));

        return (ManagerComplexeResponse) FrontendManager.managerOperationRequest(getUserFilesRequest);
    }

    /**
     * <ul>
     * 	<li>Functie pentru solicitarea tuturor versiunilor unui fisier</li>
     * 	<li>Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param userId identificatorul unic al utilizatorului
     * @param filename Numele fisierului
     * @return Raspunsul nodului general.
     */
    public ManagerComplexeResponse getUserFileHistory(int userId, String filename) throws IOException, ClassNotFoundException {
        GetUserFileHistory getUserFileHistoryRequest = new GetUserFileHistory();
        getUserFileHistoryRequest.setUserId(String.format("%d", userId));
        getUserFileHistoryRequest.setFilename(filename);

        return (ManagerComplexeResponse) FrontendManager.managerOperationRequest(getUserFileHistoryRequest);
    }

    /**
     * <ul>
     * 	<li>Functie pentru solicitarea adresei unui nod care ar putea furniza o anumita replica pentru (download)</li>
     * 	<li>Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param userId identificatorul unic al utilizatorului
     * @param filename Numele fisierului
     * @return Raspunsul nodului general.
     */
    private ManagerTextResponse getNodeCandidateForFile(int userId, String filename) throws IOException, ClassNotFoundException {
        GetNodeForDownload nodeForDownloadRequest = new GetNodeForDownload();
        nodeForDownloadRequest.setUserId(String.format("%d", userId));
        nodeForDownloadRequest.setFilename(filename);

        return (ManagerTextResponse) FrontendManager.managerOperationRequest(nodeForDownloadRequest);

    }

    /**
     * <ul>
     * 	<li>Functie pentru solicitarea descarcarii unui fisier.</li>
     * 	<li>Initial, se solicita nodului general adresa unui nod intern care poate furniza fisierul.</li>
     * 	<li>Apoi, se descarca fisierul de la nodul intern si se stocheaza in memoria locala.</li>
     * 	<li>Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param userId identificatorul unic al utilizatorului
     * @param filename Numele fisierului
     * @return Calea catre locatia fisierului descarcat.
     */
    public String downloadFile(int userId, String filename) throws IOException, ClassNotFoundException {
        String nodeCandidate = this.getNodeCandidateForFile(userId, filename).getResponse();
        return FileSender.downloadFile(nodeCandidate, String.format("%d", userId), filename);
    }

    /**
     * <ul>
     * 	<li>Functie pentru solicitarea adreselor tuturo nodurilor care stocheaza un anumit fisier.</li>
     * 	<li>Se va trimite cerere catre nodul general, iar raspunsul va fi intors catre client.</li>
     * </ul>
     * @param user identificatorul unic al utilizatorului
     * @param filename Numele fisierului
     * @return Raspunsul nodului general.
     */
    public ManagerComplexeResponse getNodesStoringUserFile(String user, String filename) throws IOException, ClassNotFoundException {
        GetNodesForFileRequest getNodesForFileRequest = new GetNodesForFileRequest();
        getNodesForFileRequest.setUserId(user);
        getNodesForFileRequest.setFilename(filename);

        return (ManagerComplexeResponse) FrontendManager.managerOperationRequest(getNodesForFileRequest);
    }

    /**
     * Functie pentru actualizare a tipului corespunzator unui utilizator din tabela de continut a nodului general
     * @param user Identificatorul unic al utilizatorului
     * @param newUserType Noul tip al utilizatorului
     */
    public ManagerTextResponse updateInternalUserType(String user, String newUserType) throws IOException, ClassNotFoundException {
        UpdateUserTypeRequest updateReplicationFactor = new UpdateUserTypeRequest();
        updateReplicationFactor.setUserId(user);
        updateReplicationFactor.setUser_type(newUserType);

        return (ManagerTextResponse)FrontendManager.managerOperationRequest(updateReplicationFactor);

    }
}
