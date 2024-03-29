import React, { Component }      from 'react';
import { GeneralPurposeService } from '../services/GeneralPurposeService';
import {UsersHandlerService}     from '../services/UsersHandlerService';

import '../styles/pages-style.css';

class StartPage extends Component {
    static availableUserTypes = null

    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Login Page";
        this.userData = localStorage.getItem('user_data')
        this.accountCredentials = {email : "", password : ""}
        this.state = {
            isUserConnected            : false,
            userType                   : "",
            accountAvailable           : true,
            accountSuccessfullyCreated : false
        }
    }

    componentDidMount = () => {
        GeneralPurposeService.setHeaderLayout("START")
        this.fetchAvailableUserTypes()
        this.checkForConnectedUser()
        this.fetchUserType()
    }

    componentDidUpdate = () => {
        if(this.state.accountAvailable === true && this.state.accountSuccessfullyCreated === true){
            document.getElementById("status_message").innerHTML = ""
            this.setState({accountSuccessfullyCreated : false})
        }
        if(this.state.isUserConnected === true && this.state.userType !== ""){
            this.redirect(this.state.userType === "ADMIN" ? "/ahome" : "/uhome")
        }
    }

    checkForConnectedUser = () => {
        this.userData = localStorage.getItem('user_data')
        var status = (this.userData !== null && this.userData !== '')
        this.setState({isUserConnected : status});
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
            }
            else{
            }
        })
    }

    fetchUserType = () => {
        if(this.userData !== null && this.userData !== ''){
            UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                if(response.code === 1){
                    this.setState({userType : response["content"]})
                }
                else if(response.code === 401){
                    localStorage.setItem("user_data", '')
                }
            })
        }
    }

    login = () => {
        //this.accountCredentials = {"email" : "stefanc.stratulat@gmail.com", "password" : "parola.dropbox123"}
        //this.accountCredentials = {"email" : "dropbox.com@dpbox.com", "password" : "82467913"}
        UsersHandlerService.login(this.accountCredentials).then(response => {
            if(response.code === 1){
                localStorage.setItem("user_data", JSON.stringify(response.content))
                this.checkForConnectedUser()
                this.fetchUserType()
                document.getElementById("logoutButton").style.visibility = "visible"
            }
            else{
                document.getElementById("status_message").style.display = "block"
                document.getElementById("status_message").style.color   = "#810000";
                document.getElementById("status_message").innerHTML     = response.content
            }
        })
    }

    createAccount = () => {
        UsersHandlerService.register(this.accountCredentials).then(response => {
            document.getElementById("status_message").style.display = "block"
            if(response.code === 1){
                document.getElementById("status_message").innerHTML   = response.content["success status"]
                document.getElementById("status_message").style.color = "#206a5d";
                this.setState({accountSuccessfullyCreated : true})
            }
            else{
                document.getElementById("status_message").style.color = "#810000";
                document.getElementById("status_message").innerHTML   = response.content
            }
        })
    }

    redirect = (destination) => {
        if(destination !== ""){
            this.props.history.push({
                "pathname" : destination,
                "state" : {"detail" : {"user_type" : this.state.userType}}
            })
        }
        else{
            this.props.history.push("/")
        }
    }

    render() {
        let userypesoptions = []
        if(StartPage.availableUserTypes !== null){
            StartPage.availableUserTypes.forEach(usertype => {
                userypesoptions.push(
                    <option key={`option_${usertype["user_type"]}`} value={usertype["user_type"]}>{usertype["user_type"]}</option>
                )
            })
        }


      return (
        <div className="App">
            <div className="title">
                <img id="title_logo" src="images/logo.png" alt="Logo not found"/>
                <label id="title_text">Safestorage</label>
            </div>
            <hr/>
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
                            </p>
                        </div>
                    </div>
                    <div id="homediv_right">
                        {this.state.accountAvailable === true ? 
                        <div>
                            <p className="login_header">Log into your account</p>
                            <p>Email</p>
                            <input 
                                onChange={(event) => {this.accountCredentials["email"] = event.target.value}} 
                                type="email"/>
                            <p>Password</p>
                            <input 
                                onChange={(event) => {this.accountCredentials["password"] = event.target.value}} 
                                type="password"/>
                            <p><button className="redirector" onClick={this.login}>Autentificare</button></p>
                        </div> : 
                        <div>
                            <p className="login_header">Create account</p>
                            <p>Name</p>
                            <input 
                                onChange={(event) => {this.accountCredentials["name"] = event.target.value}} 
                                type="text" />
                            <p>Email</p>
                            <input 
                                onChange={(event) => {this.accountCredentials["email"] = event.target.value}} 
                                type="email" />
                            <p>Password</p>
                            <input 
                                onChange={(event) => {this.accountCredentials["password"] = event.target.value}} 
                                type="password" />
                            <p>User type</p>
                            <select onChange={(event) => {this.accountCredentials["type"] = event.target.value}}>
                                {userypesoptions}
                            </select>
                            <p>Country</p>
                            <input 
                                onChange={(event) => {this.accountCredentials["country"] = event.target.value}} 
                                type="text" />
                            <br/>
                            <label><button className="redirector" onClick={this.createAccount}>Create account</button></label>
                        </div>}
                        <p id="status_message">gol</p>
                        {this.state.accountSuccessfullyCreated === true ? 
                            <p><button className="a_redirector" onClick={() => {
                                    this.setState({accountAvailable : true})
                                    this.accountCredentials = {email : "", password : ""}
                                }}>
                                Go to login.
                                </button>
                            </p> : <p></p>
                        }
                        {this.state.accountAvailable === true ? 
                            <p>No account ? <button className="a_redirector" onClick={() => {
                                document.getElementById("status_message").style.color = "#02475e";
                                document.getElementById("status_message").style.display = "none"
                                this.setState({accountAvailable : false})
                                this.accountCredentials = {name : "", email : "", password : "", type: `${StartPage.availableUserTypes[0]["user_type"]}`, country : ""}
                                }}>Create an account</button></p> : 
                            <p>Have account ? <button className="a_redirector" onClick={() => {
                                document.getElementById("status_message").style.color = "#02475e";
                                document.getElementById("status_message").style.display = "none"
                                this.setState({accountAvailable : true})
                                this.accountCredentials = {name : "", email : "", password : "", type: `${StartPage.availableUserTypes[0]["user_type"]}`, country : ""}
                                }}>Sign in</button>
                            </p>
                        }
                    </div>
                </div> : <p></p>
            }
        </div>
      );
    }
  }
  
  export default StartPage;