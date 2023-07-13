package com.example.lvdk

class Order (_fromTo : String, _where : String) {
    init {

    }
    public lateinit var fromTo : String
    public lateinit var where : String

    init {
        fromTo= _fromTo
        where = _where
    }
}