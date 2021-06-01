import {Environment} from '../environment';
import {HTTPResponseHandler} from '../services/HTTPResponseHandler';

export class AdminHandlerService{
    static prepareURLForLog = (logCriteria) => {
        let options = []
        if(logCriteria.node_address !== null && logCriteria.node_address !== "ALL"){
            options.push(`node_address=${logCriteria.node_address}`)
        }
        if(logCriteria.message_type !== null && logCriteria.message_type !== "ALL"){
            options.push(`message_type=${logCriteria.message_type}`)
        }
        /*if(logCriteria.date1 !== null){
            options.push(`date1=${logCriteria.date1}`) // 2021-05-31/12:54
        }*/ // tre adaugata si asta

        if(options.length > 0){
            return "?" + options.join("&")
        }
        return ""
    } 

    static fetchLog = (logCriteria) => {
        let url = `${Environment.rest_api}/log`
        
        url += AdminHandlerService.prepareURLForLog(logCriteria)

        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
                mode : "cors",
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

    static fetchAvailableNodesFromAPI = () => {
        let url = `${Environment.rest_api}/internalnode/all`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
                mode : "cors",
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

    static cleanLog = (logCriteria) => {
        console.log("cleaning log...")

        let url = `${Environment.rest_api}/log`
        
        url += AdminHandlerService.prepareURLForLog(logCriteria)
        console.log(url)

        return new Promise((resolve) => {
            fetch(url, {
                method: 'DELETE',
                mode : "cors",
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
}