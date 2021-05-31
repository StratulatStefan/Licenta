export class GeneralPurposeService{
    static getFileSizeUnit = (filesize) => {
        let units = ['', 'K', 'M', 'G']
        let index = 0;
        while(true){
            if(filesize / 1024 > 1){
                filesize = filesize / 1024;
                index = index + 1;
            }
            else{
                break
            }
        }
        return Math.round(filesize * 100) / 100 + " " + units[index] + "B"
    }

    static getCurrentTimestamp = (currentdate) => {
        return currentdate.getDate() + "/"
            + (currentdate.getMonth()+1)  + "/" 
            + currentdate.getFullYear() + " "  
            + currentdate.getHours() + ":"  
            + currentdate.getMinutes()
    }
}
