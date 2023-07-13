package com.example.lvdk

class User {
    public lateinit var email : String
    public lateinit var password : String
    public lateinit var id : String

    constructor(email: String, password: String, id: String) {
        this.email = email
        this.password = password
        this.id = id
    }
}