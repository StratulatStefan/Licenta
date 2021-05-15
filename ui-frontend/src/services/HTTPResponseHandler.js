export class HTTPResponseHandler{
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
}