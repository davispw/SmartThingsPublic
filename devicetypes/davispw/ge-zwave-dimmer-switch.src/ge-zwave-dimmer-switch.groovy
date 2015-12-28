/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "GE Z-Wave Dimmer Switch", namespace: "davispw", author: "Peter Davis") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Configuration"
        
		attribute "ignoreStartLevel", "string"
        attribute "invertSwitch", "string"
        attribute "onOffFadeDuration", "number"
        attribute "onOffFadeDurationDisplay", "string"
        attribute "onOffFadeDurationSlider", "number"
        attribute "dimFadeDuration", "number"
        attribute "manualFadeDuration", "number"
        
        command "invertSwitch"
        command "ignoreStartLevel"
        command "setOnOffFadeDuration"
        command "setOnOffFadeDurationSlider"
        command "clearOnOffFadeDuration"
        command "restoreOnOffFadeDuration"
        command "setDimFadeDuration"
        command "setManualFadeDuration"
        
		fingerprint inClusters: "0x26"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}
        
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		multiAttributeTile(name:"onOffFadeMulti", type: "generic", width: 6, height: 4){
			tileAttribute ("device.onOffFadeDurationDisplay", key: "PRIMARY_CONTROL") {
				attributeState "default", label:'${currentValue}', action:"clearOnOffFadeDuration", backgroundColors: [
                    [value: 0, color: "#ffffff"],
                    [value: 3.3, color: "#888888"],
                    [value: 252.45, color: "#aaaa88"]
                ]
				attributeState "0.0", label:'Instant', action:"restoreOnOffFadeDuration", backgroundColor:"#dddddd"
			}
			tileAttribute ("device.onOffFadeDurationDisplay", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'Manual Dimming \u2014 ${currentValue} seconds from 1-99'
			}
			tileAttribute ("device.onOffFadeDurationSlider", key: "SLIDER_CONTROL", range:'(0..5)') {
				attributeState "default", action:"setOnOffFadeDurationSlider", icon: 'st.Office.office6'
			}
		}
        
		valueTile("onOffFade", "device.onOffFadeDuration", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", action:"clearOnOffFadeDuration", label:'${currentValue} secs on/off fade', unit:"seconds", backgroundColors: [
                [value: 0, color: "#ffffff"],
                [value: 3.3, color: "#888888"],
                [value: 252.45, color: "#aaaa88"]
            ]
			state "0.00", label:'INSTANT on/off', action:"restoreOnOffFadeDuration", backgroundColor:"#dddddd"
		}
        
		controlTile("onOffFadeSlider", "device.onOffFadeDurationSlider", "slider", width: 4, height: 1) {
			state "default", action:"setOnOffFadeDurationSlider"
		}
        
		valueTile("dimFade", "dimFadeDuration", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue}s dimmer fade', unit:"seconds", backgroundColor:"#ffffff"
		}
        
		valueTile("manualFade", "manualFadeDuration", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue}s manual fade', unit:"seconds", backgroundColor:"#ffffff"
		}
        
		standardTile("invert", "device.invertSwitch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "normal", label: "Switch Inverted", action:"device.invertSwitch"
			state "inverted", label: "Normal Switch", action:"device.normalSwitch"
		}
        
		standardTile("ignoreStartLevel", "device.ignoreStartLevel", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "use", label: "Using Start Level", action:"device.ignoreStartLevel"
			state "ignore", label: "Ignoring Start Level", action:"device.useStartLevel"
		}

		main(["switch"])
		details(["switch", "level", "configure", "refresh", "onOffFade", "onOffFadeSlider", "onOffFadeMulti", "dimFade", "manualFade", "indicator", "invert", "ignoreStartLevel"])
	}
    
    preferences {
        // Parameter No: 4
        // Length: 1 Byte
        // Valid Values = 0 or 1 (default 0)
        input "invertSwitch", "bool",
            title: "Invert Switch?",
            description: """If the switch is accidentally installed upside down with “On” at the bottom 
				and “Off” at the top, the default On/Off rocker settings can be reversed."""
		    defaultValue: false

        // When Receiving an All-On or All-Off Command
        // Parameter 11 (number of steps or levels)
        // Parameter 12 (timing of the steps)
        // Length: 1 Byte
        // Valid Values:
        //     Parameter 11 (default = 1) Valid Values: 1-99
        //     Parameter 12 (default = 3) Valid Values: 1-255
        input "onOffFadeDuration", "decimal",
            title: "Fade On/Off",
            description: "seconds to fade fully from 1 to 99 (default is 3.0)",
            range: "0..250"

        // Both the number of steps (or levels) that the dimmer will change and the timing
        // of the steps can be modified to suit personal preferences. The timing of the
        // steps can be adjusted in 10 millisecond intervals.

        // When Receiving a Z-Wave Dim Command
        // Parameter 7 (number of steps or levels)
        //     NOTE this is confusingly the SIZE of the steps (e.g. 1 = 1% per step)
        // Parameter 8 (timing of the steps)
        // Length: 1 Byte
        // Valid Values:
        //     Parameter 7 (default = 1) Valid Values: 1-99
        //     Parameter 8 (default = 3) Valid Values: 1-255
        // UI input fade duration; must convert (inverse) to rate + step size.
        input "dimFadeDuration", "decimal",
            title: "Fade Dimming",
            description: "seconds to fade fully from 1 to 99 (default is 3.0)",
            range: "0..250"

        // Manual Control Dimming (pressing the Dimmer’s rocker)
        // Parameter 9 (number of steps or levels)
        // Parameter 10 (timing of the steps)
        // Length: 1 Byte
        // Valid Values:
        //     Parameter 9 (default = 1) Valid Values: 1-99
        //     Parameter 10 (default = 3) Valid Values: 1-255
        input "manualFadeDuration", "decimal",
            title: "Fade Manual Dimming",
            description: "seconds to fade fully from 1 to 99 (default is 3.0)",
            range: "0..250"

        // Ignore Start Level When Receiving Dim Commands
        // Please note: Every “Dim” command includes a start level embedded in it.
        // The 45639 can be set to ignore the start level that is part of the dim command.
        // Setting parameter 5 to a value of 0 will cause the 45639 to dim or brighten from
        // the start level embedded in the command.
        // Parameter No: 5
        // Length: 1 Byte
        // Valid Values = 0 or 1 (default 1)
        input "ignoreStartLevel", "bool",
            title: "Ignore Start Level?",
            defaultValue: true
    }
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}

def synchronized zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
    def value = cmd.configurationValue[0]

    switch (cmd.parameterNumber) {
	    case 11:
	        data.onOffFadeRate = value
            break
        case 12:
        	data.onOffFadeSteps = value
            break
	}

	switch (cmd.parameterNumber) {
    case 3:
        return createEvent([name: "indicatorStatus", value:
        	value == 1 ? "when on" : value == 2 ? "never" : "when off"])
    case 4:
		return createEvent(name: "invertSwitch", value: value == 1 ? "inverted" : "normal")
    case 5:
        return createEvent(name: "ignoreStartLevel", value: value == 1 ? "ignore" : "use")
    case 11: case 12:
		return data.onOffFadeRate == null || data.onOffFadeSteps == null ? null : [
        	createEvent(name: "onOffFadeDuration", value:
        		getFadeDuration(data.onOffFadeRate, data.onOffFadeSteps), unit: "seconds"),
        	createEvent(name: "onOffFadeDurationDisplay", value:
        		String.format("%.2f", getFadeDuration(data.onOffFadeRate, data.onOffFadeSteps)),
                displayed: false),
        	createEvent(name: "onOffFadeDurationSlider", value:
        		fadeDurationToSlider(getFadeDuration(data.onOffFadeRate, data.onOffFadeSteps)),
                displayed: false)]   
    case 7:
        data.dimFadeRate = value
		return data.dimFadeSteps == null ? null : createEvent(name: "dimFadeDuration", value: 
        	getFadeDuration(data.dimFadeRate, data.dimFadeSteps), unit: "seconds")
	case 8:
    	data.dimFadeSteps = value
		return data.dimFadeRate == null ? null : createEvent(name: "dimFadeDuration", value: 
        	getFadeDuration(data.dimFadeRate, data.dimFadeSteps), unit: "seconds")
    case 9:
        data.manualFadeRate = value
		return data.manualFadeSteps == null ? null : createEvent(name: "manualFadeDuration", value: 
        	getFadeDuration(data.manualFadeRate, data.manualFadeSteps), unit: "seconds")
	case 10:
    	data.manualFadeSteps = value
		return data.manualFadeRate == null ? null : createEvent(name: "manualFadeDuration", value: 
        	getFadeDuration(data.manualFadeRate, data.manualFadeSteps), unit: "seconds")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	commands << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 5).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 11).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
	delayBetween(commands, 1000)
}

def indicatorWhenOn() {
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 3).format()])
}

def indicatorWhenOff() {
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 3).format()])
}

def indicatorNever() {
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 3).format()])
}

def invertSwitch(invert=true) {
	if (invert) {
		delayBetween([
        	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 4).format()])
	}
	else {
		delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 4).format()])
	}
}

def ignoreStartLevel(ignore=true) {
	if (ignore) {
		delayBetween([
        	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 5, size: 1).format(),
		    zwave.configurationV1.configurationGet(parameterNumber: 5).format()])
	}
	else {
		delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 5, size: 1).format(),
		    zwave.configurationV1.configurationGet(parameterNumber: 5).format()])
	}
}

def clearOnOffFadeDuration() {
	setOnOffFadeDuration(0.0)
}

def restoreOnOffFadeDuration() {
    setOnOffFadeDuration(data.restoreOnOffFadeDuration ?: 0.33)
}

def setOnOffFadeDurationSlider(percent) {
	setOnOffFadeDuration(sliderToFadeDuration(percent))
}

def setOnOffFadeDuration(duration) {
	def values = getDimRateAndSteps(duration), rate = values[0], steps = values[1]
	log.debug "onOffFadeDuration: ${duration}, rate: ${rate}, steps: ${steps}"
    data.onOffFadeRate = data.onOffFadeSteps = null
    data.restoreOnOffFadeDuration = device.currentValue("onOffFadeDuration")
    delayBetween([
    	delayBetween([
            zwave.configurationV1.configurationSet(configurationValue: [rate], parameterNumber: 11, size: 1).format(),
            zwave.configurationV1.configurationSet(configurationValue: [steps], parameterNumber: 12, size: 1).format()
        ], 100),
		zwave.configurationV1.configurationGet(parameterNumber: 11).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    ], 1000)
}

def setDimFadeDuration(duration) {
	def values = getDimRateAndSteps(duration), rate = values[0], steps = values[1]
	log.debug "dimFadeDuration: ${duration}, rate: ${rate}, steps: ${steps}"
    data.dimFadeRate = data.dimFadeSteps = null
    delayBetween([
		zwave.configurationV1.configurationSet(configurationValue: [rate], parameterNumber: 7, size: 1).format(),
		zwave.configurationV1.configurationSet(configurationValue: [steps], parameterNumber: 8, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 7).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 8).format()
    ], 100)
}

def setManualFadeDuration(duration) {
	def values = getDimRateAndSteps(duration), rate = values[0], steps = values[1]
	log.debug "manualFadeDuration: ${duration}, rate: ${rate}, steps: ${steps}"
    data.manualFadeRate = data.manualFadeSteps = null
    delayBetween([
		zwave.configurationV1.configurationSet(configurationValue: [rate], parameterNumber: 9, size: 1).format(),
		zwave.configurationV1.configurationSet(configurationValue: [steps], parameterNumber: 10, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 9).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 10).format()
    ], 100)
}

def configure() {
	log.debug "configuring..."
    def commands = []

	if (settings.indicator == "when off") {
    	commands << indicatorWhenOn()
    } else if (settings.indicator == "never") {
    	commands << indicatorWhenOff()
    } else { // "when on"
    	commands << indicatorWhenOff()
    }
	
	commands << invertSwitch(settings.invertSwitch)
    
    commands << ignoreStartLevel(settings.ignoreStartLevel)
    
    commands << setOnOffFadeDuration(settings.onOffFadeDuration ?: 3)
    commands << setDimFadeDuration(settings.dimFadeDuration ?: 3)
    commands << setManualFadeDuration(settings.manualFadeDuration ?: 3)
    
    log.debug "commands: $commands"
    commands
}

def getDimRateAndSteps(duration) {
    // The first of these parameters is the “dim step” (dim rate) parameter. 
    // It can be set to a value of 1 to 99. This value indicates how many levels
    // the dimmer will change when the timer (discussed below) expires.
    // The second parameter is the timing (how fast the dim rate) parameter.
    // It can be set to a value of 1 to 255. This value indicates in 10 millisecond
    // resolution, how often the dim level will change. For example, if you set
    // this parameter to 1, then every 10mS the dim level will change. If you
    // set it to 255, then every 2.55 seconds the dim level will change.
    // With the combination of the two parameters that can control the dim rate,
    // the dimmer can be adjusted to dim from maximum to minimum or minimum to
    // maximum at various speeds between 10 millisecond and 252.45 seconds (over 4 minutes).
    if (duration <= 0.01) {
    	return [1, 99]
    }
    if (duration >= 252.45) {
    	return [255, 1]
    }
    def minDim = 1.0
    def availableSteps = 99.0 - minDim + 1.0
    def durationTicks = duration * 100.0 // number of 10ms ticks desired
    def stepSize = 1.0 // assume want to fade as smoothly as possible
    def rate = durationTicks / stepSize / availableSteps // number of ticks per step
    if (rate < 1.0) { // step size needs to increase to fade any faster than 10s
    	stepSize = availableSteps * rate
    	rate = 1.0
    }
	return [rate.intValue(), stepSize.intValue()]
}

def getFadeDuration(rate, steps) {
	if (rate == 1 && steps == 99) {
    	return 0.0 // show as "instant"
    } else if (rate != null && steps != null) {
        def minDim = 1.0
        def availableSteps = 99 - minDim + 1
        return ((availableSteps / steps) * rate / 100.0).setScale(2, BigDecimal.ROUND_HALF_UP)
    }
}

def sliderToFadeDuration(percent) {
	if (percent == null) {
    	null
	} else if (0.0 <= percent && percent <= 50.0) {
    	(percent / 50.0) * 3.3
	} else {
    	3.3 + (((percent - 50.0) / 50.0) * (252.45 - 3.3))
	}
}

def fadeDurationToSlider(duration) {
	if (duration == null) {
    	null
	} else if (0.0 <= duration && duration <= 3.3) {
    	(duration / 3.3 * 50.0).intValue()
	} else {
    	((duration - 3.3) / (252.45 - 3.3) * 50.0 + 50.0).intValue()
    }
}