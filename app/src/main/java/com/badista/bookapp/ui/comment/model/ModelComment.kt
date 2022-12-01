package com.badista.bookapp.ui.comment.model

class ModelComment {

    // variables, should be with same spellings and type as we added in firebase
    var id = ""
    var bookId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    // emptry constructor
    constructor()

    // param constructor
    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }
}