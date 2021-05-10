import {Environment} from '../environment';

export class UsersHandlerService{
    static basicCredentialsCheck = (username, password) => {
        return true
    }

    static handleErrorStatus = (response) => {
        return new Promise((resolve) => {
            response.json().then(response => {
                resolve(response['error status'])
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
                                "code" : 0,
                                "content" : status
                            })
                        });
                    }
                })
            }
            else{
                resolve(credentialsCheckStatus)
            }
        })
    }
}