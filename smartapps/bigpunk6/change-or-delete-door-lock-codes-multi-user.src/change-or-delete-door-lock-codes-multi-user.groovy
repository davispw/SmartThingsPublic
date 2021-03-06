/**
 *  Change or Delete Lock Codes
 *
 *  Author: bigpunk6
 */
 
definition(
    name: "Change or Delete Door Lock Codes - Multi User",
    namespace: "bigpunk6",
    author: "bigpunk6",
    description: "This app alows you to change or delete the user codes for your smart door locks",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")

import groovy.json.JsonSlurper

preferences {
    page(name: "page1", title: "Select Locks and Users", nextPage: "page2", uninstall: true) {
        section("What Locks") {
                input "locks","capability.lockCodes", title: "Locks", multiple: true
        }
        section( "Notifications" ) {
            //input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
            input "sendPushMessage", "bool", title: "Send a push notification?", defaultValue: false
            input "phone", "phone", title: "Send a Text Message?", description: "Phone Number", required: false
        }
        section( "Number of Users" ) {
            input "numUsers", "number", title: "How many users do you want to configure?", description: '1 to 30 (default is 2)', defaultValue: 2, range: '1..30'
            paragraph "If you want to remove users, remember to delete them by entering \"X\" on the next page BEFORE reducing this number."
            paragraph "Check your lock's instructions for the maximum number of users."
        }
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false
        }
    }
//    page(name: "page2", title: "Set user preferences", nextPage: "page3", uninstall: true) {
	page(name: "page2", title: "Set user preferences", uninstall: true);
}

def page2() {
	dynamicPage(name: "page2") {
    	for ( i in 1..settings.numUsers ) {
            section("User $i") {
	            input "username$i", "text", title: "Name for User", required:false
    	        input "code$i", "text", title: "Code (4 to 8 digits) or X to Delete", required:false
        	    input "sendCode$i", "bool", title: "Send notification when users code is used", defaultValue: false
        	}
        }

//        section("User 1") {
//            input "username1", "text", title: "Name for User", required:false
//            input "code1", "text", title: "Code (4 to 8 digits) or X to Delete", required:false
//            input "sendCode1", "bool", title: "Send notification when users code is used", defaultValue: false
//        }
//        section("User 2") {
//            input "username2", "text", title: "Name for User", required:false
//            input "code2", "text", title: "Code (4 to 8 digits) or X to Delete", required:false
//            input "sendCode2", "bool", title: "Send notification when users code is used", defaultValue: false
//        }
//    }
//    page(name: "page3", title: "Set user preferences", nextPage: "page4", uninstall: true) {
//        section("User 3") {
//            input "username3", "text", title: "Name for User", required:false
//            input "code3", "text", title: "Code (4 to 8 digits) or X to Delete", required:false
//            input "sendCode3", "bool", title: "Send notification when users code is used", defaultValue: false
//        }
//        section("User 4") {
//            input "username4", "text", title: "Name for User", required:false
//            input "code4", "text", title: "Code (4 to 8 digits) or X to Delete", required:false
//            input "sendCode4", "bool", title: "Send notification when users code is used", defaultValue: false
//        }
//    }
//    page(name: "page4", title: "Set user preferences", nextPage: "page4", install: true, uninstall: true) {
//        section("User 5") {
//            input "username5", "text", title: "Name for User", required:false
//            input "code5", "text", title: "Code (4 to 8 digits) or X to Delete", required:false
//            input "sendCode5", "bool", title: "Send notification when users code is used", defaultValue: false
//        }
//        section("User 6") {
//            input "username6", "text", title: "Name for User", required:false
//            input "code6", "text", title: "Code (4 to 8 digits) or X to Delete", required:false
//            input "sendCode6", "bool", title: "Send notification when users code is used", defaultValue: false
//        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(locks, "codeReport", codereturn)
    subscribe(locks, "lock", codeUsed)
    checkUsers()
}

private checkUsers() {
    for ( i in 1..settings.numUsers ) {
    updateCode(i)
    }
}
    
def updateCode(userslot) {
    def username
    def code = null
    settings.each {
       if( it.key == "username${userslot}" ) {
           username = "$it.value"
       }
       if ( it.key == "code${userslot}" ) {
       code = "$it.value".toString()
       }
    }
    if (code == null) {
        log.info "no code set for slot $userslot"
    } else {
        if (code.equalsIgnoreCase("X")) {
            log.info "Deleting user in slot $userslot"
            locks.deleteCode(userslot)
        } else {
            log.info "Set code for $username in slot $userslot to $code"
            locks.setCode(userslot, code)
        }
    }
}

def codereturn(evt) {
    log.debug "codereturn"
    def username = "$evt.value"
    def code = evt.data.replaceAll("\\D+","")
    settings.each {
       if( it.key == "username${evt.value}" ) {
           username = "$it.value"
       }
    }
    if (code == "") {
        def message = "User in slot $evt.value was deleted from $evt.displayName"
        send(message)
    } else {
        def message = "Code for user $username in user slot $evt.value was set to $code on $evt.displayName"
        send(message)
    }
}

def codeUsed(evt) {
    if(evt.value == "unlocked" && evt.data) {
        def username
        def sendCode
        def codeData = new JsonSlurper().parseText(evt.data)
        settings.each {
            if( it.key == "username${codeData.usedCode}" ) {
                username = "$it.value"
            }
            if ( it.key == "sendCode${codeData.usedCode}" ) {
                sendCode = !!it.value
            }
        }
        def message = "$evt.displayName was unlocked by $username" // in user slot $codeData.usedCode"
        if(sendCode) {
            send(message)
        }
    }
}

private send(msg) {
    if (sendPushMessage) {
        sendPush(msg)
    }
    if (phone) {
        sendSms(phone, msg)
    }
    log.info msg
}