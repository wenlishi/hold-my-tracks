import{Point}from './Point'
export default class myTrack{
    constructor(trackMsg,lineName,description){
        this.trackMsg = trackMsg;
		this.lineName = lineName;
		this.description = description;
	    const now = new Date();
	    const year = now.getFullYear();
	    const month = (now.getMonth() + 1).toString().padStart(2, '0');
	    const date = now.getDate().toString().padStart(2, '0');
	    const hour = now.getHours().toString().padStart(2, '0');
	    const minute = now.getMinutes().toString().padStart(2, '0');
	    const second = now.getSeconds().toString().padStart(2, '0');
	    this.createdAt = `${year}/${month}/${date} ${hour}:${minute}:${second}`
       
    }
   
}
 // addPoint(point){
    //     this.points.push(point)
    // }
    // printTrack(){
    //     console.log("运行了")
    //     points.forEach(point => {
    //         console.log(point.address)
    //     });
    // }
