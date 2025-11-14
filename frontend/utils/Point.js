export default class Point{
    constructor(x,y,z,address){
        this.x = x;
        this.y = y;
        this.z = z;
        this.position = { x: x ,y: y,z:z}
        this.address = address;
    }
    getPosition(){
        return this.position
    }
 
}
