export class HTTPResponseHandler{
    static handleErrorStatus = (response) => {
        return new Promise((resolve) => {
            let response_status = response.status
            let status_code = 0
            response.json().then(response => {
                if(response_status === 401){
                    status_code = 401
                    console.log(response)
                    if(response["error status"] !== null && response["error status"].includes("expired")){
                        alert("Sesiunea a expirat! Incercati sa va reautentificati!")
                    }
                    else{
                        alert("S-a produs o eroare interna! Verificati serverele..")
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