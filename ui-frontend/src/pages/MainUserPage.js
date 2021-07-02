import React, { Component }  from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import { FileHandlerService }    from '../services/FileHandlerService';
import { Environment }           from '../environment';
import { GeneralPurposeService } from '../services/GeneralPurposeService';

import '../styles/pages-style.css';
import '../styles/pages_usermain.css';
import '../styles/pages-home-style.css';

class MainUserPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Home Page";
        this.userData = localStorage.getItem('user_data')
        this.description = null
        this.newname     = null
        this.state = {
            isUserConnected            : false,
            userType                   : null,
            accountAvailable           : true,
            accountSuccessfullyCreated : false,
            userFiles                  : null,
            currentFileName            : null,
            availableNodes             : null,
            fileDetails                : null
        }
    }

    componentDidMount = () => {
        GeneralPurposeService.setHeaderLayout("USER")
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
                UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                    if(response.code === 1){
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
        document.getElementById("number_of_file").innerHTML = ""
        FileHandlerService.getUserFiles(this.userData["jwt"]).then(response => {
            console.log(response)
            if(response.code === 1){
                document.getElementById("number_of_file").innerHTML = `Found ${response.content.length} files`;
                this.setState({userFiles : (response.content.length === 0) ? null : response.content})
            }
            else{
                this.setState({userFiles : null})
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

    redirect = (destination, param) => {
        if(destination !== ""){
            localStorage.setItem("userfile", JSON.stringify(param))
            this.props.history.push(destination);
        }
        else{
            this.props.history.push("/")
        }
    }

    render(){
        var userFiles = []
        if(this.state.userType !== null){
            if(this.state.userFiles !== null){
                this.state.userFiles.forEach(userFile => {
                    let logosrc = `/images/file_logo/${userFile.filename.split(".")[1]}.png`
                    if(!Environment.available_logos.includes(userFile.filename.split(".")[1])){
                        logosrc = `/images/file_logo/extra.png` 
                    }
                    userFiles.push(
                        <tr key={`div_${userFile.filename}`}>
                            <td><img alt="logo" src={logosrc}></img></td>
                            <td className = "table_fname">
                                <p><button className="a_redirector"
                                    style={{fontSize:"90%", textDecoration:"underline"}} 
                                    onClick={() => this.redirect("/details", userFile)} 
                                    >{`${userFile.filename + ""}`}</button>
                                </p><br/>
                                <span>{`Version : ${userFile.version}`}</span><br/>
                                <span>{`Size : ${GeneralPurposeService.getFileSizeUnit(userFile.filesize)}`}</span>
                            </td>
                            <td className = "table_version"><p>{`${userFile.version_description}`}</p></td>
                            <td className = "table_version"><p>{`${userFile.hash.toString(16)}`}</p></td>
                        </tr>
                    )
                })
            }
        }
        return(
            <div className="App">
                <div className="Home">
                    <div>
                        <div className="home_header">
                            <p id="p_filename">My files</p>
                            <p id="number_of_file">&nbsp;</p>
                        </div>
                        <hr id="mainpage_hr"/>
                        <br/><br/>
                        <div className = "home_body_main_div">
                            {this.state.userFiles === null ? 
                                <div style={{width:"100%"}}>
                                    <img src= "/images/not_found.png" />
                                    <br/><br/><br/>
                                    <p>No file found!</p>
                                    <br/>
                                    <p>Please consider uploading a file first!</p>
                                </div>:
                                <table>
                                    <thead>
                                        <tr>
                                            <td>Logo</td>
                                            <td>File name</td>
                                            <td>Version Description</td>
                                            <td>Hash</td>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {userFiles}
                                    </tbody>
                                </table>
                            }
                        </div>
                    </div> 
                </div>
            </div>
        );
    }
}

export default MainUserPage;