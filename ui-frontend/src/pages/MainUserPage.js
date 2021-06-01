import React, { Component } from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import '../styles/pages-style.css';
import '../styles/pages-home-style.css';
import { FileHandlerService } from '../services/FileHandlerService';
import { Environment } from '../environment';
import { GeneralPurposeService } from '../services/GeneralPurposeService';


class MainUserPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Home Page";
        this.userData = localStorage.getItem('user_data')
        this.description = null
        this.newname = null
        this.searchData = null
        this.state = {
            isUserConnected : false,
            userType : null,
            accountAvailable : true,
            accountSuccessfullyCreated : false,
            userFiles : null,
            currentFileName : null,
            availableNodes : null,
            fileDetails : null
        }
    }

    componentDidMount = () => {
        this.checkForConnectedUser()
        this.fetchUserType().then(_ => {
            this.fetchUserFiles()
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

    fetchUserFiles = () =>{
        FileHandlerService.getUserFiles(this.userData["jwt"]).then(response => {
            console.log("========== user files ==========")
            response.content.forEach(console.log)
            console.log("================================")
            this.setState({
                userFiles : response.content
            })
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

    search = () =>{
        console.log(`searching ${this.userData}`)
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
            document.getElementById("file_details").style.visibility = "visible"
            document.getElementById("details_filename").innerHTML = file.filename
            document.getElementById("details_hash").innerHTML = file.hash.toString(16)
            this.setState({fileDetails : response.content, currentFileName : file.filename})
        })
    }

    downloadFile = () => {
        this.resetFields()
        document.getElementsByClassName("request_description")[0].style.visibility = "hidden"
        document.getElementById("file_status").innerHTML = "Downloading file..."
        FileHandlerService.downloadFile(this.userData["jwt"], this.state.currentFileName).then(response => {
            console.log(response.content)
            document.getElementById("file_status").innerHTML = "File successfully downloaded"
            document.getElementById("downloaduri").href = response.content
            document.getElementById("preview").src = response.content
            document.getElementById("downloaduri").click()
        })
    }

    deleteFile = () => {
        this.resetFields()
        document.getElementById("file_status").innerHTML = "Deleting file..."
        document.getElementsByClassName("request_description")[0].style.visibility = "visible"

        if(this.description === null || this.description === ""){
            document.getElementById("file_status").innerHTML = "Invalid input!"
        }
        else{
            FileHandlerService.deleteFile(this.userData["jwt"], this.state.currentFileName, this.description).then(response => {
                document.getElementById("file_status").innerHTML = "File successfully deleted. Refresh."
            })
        }
    }

    renameFile = (state) => {
        this.resetFields()
        if(state === 0){
            document.getElementsByClassName("request_description")[0].style.visibility = "visible"
            document.getElementsByClassName("request_description")[1].style.visibility = "visible"
            document.getElementsByClassName("request_description")[2].style.visibility = "visible"
            document.getElementById("input_label").style.visibility = "visible"
        }
        else if(state === 1){
            if(this.newname === null || this.newname === "" || this.description === null || this.description === ""){
                document.getElementById("file_status").innerHTML = "Invalid input!"
            }
            else{
                FileHandlerService.renameFile(this.userData["jwt"], this.state.currentFileName, this.newname, this.description).then(response => {
                    document.getElementById("file_status").innerHTML = "File successfully rename. Refresh."
                })
            }
        }

    }

    uploadNewVersionFile = () => {
        this.resetFields()
        document.getElementById("input_label").style.visibility = "visible"
        document.getElementsByClassName("request_description")[0].style.visibility = "hidden"
        document.getElementById("input_label").style.visibility = "visible"
        document.getElementById("input_label").innerHTML = "Edit your file and upload it with the same name."
        document.getElementsByClassName("request_description")[2].style.visibility = "visible"
        document.getElementById("proceed_btn").innerHTML = "Upload"
    }

    resetFields = () => {
        document.getElementsByClassName("request_description")[0].style.visibility = "visible"
        document.getElementsByClassName("request_description")[1].style.visibility = "hidden"
        document.getElementsByClassName("request_description")[2].style.visibility = "hidden"
        document.getElementById("proceed_btn").innerHTML = "Rename"
        document.getElementById("input_label").innerHTML = "New name : "
        document.getElementById("input_label").style.visibility = "hidden"

    }

    render(){
        var userFiles = []
        var fileDetails = []
        if(this.state.userType !== null){
            if(this.state.userFiles !== null){
                this.state.userFiles.forEach(userFile => {
                    let logosrc = `/images/file_logo/${userFile.filename.split(".")[1]}.png`
                    if(!Environment.available_logos.includes(userFile.filename.split(".")[1])){
                        logosrc = `/images/file_logo/extra.png` 
                    }
                    userFiles.push(
                        <tr key={`div_${userFile.filename}`}>
                            <td>
                                <img alt="logo" src={logosrc}></img>
                            </td>
                            <td className = "table_fname">
                                <p><a href="#" onClick={() => this.fileDetails(userFile)}>{`${userFile.filename + ""}`}</a></p><br/>
                                <span>{`Version : ${userFile.version}`}</span><br/>
                                <span>{`Size : ${GeneralPurposeService.getFileSizeUnit(userFile.filesize)}`}</span>
                            </td>
                            <td className = "table_version">
                                <p>{`${userFile.version_description}`}</p>
                            </td>
                            <td className = "table_version">
                                <p>{`${userFile.hash.toString(16)}`}</p>
                            </td>
                        </tr>
                    )
                })
            }
            if(this.state.fileDetails !== null){
                this.state.fileDetails.forEach(detail => {
                    console.log(detail)
                    fileDetails.push(
                        <div className="details_history">
                            <p>&#9673; {`${detail.version_desc}`}</p>
                            <br/>
                            <p>{`${detail.version_timestamp}`}</p>
                            <br/>
                            <p>{`${detail.version_hash.toString(16)}`}</p>
                            <br/>
                            <p>{`${detail.version_no}`}</p><br/>
                        </div>)
                    fileDetails.push(<br/>)
                })
            }
        }
        return(
            <div className="App">
                <div className="Home">
                    <div>
                        <div className="home_header">
                            <p>My files</p>
                            <div className="home_searchbar">
                                <input type="text" placeholder="Search file.."
                                    onChange={(event) => {this.searchData = event.target.value}}/>
                                <button onClick={this.search}>&#128269;</button>
                            </div>
                        </div>
                        <hr/>
                        <br/><br/>
                        <div className = "home_body_main_div">
                            <table className="home_body">
                                <thead>
                                    <tr>
                                        <th>Register ID</th>
                                        <th>Source Address</th>
                                        <th>Message Type</th>
                                        <th>Description</th>
                                        <th>Register Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {userFiles}
                                </tbody>
                            </table>
                            <div id="file_details">
                                <p id="details_filename"></p>
                                <p id="details_hash"></p>
                                <hr/>
                                <p className="details_activity">Actions</p><br/>
                                <button className="file_button" onClick={this.downloadFile}>Download</button>
                                <button className="file_button" onClick={this.deleteFile}>Delete</button>
                                <button className="file_button" onClick={() => this.renameFile(0)}>Rename</button>
                                <button className="file_button" onClick={this.uploadNewVersionFile}>Upload new version</button>
                                <a id="downloaduri" href="/" download>Click to download</a><br/>
                                <p><iframe id="preview" src="#"></iframe></p>
                                <br/>
                                <div className="request_description">
                                    <p>Description : </p>
                                    <input
                                        onChange={(event) => {this.description = event.target.value}} 
                                        type="text" />
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
                                <hr/>
                                <p className="details_activity">Activity</p><br/>
                                {fileDetails} 
                            </div>
                        </div>
                    </div> 
                </div>
            </div>
        );
    }
}

export default MainUserPage;