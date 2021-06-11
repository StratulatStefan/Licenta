export class HTTPResponseHandler{
    static handleErrorStatus = (response) => {
        return new Promise((resolve) => {
            let response_status = response.status
            let status_code = 0
            response.json().then(response => {
                if(response_status === 401){
                    status_code = 401
                    if(response["error status"] !== null && response["error status"].includes("expired")){
                        response['error status'] = "You session expired. Please try to reauthenticate."
                        status_code = 402
                    }
                    else{
                        response['error status'] = "Internal error. We are sorry."
                    }
                }
                resolve({
                    "code" : status_code,
                    "message": response['error status']
                })
            })
        })
    }
}