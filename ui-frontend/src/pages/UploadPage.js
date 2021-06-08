import React, { Component } from 'react';

import { FileHandlerService }    from '../services/FileHandlerService';
import { GeneralPurposeService } from '../services/GeneralPurposeService';

import '../styles/pages-style.css';
import '../styles/pages-home-style.css';
import '../styles/pages-upload.css';

class UploadPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Upload Page";
        this.userData = JSON.parse(localStorage.getItem('user_data'))
        this.descriptionData = null;
        this.state = {
            currentFile : null,
            userType    : "STANDARD",
            preview     : <p id="upload_data_preview">No preview available</p>
        }
    }

    componentDidMount = () => {
        GeneralPurposeService.setHeaderLayout("USER")
        this.dragDropStyle("uploader")
    }

    dragDropStyle = (div_name) => {
        let dropArea = document.getElementById(div_name);

        ["dragover", "drop"].forEach(event => {
            dropArea.addEventListener(event, (e) => {e.preventDefault()}, false)
        })

        dropArea.addEventListener('dragover', () => {
            document.getElementById("dropmessage").innerHTML = "Drop your file"
            dropArea.style.borderStyle = "dashed"
        }, false)
        dropArea.addEventListener('dragleave', () => {
            document.getElementById("dropmessage").innerHTML = ""
            dropArea.style.borderStyle = "solid"
        }, false)
        dropArea.addEventListener('drop', this.handleDropFiles, false)
    }

    handleDropFiles = (e) => {
        document.getElementById("upload_data").style.display = "block"
        document.getElementById("uploader").style.display = "none"
        document.getElementById("dropmessage").innerHTML = ""
        document.getElementById("uploader").style.borderStyle = "solid"

        let dt = e.dataTransfer
        let file = dt.files[0]
      
        document.getElementById("upload_date_filename").innerHTML = file.name
        document.getElementById("upload_date_filesize").innerHTML = `Size : ${Math.round(file.size / 1024 * 100) / 100} KB`
        document.getElementById("upload_date_timedate_last_modified").innerHTML = `Last modified : ${GeneralPurposeService.getCurrentTimestamp(file.lastModifiedDate)}`
        document.getElementById("upload_date_timedate").innerHTML = `Upload data : ${GeneralPurposeService.getCurrentTimestamp(new Date())}`

        let fileType = GeneralPurposeService.getFileType(file.name)
        if(fileType === "image"){
            this.setState({preview : <embed id="upload_data_preview" type="image/jpg" src={URL.createObjectURL(file)}/>})
        }
        else if(fileType === "text"){
            this.setState({preview : <iframe title={file.name} id="upload_data_preview" src={URL.createObjectURL(file)} alt="No preview available"></iframe>})
        }
        else{
            this.setState({preview : <p id="upload_data_preview">No preview avaialble!</p>})
        }

        this.setState({currentFile : file})
    }

    uploadFile = () => {
        document.getElementById("dropmessage_1").innerHTML = "Uploading file..."
        FileHandlerService.uploadFile(this.state.currentFile, this.userData["jwt"], this.descriptionData, this.state.userType).then(response => {
            if(response.code === 1){
                document.getElementById("dropmessage_1").innerHTML = response.content
            }
            else if(response.code === 401){
                document.getElementById("dropmessage_1").innerHTML = response
                console.log(`eroare : ${response}`)
            }
        })
    }

    render(){
        return(
            <div className="App">
                <div className="Home">
                    <div className="upload_header">
                        <p>Upload new file</p>
                    </div>
                    <hr/>
                    <div className="upload_body">
                        <div className="upload_div" id="uploader">
                            <p>Select a file or drop it here</p>
                            <br/>
                            <p id="dropmessage"></p>
                        </div>
                        <div className="upload_div" id="upload_data">
                            <p id="upload_date_filename"></p>
                            <br/>
                            {this.state.preview}<br/>
                            <p className="upload_data_details" id="upload_date_filesize"></p><br/>
                            <p className="upload_data_details" id="upload_date_timedate_last_modified"></p><br/>
                            <p className="upload_data_details" id="upload_date_timedate"></p><br/>
                            <input type="text" placeholder="Upload description"
                                onChange={(event) => {this.descriptionData = event.target.value}}/>
                            <br/>
                            <button className="upload_btn" onClick={this.uploadFile}>Upload</button><br/>
                            <button className="a_redirector" id="selector_storage_status" href="#" onClick={() => {
                                    document.getElementById("upload_data").style.display = "none"
                                    document.getElementById("uploader").style.display = "block"
                                }}>Upload another file</button><br/>
                            <p id="dropmessage_1"></p>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
};

export default UploadPage;
