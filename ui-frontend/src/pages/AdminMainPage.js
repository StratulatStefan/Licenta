import React, { Component }  from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import { GeneralPurposeService } from '../services/GeneralPurposeService';
import { AdminHandlerService }   from '../services/AdminHandlerService';
import { Environment }           from '../environment';
import { PieChart }              from 'react-minimal-pie-chart';

import '../styles/pages-style.css';
import '../styles/pages-home-style.css';
import '../styles/pages-admin-style.css';

class AdminMainPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Admin console";
        this.userData = localStorage.getItem('user_data')
        this.menu_selection = null
        this.state = {
            isUserConnected    : false,
            userType           : null,
            accountAvailable   : true,
            availableNodes     : null,
            log                : null,
            replication_status : null,
            content            : null,
            content_nodes_data : null,
            file_versions      : null,
            storagestatus      : null,
            current_address    : null,
            connectionTable    : {"addresses" : null, "status" : null, "current_address" : null},
            websocket          : {"connected" : false, "subscriptions" : null},
            error_status       : {code : -1, message : ""}
        }
        this.logCriteria = {message_type : "ALL", node_address : "ALL", date1 : GeneralPurposeService.getCurrentTimestampForLogging("1 year")}
        this.webSocketConnection = null;
        this.selectedFile        = null
        GeneralPurposeService.setHeaderLayout("ADMIN")
        document.getElementById("log_redirector").onclick = () => this.adminAction("log")
        document.getElementById("content-table_redirector").onclick = () => this.adminAction("content")
        document.getElementById("storage-table_redirector").onclick = () => this.adminAction("storage")
        document.getElementById("nodes-status_redirector").onclick = () => this.adminAction("nodes")
        document.getElementById("replication-status_redirector").onclick = () => this.adminAction("replication")
    }

    componentDidMount = () => {
        this.checkForConnectedUser()
        this.fetchUserType().then(usertype_result => {
            if(usertype_result !== undefined){
                this.fetchAvailableNodes().then(response => {
                    if(response !== undefined){
                        if(this.state.error_status.code === -1){
                            this.adminAction('log')
                            Environment.getWebSocket().then(response => {
                                this.webSocketConnection = response
                                this.setState({websocket : {"connected" : true}}, () => {
                                    if(this.menu_selection !== null){
                                        this.adminAction(this.menu_selection)
                                    }
                                })
                            })
                        }
                    }
                })
            }
        })
    }

    fetchAvailableNodes = () => {
        return new Promise(resolve => {
            AdminHandlerService.fetchAvailableNodesFromAPI(this.userData["jwt"]).then(response => {
                if(response.code === 1){
                    this.setState({availableNodes : response.content})
                    resolve(null)
                }
                else{
                    this.setState({error_status : {code : response.code, message : response.content}})
                    if(response.code === 401 || response.code === 402){
                        resolve(undefined)
                    }
                    else{
                        resolve(null)
                    }
                }
            })
        })
    }

    fetchUserType = () => {
        return new Promise(resolve => {
            try{
                this.setState({userType : this.props.location.state.detail["user_type"]})
                resolve(null)
            }
            catch(e){
                UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                    if(response.code === 1){
                        this.setState({userType : response["content"]})
                        resolve(null)
                    }
                    else{
                        this.setState({error_status : {code : response.code, message : response.content}})
                        if(response.code === 401 || response.code === 402){
                            resolve(undefined)
                        }
                        else{
                            resolve(null)
                        }
                    }
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
            let content = JSON.parse(message.body)
            console.log(content)
            if(content.length > 0){
                this.setState({content : content})
            }
            else{
                this.setState({content : undefined})
            }
        }
    }

    fetchFileVersions = (userid, filename) => {
        AdminHandlerService.getFileVersions(this.userData["jwt"], userid, filename).then(response => {
            if(response.code === 1){
                this.setState({file_versions : response.content})
                document.getElementById("replication_nodes_div").style.display = "none"
                document.getElementById("versions_nodes_div").style.display = "block"
                document.getElementById("subject_filename").innerHTML = filename
            }
            else{
                this.setState({error_status : {code : response.code, message : response.content}})
            }
        })
    }

    handleReplicationManagerStatus = (message) => {
        if (message.body) {
            let content = JSON.parse(message.body)
            console.log(content)
            if(content.length > 0){
                this.setState({replication_status : content})
            }
            else{
                this.setState({replication_status : undefined})
            }
        }
    }

    handleConnectionTable = (message) => {
        if (message.body) {
            let content = JSON.parse(message.body)
            let nodes_details = this.state.connectionTable.status
            let current_address = this.state.connectionTable.current_address
            this.setState({connectionTable : {"addresses" : content, "status" : nodes_details, "current_address" : current_address}})
        }
    }

    handleNodesActivity = (message) => {
        if (message.body) {
            let addresses = this.state.connectionTable.addresses
            let current_address = this.state.connectionTable.current_address
            this.setState({connectionTable : {"addresses" : addresses, "status" : JSON.parse(message.body), "current_address" : current_address}})
        }
    }

    highlightNodeStatus = (selectedAddress, label) => {
        let addressList = null
        let identificator = null
        if(label === 0){
            addressList = this.state.connectionTable.addresses
            identificator = "node_status_address"
            
        }
        else if(label === 1){
            addressList = Object.keys(this.state.storagestatus)
            identificator = "storage_status_address"
        }

        addressList.forEach(address => {
            if(address === selectedAddress){
                document.getElementById(`${identificator}_${address}`).style.fontSize = "125%" 
                document.getElementById(`${identificator}_${address}`).style.color = "#39A9CB"; 
            }
            else{
                document.getElementById(`${identificator}_${address}`).style.fontSize = "105%" 
                document.getElementById(`${identificator}_${address}`).style.color = "#344fa1"; 
            }
        })
        
    }

    handleStorageStatus = (message) => {
        if (message.body) {
            let storage_status = {}
            let first_address = this.state.current_address
            let content = JSON.parse(message.body)
            if(content.length > 0){
                content.forEach(register => {
                    register.nodesAddresses.forEach(address => {
                        if(first_address === null){
                            first_address = address
                        }
                        let node_registers = []
                        if(Object.keys(storage_status).includes(address)){
                            node_registers = storage_status[address]
                        }
                        node_registers.push(register)
                        storage_status[address] = node_registers
                    })
                })
                this.setState({storagestatus : storage_status, current_address : first_address})
            }
            else{
                this.setState({storagestatus : undefined, current_address : null})
            }
        }
    }

    adminAction = (actionName) => {
        if(this.state.error_status.code !== 401 && this.state.error_status.code !== 402){
            this.menu_selection = actionName
            let views = ["admin_log_view", "admin_content_view", "admin_nodes_view",
                "admin_replication_view", "admin_storagestatus_view"    
            ]

            views.forEach(view => {
                document.getElementById(view).style.display = "none"
            })

            if(this.state.websocket.connected === true && this.state.websocket.subscriptions !== undefined){
                this.state.websocket.subscriptions.forEach(_ => _.unsubscribe())
            }

        let current_topic = []
        let handleFunction = []
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
                current_topic = ["/topic/content"]
                handleFunction = [this.handleContentTable]
                break;
            }
            case "storage":{
                document.getElementById("admin_storagestatus_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Storage Status Table"
                current_topic = ["/topic/storage"]
                handleFunction = [this.handleStorageStatus]
                break;
            }
            case "nodes":{
                document.getElementById("admin_nodes_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Nodes Status Table"
                current_topic = ["/topic/nodes", "/topic/connection"]
                handleFunction = [this.handleNodesActivity, this.handleConnectionTable]
                break;
            }
            case "replication":{
                document.getElementById("admin_replication_view").style.display = "block"
                document.getElementById("admin_title").innerHTML = "Replication Status"
                current_topic = ["/topic/replication"]
                handleFunction = [this.handleReplicationManagerStatus]
                break;
            }
            default : break;
        }
        if(this.state.websocket.connected === true){
            let subscriptions = []
            let index = 0;
            current_topic.forEach(topic => {
                let func = handleFunction[index]
                subscriptions.push(this.webSocketConnection.subscribe(topic, (msg) => func(msg)))
                index += 1
            })
            this.setState({websocket : {"connected" : true, "subscriptions" : subscriptions}})
        }
    }
    }

    fetchLogByCriteriaUpdate = (criteria, updatevalue) => {
        if(criteria !== null && updatevalue !== null){
            this.logCriteria[criteria] = updatevalue
        }
        AdminHandlerService.fetchLog(this.userData["jwt"], this.logCriteria).then(response => {
            if(response.code === 1){
                this.setState({log : response.content})
            }
            else{
                this.setState({log : []})
            }
        })
    }

    cleanLog = () =>{
        AdminHandlerService.cleanLog(this.userData["jwt"], this.logCriteria).then(response => {
            if(response.code === 1){
                this.fetchLogByCriteriaUpdate(null, null)
            }
            else{
                this.setState({error_status : {code : response.code, message : response.content}})
            }
        })
    }

    fetchReplicationNodesForFile = (userId, filename) => {
        this.setState({content_nodes_data : []})
        AdminHandlerService.fetchNodesStoringFile(this.userData["jwt"], userId, filename).then(response => {
            if(response.code === 1){
                document.getElementById("replication_nodes_div").style.display = "block"
                document.getElementById("versions_nodes_div").style.display = "none"
                document.getElementById("subject_filename").innerHTML = filename
                response.content.forEach(address => {
                    AdminHandlerService.fetchNodeData(this.userData["jwt"], address).then(response => {
                        if(response.code === 1){
                            let content_nodes = this.state.content_nodes_data
                            content_nodes.push(response.content)
                            this.setState({content_nodes_data : content_nodes})
                        }
                        else{
                            this.setState({error_status : {code : response.code, message : response.content}})
                        }
                    })
                })
            }
            else{
                this.setState({error_status : {code : response.code, message : response.content}})
            }
        })
    }

    deleteFileFromInternalNode = () => {
        AdminHandlerService.deleteFileFromInternalNode(this.userData["jwt"], this.selectedFile).then(response => {
            if(response.code === 1){
                document.getElementById("storagestatus_delete_status").style.visibility = "visible"
            }
            else{
                this.setState({error_status : {code : response.code, message : response.content}})
            }
        })
    }

    redirect = (destination) => {
        if(destination !== ""){
            this.props.history.push(destination)
        }
        else{
            this.props.history.push("/")
        }
    }

    render(){
        var replicationNodesForFile = []
        var versionForFile = []
        var availableNodesSelect = []
        var logData = []
        var content = []
        var storagestatus_nodes = {addresses : [], files : [], additional_data : []}
        var nodes_status = {addresses : [], status : []}
        var replication_status = []
        if(this.state.userType !== null && this.state.code !== 402){
            if(this.state.availableNodes != null){
                this.state.availableNodes.forEach(node => {
                    availableNodesSelect.push(<option key={`optionkey_${node["ip_address"]}`} value={node["ip_address"]}>{node["ip_address"]}</option>)
                })
            }
            if(this.state.log !== null){
                this.state.log.forEach(log_register => {
                    logData.push(
                        <tr key={`log_${log_register.registerId}`}>
                            <td><p>{log_register.node_address}</p></td>
                            <td><p>{log_register.message_type}</p></td>
                            <td><p>{log_register.description}</p></td>
                            <td><p>{GeneralPurposeService.getCurrentTimestamp(new Date(log_register.register_date))}</p></td>
                        </tr>
                    )
                })
            }
            if(this.state.content !== null){
                document.getElementById("admin_view_title_0").style.visibility = "hidden"
                if(this.state.content !== undefined){
                    this.state.content.forEach(content_register => {
                        content.push(
                            <tr key={`content_${content_register.userId}_${content_register.filename}`}>
                                <td><p>{content_register.userId}</p></td>
                                <td><p>{content_register.filename}</p></td>
                                <td>
                                    {content_register.status === "[DELETED]"? <p>{content_register.versionNo}</p> : 
                                        <p><button className="a_redirector"
                                            style={{fontSize:"120%", textDecoration:"underline"}} 
                                            onClick={() => {
                                                this.fetchFileVersions(content_register.userId, content_register.filename)
                                            }}
                                            onMouseOver={() => {
                                                document.getElementById("admin_view_title_1").style.visibility = "visible"
                                                document.getElementById("admin_view_title_1").innerHTML = "Click to see the version of this file"
                                            }}
                                            onMouseLeave={() => {
                                                document.getElementById("admin_view_title_1").style.visibility = "hidden"
                                            }}
                                            >{content_register.versionNo}</button>
                                        </p>
                                    }
                                </td>
                                <td><p>{content_register.crc.toString(16)}</p></td>
                                <td><p>{GeneralPurposeService.getFileSizeUnit(content_register.fileSize)}</p></td>
                                <td>
                                    {content_register.status === "[DELETED]"? <p>{content_register.replication_factor}</p> : 
                                        <p><button className="a_redirector"
                                            style={{fontSize:"120%", textDecoration:"underline"}} 
                                            onClick={() => this.fetchReplicationNodesForFile(content_register.userId, content_register.filename)} 
                                            onMouseOver={() => {
                                                document.getElementById("admin_view_title_1").style.visibility = "visible"
                                                document.getElementById("admin_view_title_1").innerHTML = "Click to see the nodes that store this file"
                                            }}
                                            onMouseLeave={() => {document.getElementById("admin_view_title_1").style.visibility = "hidden"}}
                                            >{content_register.replication_factor}</button>
                                        </p>
                                    }
                                </td>
                                <td><p>{content_register.status}</p></td>
                            </tr>
                        )
                    })
                }
            }
            if(this.state.file_versions !== null){
                this.state.file_versions.forEach(version => {
                    versionForFile.push(
                        <p className="admin_view_title" style={{fontSize:"90%"}}>{version.version_no}. {version.version_desc} ({version.version_hash.toString(16)})</p>
                    )
                })
            }
            if(this.state.content_nodes_data !== null){
                this.state.content_nodes_data.forEach(node => {
                    replicationNodesForFile.push(
                        <div>
                            <p className="admin_view_title" style={{fontSize:"90%"}}>{node.ip_address} from {node.location_country}</p>
                        </div>
                    )
                })
            }
            if(this.state.storagestatus !== null && this.state.storagestatus !== undefined){
                replicationNodesForFile = []
                storagestatus_nodes.addresses = []
                Object.keys(this.state.storagestatus).forEach(address => {
                    storagestatus_nodes.addresses.push(
                        <button
                            id = {`storage_status_address_${address}`}  
                            className="a_redirector" 
                            href="#"
                            style={{marginTop:"-15%"}} 
                            onClick={() => { 
                                this.setState({current_address : address})
                                document.getElementById("storage_additional_data").style.visibility = "hidden"
                                document.getElementById("storagestatus_delete_status").style.visibility = "hidden"
                                document.getElementById("subject_filename_1").innerHTML = ""
                                this.highlightNodeStatus(address, 1)
                            }}>{address}
                        </button>
                    )
                })
            }
            if(this.state.current_address !== null && this.state.current_address !== undefined){
                storagestatus_nodes.files = []
                console.log(this.state.storagestatus[this.state.current_address])
                if(this.state.storagestatus[this.state.current_address] !== undefined){
                    this.state.storagestatus[this.state.current_address].forEach(file => {
                        let indexOfAddress = file.nodesAddresses.indexOf(this.state.current_address)
                        let another_nodes = []
                        file.nodesAddresses.forEach(address => {
                            if(address !== this.state.current_address){
                                another_nodes.push(<p style={{display:"block", fontSize: "80%"}}>{address}</p>)
                            }
                        })
                        storagestatus_nodes.files.push(
                            <tr key={`storagestatus_${file.userId}_${file.filename}`}>
                                <td><p>{file.userId}</p></td>
                                <td>
                                    <p><button className="a_redirector"
                                        style={{fontSize:"120%", textDecoration:"underline"}} 
                                        onClick={() => {
                                            document.getElementById("storagestatus_delete_status").style.visibility = "hidden"
                                            document.getElementById("storage_additional_data").style.visibility = "visible"
                                            document.getElementById("subject_filename_1").innerHTML = file.filename
                                            this.selectedFile = {"user" : file.userId, "filename" : file.filename, "address" : this.state.current_address}
                                        }} 
                                        onMouseOver={() => {
                                            document.getElementById("admin_view_title_11").style.visibility = "visible"
                                            document.getElementById("admin_view_title_11").innerHTML = "Click for more details about this file"
                                        }}
                                        onMouseLeave={() => {
                                            document.getElementById("admin_view_title_11").style.visibility = "hidden"
                                        }}
                                        >{file.filename}</button>
                                    </p>
                                </td>
                                <td><p>{file.nodesVersions[indexOfAddress]}</p></td>
                                <td><p>{file.nodesCRCs[indexOfAddress].toString(16)}</p></td>
                                <td>{another_nodes}</td>
                            </tr>
                        )
                    })
                }
            }

            if(this.state.connectionTable.addresses !== null){
                nodes_status.addresses = []
                let first_address = this.state.connectionTable.current_address
                console.log("first_address: " + first_address)
                this.state.connectionTable.addresses.forEach(address => {
                    if(first_address === null || first_address === undefined){
                        first_address = address;
                        let addresses = this.state.connectionTable.addresses
                        let status = this.state.connectionTable.status
                        this.setState({connectionTable : {addresses : addresses, status : status, current_address : first_address}})
                    }
                    nodes_status.addresses.push(
                        <button
                            id = {`node_status_address_${address}`} 
                            className = "a_redirector" 
                            href="#"
                            style={{marginTop:"-10%"}} 
                            onClick={() => {
                                let addresses = this.state.connectionTable.addresses
                                let status = this.state.connectionTable.status
                                this.highlightNodeStatus(address, 0)
                                this.setState({connectionTable : {addresses : addresses, status : status, current_address : address}})
                            }}>{address}
                        </button>
                    )                          
                })
            }

            if(this.state.connectionTable.current_address !== null && this.state.connectionTable.current_address !== undefined){
                let warnings = 0
                let errors = 0
                let internal_log = this.state.log.filter(register => register.node_address === this.state.connectionTable.current_address)
                internal_log.forEach(register => {
                    if(register.message_type === "WARNING"){
                        warnings += 1
                    }
                    if(register.message_type === "ERROR"){
                        errors += 1
                    }
                })
                nodes_status.status = []
                nodes_status.status.push(<p className="admin_view_title">The status of the internal node</p>)
                nodes_status.status.push(<br/>)
                nodes_status.status.push(<p className="admin_view_title">{this.state.connectionTable.current_address}</p>)
                nodes_status.status.push(<br/>)
                nodes_status.status.push(<br/>)
                if(this.state.availableNodes !== null){
                    this.state.availableNodes.forEach(node => {
                        if(node.ip_address === this.state.connectionTable.current_address){
                            nodes_status.status.push(<p className="admin_view_title">Location : {node.location_country}</p>)
                            nodes_status.status.push(<br/>)
                            nodes_status.status.push(<br/>)
                        }
                    })
                }
                if(this.state.connectionTable.status !== null){
                    this.state.connectionTable.status.forEach(address => {
                        if(address.ip_address === this.state.connectionTable.current_address){
                                let used_storage  = GeneralPurposeService.getFileSizeUnit(address.used_storage)
                            let total_storage = GeneralPurposeService.getFileSizeUnit(address.total_storage)
                            let percent = address.used_storage / address.total_storage * 100
                            percent = Math.round(percent * 100000) / 100000

                            nodes_status.status.push(<p className="admin_view_title">
                                Used storage<br/><br/>
                                {used_storage} / {total_storage}&nbsp;&nbsp;&nbsp;
                                ({percent} %)
                            </p>)
                            nodes_status.status.push(<br/>)
                            nodes_status.status.push(<br/>)
                            used_storage = Math.ceil(address.used_storage / 1000)
                            let available_storage = Math.ceil((address.total_storage - used_storage) / 1000)
                            if(used_storage < available_storage * 0.005){
                                used_storage = available_storage * 0.005 + used_storage
                            }

                            nodes_status.status.push(
                                <PieChart
                                    style={{width:"20%", height:"20%"}}
                                    data={[
                                        { title: 'Used Storage', value: used_storage, color: '#c84b31' },
                                        { title: 'Available Storage', value: available_storage, color: '#1eae98' },
                                    ]}
                                />
                            )
                        }
                    })
                }
                nodes_status.status.push(<br/>)
                nodes_status.status.push(<br/>)
                nodes_status.status.push(<p className="admin_view_title">This node encountered {warnings} warnings and {errors} errors.</p>)
                nodes_status.status.push(<br/>)
                nodes_status.status.push(<br/>)
            }
            if(this.state.replication_status !== null && this.state.replication_status !== undefined){
                replication_status = []
                this.state.replication_status.forEach(status => {
                    replication_status.push(<p className="admin_view_title">{status}</p>)
                    replication_status.push(<br/>)
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
                    {this.state.error_status.code === 401 || this.state.error_status.code === 402 ?
                        <div>
                            <p className="admin_view_title" style={{fontSize:"100%"}}>{this.state.error_status.message}</p>
                            <br/><br/>
                            <button style={{marginTop:"-1%", marginBottom:"2%", fontSize:"80%"}} onClick={() => {
                                localStorage.setItem("user_data", '')
                                this.redirect("/")
                            }}>Go to login page</button>
                        </div>:
                        <div>
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
                                <button onClick={() => this.fetchLogByCriteriaUpdate(null, null)}>&#x27F3;</button><br/><br/>
                                <button style={{marginTop:"-1%", marginBottom:"2%"}}onClick={() => this.cleanLog()}>Clean log with given criteria</button>
                                <br/>
                                {this.state.log === null ? <p className="admin_view_title">Fetching log data...</p> : 
                                    this.state.log.length === 0 ? 
                                        <p className="admin_view_title">No log register found!</p> : 
                                        <div>
                                            <p className="admin_view_title" id="log_registers_count">Found {this.state.log.length} log registers.</p>
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
                                {this.state.websocket.connected === false ? 
                                <p className="admin_view_title">Initializing the connection ...<br/><br/></p> : 
                                <p id="admin_view_title_0" className="admin_view_title">Fetching content...<br/><br/></p>}
                                <br/>
                                {this.state.content === null ? <p></p> : 
                                    this.state.content === undefined ? 
                                        this.state.error_status.code === 0 ? <p className="admin_view_title">{this.state.error_status.message}</p> :
                                        <div style={{width:"100%"}}>
                                            <img style={{maxHeight:"350px", maxWidth:"350px"}} src= "/images/not_found.png" />
                                            <br/><br/><br/>
                                            <p className="admin_view_title">No register found in the Content Table!</p>
                                            <br/>
                                        </div> : 
                                        <div className="content_div" id="contentDiv">
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
                                                <p className="admin_view_title">Content Status Table</p><br/>
                                                <p id="admin_view_title_1" className="admin_view_title">&nbsp;</p><br/>
                                                <hr/><br/>
                                                <p className="admin_view_title" 
                                                    id="subject_filename">
                                                </p><br/>
                                                <div id="replication_nodes_div">{replicationNodesForFile}</div>
                                                <div id="versions_nodes_div">{versionForFile}</div>
                                            </div>
                                        </div>
                                }
                            </div>
                            <div id="admin_storagestatus_view" className="admin_div_view">
                                {this.state.websocket.connected === false ? <p className="admin_view_title">Initializing the connection ...<br/><br/></p> : 
                                    this.state.storagestatus === null ? 
                                        <p className="admin_view_title">Fetching internal nodes storage status...</p> : 
                                        this.state.storagestatus === undefined ? 
                                            this.state.error_status.code === 0 ? <p className="admin_view_title">{this.state.error_status.message}</p> :
                                            <div style={{width:"100%"}}>
                                                <br/><br/><br/><br/>
                                                <img style={{maxHeight:"350px", maxWidth:"350px"}} src= "/images/not_found.png" />
                                                <br/><br/><br/>
                                                <p className="admin_view_title">No register found in the Storage Status Table!</p>
                                                <br/>
                                            </div>:
                                        <div>
                                            {storagestatus_nodes.addresses}
                                            <br/><br/>
                                            <div className="content_div" id="storageDiv">
                                                <table id="content_table">
                                                    <thead>
                                                        <tr>
                                                            <th>User ID</th>
                                                            <th>Filename</th>
                                                            <th>Version No</th>
                                                            <th>Hash</th>
                                                            <th>Another nodes</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {storagestatus_nodes.files}
                                                    </tbody>
                                                </table>
                                                <div id="content_table_additional_data">
                                                    <p className="admin_view_title">Storage Status Table</p><br/>
                                                    <p id="admin_view_title_11" className="admin_view_title">&nbsp;</p><br/>
                                                    <hr/>
                                                    <p className="admin_view_title" 
                                                        id="subject_filename_1">
                                                    </p><br/>
                                                    <div id="storage_additional_data" style={{visibility:"hidden"}}>
                                                        <p className="admin_view_title">Delete this file from this node.<br/>This will trigger a replication to another node.</p>
                                                        <br/><button className="redirector" onClick={this.deleteFileFromInternalNode}>Delete</button>
                                                        <p className="admin_view_title" id="storagestatus_delete_status" style={{visibility : "hidden"}}>
                                                            File successfully deleted from node.<br/>
                                                            The change will be visible at the next update from General Manager<br/>
                                                            You can also check the Replication Manager to see the workaround.
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                }
                            </div>
                            <div id="admin_nodes_view" className="admin_div_view">
                            {this.state.websocket.connected === false ? <p className="admin_view_title">Initializing the connection ...<br/><br/></p> : 
                                this.state.connectionTable.addresses === null ? 
                                    <p className="admin_view_title">Fetching internal nodes status...</p> : 
                                    this.state.connectionTable.addresses === undefined && this.state.error_status.code === 0? 
                                        <p className="admin_view_title">{this.state.error_status.message}</p> :
                                            this.state.connectionTable.addresses === [] ? 
                                                <p className="admin_view_title">No internal node found!</p> :
                                                <div>
                                                    {nodes_status.addresses}
                                                    <br/><br/>
                                                    <div className="node_status">
                                                        {nodes_status.status}
                                                     </div>
                                                </div>
                            }
                            </div>
                            <div id="admin_replication_view" className="admin_div_view">
                            {this.state.websocket.connected === false ? <p className="admin_view_title">Initializing the connection ...<br/><br/></p> : 
                                this.state.replication_status === null ? 
                                    <p className="admin_view_title">Fetching replication manager status...</p> : 
                                    this.state.replication_status === undefined ? 
                                        this.state.error_status.code === 0 ? <p className="admin_view_title">{this.state.error_status.message}</p> :
                                        <div style={{width:"100%"}}>
                                            <br/><br/><br/><br/>
                                            <img style={{maxHeight:"350px", maxWidth:"350px"}} src= "/images/not_found.png" />
                                            <br/><br/><br/>
                                            <p className="admin_view_title">No register found in the Replication Manager Table!</p>
                                            <br/>
                                        </div> :
                                    <div>
                                        <div className="node_status">
                                            <p className="admin_view_title">Replication Manager Status</p>
                                            <br/><br/>
                                            {replication_status}
                                        </div>
                                    </div>
                                }
                            </div>
                        </div>}
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