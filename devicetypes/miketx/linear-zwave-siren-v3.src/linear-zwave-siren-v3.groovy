/**
* Linear Zwave Siren v3
*
* Copyright 2015 Mike Wilson
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
*/
metadata {
    definition (name: "Linear Z-Wave Siren v3", namespace: "miketx", author: "Mike Wilson") {
        capability "Actuator"
        capability "Alarm"
        capability "Battery"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
        capability "Configuration"
        attribute "mode", "string"
        attribute "autoStopTime", "string"
        command "setstrobe"
        command "setsiren"
        command "setboth"
        //Supported Command Classes
        //0x20-Basic ,0x25-Binary Switch ,0x70-Configuration , 0x72-Manufacturer Specific ,0x86-Version
        // fingerprint inClusters: "0x20,0x25,0x70,0x72,0x86"
        fingerprint deviceId:"0x1000", inClusters: "0x25,0x70,0x72,0x80,0x86"
        // 0 0 0x1000 0 0 0 4 0x25 0x70 0x72 0x86
    }
    simulator {
        // reply messages
        reply "2001FF,2002": "command: 2002, payload: FF"
        reply "200100,2002": "command: 2002, payload: 00"
        reply "200121,2002": "command: 2002, payload: 21"
        reply "200142,2002": "command: 2002, payload: 42"
        reply "2001FF,delay 3000,200100,2002": "command: 2002, payload: 00"
    }
    tiles(scale: 2) {
    	multiAttributeTile(name:"multi", type:"generic", width:6, height:4) {
    		tileAttribute("device.alarm", key: "PRIMARY_CONTROL") {
	            attributeState "off", label:'Panic', icon: "st.alarm.alarm.alarm", action:"on", nextState: "alarm", backgroundColor: "#e86d13"
    	        attributeState "alarm", label:'Alarm!', icon: "st.alarm.alarm.alarm", action:"off", nextState: "off", backgroundColor: "#ff0000"
        	    attributeState "siren", icon: "st.secondary.siren", action:"off", nextState: "off", backgroundColor: "#ff0000"
            	attributeState "strobe", icon: "st.secondary.strobe", action:"off", nextState: "off", backgroundColor: "#ff0000"
    		}
            tileAttribute("device.mode", key: "SECONDARY_CONTROL") {
                attributeState "both", label:'Both Siren & Strobe', action:"setsiren", nextState: "siren", icon:"st.alarm.alarm.alarm"
                attributeState "siren", label:'Siren Only', action:"setstrobe", nextState: "strobe", icon:"st.secondary.siren"
                attributeState "strobe", label:'Strobe Only', action:"setboth", nextState: "both", icon:"st.secondary.strobe"
            }
  		}
        standardTile("mode", "device.mode", decoration: "flat", width: 3, height: 3) {
            state "both", label:'Alarm', action:"setsiren", nextState: "siren", icon:"st.alarm.alarm.alarm"
            state "siren", action:"setstrobe", nextState: "strobe", icon:"st.secondary.siren"
            state "strobe", action:"setboth", nextState: "both", icon:"st.secondary.strobe"
        }
        standardTile("mode1", "device.mode", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Alarm', action:"setboth", nextState: "both", icon:"st.alarm.alarm.alarm"
            state "both", label:'Alarm', action:"setboth", icon:"st.alarm.alarm.alarm", backgroundColor: "#53a7c0"
        }
        standardTile("mode2", "device.mode", decoration: "flat", width: 2, height: 2) {
            state "default", action:"setsiren", nextState: "siren", icon:"st.secondary.siren"
            state "siren", action:"setsiren", icon:"st.secondary.siren", backgroundColor: "#53a7c0"
        }
        standardTile("mode3", "device.mode", decoration: "flat", width: 2, height: 2) {
            state "default", action:"setstrobe", nextState: "strobe", icon:"st.secondary.strobe"
            state "strobe", action:"setstrobe", icon:"st.secondary.strobe", backgroundColor: "#53a7c0"
        }
        standardTile("panic", "device.alarm", width: 6, height: 4) {
            state "off", label:'Panic', icon: "st.alarm.alarm.alarm", action:"on", nextState: "alarm", backgroundColor: "#e86d13"
            state "alarm", label:'Alarm!', icon: "st.alarm.alarm.alarm", action:"off", nextState: "off", backgroundColor: "#ff0000"
            state "siren", label:'Alarm!', icon: "st.secondary.siren", action:"off", nextState: "off", backgroundColor: "#ff0000"
            state "strobe", label:'Alarm!', icon: "st.secondary.disarm", action:"off", nextState: "off", backgroundColor: "#ff0000"
        } 
        valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) { 
            state "battery", label:'${currentValue}% battery', unit:"%"
        }
        standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("configure", "device.configure", decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"configuration.configure", icon:"st.secondary.configure"
        }

        main "panic"
        details(["multi","mode1","mode2","mode3","battery","refresh", "configure"])
    }

    preferences {
        input "autoStopTime", "enum",
        	title: "Disarm Time",
            required: true,
        	displayDuringSetup: true,
            options: ["30","60","120","Infinite"],
            defaultValue: '30'
    }
}

def setstrobe() {
    log.debug "Setting alarm to strobe."
//    state.LastAlarmtype = 2
//    sendEvent(name: "mode", value: "strobe", descriptionText: "Mode is Strobe Only")
    delayBetween([
    	zwave.configurationV1.configurationSet(parameterNumber: 0, size: 1, configurationValue: [2]).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 0).format()
    ])
}

def setsiren() {
    log.debug "Setting alarm to siren."
//    state.LastAlarmtype = 1
//    sendEvent(name: "mode", value: "siren", descriptionText: "Mode is Siren Only")
    delayBetween([
	    zwave.configurationV1.configurationSet(parameterNumber: 0, size: 1, configurationValue: [1]).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 0).format()
    ])
}

def setboth() {
    log.debug "Setting alarm to both."
//    state.LastAlarmtype = 0
//    sendEvent(name: "mode", value: "both", descriptionText: "Mode is both Siren & Strobe")
    delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 0, size: 1, configurationValue: [0]).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 0).format()
    ])
}

def off() {
    log.debug "sending off"
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.basicV1.basicGet().format()
    ])
}

def on() {
	def currentAutoStopTime = Integer.valueOf(device.currentValue('autoStopTime') ?: '0')
	log.debug "sending on. Will refresh after ${currentAutoStopTime} + 5s"
    def cmd = delayBetween([
        zwave.basicV1.basicSet(value: 0xff).format(),
        zwave.basicV1.basicGet().format()
    ], 200)
    if (currentAutoStopTime && currentAutoStopTime != 'Infinity') {
        return delayBetween([
    		cmd, zwave.basicV1.basicGet().format()
        ], currentAutoStopTime * 1000 + 5000)
    } else {
    	return cmd
	}
}

def both() {
	log.debug "sending alarm on via both"
    // Linear WA105DBZ-1 is not like other alarms that can activate siren vs. strobe on demand.
    // SmartThings Smart Home Automation app always calls both() method, 
    // so just delegate to on() with pre-configured mode
    on()
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description)
    log.debug "parse($description) - command is $cmd"
    if (cmd) {
    	result = createEvents(cmd)
    }
    log.debug "Parse returned ${result}"
    return result
}

def createEvents(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def currentMode = device.currentValue('mode') ?: 'alarm'
    log.debug "createEvents with cmd value {$cmd.value}, currentMode: $currentMode"
    def alarmValue = "off"
    if (cmd.value) {
//        if (state.LastAlarmtype == 2) {
//            alarmValue = "strobe"
//        } else if (state.LastAlarmtype == 1) {
//            alarmValue = "siren"
//        } else {
//            alarmValue = "both"
//		}
		alarmValue = currentMode
    }

	def switchValue = cmd.value ? "on" : "off"
    def descriptionText = cmd.value ? "Alarm! Activating ${alarmValue}" : "Off"
    log.debug "switchValue: $switchValue, alarmValue: $alarmValue, descriptionText: $descriptionText"

	return [
        createEvent(name: "alarm", value: alarmValue, descriptionText: descriptionText),
        createEvent(name: "switch", value: switchValue, descriptionText: descriptionText, displayed: false)
    ]
}

def createEvents(physicalgraph.zwave.Command cmd) {
	log.warn "UNEXPECTED COMMAND: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
    def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
    if (!state.lastbat || (new Date().time) - state.lastbat > 53*60*60*1000) {
        result << response(zwave.batteryV1.batteryGet())
        result << response("delay 1200")
    }
    result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
    result
}

def createEvents(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "$device.displayName has a low battery"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    state.lastbatt = new Date().time
    [createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def createEvents(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	switch (cmd.parameterNumber) {
    case 0:
    	log.debug "Got mode configuration: ${cmd.configurationValue}"
        switch (cmd.configurationValue[0]) {
        case 1:
            return [createEvent(name: "mode", value: "siren", descriptionText: "Set to Siren Only")]
        case 2:
            return [createEvent(name: "mode", value: "strobe", descriptionText: "Set to Strobe Only")]
        case 0:
        default:
            return [createEvent(name: "mode", value: "both", descriptionText: "Set to both Siren & Strobe")]
		}
    case 1:
    	log.debug "Got autoStopTime configuration: ${cmd.configurationValue}"
        switch (cmd.configurationValue[0]) {
        case 1:
            return [createEvent(name: "autoStopTime", value: "60", descriptionText: "Set to turn off after 60 seconds")]
        case 2:
            return [createEvent(name: "autoStopTime", value: "120", descriptionText: "Set to turn off after 120 seconds")]
        case 3:
            return [createEvent(name: "autoStopTime", value: "Infinite", descriptionText: "Set to never turn off automatically: must disarm siren manually!")]
        case 0:
        default:
        	return [createEvent(name: "autoStopTime", value: "30", descriptionText: "Set to turn off after 30 seconds")]
		}
    }
    log.warn "Unexpected configuration: $cmd"
}

def poll() {
    log.debug "checking battery... last checked ${state.lastbatt}"
    if (secondsPast(state.lastbatt, 36*60*60)) {
    	return zwave.batteryV1.batteryGet().format()
    } else {
    	return null
    }
}

private Boolean secondsPast(timestamp, seconds) {
    if (!(timestamp instanceof Number)) {
        if (timestamp instanceof Date) {
        	timestamp = timestamp.time
        } else if ((timestamp instanceof String) && timestamp.isNumber()) {
        	timestamp = timestamp.toLong()
        } else {
        	return true
        }
    }
    return (new Date().time - timestamp) > (seconds * 1000)
}

def refresh() {
    log.debug "sending refresh commands"
    delayBetween([
        zwave.basicV1.basicGet().format(),
        zwave.configurationV1.configurationGet(parameterNumber: 0).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 1).format(),
        zwave.batteryV1.batteryGet().format()
    ], 2000)
}

def configure() {
    def autoStopTimeParameter
    if (autoStopTime == '30') {
    	log.info "XXX 30"
    } else if (autoStopTime == '60') {
    	log.info "XXX 60"
    } else if (autoStopTime == '60') {
    	log.info "XXX 60"
    } else {
    	log.info "ZZZ $autoStopTime"
    }


    switch (settings.autoStopTime) {
    case '60':
    	autoStopTimeParameter = 1
    	log.info "CASE 60 -> $autoStopTimeParameter"
        break
    case '120':
        autoStopTimeParameter = 2
    	log.info "CASE 120 -> $autoStopTimeParameter"
        break
    case 'Infinite':
    	autoStopTimeParameter = 3
    	log.info "CASE Infinite -> $autoStopTimeParameter"
        break
    case '30':
    default:
        autoStopTimeParameter = 0
    	log.info "CASE 30 -> $autoStopTimeParameter"
        break
    }
    log.debug "autoStopTime: $settings.autoStopTime, autoStopTimeParameter: $autoStopTimeParameter"
    delayBetween ([
    	zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [autoStopTimeParameter]).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 1).format()
    ], 1000)
}