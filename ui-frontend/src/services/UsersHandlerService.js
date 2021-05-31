import {Environment} from '../environment';
import {HTTPResponseHandler} from '../services/HTTPResponseHandler';

export class UsersHandlerService{
    /* ================= ADDITIONAL VALIDATION FUNCTIONS ================= */
    static basicCredentialsCheck = (credentials) => {
        let status = [true, null]
        const forbiddens = ["\"", " ", "/", ";", ":"]
        const Exception = {}
        // verificam sa nu avem campuri goale sau caractere interzise
        try{
            Object.keys(credentials).forEach(credential_key => {
                if(credentials[credential_key] === ""){
                    status = [false, `Nu ati introdus ${credential_key}`]
                    throw Exception
                }
                
                forbiddens.forEach(character => {
                    if(character === " " && credential_key === "name"){
                        return;
                    }
                    if(credentials[credential_key].includes(character)){
                        status = [false, `${credential_key} contine caractere interzise! (${character})`]
                        throw Exception
                    }
                })

            })
        }
        catch(e){
            return status
        }

        // verificam ca email-ul sa aiba formatul necesar
        if(!credentials["email"].includes("@") || !credentials["email"].includes(".")){
            return [false, "Username-ul nu respecta formatul unui email."];
        }

        return status
    }


    /* ================= RETRIEVE ================= */
    static getAdditionalUserData = (jwt) => {
        return new Promise((resolve) => {
            fetch(`${Environment.rest_api}/user/search`, {
                method : 'GET',
                mode : "cors",
                headers : {
                    'Authorization' : `Bearer ${jwt}`,
                },
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

    static getUserRole = (jwt) => {
        return new Promise((resolve) => {
            fetch(`${Environment.rest_api}/user/role`, {
                method : 'GET',
                mode : "cors",
                headers : {
                    'Authorization' : `Bearer ${jwt}`,
                },
            }).then(response => {
                if(response.ok){
                    response.json().then(response => {
                        resolve({
                            "code" : 1,
                            "content" : response["role"]
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

    static getAvailableUserTypes = () => {
        return new Promise((resolve) => {
            fetch(`${Environment.rest_api}/usertype/all`, {
                method : 'GET',
                mode : "cors",
            }).then(response => {
                if(response.ok){
                    response.json().then(response => {
                        console.log(response)
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


    /* ================= CREATE ================= */
    static login = (credentials) => {
        return new Promise((resolve) => {
            let credentialsCheckStatus = this.basicCredentialsCheck(credentials)
            if(credentialsCheckStatus[0] === true){
                let userdata = {
                    "username" : credentials["email"],
                    "password" : credentials["password"]
                }
                fetch(`${Environment.rest_api}/user/login`, {
                    method : 'POST',
                    mode : 'cors',
                    headers : {
                        'Content-Type' : 'application/json'
                    },
                    body : JSON.stringify(userdata)
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
            }
            else{
                resolve({
                    "code" : 0,
                    "content" : credentialsCheckStatus[1]
                })
            }
        })
    }

    static register = (credentials) => {
        return new Promise((resolve) => {
            let credentialsCheckStatus = this.basicCredentialsCheck(credentials)
            if(credentialsCheckStatus[0] === true){
                fetch(`${Environment.rest_api}/user`, {
                    method : 'POST',
                    mode : 'cors',
                    headers : {
                        'Content-Type' : 'application/json'
                    },
                    body : JSON.stringify(credentials)
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
            }
            else{
                resolve({
                    "code" : 0,
                    "content" : credentialsCheckStatus[1]
                })
            }
        })
    }


    /* ================= UPDATE ================= */
    static updatePlan = (jwt, newplan) => {
        return new Promise((resolve) => {
            fetch(`${Environment.rest_api}/user`, {
                method : 'PUT',
                mode : 'cors',
                headers : {
                    'Content-Type' : 'application/json',
                    'Authorization' : `Bearer ${jwt}`
                },
                body : JSON.stringify({"type" : newplan})
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

}