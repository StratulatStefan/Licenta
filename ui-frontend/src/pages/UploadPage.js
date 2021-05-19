import React, { Component } from 'react';

import '../styles/pages-style.css';
import '../styles/pages-home-style.css';
import { FileHandlerService } from '../services/FileHandlerService';

class UploadPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Upload Page";
        this.descriptionData = null;
        this.state = {
            currentFile : null
        }
    }

    componentDidMount = () => {
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

    getCurrentTimestamp = (currentdate) => {
        return currentdate.getDate() + "/"
            + (currentdate.getMonth()+1)  + "/" 
            + currentdate.getFullYear() + " "  
            + currentdate.getHours() + ":"  
            + currentdate.getMinutes()
    }

    handleDropFiles = (e) => {
        document.getElementById("upload_data").style.display = "block"
        document.getElementById("dropmessage").innerHTML = ""
        document.getElementById("uploader").style.borderStyle = "solid"

        let dt = e.dataTransfer
        let file = dt.files[0]
      
        document.getElementById("upload_date_filename").innerHTML = file.name
        document.getElementById("upload_date_filesize").innerHTML = `Size : ${Math.round(file.size / 1024 * 100) / 100} KB`
        document.getElementById("upload_date_timedate_last_modified").innerHTML = `Last modified : ${this.getCurrentTimestamp(file.lastModifiedDate)}`
        document.getElementById("upload_date_timedate").innerHTML = `Upload data : ${this.getCurrentTimestamp(new Date())}`
        document.getElementById("upload_date_type").innerHTML = `Type : ${file.type}`
        document.getElementById("upload_data_preview").src = URL.createObjectURL(file)

        console.log(file)
        this.setState({
            currentFile : file
        })
    }

    uploadFile = () => {
        document.getElementById("dropmessage").innerHTML = "Uploading file..."
        FileHandlerService.uploadFile(this.state.currentFile, this.descriptionData, "STANDARD").then(response => {
            if(response.code === 1){
                document.getElementById("dropmessage").innerHTML = response.content
                console.log(response.content)
            }
            else if(response.code === 401){
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
                            <button>Browse</button>
                            <br/>
                            <p id="dropmessage"></p>

                            {/*<input type="file" multiple accept="*" onChange={(event) => {this.handleFiles(event)}} /> */}
                        </div>
                        <div className="upload_data" id="upload_data">
                            <p id="upload_date_filename"></p>
                            <br/>
                            <img id="upload_data_preview" src="#" alt="No preview available" />
                            <br/>
                            <p className="upload_data_details" id="upload_date_filesize"></p>
                            <br/>
                            <p className="upload_data_details" id="upload_date_timedate_last_modified"></p>
                            <br/>
                            <p className="upload_data_details" id="upload_date_timedate"></p>
                            <br/>
                            <p className="upload_data_details" id="upload_date_type"></p>
                            <br/>
                            <br/>
                            <input type="text" placeholder="Upload description"
                                onChange={(event) => {this.descriptionData = event.target.value}}/>
                            <br/>
                            <button className="upload_btn" onClick={this.uploadFile}>Upload</button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
};

export default UploadPage;
