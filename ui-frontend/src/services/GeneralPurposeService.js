export class GeneralPurposeService{
    static getFileSizeUnit = (filesize) => {
        let units = ['', 'K', 'M', 'G']
        let index = 0;
        while(true){
            if(filesize / 1024 >= 1){
                filesize = filesize / 1024;
                index = index + 1;
            }
            else{
                break
            }
        }
        return Math.round(filesize * 10000) / 10000 + " " + units[index] + "B"
    }

    static getCurrentTimestamp = (currentdate) => {
        return currentdate.getDate() + "/"
            + (currentdate.getMonth()+1)  + "/" 
            + currentdate.getFullYear() + " "  
            + currentdate.getHours() + ":"  
            + currentdate.getMinutes()
    }

    static getCurrentTimestampForLogging = (limit) => {
        let currentdate = new Date()
        let year = currentdate.getFullYear()
        let month = currentdate.getMonth() + 1
        let day = currentdate.getDate()
        let hours = currentdate.getHours()
        let minutes = currentdate.getMinutes()
        let time = limit.split(" ")
        if(time[1].includes("minute")){
            minutes -= parseInt(time[0])
        }
        else if(time[1].includes("hour")){
            hours -= parseInt(time[0])
        }
        else if(time[1].includes("day")){
            day -= parseInt(time[0])
        }
        else if(time[1].includes("week")){
            day -= parseInt(time[0]) * 7
        }
        else if(time[1].includes("month")){
            month -= parseInt(time[0])
        }
        else if(time[1].includes("year")){
            year -= parseInt(time[0])
        }

        return  `${year}-${month}-${day}%20${hours}:${minutes}`
    }

    static getFileExtension = (filename) => {
        let tokens = filename.split(".")
        return tokens[tokens.length - 1];
    }

    static getFileType = (filename) => {
        let extension = GeneralPurposeService.getFileExtension(filename)
        let img = ["jpg", "jpeg", "png", "ico", "gif"]
        let exe = ["msi", "exe"]
        if(img.includes(extension)){
            return "image"
        }
        if(exe.includes(extension)){
            return "exe"
        }
        return "text"
    }

    static setHeaderLayout = (usertype) => {
        let adminItems = [
            "log_redirector", "content-table_redirector", 
            "storage-table_redirector", "nodes-status_redirector", 
            "replication-status_redirector"
        ]
        let userItems = ["upload_redirector", "home_redirector", "about_redirector"]
        adminItems.concat(userItems).forEach(redirector => {
            document.getElementById(redirector).style.display = "none"
        })
        if(usertype === "ADMIN"){
            adminItems.forEach(redirector => {
                document.getElementById(redirector).style.display = "block"
            })
        }
        else{
            userItems.forEach(redirector => {
                document.getElementById(redirector).style.display = "block"
            })
        }
    }
}
