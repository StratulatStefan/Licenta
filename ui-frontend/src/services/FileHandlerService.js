import {Environment} from '../environment';
import {HTTPResponseHandler} from '../services/HTTPResponseHandler';

export class FileHandlerService{
    static uploadFile = (file, description) => {
        console.log(`File : ${file}`)
        console.log(`Description : ${description}`)
        
        let url = `${Environment.frontend_proxy}/upload`

        let formData = new FormData()
          
        formData.append('file', file)
          
        return new Promise((resolve) => {
            fetch(url, {
                method: 'POST',
                mode : "cors",
                body: formData,
                headers : {
                    "version_description" : description
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
}
