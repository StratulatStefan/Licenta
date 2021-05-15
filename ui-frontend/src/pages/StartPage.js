import React, { Component } from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import '../styles/pages-style.css';

class StartPage extends Component {
    static availableUserTypes = null

    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Home Page";
        this.userData = localStorage.getItem('user_data')
        this.accountCredentials = {email : "", password : ""}
        this.state = {
            isUserConnected : false,
            userType : "",
            accountAvailable : true,
            accountSuccessfullyCreated : false
        }
        
    }

    componentDidMount = () => {
        this.fetchAvailableUserTypes()
        this.checkForConnectedUser()
        this.fetchUserType()
    }

    componentDidUpdate = () => {
        if(this.state.accountAvailable === true && this.state.accountSuccessfullyCreated === true){
            document.getElementById("status_message").innerHTML = ""
            this.setState({
                accountSuccessfullyCreated : false
            })
        }
    }

    checkForConnectedUser = () => {
        this.userData = localStorage.getItem('user_data')
        var status = (this.userData !== null && this.userData !== '')
        this.setState({
            isUserConnected : status,
        });
        if(status === true){
            this.userData = JSON.parse(this.userData)
            document.getElementById("log_data_uname").innerHTML = this.userData["name"];
            document.getElementById("log_data_profile").style.visibility = "visible";
        }
        else{
            document.getElementById("log_data_uname").innerHTML = "";
            document.getElementById("log_data_profile").style.visibility = "hidden";
        }
    }

    fetchAvailableUserTypes = () => {
        UsersHandlerService.getAvailableUserTypes().then(response => {
            if(response.code === 1){
                StartPage.availableUserTypes = response.content
                console.log(response.content)
            }
            else{
                console.log(response.content)
            }
        })
    }

    fetchUserType = () => {
        if(this.userData !== null && this.userData !== ''){
            UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                if(response.code === 1){
                    console.log("role : " + response["content"])
                    this.setState({userType : response["content"]})
                }
                else if(response.code === 401){
                    localStorage.setItem("user_data", '')
                }
            })
        }
    }

    login = () => {
        // {"email" : stefanc.stratulat@gmail.com", "password" : "parola.dropbox123"}
        UsersHandlerService.login(this.accountCredentials).then(response => {
            if(response.code === 1){
                localStorage.setItem("user_data", JSON.stringify(response.content))
                this.checkForConnectedUser()
                this.fetchUserType()
                document.getElementById("logoutButton").style.visibility = "visible"
            }
            else{
                document.getElementById("status_message").innerHTML = response.content
            }
        })
    }

    createAccount = () => {
        console.log(this.accountCredentials)
        UsersHandlerService.register(this.accountCredentials).then(response => {
            console.log(response)
            if(response.code === 1){
                document.getElementById("status_message").innerHTML = response.content["success status"]
                this.setState({
                    accountSuccessfullyCreated : true
                })
            }
            else{
                document.getElementById("status_message").innerHTML = response.content
            }
        })

    }

    credentialInput = (event, type) => {
        this.accountCredentials[type] = event.target.value
    }
    
    render() {
        let userypesoptions = []
        if(StartPage.availableUserTypes !== null){
            StartPage.availableUserTypes.forEach(usertype => {
                userypesoptions.push(
                    <option value={usertype["user_type"]}>{usertype["user_type"]}</option>
                )
            })
        }


      return (
        <div className="App">
            <p id="title">Home</p>
            {this.state.isUserConnected === false ?
                <div id="homediv">
                    <div id="homediv_left">
                        <div id="homediv_imgdiv">
                            <img id="homediv_img" src="/images/mainpage_img.png" alt=""/>
                        </div>
                        <div id="homediv_descdiv">
                            <p>
                                File storage and versioning
                                <br/>
                                <br/>
                                The ultimate app for storing and versioning your files in the safest way
                                <br/>
                                Inca ceva text aici, de umplutura, ca sa stim ce avem, eventual o lista!
                            </p>
                        </div>
                    </div>
                    <div id="homediv_right">
                        {this.state.accountAvailable === true ? 
                        <div>
                            <p id="login_header">Log into your account</p>

                            <p>Email</p>
                            <input 
                                onChange={(event) => {this.credentialInput(event, "email")}} 
                                type="email" />

                            <p>Password</p>
                            <input 
                                onChange={(event) => {this.credentialInput(event, "password")}} 
                                type="password" />
                            
                            <p><button className="redirector" onClick={this.login}>Autentificare</button></p>
                            <p>Nu ai un cont? <a href="#" onClick={() => {
                                this.setState({accountAvailable : false})
                                this.accountCredentials = {name : "", email : "", password : "", type: `${StartPage.availableUserTypes[0]["user_type"]}`, country : ""}
                                }}>Creaza un cont</a></p>
                        </div> : 
                        <div>
                            <p id="login_header">Create account</p>

                            <p>Name</p>
                            <input 
                                onChange={(event) => {this.credentialInput(event, "name")}} 
                                type="text" />
                            
                            <p>Email</p>
                            <input 
                                onChange={(event) => {this.credentialInput(event, "email")}} 
                                type="email" />

                            <p>Password</p>
                            <input 
                                onChange={(event) => {this.credentialInput(event, "password")}} 
                                type="password" />

                            <p>User type</p>
                            <select onChange={(event) => {this.accountCredentials["type"] = event.target.value}}>
                                {userypesoptions}
                            </select>

                            <p>Country</p>
                            <input 
                                onChange={(event) => {this.credentialInput(event, "country")}} 
                                type="text" />
                        
                            <p><button className="redirector" onClick={this.createAccount}>Create account</button></p>
                        </div>
                        }
                        <p id="status_message"></p>
                        {this.state.accountSuccessfullyCreated === true ? 
                            <p><a href="#" onClick={() => {
                                this.setState({accountAvailable : true})
                                this.accountCredentials = {email : "", password : ""}
                                }}>Go to login.</a>
                            </p> : <p></p>
                        }
                    </div>
                </div> : <p></p>
            }
        </div>
      );
    }
  }
  
  export default StartPage;