import React, { Component }  from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import { FileHandlerService }    from '../services/FileHandlerService';
import { GeneralPurposeService } from '../services/GeneralPurposeService';

import '../styles/pages-style.css';
import '../styles/pages_usermain.css';
import '../styles/pages-home-style.css';

class FileDetailsPage extends Component {
    static userActions = ["download", "delete", "rename", "update", "no-selected"]

    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Home Page";
        this.userData = localStorage.getItem('user_data')
        this.description = null
        this.newname     = null
        this.fileSuccessfullyDownloaded = false
        this.state = {
            isUserConnected            : false,
            userType                   : null,
            accountAvailable           : true,
            accountSuccessfullyCreated : false,
            currentFileName            : null,
            availableNodes             : null,
            fileDetails                : null,
            userFile                   : null,
            filePreview                : <p>No preview avaialble!</p>,
            currentSelectedAction      : FileDetailsPage.userActions[4]
        }
    }

    componentDidMount = () => {
        this.checkForConnectedUser()
        this.fetchUserType().then(_ => {
            let userFile = null
            try{
                userFile = this.props.location.state.detail.user_file
                localStorage.setItem("userfile", JSON.stringify(userFile))
            }
            catch(e){
                try{
                    userFile = JSON.parse(localStorage.getItem('userfile'))
                }
                catch(e){
                    console.log("How did you get here ? ")
                }
            }
            if(userFile !== null){
                document.getElementById("p_filename").innerHTML = userFile.filename
                this.fileDetails(userFile)
                this.setState({userFile : userFile})
            }
        })
    }

    componentDidUpdate = () => {
        if(this.state.currentSelectedAction !== FileDetailsPage.userActions[4]){
            FileDetailsPage.userActions.forEach(action => {
                if(action !== FileDetailsPage.userActions[4]){
                    document.getElementById(`selector_${action}`).style.borderBottom = 
                        (action === this.state.currentSelectedAction) ? "3px solid #23049d" : "none";
                }
            })
        }
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

    redirect = (destination) => {
        if(destination !== ""){
            this.props.history.push(destination)
        }
        else{
            this.props.history.push("/")
        }
    }

    fileDetails = (file) => {
        FileHandlerService.getFileHistory(this.userData["jwt"], file.filename).then(response => {
            this.setState({fileDetails : response.content, currentFileName : file.filename}, () => {this.downloadFile()})
        })
    }

    downloadFile = () => {
        if(this.fileSuccessfullyDownloaded === true){
            document.getElementById("file_status").innerHTML = "Downloading file..."
            document.getElementById("downloaduri").click()
            document.getElementById("file_status").innerHTML = "File successfully downloaded!"
        }
        else{
            FileHandlerService.downloadFile(this.userData["jwt"], this.state.currentFileName).then(response => {
                document.getElementById("downloaduri").href = response.content
                let fileType = GeneralPurposeService.getFileType(this.state.currentFileName)
                if(fileType === "image"){
                    this.setState({filePreview : <embed type="image/jpg" src={response.content}/>})
                }
                else if(fileType === "text"){
                    this.setState({filePreview : <iframe title={this.state.currentFileName} src={response.content} alt="No preview available"></iframe>})
                }
                else{
                    this.setState({filePreview : <p>No preview avaialble!</p>})
                } 
                this.fileSuccessfullyDownloaded = true
            })
        }
    }

    deleteFile = () => {
        document.getElementById("file_status_delete").innerHTML = "Deleting file..."
        document.getElementById("delete_file_button").style.visibility = "hidden"
        FileHandlerService.deleteFile(this.userData["jwt"], this.state.currentFileName, this.description).then(response => {
            document.getElementById("file_status_delete").innerHTML = "File successfully deleted. Please Refresh."
            document.getElementById("delete_file_button").style.visibility = "visible"
            document.getElementById("delete_file_button").innerHTML = "Go to my files"
            document.getElementById("delete_file_button").onclick = () => this.redirect("")
        })
    }

    renameFile = () => {
        if(this.newname === null || this.newname === ""){
            document.getElementById("file_status_rename_1").innerHTML = "Please provide a valid filename!"
        }
        else if(GeneralPurposeService.getFileExtension(this.newname) !== GeneralPurposeService.getFileExtension(this.state.currentFileName)){
            document.getElementById("file_status_rename_1").innerHTML = "You are trying to change the file type ? <br/> This is now allowed"
        }
        else if(this.description === null || this.description === ""){
            document.getElementById("file_status_rename_1").innerHTML = "Please provide a valid description!"
        }
        else{
            FileHandlerService.renameFile(this.userData["jwt"], this.state.currentFileName, this.newname, this.description).then(response => {
                document.getElementById("file_status_rename_1").innerHTML = "File successfully renamed."
                document.getElementById("p_filename").innerHTML = this.newname;
                let userFile = this.state.userFile
                userFile.filename = this.newname
                this.setState({userFile : userFile}, () => localStorage.setItem("userfile", JSON.stringify(userFile)))
            })
        }

    }

    updateAction = (actionIndex) => {
        document.getElementById("download_action").style.display = "none"
        document.getElementById("delete_action").style.display   = "none"
        document.getElementById("rename_action").style.display   = "none"
        document.getElementById("update_action").style.display   = "none"

        document.getElementById(`${FileDetailsPage.userActions[actionIndex]}_action`).style.display = "block"
        this.setState({currentSelectedAction: FileDetailsPage.userActions[actionIndex]})
    }

    render(){
        var fileDetails = []
        if(this.state.userType !== null){
            if(this.state.fileDetails !== null){
                this.state.fileDetails.forEach(detail => {
                    console.log(detail)
                    fileDetails.push(
                        <div className="details_history">
                            <label>{detail.version_no} : {detail.version_hash.toString(16)}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{detail.version_desc}</label><br/><br/>
                            <label>{detail.version_timestamp}</label><br/><br/><br/>
                        </div>)
                    fileDetails.push(<br/>)
                })
            }
        }
        return(
            <div className="App">
                <div className="Home">
                    <div>
                        <div className="home_header" id="home_header_details">
                            <p id="p_filename" style={{width:"100%",textAlign:"center"}}></p>
                        </div>
                        <hr id="mainpage_hr"/>
                        <br/><br/>
                        <div className = "home_body_main_div">
                            <div id="file_versions">
                                {this.state.userFile !== null ? 
                                    <div>
                                        <p>Details</p>
                                        <hr style={{width:"50%", marginBottom:"10%"}}/>
                                        <div>
                                            <label>Hash : {this.state.userFile.hash.toString(16)}</label><br/><br/>
                                            <label>Size : {GeneralPurposeService.getFileSizeUnit(this.state.userFile.filesize)}</label><br/><br/>
                                            <label>Version : {this.state.userFile.version}</label><br/>
                                        </div>
                                        <br/><br/>
                                        <p>History</p>
                                        <hr style={{width:"50%", marginBottom:"10%"}}/>
                                        {fileDetails}
                                    </div> : <p></p>
                                }
                            </div>
                            <div id="previews">
                                {this.state.filePreview}
                            </div>
                            <div id="file_details">
                                <p>Actions</p>
                                <hr/>
                                <button className="a_redirector" id="selector_download" style={{fontSize:"90%"}} 
                                    onClick={() => {
                                        this.updateAction(0)
                                        this.downloadFile()
                                    }}>Download
                                </button>
                                <button className="a_redirector" id="selector_delete"  style={{fontSize:"90%"}} onClick={() => this.updateAction(1)}>Delete</button>
                                <button className="a_redirector" id="selector_rename"  style={{fontSize:"90%"}} onClick={() => this.updateAction(2)}>Rename</button>
                                <button className="a_redirector" id="selector_update"  style={{fontSize:"90%"}} onClick={() => this.updateAction(3)}>Update</button>
                                <a id="downloaduri" href="/" download>&nbsp;</a><br/>
                                <div className="action_div" id="download_action">
                                    <p id="file_status"></p>
                                </div>
                                <div className="action_div" id="delete_action">
                                    <p id="file_status_delete">The erasing procedure is irreversible.<br/> 
                                        Your files will be deleted permanently.<br/><br/>
                                        Do you want to proceed ? 
                                    </p>
                                    <button id="delete_file_button" style={{fontSize:"80%", padding : 0, paddingLeft:"5px", paddingRight:"5px", height:"40px"}} 
                                        onClick={() => this.deleteFile()}>Delete my file
                                    </button>
                                </div>
                                <div className="action_div" id="rename_action">
                                    <p id="file_status_rename">Please provide the new file name<br/></p>
                                    <input
                                        onChange={(event) => {this.newname = event.target.value}} 
                                        type="text" />
                                    <br/>
                                    <p id="file_status_rename">Please also provide a description for your update<br/></p>
                                    <input
                                        onChange={(event) => {this.description = event.target.value}} 
                                        type="text" /><br/><br/>
                                    <button id="rename_file_button" style={{fontSize:"80%", padding : 0, paddingLeft:"5px", paddingRight:"5px", height:"40px"}} 
                                        onClick={() => this.renameFile()}>Rename my file
                                    </button>
                                    <p id="file_status_rename_1"><br/></p>

                                    <br/><br/>
                                </div>
                                <div className="action_div" id="update_action">
                                    <p id="file_status_update">
                                        Direct update of file is not supported yet<br/><br/>
                                        Please update your file locally and upload it<br/>
                                        The new version will be registered<br/><br/>
                                    </p>
                                    <button id="rename_file_button" style={{fontSize:"80%", padding : 0, paddingLeft:"5px", paddingRight:"5px", height:"40px"}} 
                                        onClick={() => this.redirect("/upload")}>Go to upload page
                                    </button>
                                </div>
                                <div className="request_description">
                                    <p id="input_label">New name : </p>
                                    <input
                                        onChange={(event) => {this.newname = event.target.value}} 
                                        type="text" />
                                </div>
                                <div className="request_description">
                                    <button className="file_button" id="proceed_btn" onClick={() => this.renameFile(1)}>Rename</button>
                                </div>
                                <br/>
                                <p id="file_status"></p>
                            </div>
                        </div>
                    </div> 
                </div>
            </div>
        );
    }
}

export default FileDetailsPage;