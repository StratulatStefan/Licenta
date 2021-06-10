import {Environment} from '../environment';
import {HTTPResponseHandler} from '../services/HTTPResponseHandler';
import { GeneralPurposeService } from './GeneralPurposeService';

export class AdminHandlerService{
    static fetchLog = (jwt, logCriteria) => {
        let url = `${Environment.rest_api}/log`
        
        url += GeneralPurposeService.prepareURLQuery(logCriteria)

        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
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

    static fetchAvailableNodesFromAPI = (jwt) => {
        let url = `${Environment.rest_api}/internalnode/all`

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

    static cleanLog = (jwt, logCriteria) => {
        console.log("cleaning log...")

        let url = `${Environment.rest_api}/log`
        
        url += GeneralPurposeService.prepareURLQuery(logCriteria)
        console.log(url)

        return new Promise((resolve) => {
            fetch(url, {
                method: 'DELETE',
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
    

    static fetchNodesStoringFile = (jwt, userId, filename) => {
        let url = `${Environment.frontend_proxy}/nodesforfile?user=${userId}&filename=${GeneralPurposeService.sanitizeURL(filename)}`
        console.log(url)
        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
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

    static fetchNodeData = (jwt, nodeAddress) => {
        let url = `${Environment.rest_api}/internalnode/${nodeAddress}`
        
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

    static getFileVersions = (jwt, userid, filename) => {
        let url = `${Environment.frontend_proxy}/versions?filename=${filename}&userid=${userid}`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'GET',
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

    static deleteFileFromInternalNode = (jwt, file) => {
        console.log(JSON.stringify(file))
        let url = `${Environment.frontend_proxy}/internalnodefile?user=${file.user}&filename=${file.filename}&address=${file.address}`

        return new Promise((resolve) => {
            fetch(url, {
                method: 'DELETE',
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
}

