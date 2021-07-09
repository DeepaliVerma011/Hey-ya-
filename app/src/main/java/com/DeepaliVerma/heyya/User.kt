package com.DeepaliVerma.heyya

import com.google.firebase.firestore.FieldValue

data class User(
    val name:String,
    val imageUrl:String,
    val thumbImage:String,
    val deviceToken:String,
    val status:String,
    val onlineStatus: String,
    val uid:String
) {
    //empty constructor for Firebase
    constructor():this("","","","","","","")

    constructor(name: String,imageUrl: String,thumbImage: String,uid: String):this(
        name,
        imageUrl,
        thumbImage,
        "",
        "Hey there I am using Heyya!",
       "",
        uid


    )
}
