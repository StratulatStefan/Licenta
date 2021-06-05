import React, { Component } from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import '../styles/pages-style.css';
import '../styles/pages_usermain.css';
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
        document.getElementById("number_of_file").innerHTML = ""
        FileHandlerService.getUserFiles(this.userData["jwt"]).then(response => {
            console.log("========== user files ==========")
            response.content.forEach(console.log)
            console.log("================================")
            this.setState({
                userFiles : response.content
            })
            document.getElementById("number_of_file").innerHTML = `Found ${response.content.length} files`;
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
            // refactor cand apare si alte destinations
            this.props.history.push({
                "pathname" : destination,
                "state" : {"detail" : {
                    "user_file" : param
                }}
            })
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
                            <td>
                                <img alt="logo" src={logosrc}></img>
                            </td>
                            <td className = "table_fname">
                                <p><a href="#" onClick={() => this.redirect("/details", userFile)}>{`${userFile.filename + ""}`}</a></p><br/>
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
                            <table>
                                <thead>
                                    <td>Logo</td>
                                    <td>File name</td>
                                    <td>Version Description</td>
                                    <td>Hash</td>
                                </thead>
                                <tbody>
                                    {userFiles}
                                </tbody>
                            </table>
                        </div>
                    </div> 
                </div>
            </div>
        );
    }
}

export default MainUserPage;