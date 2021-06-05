import React, { Component } from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import '../styles/pages-style.css';
import '../styles/pages-home-style.css';
import '../styles/pages-admin-style.css';
import { GeneralPurposeService } from '../services/GeneralPurposeService';
import { AdminHandlerService } from '../services/AdminHandlerService';
import { Client } from '@stomp/stompjs';
import { Environment } from '../environment';

class AdminMainPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Admin console";
        this.userData = localStorage.getItem('user_data')
        this.state = {
            isUserConnected : false,
            userType : null,
            accountAvailable : true,
            availableNodes : null,
            log : [],
            websocket : {"connected" : false, "subscription" : null},
            content : null,
            content_nodes_data : [],
            file_versions : null,
            storagestatus : null
        }
        this.logCriteria = {message_type : "ALL", node_address : "ALL", date1 : GeneralPurposeService.getCurrentTimestampForLogging("1 year")}
        this.webSocketConnection = null;
        this.setHeaderLayout()
    }

    setHeaderLayout = () => {
        GeneralPurposeService.setHeaderLayout("ADMIN")
        document.getElementById("log_redirector").onclick = () => this.adminAction("log")
        document.getElementById("content-table_redirector").onclick = () => this.adminAction("content")
        document.getElementById("storage-table_redirector").onclick = () => this.adminAction("storage")
        document.getElementById("nodes-status_redirector").onclick = () => this.adminAction("nodes")
        document.getElementById("replication-status_redirector").onclick = () => this.adminAction("replication")
        document.getElementById("connection-table_redirector").onclick = () => this.adminAction("connection")
    }

    componentDidMount = () => {
        Environment.getWebSocket().then(response => {
            this.webSocketConnection = response
            this.setState({websocket : {"connected" : true}})
        })
        this.checkForConnectedUser()
        this.fetchUserType().then(_ => {
            this.fetchAvailableNodes()
        })
    }
    

    fetchAvailableNodes = () => {
        AdminHandlerService.fetchAvailableNodesFromAPI().then(response => {
            this.setState({availableNodes : response.content})
        })
    }

    fetchUserType = () => {
        return new Promise(resolve => {
            try{
                this.setState({userType : this.props.location.state.detail["user_type"]})
                resolve(null)
            }
            catch(e){
                // am ajuns pe aceasta pagina din alta parte, prin click pe meniu, prin scriere directa in link
                UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                    if(response.code === 1){
                        console.log(`props fetch: ${response["content"]}`)
                        this.setState({userType : response["content"]})
                    }
                    else if(response.code === 401){
                        localStorage.setItem("user_data", '')
                    }
                    resolve(null)
                })
            }
        })
    }

    checkForConnectedUser = () => {
        this.userData = localStorage.getItem('user_data')
        if(this.userData === null || this.userData === ''){
            this.redirect("")
        }
        else{
            this.userData = JSON.parse(this.userData)
        }
    }

    handleContentTable = (message) => {
        if (message.body) {
            document.getElementById("admin_view_title_0").innerHTML = ""
            if(message.body.length > 0){
                this.setState({content : JSON.parse(message.body)})
                console.log(message.body)
            }
            else{
                this.setState({content : undefined})
                console.log("content table empty")
            }
        }
    }

    fetchFileVersions = (userid, filename) => {
        AdminHandlerService.getFileVersions(userid, filename).then(response => {
            this.setState({file_versions : response.content})
            document.getElementById("replication_nodes_div").style.display = "none"
            document.getElementById("versions_nodes_div").style.display = "block"
        })
    }

    handleReplicationManagerStatus = (message) => {
        if (message.body) {
            console.log(message.body)
        }
    }

    handleNodesActivity = (message) => {
        if (message.body) {
            console.log(message.body)
        }
    }

    handleStorageStatus = (message) => {
        if (message.body) {
            this.setState({storagestatus : JSON.parse(message.body)})
            console.log(message.body)
        }
    }

    handleConnectionTable = (message) => {
        if (message.body) {
            console.log(message.body)
        }
    }

    adminAction = (actionName) => {
        document.getElementById("admin_log_view").style.display               = "none"
        document.getElementById("admin_content_view").style.display           = "none"
        document.getElementById("admin_nodes_view").style.display             = "none"
        document.getElementById("admin_replication_view").style.display       = "none"
        document.getElementById("admin_connectiontable_view").style.display   = "none"
        document.getElementById("admin_storagestatus_view").style.display     = "none"
        if(this.state.websocket.connected === true && this.state.websocket.subscription !== undefined){
            this.state.websocket.subscription.unsubscribe()
        }

        let current_topic = null
        let handleFunction = null
        switch(actionName){
            case "log":{
                document.getElementById("admin_log_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Log Data"
                this.fetchLogByCriteriaUpdate(null, null)
                break;
            }
            case "content":{
                document.getElementById("admin_content_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Content Table"
                current_topic = "/topic/content"
                handleFunction = this.handleContentTable
                break;
            }
            case "storage":{
                document.getElementById("admin_storagestatus_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Storage Status Table"
                current_topic = "/topic/storage"
                handleFunction = this.handleStorageStatus
                break;
            }
            case "nodes":{
                document.getElementById("admin_nodes_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Nodes Status Table"
                current_topic = "/topic/nodes"
                handleFunction = this.handleNodesActivity
                break;
            }
            case "replication":{
                document.getElementById("admin_replication_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Replication Status"
                current_topic = "/topic/replication"
                handleFunction = this.handleReplicationManagerStatus
                break;
            }
            case "connection":{
                document.getElementById("admin_connectiontable_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Connection Table"
                current_topic = "/topic/connection"
                handleFunction = this.handleConnectionTable
                break;
            }
            default : break;
        }
        if(this.state.websocket.connected === true){
            this.setState({websocket : {"connected" : true, "subscription" : this.webSocketConnection.subscribe(current_topic, (msg) => handleFunction(msg))}})
        }

    }

    fetchLogByCriteriaUpdate = (criteria, updatevalue) => {
        if(criteria !== null && updatevalue !== null){
            this.logCriteria[criteria] = updatevalue
        }
        console.log(this.logCriteria)
        AdminHandlerService.fetchLog(this.logCriteria).then(response => {
            if(response.code === 1){
                this.setState({log : response.content})
            }
            else{
                this.setState({log : []})
            }
            console.log(response.content)
        })
    }

    cleanLog = () =>{
        AdminHandlerService.cleanLog(this.logCriteria).then(response => {
            if(response.code === 1){
                this.fetchLogByCriteriaUpdate(null, null)
            }
        })
    }

    fetchReplicationNodesForFile = (userId, filename) => {
        console.log("Fetching nodes that store " + filename + " of user " + userId)
        this.setState({content_nodes_data : []})
        AdminHandlerService.fetchNodesStoringFile(userId, filename).then(response => {
            console.log(response.content)
            document.getElementById("replication_nodes_div").style.display = "block"
            document.getElementById("versions_nodes_div").style.display = "none"
            if(response.code === 1){
                response.content.forEach(address => {
                    console.log(address)
                    AdminHandlerService.fetchNodeData(address).then(response => {
                        if(response.code === 1){
                            let content_nodes = this.state.content_nodes_data
                            content_nodes.push(response.content)
                            this.setState({content_nodes_data : content_nodes})
                        }
                    })
                })
            }
        })
    }

    render(){
        var availableNodesSelect = []
        var logData = []
        var content = []
        var replicationNodesForFile = []
        var versionForFile = []
        if(this.state.userType !== null){
            if(this.state.availableNodes != null){
                this.state.availableNodes.forEach(node => {
                    availableNodesSelect.push(<option key={`optionkey_${node["ip_address"]}`} value={node["ip_address"]}>{node["ip_address"]}</option>)
                })
            }
            if(this.state.log !== null){
                this.state.log.forEach(log_register => {
                    logData.push(
                        <tr key={`log_${log_register.registerId}`}>
                            <td>
                                <p>{log_register.node_address}</p>
                            </td>
                            <td>
                                <p>{log_register.message_type}</p>
                            </td>
                            <td>
                                <p>{log_register.description}</p>
                            </td>
                            <td>
                                <p>{log_register.register_date}</p>
                            </td>
                        </tr>
                    )
                })
            }
            if(this.state.content !== null){
                this.state.content.forEach(content_register => {
                    content.push(
                        <tr key={`content_${content_register.userId}_${content_register.filename}`}>
                            <td><p>{content_register.userId}</p></td>
                            <td><p>{content_register.filename}</p></td>
                            <td>
                                <a href="#"
                                    onClick={() => this.fetchFileVersions(content_register.userId, content_register.filename)} 
                                    onMouseOver={() => {
                                        document.getElementById("admin_view_title_1").style.visibility = "visible"
                                        document.getElementById("admin_view_title_1").innerHTML = "Click to see the version of this file"
                                    }}
                                    onMouseLeave={() => {
                                        document.getElementById("admin_view_title_1").style.visibility = "hidden"
                                    }} >
                                    {content_register.versionNo}
                                </a>
                            </td>
                            <td><p>{content_register.crc.toString(16)}</p></td>
                            <td><p>{GeneralPurposeService.getFileSizeUnit(content_register.fileSize)}</p></td>
                            <td>
                                <a href="#"
                                    onClick={() => this.fetchReplicationNodesForFile(content_register.userId, content_register.filename)} 
                                    onMouseOver={() => {
                                        document.getElementById("admin_view_title_1").style.visibility = "visible"
                                        document.getElementById("admin_view_title_1").innerHTML = "Click to see the nodes that store this file"
                                    }}
                                    onMouseLeave={() => {document.getElementById("admin_view_title_1").style.visibility = "hidden"}} >
                                    {content_register.replication_factor}
                                </a>
                            </td>
                            <td><p>{content_register.status}</p></td>
                        </tr>
                    )
                })
            }
            if(this.state.file_versions !== null){
                
                versionForFile.push(<div><p className="admin_view_title">This file has the following versions</p><br/><br/></div>)
                this.state.file_versions.forEach(version => {
                    versionForFile.push(
                        <p className="admin_view_title">{version.version_no}. {version.version_desc} ({version.version_hash.toString(16)})</p>
                    )
                })
            }
            if(this.state.content_nodes_data !== []){
                replicationNodesForFile.push(<div><p className="admin_view_title">This file is stored by the following internal nodes</p><br/><br/></div>)
                this.state.content_nodes_data.forEach(node => {
                    replicationNodesForFile.push(
                        <div>
                            <p className="admin_view_title">{node.ip_address} from {node.location_country}</p>
                        </div>
                    )
                })
            }
        }
        return(
            <div className="App">
                <div className="Home">
                    <div className="home_header">
                        <p id="admin_title">Admin console</p><br/>
                        
                        <hr style={{width:"90%"}}/>
                        <br/>
                    </div>
                        <div id="admin_log_view" className="admin_div_view">
                            <p className="admin_view_title">Message type</p>
                            <select onChange={(event) => {this.fetchLogByCriteriaUpdate("message_type",event.target.value)}}>
                                <option value="ALL">ALL</option>
                                <option value="SUCCESS">SUCCESS</option>
                                <option value="WARNING">WARNING</option>
                                <option value="ERROR">ERROR</option>
                            </select>
                            <p className="admin_view_title">Source node</p>
                            <select onChange={(event) => {this.fetchLogByCriteriaUpdate("node_address",event.target.value)}}>
                                <option value="ALL">ALL</option>
                                {availableNodesSelect}
                            </select>
                            <p className="admin_view_title">Time interval</p>
                            <select onChange={(event) => {this.fetchLogByCriteriaUpdate("date1",GeneralPurposeService.getCurrentTimestampForLogging(event.target.value))}}>
                                <option value="1 year">1 year</option>
                                <option value="1 month">1 month</option>
                                <option value="1 week">1 week</option>
                                <option value="1 day">1 day</option>
                                <option value="12 hours">12 hours</option>
                                <option value="6 hours">6 hours</option>
                                <option value="2 hours">2 hours</option>
                                <option value="1 hour">1 hour</option>
                                <option value="30 minutes">30 minutes</option>
                                <option value="15 minutes">15 minutes</option>
                                <option value="5 minutes">5 minutes</option>
                            </select>
                            <button onClick={() => this.fetchLogByCriteriaUpdate(null, null)}>&#x27F3;</button><br/><br/>
                            <button style={{marginTop:"-1%", marginBottom:"2%"}}onClick={() => this.cleanLog()}>Clean log with given criteria</button>
                            <br/>
                            {this.state.log.length === 0 ? 
                                <p className="admin_view_title">No log register found!</p> : 
                                <div>
                                    <table>
                                        <thead>
                                            <tr>
                                                <td>Address</td>
                                                <td>Message type</td>
                                                <td>Message</td>
                                                <td>Timestamp</td>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {logData}
                                        </tbody>
                                    </table>
                                </div>

                            }
                        </div>
                        <div id="admin_content_view" className="admin_div_view">
                            <p id="admin_view_title_0" className="admin_view_title">Fetching content...</p><br/><br/>
                            <br/>
                            {this.state.content === null ? <p></p> : this.state.content === undefined ? 
                                <p className="admin_view_title">No log register found!</p> : 
                                <div id="content_div">
                                    <table id="content_table">
                                        <thead>
                                            <tr>
                                                <th>User ID</th>
                                                <th>Filename</th>
                                                <th>Version No</th>
                                                <th>Hash</th>
                                                <th>Filesize</th>
                                                <th>Replicas</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {content}
                                        </tbody>
                                    </table>
                                    <div id="content_table_additional_data">
                                        <p className="admin_view_title">Content Status Table</p>
                                        <p id="admin_view_title_1" className="admin_view_title">&nbsp;</p>
                                        <div id="replication_nodes_div">{replicationNodesForFile}</div>
                                        <div id="versions_nodes_div">{versionForFile}</div>
                                    </div>
                                </div>
                            }
                        </div>
                        <div id="admin_storagestatus_view" className="admin_div_view">
                            <p id="admin_view_title_0">Fetching internal nodes storage status...</p><br/><br/>
                            <p id="admin_view_title_1">&nbsp;</p>
                            <br/>
                            {this.state.content === null || this.state.content.length === 0 ? 
                                <p>No log register found!</p> : 
                                <div>
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>User ID</th>
                                            <th>Filename</th>
                                            <th>Version No</th>
                                            <th>Hash</th>
                                            <th>Filesize</th>
                                            <th>Replication Factor</th>
                                            <th>Status</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            {content}
                                        </tbody>
                                    </table>
                                </div>
                            }
                        </div>
                        <div id="admin_nodes_view" className="admin_div_view">
                            <p id="admin_view_title">Fetching available nodes...</p>
                        </div>
                        <div id="admin_connectiontable_view" className="admin_div_view">
                            <p id="admin_view_title">Fetching available nodes...</p>
                        </div>
                        <div id="admin_replication_view" className="admin_div_view">
                            <p id="admin_view_title">Fetching replication manager status...</p>
                        </div>
                </div>
            </div>
        );
    }
}

export default AdminMainPage;

//https://developer.okta.com/blog/2018/09/25/spring-webflux-websockets-react
//https://dev.to/fpeluso/a-simple-websocket-between-java-and-react-5c98
//https://blog.cloudboost.io/simple-chat-react-java-6923b54d65a0

// https://programming.vip/docs/four-ways-of-integrating-websocket-with-spring-boot.html