package com.example.assignment.Resources

data class ResourceModel(
    var resourceId:String,
    var name: String ,
    var email:String,
    var resourceTitle: String,
    var resourceDocument: String,
    var resourceDesc: String
){
    // No-argument constructor
    constructor() : this("", "", "", "", "","")
}
