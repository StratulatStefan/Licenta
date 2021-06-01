import {Environment} from '../environment';
import {HTTPResponseHandler} from '../services/HTTPResponseHandler';
import { UsersHandlerService } from './UsersHandlerService';

export class FileHandlerService{
    static uploadFile = (file, jwt, description, usertype) => {
        // https://www.smashingmagazine.com/2018/01/drag-drop-file-uploader-vanilla-js/
        
        let url = `${Environment.frontend_proxy}/upload`

        let additional_user_data = localStorage.getItem("additional_user_data")
        if(additional_user_data === null || additional_user_data === ""){
            UsersHandlerService.getAdditionalUserData(jwt).then(response => {
                additional_user_data = response.content
                localStorage.setItem("additional_user_data", JSON.stringify(additional_user_data))
            })
        }
        else{
            additional_user_data = JSON.parse(additional_user_data)
        }
        let formData = new FormData()
        formData.append('file', file)
          
        return new Promise((resolve) => {
            fetch(url, {
                method: 'POST',
                mode : "cors",
                body: formData,
                headers : {
                    'Authorization' : `Bearer ${jwt}`,
                    "version_description" : description,
                    "user_type" : usertype,
                    "available_storage" : additional_user_data.storage_quantity
                }
            }).then(response => {
                if(response.ok){
                    response.json().then(response => {
                        console.log(response)
                        resolve({
                            "code" : 1,
                            "content" : response["success status"]
                        })
                    });
                }
                else{
                    HTTPResponseHandler.handleErrorStatus(response).then(status => {
                        resolve({
                            "code" : status.code,
                            "content" : status.message
                        })
                    });
                }
            })
        })
    }

    static getUserFiles = (jwt) => {
        let url = `${Environment.frontend_proxy}/files`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
                mode : "cors",
                headers : {
                    'Authorization' : `Bearer ${jwt}`,
                }
            }).then(response => {
                if(response.ok){
                    response.json().then(response => {
                        resolve({
                            "code" : 1,
                            "content" : response
                        })
                    });
                }
                else{
                    HTTPResponseHandler.handleErrorStatus(response).then(status => {
                        resolve({
                            "code" : status.code,
                            "content" : status.message
                        })
                    });
                }
            })
        })
    }

    static getFileHistory = (jwt, filename) => {
        let url = `${Environment.frontend_proxy}/history?filename=${filename}`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
                mode : "cors",
                headers : {
                    'Authorization' : `Bearer ${jwt}`,
                }
            }).then(response => {
                if(response.ok){
                    response.json().then(response => {
                        resolve({
                            "code" : 1,
                            "content" : response
                        })
                    });
                }
                else{
                    HTTPResponseHandler.handleErrorStatus(response).then(status => {
                        resolve({
                            "code" : status.code,
                            "content" : status.message
                        })
                    });
                }
            })
        })
    }

    static deleteFile = (jwt, filename, description) => {
        let url = `${Environment.frontend_proxy}/${filename}`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'DELETE',
                mode : "cors",
                headers : {
                    'Content-Type': 'application/json',
                    'Authorization' : `Bearer ${jwt}`,
                },
                body : JSON.stringify({"description" : description})
            }).then(response => {
                console.log(response)
                if(response.ok){
                    response.json().then(response => {
                        resolve({
                            "code" : 1,
                            "content" : response
                        })
                    });
                }
                else{
                    HTTPResponseHandler.handleErrorStatus(response).then(status => {
                        resolve({
                            "code" : status.code,
                            "content" : status.message
                        })
                    });
                }
            })
        })
    }

    static renameFile = (jwt, filename, newname, description) => {
        let url = `${Environment.frontend_proxy}/${filename}`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'PUT',
                mode : "cors",
                headers : {
                    'Content-Type': 'application/json',
                    'Authorization' : `Bearer ${jwt}`,
                },
                body : JSON.stringify({"description" : description, "newname" : newname})
            }).then(response => {
                console.log(response)
                if(response.ok){
                    response.json().then(response => {
                        resolve({
                            "code" : 1,
                            "content" : response
                        })
                    });
                }
                else{
                    HTTPResponseHandler.handleErrorStatus(response).then(status => {
                        resolve({
                            "code" : status.code,
                            "content" : status.message
                        })
                    });
                }
            })
        })
    }
    
    static downloadFile = (jwt, filename) => {
        let url = `${Environment.frontend_proxy}/${filename}`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
                mode : "cors",
                headers : {
                    'Authorization' : `Bearer ${jwt}`,
                }
            }).then(response => {
                if(response.ok){
                    response.json().then(response => {
                        resolve({
                            "code" : 1,
                            "content" : response["success status"]
                        })
                    });
                }
                else{
                    HTTPResponseHandler.handleErrorStatus(response).then(status => {
                        resolve({
                            "code" : status.code,
                            "content" : status.message
                        })
                    });
                }
            })
        })
    }
}
