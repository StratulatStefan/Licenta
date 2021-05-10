import {Environment} from '../environment';

export class UsersHandlerService{
    static basicCredentialsCheck = (username, password) => {
        if(username === "" || password === ""){
            return false;
        }
        if(!username.includes("@") || !username.includes(".")){
            return false;
        }
        
        let forbiddens = ["\"", " ", "/", ";", ":"]
        let contains = false
        forbiddens.forEach(character => {
            if((username + password).includes(character)){
                contains = true
            }
        })
        return !contains
    }

    static handleErrorStatus = (response) => {
        return new Promise((resolve) => {
            let response_status = response.status
            let status_code = 0
            response.json().then(response => {
                if(response_status === 401){
                    status_code = 401
                    if(response["error status"].includes("expired")){
                        alert("Sesiunea a expirat! Incercati sa va reautentificati!")
                    }
                    else{
                        alert("Nu aveti permisiunea sa executati aceasta actiune!")
                    }
                }
                resolve({
                    "code" : status_code,
                    "message": response['error status']
                })
            })
        })
    }

    static login = (username, password) => {
        return new Promise((resolve) => {
            let credentialsCheckStatus = this.basicCredentialsCheck(username, password)
            if(credentialsCheckStatus === true){
                let userdata = {
                    "username" : username,
                    "password" : password
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
                        this.handleErrorStatus(response).then(status => {
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
                    "content" : credentialsCheckStatus
                })
            }
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
                    this.handleErrorStatus(response).then(status => {
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